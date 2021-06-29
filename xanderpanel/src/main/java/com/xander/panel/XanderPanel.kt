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
package com.xander.panel

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.TypedValue
import android.view.*
import androidx.annotation.MenuRes

@SuppressLint("ResourceType")
class XanderPanel private constructor(context: Context, theme: Int) : Dialog(context, theme),
    DialogInterface.OnKeyListener {

    private var statusBarColor = 0x30000000
    private var navigationBarColor = 0x30000000
    private var panelController: PanelController
    private var dismissListener: PanelDismissListener? = null
    private var showListener: PanelShowListener? = null

    private val mHandler: Handler = @SuppressLint("HandlerLeak") object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_DISMISS_CANCEL, MSG_DISMISS_DIALOG -> realDismiss()
            }
        }
    }

    private var mDismissing = false
    private var mCancelable = true

    private var mGravity = Gravity.TOP

    var tintManager: SystemBarTintManager

    init {
        val typedArray = context.obtainStyledAttributes(intArrayOf(STATUS_BAR_COLOR, NAVIGATION_BAR_COLOR))
        statusBarColor = typedArray.getColor(0, 0x30000000)
        navigationBarColor = typedArray.getColor(1, 0x30000000)
        typedArray.recycle()

        tintManager = SystemBarTintManager(context, window!!)
        tintManager.setStatusBarTintEnabled(true)
        tintManager.setStatusBarTintColor(0x0000ff00)
        tintManager.setNavigationBarTintEnabled(true)
        tintManager.setNavigationBarTintColor(0x00ff0000)
        tintManager.setTintAlpha(0F)

        // 设置背景透明度
        setDimAmount(DEFAULT_DIM_AMOUNT)
        panelController = PanelController(getContext(), this)
    }

    private fun setDimAmount(dimAmount: Float) {
        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            val lp = attributes
            lp.dimAmount = dimAmount
            attributes = lp
        }
    }

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        window?.apply {
            val params = attributes
            val attrs = context.obtainStyledAttributes(intArrayOf(android.R.attr.layout_width))
            try {
                params.width = attrs.getLayoutDimension(0, ViewGroup.LayoutParams.MATCH_PARENT)
            } finally {
                attrs.recycle()
            }
            attributes = params
        }
    }

    private fun setStatusBarAndNavigationBarColor(gravity: Int) {
        mGravity = gravity
        if (Build.VERSION.SDK_INT >= 21) {
            val decorView = window?.decorView
            val option = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            decorView?.systemUiVisibility = option
            when {
                Gravity.TOP == mGravity -> {
                    window?.statusBarColor = statusBarColor
                    window?.navigationBarColor = 0x00000000
                }
                Gravity.BOTTOM == mGravity -> {
                    window?.statusBarColor = 0x00000000
                    window?.navigationBarColor = navigationBarColor
                }
                else -> {
                    window?.statusBarColor = 0x00000000
                    window?.navigationBarColor = 0x00000000
                }
            }
        }
    }

    /**
     * Show the dialog, dimming the screen and expanding the button menu
     */
    override fun show() {
        super.show()
        mDismissing = false
        panelController.animateShow()
        showListener?.onPanelShow(this)
    }

    private fun realDismiss() {
        super.dismiss()
        dismissListener?.onPanelDismiss(this)
    }

    /**
     * Dismiss the dialog, removing screen dim and hiding the expanded menu
     */
    override fun dismiss() {
        mDismissing = true
        panelController.animateDismiss()
        mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, PanelController.DURATION)
    }

    override fun cancel() {
        panelController.animateDismiss()
        mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, PanelController.DURATION)
    }

    override fun setOnKeyListener(onKeyListener: DialogInterface.OnKeyListener?) {
        super.setOnKeyListener(this)
    }

    override fun onKey(dialog: DialogInterface, keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP && !event.isCanceled && mCancelable && !mDismissing) {
            dismiss()
            return true
        }
        return false
    }

    class Builder @JvmOverloads constructor(private val mContext: Context, private val mTheme: Int = TRANSLATE_DIALOG) {

        private val mPanelParams: PanelParams = PanelParams(mContext)

        /**
         * Constructor using a context and theme for this builder and
         * the [XanderPanel] it creates.  The actual theme
         * that an XanderPanel uses is a private implementation, however you can
         * here supply either the name of an attribute in the theme from which
         * to get the dialog's style or one of the constants
         */
        /**
         * Constructor using a context for this builder and the [XanderPanel] it creates.
         */
        init {
            val margin = mContext.resources.getDimensionPixelSize(R.dimen.panel_margen)
            setPanelMargin(margin)
        }

        /**
         * Returns a [Context] with the appropriate theme for dialogs created by this Builder.
         * Applications should use this Context for obtaining LayoutInflaters for inflating views
         * that will be used in the resulting dialogs, as it will cause views to be inflated with
         * the correct theme.
         *
         * @return A Context for built Dialogs.
         */
        fun getContext(): Context {
            return mContext
        }

        /**
         * Set the resource id of the [Drawable] to be used in the title.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setIcon(iconId: Int): Builder {
            mPanelParams.iconId = iconId
            return this
        }

        /**
         * Set the [Drawable] to be used in the title.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setIcon(icon: Drawable?): Builder {
            mPanelParams.icon = icon
            return this
        }

        /**
         * Set an icon as supplied by a theme attribute. e.g. android.R.attr.ExpandDialogIcon
         *
         * @param attrId ID of a theme attribute that points to a drawable resource.
         */
        fun setIconAttribute(attrId: Int): Builder {
            val out = TypedValue()
            mContext.theme.resolveAttribute(attrId, out, true)
            mPanelParams.iconId = out.resourceId
            return this
        }

        /**
         * Set the title using the given resource id.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setTitle(titleId: Int): Builder {
            mPanelParams.title = mContext.getText(titleId)
            return this
        }

        /**
         * Set the title displayed in the [Dialog].
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setTitle(title: CharSequence): Builder {
            mPanelParams.title = title
            return this
        }

        /**
         * Set the title using the custom view `customTitleView`. The
         * methods [.setTitle] and [.setIcon] should be
         * sufficient for most titles, but this is provided if the title needs
         * more customization. Using this will replace the title and icon set
         * via the other methods.
         *
         * @param customTitleView The custom view to use as the title.
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setCustomTitle(customTitleView: View): Builder {
            mPanelParams.customTitleView = customTitleView
            return this
        }

        /**
         * Set the message to display using the given resource id.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setMessage(messageId: Int): Builder {
            mPanelParams.message = mContext.getText(messageId)
            return this
        }

        /**
         * Set the message to display.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setMessage(message: CharSequence): Builder {
            mPanelParams.message = message
            return this
        }

        /**
         * Sets whether the dialog is cancelable or not.  Default is true.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setCancelable(cancelable: Boolean): Builder {
            mPanelParams.cancelable = cancelable
            return this
        }

        fun setGravity(gravity: Int): Builder {
            mPanelParams.mGravity = gravity
            return this
        }

        fun setCanceledOnTouchOutside(outside: Boolean): Builder {
            mPanelParams.canceledOnTouchOutside = outside
            return this
        }

        fun setPanelMargin(margin: Int): Builder {
            mPanelParams.panelMargin = margin
            return this
        }

        /**
         * Sets the callback that will be called when the dialog is dismissed for any reason.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setOnDismissListener(onDismissListener: PanelDismissListener?): Builder {
            mPanelParams.dismissListener = onDismissListener
            return this
        }

        /**
         * Sets the callback that will be called if the dialog is show.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setOnShowListener(onShowListener: PanelShowListener?): Builder {
            mPanelParams.showListener = onShowListener
            return this
        }

        /**
         * Set a custom view to be the contents of the Dialog. If the supplied view is an instance
         * of a [ListView] the light background will be used.
         *
         * @param view The view to use as the contents of the Dialog.
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setView(view: View?): Builder {
            mPanelParams.customView = view
            mPanelParams.viewSpacingSpecified = false
            return this
        }

        /**
         * Set a custom view to be the contents of the Dialog, specifying the
         * spacing to appear around that view. If the supplied view is an
         * instance of a [ListView] the light background will be used.
         *
         * @param view              The view to use as the contents of the Dialog.
         * @param viewSpacingLeft   Spacing between the left edge of the view and
         * the dialog frame
         * @param viewSpacingTop    Spacing between the top edge of the view and
         * the dialog frame
         * @param viewSpacingRight  Spacing between the right edge of the view
         * and the dialog frame
         * @param viewSpacingBottom Spacing between the bottom edge of the view
         * and the dialog frame
         * @return This Builder object to allow for chaining of calls to set
         * methods
         *
         * This is currently hidden because it seems like people should just
         * be able to put padding around the view.
         * @hide
         */
        fun setView(view: View?, viewSpacingLeft: Int, viewSpacingTop: Int, viewSpacingRight: Int,
                viewSpacingBottom: Int): Builder {
            mPanelParams.customView = view
            mPanelParams.viewSpacingSpecified = true
            mPanelParams.viewSpacingLeft = viewSpacingLeft
            mPanelParams.viewSpacingTop = viewSpacingTop
            mPanelParams.viewSpacingRight = viewSpacingRight
            mPanelParams.viewSpacingBottom = viewSpacingBottom
            return this
        }

        fun setSheet(sheetItems: Array<String>, showCancel: Boolean, cancelStr: String,
                sheetListener: SheetListener?): Builder {
            mPanelParams.showSheet = true
            mPanelParams.showSheetCancel = showCancel
            mPanelParams.sheetCancelStr = cancelStr
            mPanelParams.sheetItems = sheetItems
            mPanelParams.sheetListener = sheetListener
            return this
        }

        fun setController(negative: String, positive: String, controllerListener: PanelControllerListener?): Builder {
            mPanelParams.nagetive = negative
            mPanelParams.positive = positive
            mPanelParams.controllerListener = controllerListener
            return this
        }

        fun setMenu(@MenuRes xmlRes: Int, menuListener: PanelMenuListener?): Builder {
            if (null == mPanelParams.actionMenu) {
                mPanelParams.actionMenu = ActionMenu(mContext)
            }
            MenuInflater(mContext).inflate(xmlRes, mPanelParams.actionMenu)
            mPanelParams.menuListener = menuListener
            return this
        }

        fun grid(row: Int, col: Int): Builder {
            mPanelParams.showMenuAsGrid = true
            mPanelParams.pagerGridRow = row
            mPanelParams.pagerGridCol = col
            return this
        }

        fun list(): Builder {
            mPanelParams.showMenuAsGrid = false
            return this
        }

        fun shareText(text: String): Builder {
            mPanelParams.share = true
            mPanelParams.shareText = text
            return this
        }

        fun shareImage(image: String): Builder {
            mPanelParams.share = true
            mPanelParams.shareImages = arrayOf(image)
            return this
        }

        fun shareImages(images: Array<String>): Builder {
            mPanelParams.share = true
            mPanelParams.shareImages = images
            return this
        }

        /**
         * Creates a [XanderPanel] with the arguments supplied to this builder. It does not
         * [Dialog.show] the dialog. This allows the user to do any extra processing
         * before displaying the dialog. Use [.show] if you don't have any other processing
         * to do and want this to be created and displayed.
         */
        fun create(): XanderPanel {
            val xanderPanel = XanderPanel(mContext, mTheme)
            mPanelParams.apply(xanderPanel.panelController)
            xanderPanel.setContentView(xanderPanel.panelController.getParentView())
            xanderPanel.mCancelable = mPanelParams.cancelable
            xanderPanel.showListener = mPanelParams.showListener
            xanderPanel.dismissListener = mPanelParams.dismissListener
            xanderPanel.setStatusBarAndNavigationBarColor(mPanelParams.mGravity)
            return xanderPanel
        }

        /**
         * Creates a [XanderPanel] with the arguments supplied to this builder and
         * [Dialog.show]'s the dialog.
         */
        fun show(): XanderPanel {
            val xanderPanel = create()
            xanderPanel.show()
            return xanderPanel
        }

    }

    companion object {

        private val TAG: String = "XanderPanel"
        private val TRANSLATE_DIALOG = R.style.XanderPanel

        //    private static final int TRANSLATE_DIALOG = android.R.style.Theme_Dialog;
        private val STATUS_BAR_COLOR = R.attr.xPanel_StatusBarColor
        private val NAVIGATION_BAR_COLOR = R.attr.xPanel_NavigationBarColor
        const val MSG_SHOW_DIALOG = 0
        const val MSG_DISMISS_DIALOG = 1
        const val MSG_DISMISS_CANCEL = 2

        /**
         * 背景透明度
         */
        private const val DEFAULT_DIM_AMOUNT = 0.0f
    }
}