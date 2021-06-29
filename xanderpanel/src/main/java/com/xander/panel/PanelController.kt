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
package com.xander.panel

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import androidx.core.view.isNotEmpty
import androidx.viewpager.widget.ViewPager
import com.viewpagerindicator.CirclePageIndicator
import java.util.*

class PanelController(private val mContext: Context, private val mXanderPanel: XanderPanel) : View.OnClickListener,
    MenuItem.OnMenuItemClickListener {

    /**
     * 整个布局容器
     */
    private lateinit var rootLayout: FrameLayout

    /**
     * 整个容器背景 处理点击外部消失和背景渐变效果
     */
    private lateinit var rootLayoutBg: View

    /**
     * 背景颜色
     */
    private var backgroundColor = -0x70000000

    /**
     * 内容面板 root 布局
     */
    private lateinit var rootPanel: LinearLayout

    /**
     * 标题面板
     */
    private lateinit var titlePanel: LinearLayout

    /**
     * 标题面板下的模板，不指定自定义的 title view 的时候显示这个
     */
    private lateinit var titleTemplate: LinearLayout

    /**
     * 标题 icon
     */
    private lateinit var titleTemplateIcon: ImageView

    /**
     * 标题 icon 内容
     */
    private var titleIconDrawable: Drawable? = null

    /**
     * 默认 icon 资源
     */
    private var titleIconResId = -1

    /**
     * 标题 TextView
     */
    private lateinit var titleTemplateText: TextView

    /**
     * 标题 text 内容
     */
    private var titleTextStr: CharSequence = ""

    /**
     * 用户自定义顶部内容
     */
    private var mCustomTitleView: View? = null

    /**
     * 内容面板
     */
    private lateinit var contentPanel: LinearLayout

    /**
     * 内容 ScrollView 容器
     */
    private lateinit var contentScrollView: ScrollView

    /**
     * 内容 text
     */
    private lateinit var contentMessageView: TextView

    /**
     * 内容 text 内容
     */
    private var contentMessageStr: CharSequence = ""

    /**
     * 表格显示的时候每页显示的行数
     */
    var pageGridRow = 2

    /**
     * 表格显示的时候每页显示的列数
     */
    var pageGridCol = 3

    /**
     * 用户自定义的View Panel
     */
    private lateinit var customPanel: FrameLayout

    /**
     * 用户自定义的 View
     */
    private var customView: View? = null

    /**
     * 底部的按钮
     */
    private lateinit var controllerPanel: LinearLayout

    /**
     * 取消按钮
     */
    private lateinit var controllerNegativeButton: Button
    var negativeStr: CharSequence = ""

    /**
     * 确认按钮
     */
    private lateinit var controllerPositiveButton: Button
    var positiveStr: CharSequence = ""

    var mControllerListener: PanelControllerListener? = null

    private var mViewSpacingSpecified = false
    private var mViewSpacingLeft = 0
    private var mViewSpacingTop = 0
    private var mViewSpacingRight = 0
    private var mViewSpacingBottom = 0

    private val rootLayoutId: Int = R.layout.xander_panel

    var canceledTouchOutside = true
    var showSheetCancel = true
    var sheetCancelStr: CharSequence = ""

    var showSheet = false
    lateinit var sheetItems: Array<String>
    var sheetListener: SheetListener? = null

    var actionMenu: ActionMenu? = null
    var menuListener: PanelMenuListener? = null

    var showMenuAsGrid = false
    var shareMode = false

    var shareStr = ""
    var shareImages: Array<String> = arrayOf()

    var panelItemClickListener = PanelItemClickListener()

    var gravity = Gravity.BOTTOM

    private var panelMargin = 160

    init {
        val typedArray = mContext.obtainStyledAttributes(COLORR_ATTRS)
        backgroundColor = typedArray.getColor(0, -0x70000000)
        typedArray.recycle()
    }

    fun getParentView(): View {
        return rootLayout
    }

    fun setPanelMargin(margin: Int) {
        panelMargin = margin
    }

    fun setTitle(title: String) {
        titleTextStr = title
    }

    fun setCustomTitle(customTitleView: View?) {
        mCustomTitleView = customTitleView
    }

    fun setMessage(message: String) {
        contentMessageStr = message
    }

    /**
     * Set the view to display in the dialog.
     */
    fun setCustomView(view: View?) {
        customView = view
        mViewSpacingSpecified = false
    }

    /**
     * Set the view to display in the dialog along with the spacing around that view
     */
    fun setCustomView(view: View?, viewSpacingLeft: Int, viewSpacingTop: Int, viewSpacingRight: Int,
            viewSpacingBottom: Int) {
        customView = view
        mViewSpacingSpecified = true
        mViewSpacingLeft = viewSpacingLeft
        mViewSpacingTop = viewSpacingTop
        mViewSpacingRight = viewSpacingRight
        mViewSpacingBottom = viewSpacingBottom
    }

    /**
     * Set resId to 0 if you don't want an icon.
     *
     * @param resId the resourceId of the drawable to use as the icon or 0
     * if you don't want an icon.
     */
    fun setIcon(resId: Int) {
        titleIconResId = resId
        if (resId > 0) {
            titleTemplateIcon.setImageResource(titleIconResId)
            titleTemplateIcon.visibility = View.VISIBLE
        } else if (resId == 0) {
            titleTemplateIcon.visibility = View.GONE
        }
    }

    fun setIcon(icon: Drawable?) {
        titleIconDrawable = icon
        titleIconDrawable?.let {
            titleTemplateIcon.setImageDrawable(it)
            titleTemplateIcon.visibility = View.VISIBLE
        } ?: apply {
            titleTemplateIcon.visibility = View.GONE
        }
    }

    /**
     * @param attrId the attributeId of the theme-specific drawable
     * to resolve the resourceId for.
     * @return resId the resourceId of the theme-specific drawable
     */
    fun getIconAttributeResId(attrId: Int): Int {
        val out = TypedValue()
        mContext.theme.resolveAttribute(attrId, out, true)
        return out.resourceId
    }

    fun applyView() {
        ensureInflaterLayout()
        applyRootPanel()
        when {
            showSheet -> {
                applySheet()
            }
            actionMenu?.isNotEmpty() == true -> {
                applyMenu()
            }
            else -> {
                applyTitlePanel()
                applyContentPanel()
                applyCustomPanel()
                applyControllerPanel()
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.controller_nagetive -> {
                mControllerListener?.let {
                    mXanderPanel.dismiss()
                    it.onPanelNegativeClick(mXanderPanel)
                }
            }
            R.id.controller_positive -> {
                mControllerListener?.let {
                    mXanderPanel.dismiss()
                    it.onPanelPositiveClick(mXanderPanel)
                }
            }
            R.id.root_background -> {
                if (canceledTouchOutside) {
                    mXanderPanel.dismiss()
                }
            }
            R.id.xander_panel_sheet_cancel -> {
                sheetListener?.onSheetCancelClick()
                mXanderPanel.dismiss()
            }
        }
    }

    private fun ensureInflaterLayout() {
        if (null != rootLayout) {
            return
        }
        val inflater = LayoutInflater.from(mContext)
        rootLayout = inflater.inflate(rootLayoutId, null) as FrameLayout
        rootLayoutBg = rootLayout.findViewById(R.id.root_background)
        rootLayoutBg.setOnClickListener(this)
        rootLayoutBg.setBackgroundColor(backgroundColor)

        rootPanel = rootLayout.findViewById<View>(R.id.panel_root) as LinearLayout

        titlePanel = rootPanel.findViewById<View>(R.id.title_panel) as LinearLayout
        titleTemplate = titlePanel.findViewById<View>(R.id.title_template) as LinearLayout
        titleTemplateIcon = titleTemplate.findViewById<View>(R.id.title_icon) as ImageView
        titleTemplateText = titleTemplate.findViewById<View>(R.id.title_text) as TextView

        contentPanel = rootPanel.findViewById<View>(R.id.content_panel) as LinearLayout
        contentScrollView = rootPanel.findViewById<View>(R.id.msg_scrollview) as ScrollView
        contentMessageView = contentScrollView.findViewById<View>(R.id.msg_text) as TextView

        customPanel = rootLayout.findViewById<View>(R.id.custom_panel) as FrameLayout

        controllerPanel = rootLayout.findViewById<View>(R.id.controller_pannle) as LinearLayout
        controllerNegativeButton = controllerPanel.findViewById<View>(R.id.controller_nagetive) as Button
        controllerNegativeButton.setOnClickListener(this)
        controllerPositiveButton = controllerPanel.findViewById<View>(R.id.controller_positive) as Button
        controllerPositiveButton.setOnClickListener(this)
    }

    private fun applyRootPanel() {
        val layoutParams = rootPanel.layoutParams as FrameLayout.LayoutParams
        var sheetMargin = 0
        layoutParams.gravity = gravity
        if (showSheet) {
            gravity = Gravity.BOTTOM
            layoutParams.gravity = gravity
            sheetMargin = mContext.resources.getDimension(R.dimen.panel_sheet_margin).toInt()
        }
        layoutParams.leftMargin = sheetMargin
        layoutParams.topMargin = sheetMargin
        layoutParams.rightMargin = sheetMargin
        layoutParams.bottomMargin = sheetMargin
        if (Gravity.TOP == gravity) {
            layoutParams.bottomMargin = panelMargin
        } else if (Gravity.BOTTOM == gravity) {
            layoutParams.topMargin = panelMargin
        }
        rootPanel.layoutParams = layoutParams
        rootPanel.setOnClickListener(null)
        val paddingTop = if (Build.VERSION.SDK_INT > 19) SystemBarTintManager.getStatusBarHeight(mContext) else 0
        val paddingBottom = if (Build.VERSION.SDK_INT > 19) SystemBarTintManager.getNavigationBarHeight(mContext) else 0
        if (Gravity.TOP == gravity) {
            rootPanel.setPadding(0, paddingTop, 0, 0)
        } else if (Gravity.BOTTOM == gravity) {
            rootPanel.setPadding(0, 0, 0, paddingBottom)
        }
        if (!showSheet) {
            rootPanel.setBackgroundResource(R.color.panel_root_layout_bg)
        }
    }

    private fun applyTitlePanel(): Boolean {
        var hasTitle = true
        mCustomTitleView?.let {
            titleTemplate.visibility = View.GONE // Add the custom title view directly to the titlePanel layout
            titlePanel.visibility = View.VISIBLE // hide title temple
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            titlePanel.addView(mCustomTitleView, 1, lp)
        } ?: apply {
            if (titleTextStr.isNotEmpty()) {
                titlePanel.visibility = View.VISIBLE
                /* Display the title if a title is supplied, else hide it */
                titleTemplate.visibility = View.VISIBLE
                titleTemplateText.text = titleTextStr
                /* Do this last so that if the user has supplied any
                 * icons we use them instead of the default ones. If the
                 * user has specified 0 then make it disappear.
                 */
                when {
                    titleIconResId > 0 -> {
                        titleTemplateIcon.setImageResource(titleIconResId)
                    }
                    titleIconDrawable != null -> {
                        titleTemplateIcon.setImageDrawable(titleIconDrawable)
                    }
                    titleIconResId == 0 -> {
                        /* Apply the padding from the icon to ensure the
                         * title is aligned correctly.
                         */
                        titleTemplateText.setPadding(titleTemplateIcon.paddingLeft, titleTemplateIcon.paddingTop,
                                titleTemplateIcon.paddingRight, titleTemplateIcon.paddingBottom)
                        titleTemplateIcon.visibility = View.GONE
                    }
                }
            } else {
                // Hide the title template
                titlePanel.visibility = View.GONE
                hasTitle = false
            }
        }
        return hasTitle
    }

    private fun applyContentPanel() {
        if (contentMessageStr.isNotEmpty()) {
            contentPanel.visibility = View.VISIBLE
            contentScrollView.isFocusable = false
            contentMessageView.text = contentMessageStr
        } else {
            contentPanel.visibility = View.GONE
        }
    }

    private fun applyCustomPanel() {
        customView?.let {
            titlePanel.visibility = View.GONE
            contentPanel.visibility = View.GONE
            // 清空内容
            customPanel.visibility = View.VISIBLE
            customPanel.removeAllViews()
            customPanel.addView(customView,
                    FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            if (mViewSpacingSpecified) {
                customPanel.setPadding(mViewSpacingLeft, mViewSpacingTop, mViewSpacingRight, mViewSpacingBottom)
            }
        } ?: apply {
            customPanel.visibility = View.GONE
        }
    }

    private fun applyControllerPanel() {
        controllerNegativeButton.let {
            it.text = negativeStr
            it.visibility = if (negativeStr.isNotEmpty()) View.VISIBLE else View.GONE
        }

        controllerPositiveButton.let {
            it.text = negativeStr
            it.visibility = if (positiveStr.isNotEmpty()) View.VISIBLE else View.GONE
        }

        controllerPanel.let {
            it.visibility = if (positiveStr.isEmpty() && positiveStr.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun applySheet() {
        rootPanel.removeAllViews()
        val inflater = LayoutInflater.from(mContext)
        val sheetView = inflater.inflate(R.layout.xander_panel_sheet, rootLayout, false)
        val sheetCancel = sheetView.findViewById<View>(R.id.xander_panel_sheet_cancel) as TextView
        sheetCancel.visibility = if (showSheetCancel) View.VISIBLE else View.GONE
        sheetCancel.text = sheetCancelStr
        sheetCancel.setOnClickListener(this)
        val sheetList = sheetView.findViewById<View>(R.id.xander_panel_sheet_list) as ListView
        val sheetAdapter = SheetAdapter(mContext, sheetItems)
        sheetList.adapter = sheetAdapter
        sheetList.onItemClickListener = panelItemClickListener
        // sheetList.setOnItemLongClickListener(panelItemClickListenr);
        rootPanel.addView(sheetView)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        var result = false
        menuListener?.let {
            it.onMenuClick(item)
            mXanderPanel.dismiss()
            result = true
        } ?: apply {
            if (shareMode && item is ActionMenuItem) {
                ShareTools.share(mContext, shareStr, shareImages, item.componentName)
                mXanderPanel.dismiss()
                result = true
            }
        }
        return result
    }

    private fun applyMenu() {
        rootPanel.removeAllViews()
        val count = actionMenu?.size() ?: 0
        if (count == 0) {
            return
        }
        for (i in count - 1 downTo 0) {
            actionMenu?.getItem(i)?.setOnMenuItemClickListener(this)
        }
        val inflater = LayoutInflater.from(mContext)
        if (showMenuAsGrid) {
            val view = inflater.inflate(R.layout.xander_panel_menu_gridviewpager, rootPanel, false)
            val viewPager = view.findViewById<View>(R.id.xander_panel_gridviewpager) as ViewPager
            var row = pageGridRow
            var col = pageGridCol
            if (count < col) {
                row = 1
                col = count
            }
            val pagerAdapter = GridViewPagerAdapter(mContext, row, col)
            val params = viewPager.layoutParams as LinearLayout.LayoutParams
            val screenWidth = mContext.resources.displayMetrics.widthPixels
            params.height = screenWidth / 3.coerceAtLeast(col) * row
            Log.d("wxy", "params ${params.width},${params.height}")
            viewPager.layoutParams = params
            pagerAdapter.setActionMenus(actionMenu!!, viewPager)
            viewPager.adapter = pagerAdapter
            val indicator = view.findViewById<View>(R.id.xander_panel_indicator) as CirclePageIndicator
            indicator.setViewPager(viewPager)
            rootPanel.addView(view)
        } else {
            val menuList = inflater.inflate(R.layout.xander_panel_menu_list, rootPanel, false) as ListView
            val menuAdapter = MenuAdapter(mContext, actionMenu!!)
            menuList.adapter = menuAdapter
            menuList.onItemClickListener = panelItemClickListener
            rootPanel.addView(menuList)
        }
    }

    fun animateShow() {
        doAnim(ANIM_TYPE_SHOW)
    }

    fun animateDismiss() {
        doAnim(ANIM_TYPE_DISMISS)
    }

    private fun doAnim(type: Int) {
        rootLayoutBg.startAnimation(createBgAnimation(type))
        rootPanel.startAnimation(createPanelAnimation(type))
    }

    private fun createPanelAnimation(animType: Int): Animation? {
        val type = TranslateAnimation.RELATIVE_TO_SELF
        var animation: TranslateAnimation? = null
        if (ANIM_TYPE_SHOW == animType) {
            if (Gravity.TOP == gravity) {
                animation = TranslateAnimation(type, 0F, type, 0F, type, -1F, type, 0F)
            } else if (Gravity.BOTTOM == gravity) {
                animation = TranslateAnimation(type, 0F, type, 0F, type, 1F, type, 0F)
            }
        } else {
            if (Gravity.TOP == gravity) {
                animation = TranslateAnimation(type, 0F, type, 0F, type, 0F, type, -1F)
            } else if (Gravity.BOTTOM == gravity) {
                animation = TranslateAnimation(type, 0F, type, 0F, type, 0F, type, 1F)
            }
        }
        animation?.duration = DURATION_TRANSLATE.toLong()
        animation?.fillAfter = true
        return animation
    }

    private fun createBgAnimation(animType: Int): Animation {
        var alphaAnimation: AlphaAnimation = if (ANIM_TYPE_SHOW == animType) {
            AlphaAnimation(0F, 1F)
        } else {
            AlphaAnimation(1F, 0F)
        }
        alphaAnimation.duration = DURATION_ALPHA.toLong()
        alphaAnimation.fillAfter = true
        return alphaAnimation
    }

    companion object {
        private val BACKGROUND_COLOR = R.attr.xPanel_BackgroudColor

        private val COLORR_ATTRS: IntArray = intArrayOf(BACKGROUND_COLOR)

        const val DURATION = 300L
        const val DURATION_TRANSLATE = 200
        const val DURATION_ALPHA = DURATION
        const val ANIM_TYPE_SHOW = 0
        const val ANIM_TYPE_DISMISS = 1

        /**
         * 检测 view 是否可以输入
         *
         * @param view
         * @return
         */
        fun canTextInput(view: View): Boolean {
            if (view.onCheckIsTextEditor()) {
                return true
            }
            if (view !is ViewGroup) {
                return false
            }
            var childCount = view.childCount
            while (childCount > 0) {
                var childView = view.getChildAt(childCount)
                if (canTextInput(childView)) {
                    return true
                }
                childCount--
            }
            return false
        }
    }
}


class PanelParams(var context: Context) {

    var panelMargin = 200

    var icon: Drawable? = null
    var iconId = 0
    var iconAttrId = 0

    var title = ""
    var customTitleView: View? = null
    var message = ""

    var customView: View? = null
    var viewSpacingSpecified = false
    var viewSpacingLeft = 0
    var viewSpacingTop = 0
    var viewSpacingRight = 0
    var viewSpacingBottom = 0

    var cancelable = true
    var canceledOnTouchOutside = true

    var showSheetCancel = true
    var sheetCancelStr: CharSequence = ""

    var showSheet = false

    var sheetItems: Array<String> = arrayOf()
    var sheetListener: SheetListener? = null

    var share = false
    var shareText = ""
    var shareImages: Array<String> = arrayOf()
    var filterPackages: Array<String> = arrayOf()

    var nagetive: CharSequence = ""
    var positive: CharSequence = ""
    var controllerListener: PanelControllerListener? = null

    var actionMenu: ActionMenu? = null
    var menuListener: PanelMenuListener? = null

    /**
     * 表格显示的时候每页显示的行数
     */
    var pagerGridRow = 2

    /**
     * 表格显示的时候每页显示的列数
     */
    var pagerGridCol = 3
    var showMenuAsGrid = false

    var showListener: PanelShowListener? = null
    var dismissListener: PanelDismissListener? = null

    var mGravity = Gravity.BOTTOM

    fun apply(panelController: PanelController) {
        // title
        customTitleView?.let {
            panelController.setCustomTitle(it)
        } ?: apply {
            if (title.isNotEmpty()) {
                panelController.setTitle(title)
            }
            when {
                iconAttrId > 0 -> {
                    panelController.setIcon(panelController.getIconAttributeResId(iconAttrId))
                }
                iconId >= 0 -> {
                    panelController.setIcon(iconId)
                }
                else -> {
                    icon?.apply { panelController.setIcon(this) }
                }
            }
        }

        // msg
        if (message.isNotEmpty()) {
            panelController.setMessage(message)
        }

        // custom view
        customView?.let {
            when {
                viewSpacingSpecified -> {
                    panelController.setCustomView(customView, viewSpacingLeft, viewSpacingTop, viewSpacingRight,
                            viewSpacingBottom)
                }
                else -> {
                    panelController.setCustomView(customView)
                }
            }
        }

        if (share) {
            actionMenu = ShareTools.createShareActionMenu(context, shareText, shareImages, filterPackages)
        }

        // set menu
        actionMenu?.let {
            panelController.shareMode = share
            panelController.shareStr = shareText
            panelController.shareImages = shareImages
            panelController.showMenuAsGrid = showMenuAsGrid
            panelController.pageGridRow = pagerGridRow
            panelController.pageGridCol = pagerGridCol
            panelController.menuListener = menuListener
            panelController.actionMenu = it.clone(it.size())
            panelController.actionMenu!!.removeInvisible()
        }

        // set sheet
        panelController.showSheet = showSheet
        panelController.showSheetCancel = showSheetCancel
        panelController.sheetCancelStr = sheetCancelStr
        panelController.sheetItems = sheetItems
        panelController.sheetListener = sheetListener

        // set controller
        panelController.positiveStr = positive
        panelController.negativeStr = nagetive
        panelController.mControllerListener = controllerListener

        // other settings
        panelController.gravity = mGravity
        panelController.canceledTouchOutside = canceledOnTouchOutside
        panelController.setPanelMargin(panelMargin)

        panelController.applyView()
    }
}

class MenuAdapter(context: Context, private val menu: ActionMenu) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)


    override fun getCount(): Int {
        return menu.size()
    }

    override fun getItem(position: Int): Any? {
        return menu.getItem(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var newConvertView = convertView
        val menuHolder: MenuHolderA
        if (null == convertView) {
            newConvertView = inflater.inflate(R.layout.xander_panel_menu_list_item, parent, false)
            menuHolder = MenuHolderA(newConvertView)
            newConvertView.tag = menuHolder
        } else {
            menuHolder = convertView.tag as MenuHolderA
        }
        menuHolder.bindMenuItem(menu.getItem(position))
        return newConvertView
    }

}

class MenuHolderA(parent: View) {

    private lateinit var menuIcon: ImageView
    private lateinit var menuTitle: TextView

    init {
        bindView(parent)
    }

    private fun bindView(parent: View) {
        menuIcon = parent.findViewById<View?>(R.id.panel_menu_icon) as ImageView
        menuTitle = parent.findViewById<View?>(R.id.panel_menu_title) as TextView
    }

    fun bindMenuItem(menuItem: MenuItem) {
        if (null == menuItem.icon) {
            menuIcon.visibility = View.GONE
        } else {
            menuIcon.visibility = View.VISIBLE
            menuIcon.setImageDrawable(menuItem.icon)
        }
        menuTitle.text = menuItem.title
    }
}

class SheetAdapter(contexts: Context, sheetItems: Array<String>) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(contexts)

    private val mSheetItems: MutableList<String> = ArrayList()

    init {
        mSheetItems.addAll(sheetItems)
    }

    override fun getCount(): Int {
        return mSheetItems.size
    }

    override fun getItem(position: Int): Any? {
        return mSheetItems[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var newConvertView = convertView
        if (null == convertView) {
            newConvertView = inflater.inflate(R.layout.xander_panel_sheet_item, parent, false)
        }
        val textView = newConvertView as TextView
        when {
            count == 1 -> {
                textView.setBackgroundResource(R.drawable.sheet_item_just_one)
            }
            position == 0 -> {
                textView.setBackgroundResource(R.drawable.sheet_item_top)
            }
            position == count - 1 -> {
                textView.setBackgroundResource(R.drawable.sheet_item_bottom)
            }
            else -> {
                textView.setBackgroundResource(R.drawable.sheet_item_normal)
            }
        }
        textView.text = mSheetItems[position]
        return newConvertView
    }

}

class PanelItemClickListener : OnItemClickListener, OnItemLongClickListener {

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        // if (showSheet && mSheetListener != null) {
        //     mSheetListener.onSheetItemClick(position)
        // } else if (null != actionMenu && null != menuListener) {
        //     menuListener.onMenuClick(actionMenu.getItem(position))
        // } else if (mShare) {
        //     val menuItem = actionMenu.getItem(position)
        //     if (menuItem is ActionMenuItem) {
        //         (menuItem as ActionMenuItem).invoke()
        //     }
        //     return
        // }
        // mXanderPanel.dismiss()
    }

    override fun onItemLongClick(parent: AdapterView<*>, view: View, position: Int, id: Long): Boolean {
        // if (showSheet && mSheetListener != null) {
        //     mSheetListener.onSheetItemClick(position)
        // } else if (null != actionMenu && null != menuListener) {
        //     menuListener.onMenuClick(actionMenu.getItem(position))
        // } else if (mShare) {
        //     val menuItem = actionMenu.getItem(position)
        //     if (menuItem is ActionMenuItem) {
        //         (menuItem as ActionMenuItem).invoke()
        //     }
        //     return true
        // }
        // mXanderPanel.dismiss()
        return true
    }
}