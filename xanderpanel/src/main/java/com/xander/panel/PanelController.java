/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class PanelController implements View.OnClickListener, MenuItem.OnMenuItemClickListener {

    private final Context mContext;

    private final XanderPanel mXanderPanel;

    /**
     * 整个布局容器
     */
    private FrameLayout mRootLayout;

    /**
     * 整个容器背景 处理点击外部消失和背景渐变效果
     */
    private View mRootLayoutBG;

    /**
     * 内容填充面板
     */
    private LinearLayout mPanelRoot;

    /**
     * 标题面板
     */
    private LinearLayout mTitleLayout;

    /**
     * 标题样板
     */
    private LinearLayout mTitleTemplate;

    /**
     * 标题标题 icon
     */
    private ImageView mIconView;

    /**
     * 顶部标题 icon 内容
     */
    private Drawable mIcon;

    /**
     * 默认 icon 资源
     */
    private int mIconId = -1;

    /**
     * 标题 TextView
     */
    private TextView mTitleTextView;
    /**
     * 标题 text 内容
     */
    private CharSequence mTitle;

    /**
     * 用户自定义顶部内容
     */
    private View mCustomTitleView;


    private LinearLayout mContentLayout;

    /**
     * 中间信息 ScrollView 容器
     */
    private ScrollView mScrollView;

    /**
     * 中间信息 text
     */
    private TextView mMessageTextView;

    /**
     * 中间信息 text 内容
     */
    private CharSequence mMessage;

    /**
     * 表格显示的时候每页显示的行数
     */
    private int mPagerGridRow = 1;
    /**
     * 表格显示的时候每页显示的列数
     */
    private int mPagerGridCol = 1;


    /**
     * 用户自定义的View
     */
    private View mCustomView;

    /**
     * 用户自定义的View Panel
     */
    private FrameLayout mCustomPanel;


    /**
     * 底部的按钮
     */
    private LinearLayout mControllerPanel;
    /**
     * 取消按钮
     */
    private Button mControllerNagetive;
    private CharSequence mNagetiveText;
    /**
     * 确认按钮
     */
    private Button mControllerPositive;
    private CharSequence mPositiveText;
    private PanelInterface.PanelControllerListener mControllerListener;


    private boolean mViewSpacingSpecified = false;
    private int mViewSpacingLeft;
    private int mViewSpacingTop;
    private int mViewSpacingRight;
    private int mViewSpacingBottom;

    private boolean mCanceledTouchOutside = true;

    private int mXanderLayout;

    private boolean mShowSheetCancel = true;
    private boolean showSheet = false;
    private String[] mSheetItems;
    private PanelInterface.SheetListener mSheetListener;

    private ActionMenu actionMenu;
    private PanelInterface.PanelMenuListener menuListener;
    private boolean showMenuAsGrid = false;
    private boolean mShare = false;
    private String mShareText = "";
    private String[] mShareImages = {};

    PanelItemClickListenr panelItemClickListenr = new PanelItemClickListenr();

    public static final int DURATION = 300;
    public static final int DURATION_TRANSLATE = 200;
    public static final int DURATION_ALPHA = DURATION;

    public static final int ANIM_TYPE_SHOW = 0;
    public static final int ANIM_TYPE_DISMISS = 1;

    private int mGravity = android.view.Gravity.BOTTOM;

    private int mPanelMargen = 160;

    public PanelController(Context mContext, XanderPanel xanderPanel) {
        this.mContext = mContext;
        this.mXanderPanel = xanderPanel;
        mXanderLayout = R.layout.xander_panel;
    }

    /**
     * 检测 view 是否可以输入
     *
     * @param v
     * @return
     */
    static boolean canTextInput(View v) {
        if (v.onCheckIsTextEditor()) {
            return true;
        }
        if (!(v instanceof ViewGroup)) {
            return false;
        }

        ViewGroup vg = (ViewGroup) v;
        int i = vg.getChildCount();
        while (i > 0) {
            i--;
            v = vg.getChildAt(i);
            if (canTextInput(v)) {
                return true;
            }
        }
        return false;
    }

    View getParentView() {
        return mRootLayout;
    }

    private void setPanelMargen(int margen) {
        mPanelMargen = margen;
    }

    private void setTitle(CharSequence title) {
        mTitle = title;
    }

    private void setCustomTitle(View customTitleView) {
        mCustomTitleView = customTitleView;
    }

    private void setMessage(CharSequence message) {
        mMessage = message;
    }

    /**
     * Set the view to display in the dialog.
     */
    private void setCustomView(View view) {
        mCustomView = view;
        mViewSpacingSpecified = false;
    }

    /**
     * Set the view to display in the dialog along with the spacing around that view
     */
    private void setCustomView(
            View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
        mCustomView = view;
        mViewSpacingSpecified = true;
        mViewSpacingLeft = viewSpacingLeft;
        mViewSpacingTop = viewSpacingTop;
        mViewSpacingRight = viewSpacingRight;
        mViewSpacingBottom = viewSpacingBottom;
    }

    /**
     * Set resId to 0 if you don't want an icon.
     *
     * @param resId the resourceId of the drawable to use as the icon or 0
     *              if you don't want an icon.
     */
    private void setIcon(int resId) {
        mIconId = resId;
        if (mIconView != null) {
            if (resId > 0) {
                mIconView.setImageResource(mIconId);
            } else if (resId == 0) {
                mIconView.setVisibility(View.GONE);
            }
        }
    }

    private void setIcon(Drawable icon) {
        mIcon = icon;
        if ((mIconView != null) && (mIcon != null)) {
            mIconView.setImageDrawable(icon);
        }
    }

    /**
     * @param attrId the attributeId of the theme-specific drawable
     *               to resolve the resourceId for.
     * @return resId the resourceId of the theme-specific drawable
     */
    public int getIconAttributeResId(int attrId) {
        TypedValue out = new TypedValue();
        mContext.getTheme().resolveAttribute(attrId, out, true);
        return out.resourceId;
    }

    private void applyView() {
        ensureInflaterLayout();
        applyRootPanel();

        if (showSheet) {
            applySheet();
        } else if (actionMenu != null && actionMenu.size() > 0) {
            applyMenu();
        } else {
            applyTitlePanel();
            applyContentPanel();
            applyCustomPanel();
            applyControllerPanel();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.controller_nagetive) {
            if (null != mControllerListener) {
                mXanderPanel.dismiss();
                mControllerListener.onPanelNagetiiveClick(mXanderPanel);
            }
        } else if (id == R.id.controller_positive) {
            if (null != mControllerListener) {
                mXanderPanel.dismiss();
                mControllerListener.onPanelPositiveClick(mXanderPanel);
            }
        } else if (id == R.id.root_background) {
            if (mCanceledTouchOutside) {
                mXanderPanel.dismiss();
            }
        } else if (id == R.id.xander_panel_sheet_cancel) {
            if (mSheetListener != null) {
                mSheetListener.onSheetCancelClick();
            }
            mXanderPanel.dismiss();
        }
    }

    private void ensureInflaterLayout() {
        if (null == mRootLayout) {
            LayoutInflater inflater = LayoutInflater.from(mContext);

            mRootLayout = (FrameLayout) inflater.inflate(mXanderLayout, null);
            mRootLayoutBG = mRootLayout.findViewById(R.id.root_background);
            mRootLayoutBG.setOnClickListener(this);

            mPanelRoot = (LinearLayout) mRootLayout.findViewById(R.id.panel_root);

            mTitleLayout = (LinearLayout) mPanelRoot.findViewById(R.id.title_panel);
            mTitleTemplate = (LinearLayout) mTitleLayout.findViewById(R.id.title_template);
            mIconView = (ImageView) mTitleTemplate.findViewById(R.id.title_icon);
            mTitleTextView = (TextView) mTitleTemplate.findViewById(R.id.title_text);

            mContentLayout = (LinearLayout) mPanelRoot.findViewById(R.id.content_panel);
            mScrollView = (ScrollView) mPanelRoot.findViewById(R.id.msg_scrollview);
            mMessageTextView = (TextView) mScrollView.findViewById(R.id.msg_text);

            mCustomPanel = (FrameLayout) mRootLayout.findViewById(R.id.custom_panel);

            mControllerPanel = (LinearLayout) mRootLayout.findViewById(R.id.controller_pannle);
            mControllerNagetive = (Button) mControllerPanel.findViewById(R.id.controller_nagetive);
            mControllerNagetive.setOnClickListener(this);
            mControllerPositive = (Button) mControllerPanel.findViewById(R.id.controller_positive);
            mControllerPositive.setOnClickListener(this);
        }
    }

    @SuppressWarnings("ResourceType")
    private void applyRootPanel() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mPanelRoot.getLayoutParams();
        int sheetMargin = 0;
        layoutParams.gravity = mGravity;
        if (showSheet) {
            layoutParams.gravity = mGravity = Gravity.BOTTOM;
            sheetMargin = (int) mContext.getResources().getDimension(R.dimen.panel_sheet_margin);
        }
        layoutParams.leftMargin = sheetMargin;
        layoutParams.topMargin = sheetMargin;
        layoutParams.rightMargin = sheetMargin;
        layoutParams.bottomMargin = sheetMargin;
        if (Gravity.TOP == mGravity) {
            layoutParams.bottomMargin = mPanelMargen;
        } else if (Gravity.BOTTOM == mGravity) {
            layoutParams.topMargin = mPanelMargen;
        }
        mPanelRoot.setLayoutParams(layoutParams);
        mPanelRoot.setOnClickListener(null);
        int paddingTop = Build.VERSION.SDK_INT > 19 ? SystemBarTintManager.getStatusBarHeight(mContext) : 0;
        int paddingBottom = Build.VERSION.SDK_INT > 19 ? SystemBarTintManager.getNavigationBarHeight(mContext) : 0;
        if (Gravity.TOP == mGravity) {
            mPanelRoot.setPadding(0, paddingTop, 0, 0);
        } else if (Gravity.BOTTOM == mGravity) {
            mPanelRoot.setPadding(0, 0, 0, paddingBottom);
        }
        if (!showSheet) {
            mPanelRoot.setBackgroundResource(R.color.panel_root_layout_bg);
        }
    }

    private boolean applyTitlePanel() {
        boolean hasTitle = true;
        if (mCustomTitleView != null) {
            mTitleLayout.setVisibility(View.VISIBLE);
            // hide title temple
            mTitleTemplate.setVisibility(View.GONE);
            // Add the custom title view directly to the titlePanel layout
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            mTitleLayout.addView(mCustomTitleView, 0, lp);
        } else {
            if (!TextUtils.isEmpty(mTitle)) {
                mTitleLayout.setVisibility(View.VISIBLE);
                mTitleTemplate.setVisibility(View.VISIBLE);
                /* Display the title if a title is supplied, else hide it */
                mTitleTextView.setText(mTitle);
                /* Do this last so that if the user has supplied any
                 * icons we use them instead of the default ones. If the
                 * user has specified 0 then make it disappear.
                 */
                if (mIconId > 0) {
                    mIconView.setImageResource(mIconId);
                } else if (mIcon != null) {
                    mIconView.setImageDrawable(mIcon);
                } else if (mIconId == 0) {
                    /* Apply the padding from the icon to ensure the
                     * title is aligned correctly.
                     */
                    mTitleTextView.setPadding(
                            mIconView.getPaddingLeft(),
                            mIconView.getPaddingTop(),
                            mIconView.getPaddingRight(),
                            mIconView.getPaddingBottom()
                    );
                    mIconView.setVisibility(View.GONE);
                }
            } else {
                // Hide the title template
                mTitleLayout.setVisibility(View.GONE);
                hasTitle = false;
            }
        }
        return hasTitle;
    }

    private void applyContentPanel() {
        if (!TextUtils.isEmpty(mMessage)) {
            mContentLayout.setVisibility(View.VISIBLE);
            mScrollView.setFocusable(false);
            mMessageTextView.setText(mMessage);
        } else {
            mContentLayout.setVisibility(View.GONE);
        }
    }

    private void applyCustomPanel() {
        if (mCustomView != null) {
            mTitleLayout.setVisibility(View.GONE);
            mContentLayout.setVisibility(View.GONE);
            mCustomPanel.setVisibility(View.VISIBLE);
            mCustomPanel.addView(mCustomView, new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            if (mViewSpacingSpecified) {
                mCustomPanel.setPadding(mViewSpacingLeft, mViewSpacingTop, mViewSpacingRight, mViewSpacingBottom);
            }
        } else {
            mCustomPanel.setVisibility(View.GONE);
        }
    }

    private void applyControllerPanel() {
        if (TextUtils.isEmpty(mNagetiveText)) {
            mControllerNagetive.setVisibility(View.GONE);
        } else {
            mControllerNagetive.setVisibility(View.VISIBLE);
            mControllerNagetive.setText(mNagetiveText);
        }
        if (TextUtils.isEmpty(mPositiveText)) {
            mControllerPositive.setVisibility(View.GONE);
        } else {
            mControllerPositive.setVisibility(View.VISIBLE);
            mControllerPositive.setText(mPositiveText);
        }
        if (TextUtils.isEmpty(mNagetiveText) && TextUtils.isEmpty(mPositiveText)) {
            mControllerPanel.setVisibility(View.GONE);
        } else {
            mControllerPanel.setVisibility(View.VISIBLE);
        }
    }

    private void applySheet() {
        mPanelRoot.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View sheetView = inflater.inflate(R.layout.xander_panel_sheet, mRootLayout, false);
        TextView sheetCancel = (TextView) sheetView.findViewById(R.id.xander_panel_sheet_cancel);
        sheetCancel.setVisibility(mShowSheetCancel ? View.VISIBLE : View.GONE);
        sheetCancel.setOnClickListener(this);
        ListView sheetList = (ListView) sheetView.findViewById(R.id.xander_panel_sheet_list);
        SheetAdapter sheetAdapter = new SheetAdapter(mContext, this.mSheetItems);
        sheetList.setAdapter(sheetAdapter);
        sheetList.setOnItemClickListener(panelItemClickListenr);
//        sheetList.setOnItemLongClickListener(panelItemClickListenr);
        mPanelRoot.addView(sheetView);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (null != menuListener) {
            menuListener.onMenuClick(item);
            mXanderPanel.dismiss();
            return true;
        } else if (mShare && item instanceof ActionMenuItem) {
            ShareTools.share(
                    mContext,
                    mShareText,
                    mShareImages,
                    ((ActionMenuItem) item).getComponentName()
            );
            mXanderPanel.dismiss();
            return true;
        }
        return false;
    }

    private void applyMenu() {
        mPanelRoot.removeAllViews();
        if (null == actionMenu || actionMenu.size() == 0) {
            return;
        }
        for (int i = actionMenu.size() - 1; i >= 0; i--) {
            actionMenu.getItem(i).setOnMenuItemClickListener(this);
        }
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (showMenuAsGrid) {
            View view = inflater.inflate(R.layout.xander_panel_menu_gridviewpager, mPanelRoot, false);
            ViewPager viewPager = (ViewPager) view.findViewById(R.id.xander_panel_gridviewpager);
            int row = mPagerGridRow, col = mPagerGridCol;
            if (actionMenu.size() < col) {
                row = 1;
                col = actionMenu.size();
            }
            GridViewPagerAdapter pagerAdapter = new GridViewPagerAdapter(mContext, row, col);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewPager.getLayoutParams();
            int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
            params.height = (screenWidth / Math.max(3, col)) * row;
            Log.d("wxy", "params " + params.width + " , " + params.height);
            viewPager.setLayoutParams(params);
            pagerAdapter.setActionMenus(actionMenu, viewPager);
            viewPager.setAdapter(pagerAdapter);
            CirclePageIndicator indicator = (CirclePageIndicator) view.findViewById(R.id.xander_panel_indicator);
            indicator.setViewPager(viewPager);
            mPanelRoot.addView(view);
        } else {
            ListView menuList = (ListView) inflater.inflate(R.layout.xander_panel_menu_list, mPanelRoot, false);
            MenuAdapter menuAdapter = new MenuAdapter(mContext, actionMenu);
            menuList.setAdapter(menuAdapter);
            menuList.setOnItemClickListener(panelItemClickListenr);
            mPanelRoot.addView(menuList);
        }
    }


    protected void animateShow() {
        doAnim(ANIM_TYPE_SHOW);
    }

    protected void animateDismiss() {
        doAnim(ANIM_TYPE_DISMISS);
    }

    private void doAnim(int type) {
        mRootLayoutBG.startAnimation(createBgAnimation(type));
        mPanelRoot.startAnimation(createPanelAnimation(type));
    }

    private Animation createPanelAnimation(int animType) {
        int type = TranslateAnimation.RELATIVE_TO_SELF;
        TranslateAnimation an = null;
        if (ANIM_TYPE_SHOW == animType) {
            if (Gravity.TOP == mGravity) {
                an = new TranslateAnimation(type, 0, type, 0, type, -1, type, 0);
            } else if (Gravity.BOTTOM == mGravity) {
                an = new TranslateAnimation(type, 0, type, 0, type, 1, type, 0);
            }
        } else {
            if (Gravity.TOP == mGravity) {
                an = new TranslateAnimation(type, 0, type, 0, type, 0, type, -1);
            } else if (Gravity.BOTTOM == mGravity) {
                an = new TranslateAnimation(type, 0, type, 0, type, 0, type, 1);
            }

        }
        an.setDuration(DURATION_TRANSLATE);
        an.setFillAfter(true);
        return an;
    }

    private Animation createBgAnimation(int animType) {
        AlphaAnimation an = null;
        if (ANIM_TYPE_SHOW == animType) {
            an = new AlphaAnimation(0, 1);
        } else {
            an = new AlphaAnimation(1, 0);
        }
        an.setDuration(DURATION_ALPHA);
        an.setFillAfter(true);
        return an;
    }

    public static class PanelParams {

        public Context context;

        public int panelMargen = 200;

        public Drawable icon;
        public int iconId = 0;
        public int iconAttrId = 0;
        public CharSequence title;
        public View customTitleView;

        public CharSequence message;

        public View customView;
        public boolean viewSpacingSpecified = false;
        public int viewSpacingLeft;
        public int viewSpacingTop;
        public int viewSpacingRight;
        public int viewSpacingBottom;

        public boolean cancelable = true;
        public boolean canceledOnTouchOutside = true;

        public boolean showSheetCancel = true;
        public boolean showSheet = false;
        public String[] sheetItems;
        public PanelInterface.SheetListener sheetListener;

        public boolean share = false;
        public String shareText = "";
        public String[] shareImages = {};
        public String[] filterPackages = {};

        public String nagetive;
        public String positive;
        public PanelInterface.PanelControllerListener controllerListener;

        public ActionMenu actionMenu;
        public PanelInterface.PanelMenuListener menuListener;

        /**
         * 表格显示的时候每页显示的行数
         */
        public int pagerGridRow = 2;
        /**
         * 表格显示的时候每页显示的列数
         */
        public int pagerGridCol = 3;

        public boolean showMenuAsGrid = false;

        public PanelInterface.PanelShowListener showListener;
        public PanelInterface.PanelDismissListener dismissListener;

        public int mGravity = Gravity.BOTTOM;

        public PanelParams(Context context) {
            this.context = context;
        }

        public void apply(PanelController panelController) {

            //title
            if (customTitleView != null) {
                panelController.setCustomTitle(customTitleView);
            } else {
                if (!TextUtils.isEmpty(title)) {
                    panelController.setTitle(title);
                }
                if (icon != null) {
                    panelController.setIcon(icon);
                }
                if (iconId >= 0) {
                    panelController.setIcon(iconId);
                }
                if (iconAttrId > 0) {
                    panelController.setIcon(panelController.getIconAttributeResId(iconAttrId));
                }
            }

            //msg
            if (!TextUtils.isEmpty(message)) {
                panelController.setMessage(message);
            }

            // custom view
            if (customView != null) {
                if (viewSpacingSpecified) {
                    panelController.setCustomView(
                            customView,
                            viewSpacingLeft,
                            viewSpacingTop,
                            viewSpacingRight,
                            viewSpacingBottom
                    );
                } else {
                    panelController.setCustomView(customView);
                }
            }

            if (share) {
                actionMenu = ShareTools.createShareActionMenu(
                        context,
                        shareText,
                        shareImages,
                        filterPackages
                );
            }

            // set menu
            if (null != actionMenu) {
                panelController.mShare = share;
                panelController.mShareText = shareText;
                panelController.mShareImages = shareImages;
                panelController.showMenuAsGrid = showMenuAsGrid;
                panelController.mPagerGridRow = pagerGridRow;
                panelController.mPagerGridCol = pagerGridCol;
                panelController.menuListener = menuListener;
                panelController.actionMenu = actionMenu.clone(actionMenu.size());
                panelController.actionMenu.removeInvisible();
            }

            // set sheet
            panelController.showSheet = showSheet;
            panelController.mShowSheetCancel = showSheetCancel;
            panelController.mSheetItems = sheetItems;
            panelController.mSheetListener = sheetListener;

            // set controller
            panelController.mPositiveText = positive;
            panelController.mNagetiveText = nagetive;
            panelController.mControllerListener = controllerListener;

            // other settings
            panelController.mGravity = mGravity;
            panelController.mCanceledTouchOutside = canceledOnTouchOutside;
            panelController.setPanelMargen(panelMargen);

            panelController.applyView();

        }

    }

    private class MenuAdapter extends BaseAdapter {
        private ActionMenu menu;
        private LayoutInflater inflater;

        public MenuAdapter(Context context, ActionMenu menu) {
            this.menu = menu;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return menu.size();
        }

        @Override
        public Object getItem(int position) {
            return menu.getItem(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MenuHolder menuHolder = null;
            if (null == convertView) {
                convertView = inflater.inflate(R.layout.xander_panel_menu_list_item, parent, false);
                menuHolder = new MenuHolder(convertView);
                convertView.setTag(menuHolder);
            } else {
                menuHolder = (MenuHolder) convertView.getTag();
            }
            menuHolder.bindMenuItem(menu.getItem(position));
            return convertView;
        }
    }

    private class MenuHolder {
        private ImageView menuIcon;
        private TextView menuTitle;

        public MenuHolder(View parent) {
            bindView(parent);
        }

        private void bindView(View parent) {
            menuIcon = (ImageView) parent.findViewById(R.id.panel_menu_icon);
            menuTitle = (TextView) parent.findViewById(R.id.panel_menu_title);
        }

        public void bindMenuItem(MenuItem menuItem) {
            if (null == menuItem.getIcon()) {
                menuIcon.setVisibility(View.GONE);
            } else {
                menuIcon.setVisibility(View.VISIBLE);
                menuIcon.setImageDrawable(menuItem.getIcon());
            }
            menuTitle.setText(menuItem.getTitle());
        }
    }


    private class SheetAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<String> mSheetItems = new ArrayList<>();

        public SheetAdapter(Context contexts, String[] sheetItems) {
            inflater = LayoutInflater.from(contexts);
            this.mSheetItems.addAll(Arrays.asList(sheetItems));
        }

        @Override
        public int getCount() {
            return mSheetItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mSheetItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null == convertView) {
                convertView = inflater.inflate(R.layout.xander_panel_sheet_item, parent, false);
            }
            TextView textView = (TextView) convertView;
            if (getCount() == 1) {
                textView.setBackgroundResource(R.drawable.sheet_item_just_one);
            } else if (position == 0) {
                textView.setBackgroundResource(R.drawable.sheet_item_top);
            } else if (position == getCount() - 1) {
                textView.setBackgroundResource(R.drawable.sheet_item_bottom);
            } else {
                textView.setBackgroundResource(R.drawable.sheet_item_normal);
            }
            textView.setText(mSheetItems.get(position));
            return convertView;
        }
    }

    private class PanelItemClickListenr implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (showSheet && mSheetListener != null) {
                mSheetListener.onSheetItemClick(position);
            } else if (null != actionMenu && null != menuListener) {
                menuListener.onMenuClick(actionMenu.getItem(position));
            } else if (mShare) {
                MenuItem menuItem = actionMenu.getItem(position);
                if( menuItem instanceof ActionMenuItem ) {
                    ((ActionMenuItem)menuItem).invoke();
                }
                return ;
            }
            mXanderPanel.dismiss();
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (showSheet && mSheetListener != null) {
                mSheetListener.onSheetItemClick(position);
            } else if (null != actionMenu && null != menuListener) {
                menuListener.onMenuClick(actionMenu.getItem(position));
            } else if (mShare) {
                MenuItem menuItem = actionMenu.getItem(position);
                if( menuItem instanceof ActionMenuItem ) {
                    ((ActionMenuItem)menuItem).invoke();
                }
                return true;
            }
            mXanderPanel.dismiss();
            return true;
        }
    }


}
