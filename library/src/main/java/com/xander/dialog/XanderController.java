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

package com.xander.dialog;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class XanderController {

    private final Context context;
    private final DialogInterface dialogInterface;

    /**
     * 整个父容器
     */
    private View mParent;
    /**
     * 父容器背景 处理点击外部消失和背景效果
     */
    private View mParentBG;
    /**
     * 内容填充面板
     */
    private View mContentPanel;
    /**
     * 用户自定义顶部内容
     */
    private View mCustomTitleView;
    /**
     * 顶部标题 icon
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
     * 顶部标题 text
     */
    private TextView mTitleView;
    /**
     * 顶部标题 text 内容
     */
    private CharSequence mTitle;

    /**
     * 中间信息 ScrollView 容器
     */
    private ScrollView mScrollView;
    /**
     * 中间信息 text
     */
    private TextView mMessageView;
    /**
     * 中间信息 text 内容
     */
    private CharSequence mMessage;
    /**
     * 内置的列表
     */
    private ListView mListView;

    /**
     * 用户自定义的View
     */
    private View mCustomView;

    private boolean mViewSpacingSpecified = false;
    private int mViewSpacingLeft;
    private int mViewSpacingTop;
    private int mViewSpacingRight;
    private int mViewSpacingBottom;

    private boolean mForceInverseBackground;

    private ListAdapter mAdapter;
    private int mCheckedItem = -1;

    private int mXanderLayout;
    private int mListLayout;
    private int mListItemLayout;
    private int mMultiChoiceItemLayout;
    private int mSingleChoiceItemLayout;

    public static final int DURATION                    = 300;
    private static final int DURATION_TRANSLATE         = 200;
    private static final int DURATION_ALPHA             = DURATION;

    private static final int ANIM_TYPE_SHOW             = 0;
    private static final int ANIM_TYPE_DISMISS          = 1;

    private int mGravity = android.view.Gravity.BOTTOM;

    private int mPanelMargen = 160;

    public XanderController(Context context, DialogInterface dialogInterface) {
        this.context = context;
        this.dialogInterface = dialogInterface;
        mXanderLayout = R.layout.xander_dialog;
        mListLayout = R.layout.xander_dialog_list;
        mMultiChoiceItemLayout = R.layout.xander_dialog_list_multichoice;
        mSingleChoiceItemLayout = R.layout.xander_dialog_list_singlechoice;
        mListItemLayout = R.layout.xander_dialog_list_item;
    }

    /**
     * 检测 view 是否可以输入
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

    private View tryInflaterParent() {
        if (null == mParent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            mParent = inflater.inflate(mXanderLayout, null);
            mParentBG = mParent.findViewById(R.id.parent_background);
            mContentPanel = mParent.findViewById(R.id.parent_panel);
        }
        return mParent;
    }

    View getParentView() {
        return mParent;
    }

    View getParentBgView() {
        return mParentBG;
    }

    View getContentPanelView() {
        return mContentPanel;
    }

    private void setPanelMargen(int margen) {
        mPanelMargen = margen;
    }

    public void setTitle(CharSequence title) {
        mTitle = title;
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
    }

    /**
     * @see XanderDialog.Builder#setCustomTitle(View)
     */
    public void setCustomTitle(View customTitleView) {
        mCustomTitleView = customTitleView;
    }

    public void setMessage(CharSequence message) {
        mMessage = message;
        if (mMessageView != null) {
            mMessageView.setText(message);
        }
    }

    /**
     * Set the view to display in the dialog.
     */
    public void setCustomView(View view) {
        mCustomView = view;
        mViewSpacingSpecified = false;
    }

    /**
     * Set the view to display in the dialog along with the spacing around that view
     */
    public void setCustomView(View view, int viewSpacingLeft, int viewSpacingTop,
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
    public void setIcon(int resId) {
        mIconId = resId;
        if (mIconView != null) {
            if (resId > 0) {
                mIconView.setImageResource(mIconId);
            } else if (resId == 0) {
                mIconView.setVisibility(View.GONE);
            }
        }
    }

    public void setIcon(Drawable icon) {
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
        context.getTheme().resolveAttribute(attrId, out, true);
        return out.resourceId;
    }

    public void setInverseBackgroundForced(boolean forceInverseBackground) {
        mForceInverseBackground = forceInverseBackground;
    }

    public ListView getListView() {
        return mListView;
    }

    private void initView() {
        if (null == mParent) {
            tryInflaterParent();
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mContentPanel.getLayoutParams();
        layoutParams.gravity = mGravity;
        if (Gravity.TOP == mGravity) {
            layoutParams.bottomMargin = mPanelMargen;
        } else if (Gravity.BOTTOM == mGravity) {
            layoutParams.topMargin = mPanelMargen;
        }
        mContentPanel.setLayoutParams(layoutParams);
        mContentPanel.setOnClickListener(null);

        LinearLayout topPanel = (LinearLayout) mParent.findViewById(R.id.inner_top_panel);
        boolean hasTitle = tryInitTitle(topPanel);
        LinearLayout contentPanel = (LinearLayout) mParent.findViewById(R.id.content_panel);
        tryInitContentPanel(contentPanel);
        FrameLayout customPanel = (FrameLayout) mParent.findViewById(R.id.customPanel);
        tryInitCustomPanel(customPanel);

    }

    private boolean tryInitTitle(LinearLayout topPanel) {
        boolean hasTitle = true;
        if (mCustomTitleView != null) {
            // Add the custom title view directly to the topPanel layout
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            topPanel.addView(mCustomTitleView, 0, lp);
            // Hide the title template
            View titleTemplate = mParent.findViewById(R.id.inner_title_template);
            titleTemplate.setVisibility(View.GONE);
        } else {
            final boolean hasTextTitle = !TextUtils.isEmpty(mTitle);
            mIconView = (ImageView) mParent.findViewById(R.id.icon);
            if (hasTextTitle) {
                /* Display the title if a title is supplied, else hide it */
                mTitleView = (TextView) mParent.findViewById(R.id.alertTitle);
                mTitleView.setText(mTitle);
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
                    mTitleView.setPadding(
                            mIconView.getPaddingLeft(),
                            mIconView.getPaddingTop(),
                            mIconView.getPaddingRight(),
                            mIconView.getPaddingBottom()
                    );
                    mIconView.setVisibility(View.GONE);
                }
            } else {
                // Hide the title template
                View titleTemplate = mParent.findViewById(R.id.inner_title_template);
                titleTemplate.setVisibility(View.GONE);
                mIconView.setVisibility(View.GONE);
                topPanel.setVisibility(View.GONE);
                hasTitle = false;
            }
        }
        return hasTitle;
    }

    private void tryInitContentPanel(LinearLayout contentPanel) {
        mScrollView = (ScrollView) mParent.findViewById(R.id.messsage_scrollview);
        mScrollView.setFocusable(false);
        // Special case for users that only want to display a String
        mMessageView = (TextView) mParent.findViewById(R.id.message);
        if (mMessageView == null) {
            return;
        }
        if (mMessage != null) {
            mMessageView.setText(mMessage);
        } else {
            mMessageView.setVisibility(View.GONE);
            mScrollView.removeView(mMessageView);
            if (mListView != null) {
                if ((mListView != null) && (mAdapter != null)) {
                    mListView.setAdapter(mAdapter);
                    if (mCheckedItem > -1) {
                        mListView.setItemChecked(mCheckedItem, true);
                        mListView.setSelection(mCheckedItem);
                    }
                }
                contentPanel.removeView(mScrollView);
                contentPanel.addView(mListView,
                        new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                );
                contentPanel.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, 0, 1.0f));
            } else {
                contentPanel.setVisibility(View.GONE);
            }
        }
    }

    private void tryInitCustomPanel(FrameLayout customPanel) {
        if (mCustomView != null) {
            mParent.findViewById(R.id.inner_view_panel).setVisibility(View.GONE);
            FrameLayout custom = (FrameLayout) mParent.findViewById(R.id.custom_view);
            custom.addView(mCustomView, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
            if (mViewSpacingSpecified) {
                custom.setPadding(
                        mViewSpacingLeft,
                        mViewSpacingTop,
                        mViewSpacingRight,
                        mViewSpacingBottom
                );
            }
//            if (mListView != null) {
//                ((LinearLayout.LayoutParams) customPanel.getLayoutParams()).weight = 0;
//            }
        } else {
            mParent.findViewById(R.id.customPanel).setVisibility(View.GONE);
        }
    }

    void animateShow() {
        doAnim(ANIM_TYPE_SHOW);
    }

    void animateDismiss() {
        doAnim(ANIM_TYPE_DISMISS);
    }

    private void doAnim(int type) {
        mParentBG.startAnimation(createBgAnimation(type));
        mContentPanel.startAnimation(createPanelAnimation(type));
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

    public static class RecycleListView extends ListView {
        boolean mRecycleOnMeasure = true;

        public RecycleListView(Context context) {
            super(context);
        }

        public RecycleListView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public RecycleListView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        protected boolean recycleOnMeasure() {
            return mRecycleOnMeasure;
        }
    }

    public static class XanderParams {
        public final Context mContext;
        public final LayoutInflater mInflater;
        public int mIconId = 0;
        public Drawable mIcon;
        public int mIconAttrId = 0;
        public CharSequence mTitle;
        public View mCustomTitleView;
        public CharSequence mMessage;

        public boolean mCancelable = true;
        public boolean mCanceledOnTouchOutside = true;
        public XanderDialog.XanderListener mXanderListener;

        public DialogInterface.OnCancelListener mOnCancelListener;
        public DialogInterface.OnDismissListener mOnDismissListener;
        public DialogInterface.OnKeyListener mOnKeyListener;
        public DialogInterface.OnShowListener mOnShowListener;

        public int mPanelMargen = 200;

        public CharSequence[] mItems;
        public ListAdapter mAdapter;
        public DialogInterface.OnClickListener mOnClickListener;

        public View mView;
        public int mViewSpacingLeft;
        public int mViewSpacingTop;
        public int mViewSpacingRight;
        public int mViewSpacingBottom;
        public boolean mViewSpacingSpecified = false;

        public boolean[] mCheckedItems;
        public boolean mIsMultiChoice;
        public boolean mIsSingleChoice;
        public int mCheckedItem = -1;
        public DialogInterface.OnMultiChoiceClickListener mOnCheckboxClickListener;
        public Cursor mCursor;
        public String mLabelColumn;
        public String mIsCheckedColumn;
        public boolean mForceInverseBackground;
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

        public XanderParams(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void apply(XanderController xanderController) {
            if (mCustomTitleView != null) {
                xanderController.setCustomTitle(mCustomTitleView);
            } else {
                if (mTitle != null) {
                    xanderController.setTitle(mTitle);
                }
                if (mIcon != null) {
                    xanderController.setIcon(mIcon);
                }
                if (mIconId >= 0) {
                    xanderController.setIcon(mIconId);
                }
                if (mIconAttrId > 0) {
                    xanderController.setIcon(xanderController.getIconAttributeResId(mIconAttrId));
                }
            }
            if (mMessage != null) {
                xanderController.setMessage(mMessage);
            }
            if (mForceInverseBackground) {
                xanderController.setInverseBackgroundForced(true);
            }
            // For a list, the client can either supply an array of items or an
            // adapter or a cursor
            if ((mItems != null) || (mCursor != null) || (mAdapter != null)) {
                createListView(xanderController);
            }
            if (mView != null) {
                if (mViewSpacingSpecified) {
                    xanderController.setCustomView(mView, mViewSpacingLeft, mViewSpacingTop, mViewSpacingRight,
                            mViewSpacingBottom);
                } else {
                    xanderController.setCustomView(mView);
                }
            }
            xanderController.mGravity = mGravity;
            xanderController.setPanelMargen(mPanelMargen);

            xanderController.initView();

            if (mCanceledOnTouchOutside) {
                xanderController.getParentBgView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mXanderListener.dismissExpandDialog();
                    }
                });
            }
        }

        private void createListView(final XanderController xanderController) {
            final RecycleListView listView = (RecycleListView) mInflater.inflate(xanderController.mListLayout, null);
            ListAdapter adapter;
            if (mIsMultiChoice) {
                if (mCursor == null) {
                    adapter = new ArrayAdapter<CharSequence>(mContext,
                            xanderController.mMultiChoiceItemLayout, R.id.list_item_text, mItems) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            if (mCheckedItems != null) {
                                boolean isItemChecked = mCheckedItems[position];
                                if (isItemChecked) {
                                    listView.setItemChecked(position, true);
                                }
                            }
                            return view;
                        }
                    };
                } else {
                    adapter = new CursorAdapter(mContext, mCursor, false) {
                        private final int mLabelIndex;
                        private final int mIsCheckedIndex;

                        {
                            final Cursor cursor = getCursor();
                            mLabelIndex = cursor.getColumnIndexOrThrow(mLabelColumn);
                            mIsCheckedIndex = cursor.getColumnIndexOrThrow(mIsCheckedColumn);
                        }

                        @Override
                        public void bindView(View view, Context context, Cursor cursor) {
                            CheckedTextView text = (CheckedTextView) view.findViewById(R.id.list_item_text);
                            text.setText(cursor.getString(mLabelIndex));
                            listView.setItemChecked(cursor.getPosition(),
                                    cursor.getInt(mIsCheckedIndex) == 1);
                        }

                        @Override
                        public View newView(Context context, Cursor cursor, ViewGroup parent) {
                            return mInflater.inflate(xanderController.mMultiChoiceItemLayout, parent, false);
                        }

                    };
                }
            } else {
                int layout = mIsSingleChoice ? xanderController.mSingleChoiceItemLayout : xanderController.mListItemLayout;
                if (mCursor == null) {
                    adapter = (mAdapter != null) ?
                            mAdapter : new ArrayAdapter<CharSequence>(mContext, layout, R.id.list_item_text, mItems);
                } else {
                    adapter = new SimpleCursorAdapter(mContext, layout,
                            mCursor, new String[]{mLabelColumn}, new int[]{R.id.list_item_text});
                }
            }

            if (mOnPrepareListViewListener != null) {
                mOnPrepareListViewListener.onPrepareListView(listView);
            }
            
            /* Don't directly set the adapter on the ListView as we might
             * want to add a footer to the ListView later.
             */
            xanderController.mAdapter = adapter;
            xanderController.mCheckedItem = mCheckedItem;

            if (mOnClickListener != null) {
                listView.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView parent, View v, int position, long id) {
                        mOnClickListener.onClick(xanderController.dialogInterface, position);
                        mXanderListener.dismissExpandDialog();
                    }
                });
            } else if (mOnCheckboxClickListener != null) {
                listView.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView parent, View v, int position, long id) {
                        if (mCheckedItems != null) {
                            mCheckedItems[position] = listView.isItemChecked(position);
                        }
                        mOnCheckboxClickListener.onClick(
                                xanderController.dialogInterface, position, listView.isItemChecked(position));

                        //mXanderListener.dismissExpandDialog();
                    }
                });
            } else {
                listView.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView parent, View v, int position, long id) {
                        mXanderListener.dismissExpandDialog();
                    }
                });
            }

            // Attach a given OnItemSelectedListener to the ListView
            if (mOnItemSelectedListener != null) {
                listView.setOnItemSelectedListener(mOnItemSelectedListener);
            }

            if (mIsSingleChoice) {
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            } else if (mIsMultiChoice) {
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
            listView.mRecycleOnMeasure = mRecycleOnMeasure;
            xanderController.mListView = listView;
        }
    }

}
