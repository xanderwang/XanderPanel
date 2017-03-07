/*
 * Copyright (C) 2013 Lemon Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xander.panel;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.MenuRes;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;

import com.xander.panel.PanelController.PanelParams;

public class XanderPanel extends Dialog implements DialogInterface.OnKeyListener {

    private static final String TAG = "XanderPanel";
    private static final int TRANSLATE_DIALOG = R.style.XanderPanel;
//    private static final int TRANSLATE_DIALOG = android.R.style.Theme_Dialog;

    private PanelController panelController;
    private PanelInterface.PanelDismissListener dismissListener;
    private PanelInterface.PanelShowListener showListener;

    public static final int MSG_SHOW_DIALOG     = 0;
    public static final int MSG_DISMISS_DIALOG  = 1;
    public static final int MSG_DISMISS_CANCEL  = 2;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            int what = msg.what;
            switch (what) {
                case MSG_DISMISS_CANCEL:
                case MSG_DISMISS_DIALOG:
                    realDismiss();
                    break;
                default:
                    break;
            }
        }
    };

    protected boolean mDismissing = false;
    private boolean mCancelable = true;

    private int mGravity = Gravity.TOP;

    /**
     * 背景透明度
     */
    private static final float DEFAULT_DIM_AMOUNT = 0.0f;

    SystemBarTintManager tintManager = null;

    private XanderPanel(Context context) {
        super(context, TRANSLATE_DIALOG);
        if (null == tintManager) {
//            tintManager = new SystemBarTintManager(context,getWindow());
//            tintManager.setStatusBarTintEnabled(true);
//            tintManager.setStatusBarTintColor(0x0000ff00);
//            tintManager.setNavigationBarTintEnabled(true);
//            tintManager.setNavigationBarTintColor(0x00ff0000);
//            tintManager.setTintAlpha(0.f);
        }
        // 设置背景透明度
        setDimAmount(DEFAULT_DIM_AMOUNT);
        panelController = new PanelController(getContext(), this);
    }

    private void setDimAmount(float dimAmount) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.dimAmount = dimAmount;
        getWindow().setAttributes(lp);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        TypedArray a = getContext().obtainStyledAttributes(new int[]{android.R.attr.layout_width});
        try {
            params.width = a.getLayoutDimension(0, ViewGroup.LayoutParams.MATCH_PARENT);
        } finally {
            a.recycle();
        }
        getWindow().setAttributes(params);
    }

    private void setStatusBarAndNavigationBarColor( int gravity ) {
        mGravity = gravity;
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            if( Gravity.TOP == mGravity ) {
                getWindow().setStatusBarColor(0x33000000);
                getWindow().setNavigationBarColor(0x00000000);
            } else if( Gravity.BOTTOM == mGravity ) {
                getWindow().setStatusBarColor(0x00000000);
                getWindow().setNavigationBarColor(0x33000000);
            } else {
                getWindow().setStatusBarColor(0x00000000);
                getWindow().setNavigationBarColor(0x00000000);
            }
        }
    }

    /**
     * Show the dialog, dimming the screen and expanding the button menu
     */
    @Override
    public void show() {
        super.show();
        mDismissing = false;
        panelController.animateShow();
        if (null != showListener) {
            showListener.onPanelShow(this);
        }
    }

    private void realDismiss() {
        super.dismiss();
        if (null != dismissListener) {
            dismissListener.onPanelDismiss(this);
        }
    }

    /**
     * Dismiss the dialog, removing screen dim and hiding the expanded menu
     */
    @Override
    public void dismiss() {
        mDismissing = true;
        panelController.animateDismiss();
        mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, PanelController.DURATION);
    }

    @Override
    public void cancel() {
        panelController.animateDismiss();
        mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, PanelController.DURATION);
    }

    @Override
    public void setOnKeyListener(OnKeyListener onKeyListener) {
        super.setOnKeyListener(this);
    }

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP
                && !event.isCanceled() && mCancelable && !mDismissing) {
            dismiss();
            return true;
        }
        return false;
    }

    public static class Builder {
        private Context mContext;
        private int mTheme;
        private PanelParams mPanelParams;

        /**
         * Constructor using a context for this builder and the {@link XanderPanel} it creates.
         */
        public Builder(Context context) {
            this(context, 0);
        }

        /**
         * Constructor using a context and theme for this builder and
         * the {@link XanderPanel} it creates.  The actual theme
         * that an XanderPanel uses is a private implementation, however you can
         * here supply either the name of an attribute in the theme from which
         * to get the dialog's style or one of the constants
         */
        public Builder(Context context, int theme) {
            mContext = context;
            mTheme = theme;
            mPanelParams = new PanelParams(context);
            int margen = mContext.getResources().getDimensionPixelSize(R.dimen.panel_margen);
            setPanelMargen(margen);
        }

        /**
         * Returns a {@link Context} with the appropriate theme for dialogs created by this Builder.
         * Applications should use this Context for obtaining LayoutInflaters for inflating views
         * that will be used in the resulting dialogs, as it will cause views to be inflated with
         * the correct theme.
         *
         * @return A Context for built Dialogs.
         */
        public Context getContext() {
            return mContext;
        }

        /**
         * Set the resource id of the {@link Drawable} to be used in the title.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setIcon(int iconId) {
            mPanelParams.iconId = iconId;
            return this;
        }

        /**
         * Set the {@link Drawable} to be used in the title.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setIcon(Drawable icon) {
            mPanelParams.icon = icon;
            return this;
        }

        /**
         * Set an icon as supplied by a theme attribute. e.g. android.R.attr.ExpandDialogIcon
         *
         * @param attrId ID of a theme attribute that points to a drawable resource.
         */
        public Builder setIconAttribute(int attrId) {
            TypedValue out = new TypedValue();
            mContext.getTheme().resolveAttribute(attrId, out, true);
            mPanelParams.iconId = out.resourceId;
            return this;
        }

        /**
         * Set the title using the given resource id.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setTitle(int titleId) {
            mPanelParams.title = mContext.getText(titleId);
            return this;
        }

        /**
         * Set the title displayed in the {@link Dialog}.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setTitle(CharSequence title) {
            mPanelParams.title = title;
            return this;
        }

        /**
         * Set the title using the custom view {@code customTitleView}. The
         * methods {@link #setTitle(int)} and {@link #setIcon(int)} should be
         * sufficient for most titles, but this is provided if the title needs
         * more customization. Using this will replace the title and icon set
         * via the other methods.
         *
         * @param customTitleView The custom view to use as the title.
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setCustomTitle(View customTitleView) {
            mPanelParams.customTitleView = customTitleView;
            return this;
        }

        /**
         * Set the message to display using the given resource id.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setMessage(int messageId) {
            mPanelParams.message = mContext.getText(messageId);
            return this;
        }

        /**
         * Set the message to display.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setMessage(CharSequence message) {
            mPanelParams.message = message;
            return this;
        }

        /**
         * Sets whether the dialog is cancelable or not.  Default is true.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setCancelable(boolean cancelable) {
            mPanelParams.cancelable = cancelable;
            return this;
        }

        public Builder setGravity(int gravity) {
            mPanelParams.mGravity = gravity;
            return this;
        }

        public Builder setCanceledOnTouchOutside(boolean outside) {
            mPanelParams.canceledOnTouchOutside = outside;
            return this;
        }

        public Builder setPanelMargen(int margen) {
            mPanelParams.panelMargen = margen;
            return this;
        }


        /**
         * Sets the callback that will be called when the dialog is dismissed for any reason.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setOnDismissListener(PanelInterface.PanelDismissListener onDismissListener) {
            mPanelParams.dismissListener = onDismissListener;
            return this;
        }

        /**
         * Sets the callback that will be called if the dialog is show.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setOnShowListener(PanelInterface.PanelShowListener onShowListener) {
            mPanelParams.showListener = onShowListener;
            return this;
        }

        /**
         * Set a custom view to be the contents of the Dialog. If the supplied view is an instance
         * of a {@link ListView} the light background will be used.
         *
         * @param view The view to use as the contents of the Dialog.
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setView(View view) {
            mPanelParams.customView = view;
            mPanelParams.viewSpacingSpecified = false;
            return this;
        }

        /**
         * Set a custom view to be the contents of the Dialog, specifying the
         * spacing to appear around that view. If the supplied view is an
         * instance of a {@link ListView} the light background will be used.
         *
         * @param view              The view to use as the contents of the Dialog.
         * @param viewSpacingLeft   Spacing between the left edge of the view and
         *                          the dialog frame
         * @param viewSpacingTop    Spacing between the top edge of the view and
         *                          the dialog frame
         * @param viewSpacingRight  Spacing between the right edge of the view
         *                          and the dialog frame
         * @param viewSpacingBottom Spacing between the bottom edge of the view
         *                          and the dialog frame
         * @return This Builder object to allow for chaining of calls to set
         * methods
         * <p/>
         * <p/>
         * This is currently hidden because it seems like people should just
         * be able to put padding around the view.
         * @hide
         */
        public Builder setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
            mPanelParams.customView = view;
            mPanelParams.viewSpacingSpecified = true;
            mPanelParams.viewSpacingLeft = viewSpacingLeft;
            mPanelParams.viewSpacingTop = viewSpacingTop;
            mPanelParams.viewSpacingRight = viewSpacingRight;
            mPanelParams.viewSpacingBottom = viewSpacingBottom;
            return this;
        }

        public Builder setSheet(String[] sheetItems, boolean showCancel,String cancelStr, PanelInterface.SheetListener sheetListener) {
            mPanelParams.showSheet = true;
            mPanelParams.showSheetCancel = showCancel;
            mPanelParams.sheetCancleStr = cancelStr;
            mPanelParams.sheetItems = sheetItems;
            mPanelParams.sheetListener = sheetListener;
            return this;
        }

        public Builder setController(String nagetive, String positive, PanelInterface.PanelControllerListener controllerListener) {
            mPanelParams.nagetive = nagetive;
            mPanelParams.positive = positive;
            mPanelParams.controllerListener = controllerListener;
            return this;
        }

        public Builder setMenu(@MenuRes int xmlRes, PanelInterface.PanelMenuListener menuListener) {
            if (null == mPanelParams.actionMenu) {
                mPanelParams.actionMenu = new ActionMenu(mContext);
            }
            (new MenuInflater(mContext)).inflate(xmlRes, mPanelParams.actionMenu);
            mPanelParams.menuListener = menuListener;
            return this;
        }

        public Builder grid(int row, int col) {
            mPanelParams.showMenuAsGrid = true;
            mPanelParams.pagerGridRow = row;
            mPanelParams.pagerGridCol = col;
            return this;
        }

        public Builder list() {
            mPanelParams.showMenuAsGrid = false;
            return this;
        }

        public Builder shareText( String text ) {
            mPanelParams.share = true;
            mPanelParams.shareText = text;
            return this;
        }

        public Builder shareIamge( String image ) {
            mPanelParams.share = true;
            mPanelParams.shareImages = new String[]{image};
            return this;
        }

        public Builder shareImages( String[] images ) {
            mPanelParams.share = true;
            mPanelParams.shareImages = images;
            return this;
        }

        /**
         * Creates a {@link XanderPanel} with the arguments supplied to this builder. It does not
         * {@link Dialog#show()} the dialog. This allows the user to do any extra processing
         * before displaying the dialog. Use {@link #show()} if you don't have any other processing
         * to do and want this to be created and displayed.
         */
        public XanderPanel create() {
            final XanderPanel xanderPanel = new XanderPanel(mContext);
            mPanelParams.apply(xanderPanel.panelController);
            xanderPanel.setContentView(xanderPanel.panelController.getParentView());
            xanderPanel.mCancelable = mPanelParams.cancelable;
            xanderPanel.showListener = mPanelParams.showListener;
            xanderPanel.dismissListener = mPanelParams.dismissListener;
            xanderPanel.setStatusBarAndNavigationBarColor(mPanelParams.mGravity);
            return xanderPanel;
        }

        /**
         * Creates a {@link XanderPanel} with the arguments supplied to this builder and
         * {@link Dialog#show()}'s the dialog.
         */
        public XanderPanel show() {
            XanderPanel xanderPanel = create();
            xanderPanel.show();
            return xanderPanel;
        }
    }
}
