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
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class PanelController implements View.OnClickListener {

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
     * 内置的列表
     */
    private ListView mListView;

    /**
     * 表格显示的时候每页显示的行数
     */
    private int mPagerGridRowCount = 1;
    /**
     * 表格显示的时候每页显示的列数
     */
    private int mPagerGridColCount = 1;

    /**
     * 应该是 viewpager 嵌套 gridview ,暂时用  gridview 代替
     */
    private GridView mGridView;

    /**
     * 数据
     */
    private BaseAdapter mDataAdapter;

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
    private Button mControllerCancel;
    private CharSequence mCancelText;
    /**
     * 确认按钮
     */
    private Button mControllerOk;
    private CharSequence mOkText;
    private PanelInterface.PanelControllerListener controllerListener;


    private boolean mViewSpacingSpecified = false;
    private int mViewSpacingLeft;
    private int mViewSpacingTop;
    private int mViewSpacingRight;
    private int mViewSpacingBottom;

    private boolean mCanceledTouchOutside = true;

    private int mXanderLayout;
    private int mListLayout;
    private int mListItemLayout;
    private int mGridLayout;
    private int mGridItemLayout;

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
        mListLayout = R.layout.xander_panel_list;
        mListItemLayout = R.layout.xander_panel_list_icon_text_item;
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

    View getParentBgView() {
        return mRootLayoutBG;
    }

    View getContentPanelView() {
        return mPanelRoot;
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
    private void setCustomView(View view, int viewSpacingLeft, int viewSpacingTop,
                               int viewSpacingRight, int viewSpacingBottom) {
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

    public void setDataAdapter(BaseAdapter mDataAdapter) {
        this.mDataAdapter = mDataAdapter;
    }

    public void setCanceledTouchOutside(boolean mCanceledTouchOutside) {
        this.mCanceledTouchOutside = mCanceledTouchOutside;
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

    public ListView getListView() {
        return mListView;
    }

    private void applyView() {

        ensureInflaterLayout();

        applyRootPanel();

        applyTitlePanel();

        applyContentPanel();

        applyCustomPanel();

        applyControllerPanel();

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.controller_cancel) {
            if (null != controllerListener) {
                mXanderPanel.dismiss();
                controllerListener.onPanelCancelClick(mXanderPanel);
            }
        } else if (id == R.id.controller_ok) {
            if (null != controllerListener) {
                mXanderPanel.dismiss();
                controllerListener.onPanelOkClick(mXanderPanel);
            }
        } else if (id == R.id.root_background) {
            if (mCanceledTouchOutside) {
                mXanderPanel.dismiss();
            }
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
            mControllerCancel = (Button) mControllerPanel.findViewById(R.id.controller_cancel);
            mControllerCancel.setOnClickListener(this);
            mControllerOk = (Button) mControllerPanel.findViewById(R.id.controller_ok);
            mControllerOk.setOnClickListener(this);
        }
    }

    private void applyRootPanel() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mPanelRoot.getLayoutParams();
        layoutParams.gravity = mGravity;
        if (Gravity.TOP == mGravity) {
            layoutParams.bottomMargin = mPanelMargen;
        } else if (Gravity.BOTTOM == mGravity) {
            layoutParams.topMargin = mPanelMargen;
        }
        mPanelRoot.setLayoutParams(layoutParams);
        mPanelRoot.setOnClickListener(null);
        int padding;
        if (Gravity.TOP == mGravity) {
            padding = SystemBarTintManager.getStatusBarHeight(mContext);
            mPanelRoot.setPadding(0, padding, 0, 0);
        } else if (Gravity.BOTTOM == mGravity) {
            padding = SystemBarTintManager.getNavigationBarHeight(mContext);
            mPanelRoot.setPadding(0, 0, 0, padding);
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

        if (mDataAdapter != null && mDataAdapter.getCount() > 0) {
            mContentLayout.setVisibility(View.VISIBLE);
            applyListOrGrid();
        } else if (!TextUtils.isEmpty(mMessage)) {
            mContentLayout.setVisibility(View.VISIBLE);
            mScrollView.setFocusable(false);
            mMessageTextView.setText(mMessage);
        } else {
            mContentLayout.setVisibility(View.GONE);
        }
    }

    private void applyListOrGrid() {

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
        if (TextUtils.isEmpty(mCancelText)) {
            mControllerCancel.setVisibility(View.GONE);
        } else {
            mControllerCancel.setVisibility(View.VISIBLE);
        }
        if (TextUtils.isEmpty(mOkText)) {
            mControllerOk.setVisibility(View.GONE);
        } else {
            mControllerOk.setVisibility(View.VISIBLE);
        }
        if (TextUtils.isEmpty(mCancelText) && TextUtils.isEmpty(mOkText)) {
            mControllerPanel.setVisibility(View.GONE);
        } else {
            mControllerPanel.setVisibility(View.VISIBLE);
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

        public int mPanelMargen = 200;
        public Drawable mIcon;
        public int mIconId = 0;
        public int mIconAttrId = 0;
        public CharSequence mTitle;
        public View mCustomTitleView;
        public CharSequence mMessage;
        public BaseAdapter mDataAdapter;
        public View mCustomView;
        public boolean mViewSpacingSpecified = false;
        public int mViewSpacingLeft;
        public int mViewSpacingTop;
        public int mViewSpacingRight;
        public int mViewSpacingBottom;
        public boolean mCancelable = true;
        public boolean mCanceledOnTouchOutside = true;

        private ActionMenu[] sheetMenus;
        public String[] oriDatas;
        public int mCheckedItem = -1;

        public AdapterView.OnItemSelectedListener mOnItemSelectedListener;
        public OnPrepareListViewListener mOnPrepareListViewListener;
        public boolean mRecycleOnMeasure = true;

        public int mGravity = Gravity.BOTTOM;

        /**
         * Interface definition for a callback to be invoked before the ListView
         * will be bound to an adapter.
         */
        public interface OnPrepareListViewListener {
            /**
             * Called before the ListView is bound to an adapter.
             *
             * @param listView The ListView that will be shown in the dialog.
             */
            void onPrepareListView(ListView listView);
        }

        public PanelParams() {

        }

        public void apply(PanelController panelController) {
            //title
            if (mCustomTitleView != null) {
                panelController.setCustomTitle(mCustomTitleView);
            } else {
                if (!TextUtils.isEmpty(mTitle)) {
                    panelController.setTitle(mTitle);
                }
                if (mIcon != null) {
                    panelController.setIcon(mIcon);
                }
                if (mIconId >= 0) {
                    panelController.setIcon(mIconId);
                }
                if (mIconAttrId > 0) {
                    panelController.setIcon(panelController.getIconAttributeResId(mIconAttrId));
                }
            }
            //msg
            if (!TextUtils.isEmpty(mMessage)) {
                panelController.setMessage(mMessage);
            }
            // dataadapter
            if (mDataAdapter != null) {
                panelController.setDataAdapter(mDataAdapter);
            }
            // custom view
            if (mCustomView != null) {
                if (mViewSpacingSpecified) {
                    panelController.setCustomView(
                            mCustomView,
                            mViewSpacingLeft,
                            mViewSpacingTop,
                            mViewSpacingRight,
                            mViewSpacingBottom
                    );
                } else {
                    panelController.setCustomView(mCustomView);
                }
            }

            // other settings
            panelController.mGravity = mGravity;
            panelController.setPanelMargen(mPanelMargen);

            panelController.applyView();

        }

        private BaseAdapter createBaseAdapter() {
            return new BaseAdapter() {
                @Override
                public int getCount() {
                    return 0;
                }

                @Override
                public Object getItem(int position) {
                    return null;
                }

                @Override
                public long getItemId(int position) {
                    return 0;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    return null;
                }
            };
        }

        private BaseAdapter createSheetAdapter() {
            return new BaseAdapter() {
                @Override
                public int getCount() {
                    return 0;
                }

                @Override
                public Object getItem(int position) {
                    return null;
                }

                @Override
                public long getItemId(int position) {
                    return 0;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    return null;
                }
            };
        }

    }

}
