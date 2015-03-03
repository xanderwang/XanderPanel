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

package com.xandy.expanddialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.xandy.expanddialog.ExpandController.ExpandParams;

public class ExpandDialog implements DialogInterface , DialogInterface.OnKeyListener {

    private static final String TAG = "ExpandDialog";

    private Context mContext;
    private ExpandController mExpandController;
    private Dialog mDialog; 
    
    private DialogInterface.OnCancelListener mExpandCalcelListener ;
    private DialogInterface.OnDismissListener mExpandDismissListeners;
    private DialogInterface.OnShowListener mExpandShowListener;
    
    private ExpandListener mExpandListener = new ExpandListener() {
		@Override
		public void dismissExpandDialog() {
			dismiss();
		}
	};
    
    public static final int HANDLER_SHOW_DIALOG 		= 0;
    public static final int HANDLER_DISMISS_DIALOG 	= 1;
    public static final int HANDLER_DISMISS_CANCEL 	= 2;
    
    private Handler mHandler = new Handler(){
    	public void handleMessage(android.os.Message msg) {
    		int what = msg.what;
    		switch (what) {
    		case HANDLER_DISMISS_CANCEL :
			case HANDLER_DISMISS_DIALOG :
				mDialog.dismiss();
				break;
			default:
				break;
			}
    	};
    };

    protected boolean mDismissing;
    private boolean mCancelable = true;

    /** 背景透明度 */
    private static final float DEFAULT_DIM_AMOUNT = 0.0f;

    public ExpandDialog(Context context) {
        this(context, null, 0);
    }

    public ExpandDialog(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandDialog( Context context, AttributeSet attrs, int defStyleAttr ) {
    	mContext = context;
        mDialog = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
        setDimAmount( DEFAULT_DIM_AMOUNT );
        mExpandController = new ExpandController(mContext, this );        
        
        initDialogListener();
        
    }
    
    private void initDialogListener() {
    	mDialog.setOnKeyListener( this );
    	mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if( null != mExpandCalcelListener ) {
					mExpandCalcelListener.onCancel(ExpandDialog.this);
				}
			}
		});
    	mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if( null != mExpandDismissListeners ) {
					mExpandDismissListeners.onDismiss(ExpandDialog.this);
				}
			}
		});
    	mDialog.setOnShowListener(new OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				if ( null != mExpandShowListener ) {
					mExpandShowListener.onShow(ExpandDialog.this);
				}
			}
		});
    }
    
    private void setDimAmount( float dimAmount ) {
    	if( null != mDialog ) {
    		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    		WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
    		lp.dimAmount = dimAmount;
    		mDialog.getWindow().setAttributes(lp);    		
    	}
    }

    /**
     * Show the dialog, dimming the screen and expanding the button menu
     */
    public void show() {
        mDialog.show();
        mDismissing = false;
		mExpandController.animateShow();
    }

    /**
     * Dismiss the dialog, removing screen dim and hiding the expanded menu
     */
    public void dismiss() {
    	mDismissing = true;
        mExpandController.animateDismiss();
        mHandler.sendEmptyMessageDelayed(HANDLER_DISMISS_DIALOG, ExpandController.DURATION );
    }
    
    @Override
    public void cancel() {
    	mExpandController.animateDismiss();
        mHandler.sendEmptyMessageDelayed(HANDLER_DISMISS_DIALOG, ExpandController.DURATION );
    }
    
    
    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP 
        		&& !event.isCanceled() && mCancelable && !mDismissing ) {
        	mDismissing = true;
        	mExpandController.animateDismiss();
        	dismiss();
        }
        return true;
    }
	
	/**
     * Gets the list view used in the dialog.
     *  
     * @return The {@link ListView} from the dialog.
     */
    public ListView getListView() {
        return mExpandController.getListView();
    }
    
    public void setTitle(CharSequence title) {
        mExpandController.setTitle(title);
    }

    /**
     * @see Builder#setCustomTitle(View)
     */
    public void setCustomTitle(View customTitleView) {
    	mExpandController.setCustomTitle(customTitleView);
    }
    
    public void setMessage(CharSequence message) {
    	mExpandController.setMessage(message);
    }

    /**
     * Set the view to display in that dialog.
     */
    public void setView(View view) {
    	mExpandController.setView(view);
    }
    
    /**
     * Set the view to display in that dialog, specifying the spacing to appear around that 
     * view.
     *
     * @param view The view to show in the content area of the dialog
     * @param viewSpacingLeft Extra space to appear to the left of {@code view}
     * @param viewSpacingTop Extra space to appear above {@code view}
     * @param viewSpacingRight Extra space to appear to the right of {@code view}
     * @param viewSpacingBottom Extra space to appear below {@code view}
     */
    public void setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight,
            int viewSpacingBottom) {
    	mExpandController.setView(view, viewSpacingLeft, viewSpacingTop, viewSpacingRight, viewSpacingBottom);
    }
    
    /**
     * Set resId to 0 if you don't want an icon.
     * @param resId the resourceId of the drawable to use as the icon or 0
     * if you don't want an icon.
     */
    public void setIcon(int resId) {
    	mExpandController.setIcon(resId);
    }
    
    public void setIcon(Drawable icon) {
    	mExpandController.setIcon(icon);
    }

    /**
     * Set an icon as supplied by a theme attribute. e.g. android.R.attr.ExpandDialogIcon
     *
     * @param attrId ID of a theme attribute that points to a drawable resource.
     */
    public void setIconAttribute(int attrId) {
        TypedValue out = new TypedValue();
        mContext.getTheme().resolveAttribute(attrId, out, true);
        mExpandController.setIcon(out.resourceId);
    }

    public void setInverseBackgroundForced(boolean forceInverseBackground) {
    	mExpandController.setInverseBackgroundForced(forceInverseBackground);
    }
    
    public interface ExpandListener {
    	public void dismissExpandDialog();
    }
    
    public interface ExpandShowListener {
    	public void onExpandShow();
    }
    
    public static class Builder {
    	
        private final ExpandParams mExpandParams;
        private int mTheme;
        
        /**
         * Constructor using a context for this builder and the {@link ExpandDialog} it creates.
         */
        public Builder(Context context ) {
        	this(context , 0 );
        }

        /**
         * Constructor using a context and theme for this builder and
         * the {@link ExpandDialog} it creates.  The actual theme
         * that an ExpandDialog uses is a private implementation, however you can
         * here supply either the name of an attribute in the theme from which
         * to get the dialog's style (such as {@link android.R.attr#ExpandDialogTheme}
         * or one of the constants
         * {@link ExpandDialog#THEME_TRADITIONAL ExpandDialog.THEME_TRADITIONAL},
         * {@link ExpandDialog#THEME_HOLO_DARK ExpandDialog.THEME_HOLO_DARK}, or
         * {@link ExpandDialog#THEME_HOLO_LIGHT ExpandDialog.THEME_HOLO_LIGHT}.
         */
        public Builder(Context context, int theme  ) {
        	mExpandParams = new ExpandController.ExpandParams(context);
            mTheme = theme;
            setPanleMargen(context.getResources().getDimensionPixelSize(R.dimen.dialog_panel_margen));
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
            return mExpandParams.mContext;
        }

        /**
         * Set the title using the given resource id.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setTitle(int titleId) {
            mExpandParams.mTitle = mExpandParams.mContext.getText(titleId);
            return this;
        }
        
        /**
         * Set the title displayed in the {@link Dialog}.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setTitle(CharSequence title) {
            mExpandParams.mTitle = title;
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
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setCustomTitle(View customTitleView) {
            mExpandParams.mCustomTitleView = customTitleView;
            return this;
        }
        
        /**
         * Set the message to display using the given resource id.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setMessage(int messageId) {
            mExpandParams.mMessage = mExpandParams.mContext.getText(messageId);
            return this;
        }
        
        /**
         * Set the message to display.
          *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setMessage(CharSequence message) {
            mExpandParams.mMessage = message;
            return this;
        }
        
        /**
         * Set the resource id of the {@link Drawable} to be used in the title.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setIcon(int iconId) {
            mExpandParams.mIconId = iconId;
            return this;
        }
        
        /**
         * Set the {@link Drawable} to be used in the title.
          *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setIcon(Drawable icon) {
            mExpandParams.mIcon = icon;
            return this;
        }

        /**
         * Set an icon as supplied by a theme attribute. e.g. android.R.attr.ExpandDialogIcon
         *
         * @param attrId ID of a theme attribute that points to a drawable resource.
         */
        public Builder setIconAttribute(int attrId) {
            TypedValue out = new TypedValue();
            mExpandParams.mContext.getTheme().resolveAttribute(attrId, out, true);
            mExpandParams.mIconId = out.resourceId;
            return this;
        }
        
        /**
         * Sets whether the dialog is cancelable or not.  Default is true.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setCancelable(boolean cancelable) {
            mExpandParams.mCancelable = cancelable;
            return this;
        }
        
        public Builder setGravity( int gravity ) {
        	mExpandParams.mGravity = gravity;
        	return this;
        }
        
        public Builder setCanceledOnTouchOutside(boolean outside) {
        	mExpandParams.mCanceledOnTouchOutside = outside;
        	return this;
        }
        
        public Builder setPanleMargen( int margen ) {
        	mExpandParams.mPanelMargen = margen;
        	return this;
        }
        
        /**
         * Sets the callback that will be called if the dialog is canceled.
         *
         * <p>Even in a cancelable dialog, the dialog may be dismissed for reasons other than
         * being canceled or one of the supplied choices being selected.
         * If you are interested in listening for all cases where the dialog is dismissed
         * and not just when it is canceled, see
         * {@link #setOnDismissListener(DialogInterface.OnDismissListener) setOnDismissListener}.</p>
         * @see #setCancelable(boolean)
         * @see #setOnDismissListener(android.content.DialogInterface.OnDismissListener)
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setOnCancelListener(OnCancelListener onCancelListener) {
        	mExpandParams.mOnCancelListener = onCancelListener;
            return this;
        }
        
        /**
         * Sets the callback that will be called when the dialog is dismissed for any reason.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setOnDismissListener(OnDismissListener onDismissListener) {
            mExpandParams.mOnDismissListener = onDismissListener;
            return this;
        }

        /**
         * Sets the callback that will be called if a key is dispatched to the dialog.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setOnKeyListener(OnKeyListener onKeyListener) {
        	mExpandParams.mOnKeyListener = onKeyListener;
            return this;
        }
        
        /**
         * Sets the callback that will be called if the dialog is show.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setOnShowListener(OnShowListener onShowListener) {
        	mExpandParams.mOnShowListener = onShowListener;
            return this;
        }
        
        /**
         * Set a list of items to be displayed in the dialog as the content, you will be notified of the
         * selected item via the supplied listener. This should be an array type i.e. R.array.foo
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setItems(int itemsId, final OnClickListener listener) {
            mExpandParams.mItems = mExpandParams.mContext.getResources().getTextArray(itemsId);
            mExpandParams.mOnClickListener = listener;
            return this;
        }
        
        /**
         * Set a list of items to be displayed in the dialog as the content, you will be notified of the
         * selected item via the supplied listener.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setItems(CharSequence[] items, final OnClickListener listener) {
            mExpandParams.mItems = items;
            mExpandParams.mOnClickListener = listener;
            return this;
        }
        
        /**
         * Set a list of items, which are supplied by the given {@link ListAdapter}, to be
         * displayed in the dialog as the content, you will be notified of the
         * selected item via the supplied listener.
         * 
         * @param adapter The {@link ListAdapter} to supply the list of items
         * @param listener The listener that will be called when an item is clicked.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setAdapter(final ListAdapter adapter, final OnClickListener listener) {
            mExpandParams.mAdapter = adapter;
            mExpandParams.mOnClickListener = listener;
            return this;
        }
        
        /**
         * Set a list of items, which are supplied by the given {@link Cursor}, to be
         * displayed in the dialog as the content, you will be notified of the
         * selected item via the supplied listener.
         * 
         * @param cursor The {@link Cursor} to supply the list of items
         * @param listener The listener that will be called when an item is clicked.
         * @param labelColumn The column name on the cursor containing the string to display
         *          in the label.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setCursor(final Cursor cursor, final OnClickListener listener,
                String labelColumn) {
            mExpandParams.mCursor = cursor;
            mExpandParams.mLabelColumn = labelColumn;
            mExpandParams.mOnClickListener = listener;
            return this;
        }
        
        /**
         * Set a list of items to be displayed in the dialog as the content,
         * you will be notified of the selected item via the supplied listener.
         * This should be an array type, e.g. R.array.foo. The list will have
         * a check mark displayed to the right of the text for each checked
         * item. Clicking on an item in the list will not dismiss the dialog.
         * Clicking on a button will dismiss the dialog.
         * 
         * @param itemsId the resource id of an array i.e. R.array.foo
         * @param checkedItems specifies which items are checked. It should be null in which case no
         *        items are checked. If non null it must be exactly the same length as the array of
         *        items.
         * @param listener notified when an item on the list is clicked. The dialog will not be
         *        dismissed when an item is clicked. It will only be dismissed if clicked on a
         *        button, if no buttons are supplied it's up to the user to dismiss the dialog.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setMultiChoiceItems(int itemsId, boolean[] checkedItems, 
                final OnMultiChoiceClickListener listener) {
            mExpandParams.mItems = mExpandParams.mContext.getResources().getTextArray(itemsId);
            mExpandParams.mOnCheckboxClickListener = listener;
            mExpandParams.mCheckedItems = checkedItems;
            mExpandParams.mIsMultiChoice = true;
            return this;
        }
        
        /**
         * Set a list of items to be displayed in the dialog as the content,
         * you will be notified of the selected item via the supplied listener.
         * The list will have a check mark displayed to the right of the text
         * for each checked item. Clicking on an item in the list will not
         * dismiss the dialog. Clicking on a button will dismiss the dialog.
         * 
         * @param items the text of the items to be displayed in the list.
         * @param checkedItems specifies which items are checked. It should be null in which case no
         *        items are checked. If non null it must be exactly the same length as the array of
         *        items.
         * @param listener notified when an item on the list is clicked. The dialog will not be
         *        dismissed when an item is clicked. It will only be dismissed if clicked on a
         *        button, if no buttons are supplied it's up to the user to dismiss the dialog.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setMultiChoiceItems(CharSequence[] items, boolean[] checkedItems, 
                final OnMultiChoiceClickListener listener) {
            mExpandParams.mItems = items;
            mExpandParams.mOnCheckboxClickListener = listener;
            mExpandParams.mCheckedItems = checkedItems;
            mExpandParams.mIsMultiChoice = true;
            return this;
        }
        
        /**
         * Set a list of items to be displayed in the dialog as the content,
         * you will be notified of the selected item via the supplied listener.
         * The list will have a check mark displayed to the right of the text
         * for each checked item. Clicking on an item in the list will not
         * dismiss the dialog. Clicking on a button will dismiss the dialog.
         * 
         * @param cursor the cursor used to provide the items.
         * @param isCheckedColumn specifies the column name on the cursor to use to determine
         *        whether a checkbox is checked or not. It must return an integer value where 1
         *        means checked and 0 means unchecked.
         * @param labelColumn The column name on the cursor containing the string to display in the
         *        label.
         * @param listener notified when an item on the list is clicked. The dialog will not be
         *        dismissed when an item is clicked. It will only be dismissed if clicked on a
         *        button, if no buttons are supplied it's up to the user to dismiss the dialog.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setMultiChoiceItems(Cursor cursor, String isCheckedColumn, String labelColumn, 
                final OnMultiChoiceClickListener listener) {
            mExpandParams.mCursor = cursor;
            mExpandParams.mOnCheckboxClickListener = listener;
            mExpandParams.mIsCheckedColumn = isCheckedColumn;
            mExpandParams.mLabelColumn = labelColumn;
            mExpandParams.mIsMultiChoice = true;
            return this;
        }
        
        /**
         * Set a list of items to be displayed in the dialog as the content, you will be notified of
         * the selected item via the supplied listener. This should be an array type i.e.
         * R.array.foo The list will have a check mark displayed to the right of the text for the
         * checked item. Clicking on an item in the list will not dismiss the dialog. Clicking on a
         * button will dismiss the dialog.
         * 
         * @param itemsId the resource id of an array i.e. R.array.foo
         * @param checkedItem specifies which item is checked. If -1 no items are checked.
         * @param listener notified when an item on the list is clicked. The dialog will not be
         *        dismissed when an item is clicked. It will only be dismissed if clicked on a
         *        button, if no buttons are supplied it's up to the user to dismiss the dialog.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setSingleChoiceItems(int itemsId, int checkedItem, 
                final OnClickListener listener) {
            mExpandParams.mItems = mExpandParams.mContext.getResources().getTextArray(itemsId);
            mExpandParams.mOnClickListener = listener;
            mExpandParams.mCheckedItem = checkedItem;
            mExpandParams.mIsSingleChoice = true;
            return this;
        }
        
        /**
         * Set a list of items to be displayed in the dialog as the content, you will be notified of
         * the selected item via the supplied listener. The list will have a check mark displayed to
         * the right of the text for the checked item. Clicking on an item in the list will not
         * dismiss the dialog. Clicking on a button will dismiss the dialog.
         * 
         * @param cursor the cursor to retrieve the items from.
         * @param checkedItem specifies which item is checked. If -1 no items are checked.
         * @param labelColumn The column name on the cursor containing the string to display in the
         *        label.
         * @param listener notified when an item on the list is clicked. The dialog will not be
         *        dismissed when an item is clicked. It will only be dismissed if clicked on a
         *        button, if no buttons are supplied it's up to the user to dismiss the dialog.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setSingleChoiceItems(Cursor cursor, int checkedItem, String labelColumn, 
                final OnClickListener listener) {
            mExpandParams.mCursor = cursor;
            mExpandParams.mOnClickListener = listener;
            mExpandParams.mCheckedItem = checkedItem;
            mExpandParams.mLabelColumn = labelColumn;
            mExpandParams.mIsSingleChoice = true;
            return this;
        }
        
        /**
         * Set a list of items to be displayed in the dialog as the content, you will be notified of
         * the selected item via the supplied listener. The list will have a check mark displayed to
         * the right of the text for the checked item. Clicking on an item in the list will not
         * dismiss the dialog. Clicking on a button will dismiss the dialog.
         * 
         * @param items the items to be displayed.
         * @param checkedItem specifies which item is checked. If -1 no items are checked.
         * @param listener notified when an item on the list is clicked. The dialog will not be
         *        dismissed when an item is clicked. It will only be dismissed if clicked on a
         *        button, if no buttons are supplied it's up to the user to dismiss the dialog.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setSingleChoiceItems(CharSequence[] items, int checkedItem, final OnClickListener listener) {
            mExpandParams.mItems = items;
            mExpandParams.mOnClickListener = listener;
            mExpandParams.mCheckedItem = checkedItem;
            mExpandParams.mIsSingleChoice = true;
            return this;
        } 
        
        /**
         * Set a list of items to be displayed in the dialog as the content, you will be notified of
         * the selected item via the supplied listener. The list will have a check mark displayed to
         * the right of the text for the checked item. Clicking on an item in the list will not
         * dismiss the dialog. Clicking on a button will dismiss the dialog.
         * 
         * @param adapter The {@link ListAdapter} to supply the list of items
         * @param checkedItem specifies which item is checked. If -1 no items are checked.
         * @param listener notified when an item on the list is clicked. The dialog will not be
         *        dismissed when an item is clicked. It will only be dismissed if clicked on a
         *        button, if no buttons are supplied it's up to the user to dismiss the dialog.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setSingleChoiceItems(ListAdapter adapter, int checkedItem, final OnClickListener listener) {
            mExpandParams.mAdapter = adapter;
            mExpandParams.mOnClickListener = listener;
            mExpandParams.mCheckedItem = checkedItem;
            mExpandParams.mIsSingleChoice = true;
            return this;
        }
        
        /**
         * Sets a listener to be invoked when an item in the list is selected.
         * 
         * @param listener The listener to be invoked.
         * @see AdapterView#setOnItemSelectedListener(android.widget.AdapterView.OnItemSelectedListener)
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setOnItemSelectedListener(final AdapterView.OnItemSelectedListener listener) {
            mExpandParams.mOnItemSelectedListener = listener;
            return this;
        }
        
        /**
         * Set a custom view to be the contents of the Dialog. If the supplied view is an instance
         * of a {@link ListView} the light background will be used.
         *
         * @param view The view to use as the contents of the Dialog.
         * 
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setView(View view) {
            mExpandParams.mView = view;
            mExpandParams.mViewSpacingSpecified = false;
            return this;
        }
        
        /**
         * Set a custom view to be the contents of the Dialog, specifying the
         * spacing to appear around that view. If the supplied view is an
         * instance of a {@link ListView} the light background will be used.
         * 
         * @param view The view to use as the contents of the Dialog.
         * @param viewSpacingLeft Spacing between the left edge of the view and
         *        the dialog frame
         * @param viewSpacingTop Spacing between the top edge of the view and
         *        the dialog frame
         * @param viewSpacingRight Spacing between the right edge of the view
         *        and the dialog frame
         * @param viewSpacingBottom Spacing between the bottom edge of the view
         *        and the dialog frame
         * @return This Builder object to allow for chaining of calls to set
         *         methods
         *         
         * 
         * This is currently hidden because it seems like people should just
         * be able to put padding around the view.
         * @hide
         */
        public Builder setView(View view, int viewSpacingLeft, int viewSpacingTop,
                int viewSpacingRight, int viewSpacingBottom) {
            mExpandParams.mView = view;
            mExpandParams.mViewSpacingSpecified = true;
            mExpandParams.mViewSpacingLeft = viewSpacingLeft;
            mExpandParams.mViewSpacingTop = viewSpacingTop;
            mExpandParams.mViewSpacingRight = viewSpacingRight;
            mExpandParams.mViewSpacingBottom = viewSpacingBottom;
            return this;
        }
        
        /**
         * Sets the Dialog to use the inverse background, regardless of what the
         * contents is.
         * 
         * @param useInverseBackground Whether to use the inverse background
         * 
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setInverseBackgroundForced(boolean useInverseBackground) {
            mExpandParams.mForceInverseBackground = useInverseBackground;
            return this;
        }

        /**
         * @hide
         */
        public Builder setRecycleOnMeasureEnabled(boolean enabled) {
            mExpandParams.mRecycleOnMeasure = enabled;
            return this;
        }


        /**
         * Creates a {@link ExpandDialog} with the arguments supplied to this builder. It does not
         * {@link Dialog#show()} the dialog. This allows the user to do any extra processing
         * before displaying the dialog. Use {@link #show()} if you don't have any other processing
         * to do and want this to be created and displayed.
         */
        public ExpandDialog create() {
        	
            final ExpandDialog expandDialog = new ExpandDialog( mExpandParams.mContext );
            
            mExpandParams.mExpandListener = expandDialog.mExpandListener;
            mExpandParams.apply( expandDialog.mExpandController );

            expandDialog.mDialog.setContentView( expandDialog.mExpandController.getParentView() );            

            expandDialog.mCancelable = mExpandParams.mCancelable;
            expandDialog.mExpandCalcelListener = mExpandParams.mOnCancelListener;
            expandDialog.mExpandDismissListeners = mExpandParams.mOnDismissListener;
            expandDialog.mExpandShowListener = mExpandParams.mOnShowListener;

            return expandDialog;
        }

        /**
         * Creates a {@link ExpandDialog} with the arguments supplied to this builder and
         * {@link Dialog#show()}'s the dialog.
         */
        public ExpandDialog show() {
        	ExpandDialog expandDialog = create();
            expandDialog.show();
            return expandDialog;
        }
    }
	
}
