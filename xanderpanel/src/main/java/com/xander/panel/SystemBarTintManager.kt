/*
 * Copyright (C) 2013 readyState Software Ltd
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
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import java.lang.reflect.Method

/**
 * Class to manage status and navigation bar tint effects when using KitKat
 * translucent system UI modes.
 */
@SuppressLint("ResourceType")
class SystemBarTintManager @TargetApi(19) constructor(context: Context, win: Window) {

    private var mStatusBarAvailable = false
    private var mNavBarAvailable = false
    private var mStatusBarTintEnabled = false
    private var mNavBarTintEnabled = false
    private var mStatusBarTintView: View? = null
    private var mNavBarTintView: View? = null


    /**
     * Constructor. Call this in the host activity onCreate method after its
     * content view has been set. You should always create new instances when
     * the host activity is recreated.
     *
     * @param activity The host activity.
     */
    @TargetApi(19)
    constructor(activity: Activity) : this(activity, activity.window) {
    }

    init {
        win.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        win.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        val decorViewGroup = win.decorView as ViewGroup
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // check theme attrs
            val ints = intArrayOf(android.R.attr.windowTranslucentStatus, android.R.attr.windowTranslucentNavigation)
            val attrArrays = context.obtainStyledAttributes(ints)
            try {
                mStatusBarAvailable = attrArrays.getBoolean(0, false)
                mNavBarAvailable = attrArrays.getBoolean(1, false)
            } finally {
                attrArrays.recycle()
            }

            // check window flags
            val winParams = win.attributes
            var bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            if (winParams.flags and bits != 0) {
                mStatusBarAvailable = true
            }
            bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            if (winParams.flags and bits != 0) {
                mNavBarAvailable = true
            }
        }

        // device might not have virtual navigation keys
        mConfig = SystemBarConfig(context, mStatusBarAvailable, mNavBarAvailable)
        if (!mConfig.hasNavigtionBar()) {
            mNavBarAvailable = false
        }
        if (mStatusBarAvailable) {
            setupStatusBarView(context, decorViewGroup)
        }
        if (mNavBarAvailable) {
            setupNavBarView(context, decorViewGroup)
        }
    }

    /**
     * Enable tinting of the system status bar.
     *
     *
     * If the platform is running Jelly Bean or earlier, or translucent system
     * UI modes have not been enabled in either the theme or via window flags,
     * then this method does nothing.
     *
     * @param enabled True to enable tinting, false to disable it (default).
     */
    fun setStatusBarTintEnabled(enabled: Boolean) {
        mStatusBarTintEnabled = enabled
        if (mStatusBarAvailable) {
            mStatusBarTintView?.visibility = if (enabled) View.VISIBLE else View.GONE
        }
    }

    /**
     * Enable tinting of the system navigation bar.
     *
     *
     * If the platform does not have soft navigation keys, is running Jelly Bean
     * or earlier, or translucent system UI modes have not been enabled in either
     * the theme or via window flags, then this method does nothing.
     *
     * @param enabled True to enable tinting, false to disable it (default).
     */
    fun setNavigationBarTintEnabled(enabled: Boolean) {
        mNavBarTintEnabled = enabled
        if (mNavBarAvailable) {
            mNavBarTintView?.visibility = if (enabled) View.VISIBLE else View.GONE
        }
    }

    /**
     * Apply the specified color tint to all system UI bars.
     *
     * @param color The color of the background tint.
     */
    fun setTintColor(color: Int) {
        setStatusBarTintColor(color)
        setNavigationBarTintColor(color)
    }

    /**
     * Apply the specified drawable or color resource to all system UI bars.
     *
     * @param res The identifier of the resource.
     */
    fun setTintResource(res: Int) {
        setStatusBarTintResource(res)
        setNavigationBarTintResource(res)
    }

    /**
     * Apply the specified drawable to all system UI bars.
     *
     * @param drawable The drawable to use as the background, or null to remove it.
     */
    fun setTintDrawable(drawable: Drawable?) {
        setStatusBarTintDrawable(drawable)
        setNavigationBarTintDrawable(drawable)
    }

    /**
     * Apply the specified alpha to all system UI bars.
     *
     * @param alpha The alpha to use
     */
    fun setTintAlpha(alpha: Float) {
        setStatusBarAlpha(alpha)
        setNavigationBarAlpha(alpha)
    }

    /**
     * Apply the specified color tint to the system status bar.
     *
     * @param color The color of the background tint.
     */
    fun setStatusBarTintColor(color: Int) {
        if (mStatusBarAvailable) {
            mStatusBarTintView?.setBackgroundColor(color)
        }
    }

    /**
     * Apply the specified drawable or color resource to the system status bar.
     *
     * @param res The identifier of the resource.
     */
    fun setStatusBarTintResource(res: Int) {
        if (mStatusBarAvailable) {
            mStatusBarTintView?.setBackgroundResource(res)
        }
    }

    /**
     * Apply the specified drawable to the system status bar.
     *
     * @param drawable The drawable to use as the background, or null to remove it.
     */
    fun setStatusBarTintDrawable(drawable: Drawable?) {
        if (mStatusBarAvailable) {
            mStatusBarTintView?.setBackgroundDrawable(drawable)
        }
    }

    /**
     * Apply the specified alpha to the system status bar.
     *
     * @param alpha The alpha to use
     */
    @TargetApi(11)
    fun setStatusBarAlpha(alpha: Float) {
        if (mStatusBarAvailable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mStatusBarTintView?.alpha = alpha
        }
    }

    /**
     * Apply the specified color tint to the system navigation bar.
     *
     * @param color The color of the background tint.
     */
    fun setNavigationBarTintColor(color: Int) {
        if (mNavBarAvailable) {
            mNavBarTintView?.setBackgroundColor(color)
        }
    }

    /**
     * Apply the specified drawable or color resource to the system navigation bar.
     *
     * @param res The identifier of the resource.
     */
    fun setNavigationBarTintResource(res: Int) {
        if (mNavBarAvailable) {
            mNavBarTintView?.setBackgroundResource(res)
        }
    }

    /**
     * Apply the specified drawable to the system navigation bar.
     *
     * @param drawable The drawable to use as the background, or null to remove it.
     */
    fun setNavigationBarTintDrawable(drawable: Drawable?) {
        if (mNavBarAvailable) {
            mNavBarTintView?.setBackgroundDrawable(drawable)
        }
    }

    /**
     * Apply the specified alpha to the system navigation bar.
     *
     * @param alpha The alpha to use
     */
    @TargetApi(11)
    fun setNavigationBarAlpha(alpha: Float) {
        if (mNavBarAvailable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mNavBarTintView?.alpha = alpha
        }
    }

    /**
     * Get the system bar configuration.
     *
     * @return The system bar configuration for the current device configuration.
     */
    fun getConfig(): SystemBarConfig {
        return mConfig
    }

    /**
     * Is tinting enabled for the system status bar?
     *
     * @return True if enabled, False otherwise.
     */
    fun isStatusBarTintEnabled(): Boolean {
        return mStatusBarTintEnabled
    }

    /**
     * Is tinting enabled for the system navigation bar?
     *
     * @return True if enabled, False otherwise.
     */
    fun isNavBarTintEnabled(): Boolean {
        return mNavBarTintEnabled
    }

    private fun setupStatusBarView(context: Context, decorViewGroup: ViewGroup) {
        mStatusBarTintView = View(context)
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, mConfig.getStatusBarHeight())
        params.gravity = Gravity.TOP
        if (mNavBarAvailable && !mConfig.isNavigationAtBottom()) {
            params.rightMargin = mConfig.getNavigationBarWidth()
        }
        mStatusBarTintView?.let {
            it.layoutParams = params
            it.setBackgroundColor(DEFAULT_TINT_COLOR)
            it.visibility = View.GONE
        }
        decorViewGroup.addView(mStatusBarTintView)
    }

    private fun setupNavBarView(context: Context, decorViewGroup: ViewGroup) {
        mNavBarTintView = View(context)
        val params: FrameLayout.LayoutParams
        if (mConfig.isNavigationAtBottom()) {
            params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, mConfig.getNavigationBarHeight())
            params.gravity = Gravity.BOTTOM
        } else {
            params = FrameLayout.LayoutParams(mConfig.getNavigationBarWidth(), FrameLayout.LayoutParams.MATCH_PARENT)
            params.gravity = Gravity.RIGHT
        }
        mNavBarTintView?.let {
            it.layoutParams = params
            it.setBackgroundColor(DEFAULT_TINT_COLOR)
            it.visibility = View.GONE
        }
        decorViewGroup.addView(mNavBarTintView)
    }

    companion object {
        /**
         * The default system bar tint color value.
         */
        const val DEFAULT_TINT_COLOR = -0x1000000
        var sNavBarOverride: String = ""
        lateinit var mConfig: SystemBarConfig

        init {
            // Android allows a system property to override the presence of the navigation bar.
            // Used by the emulator.
            // See https://github.com/android/platform_frameworks_base/blob/master/policy/src/com/android/internal/policy/impl/PhoneWindowManager.java#L1076
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    val clazz = Class.forName("android.os.SystemProperties")
                    val getMethod: Method = clazz.getDeclaredMethod("get", String::class.java)
                    getMethod.isAccessible = true
                    sNavBarOverride = getMethod.invoke(null, "qemu.hw.mainkeys") as String
                } catch (e: Throwable) {
                    sNavBarOverride = ""
                }
            }
        }

        fun getStatusBarHeight(context: Context): Int {
            if (null == mConfig) {
                mConfig = SystemBarConfig(context, true, true)
            }
            return mConfig?.mStatusBarHeight ?: 0
        }

        fun getNavigationBarHeight(context: Context): Int {
            if (null == mConfig) {
                mConfig = SystemBarConfig(context, true, true)
            }
            return mConfig?.mNavigationBarHeight ?: 0
        }
    }
}

/**
 * Class which describes system bar sizing and other characteristics for the current
 * device configuration.
 */
class SystemBarConfig constructor(activity: Context, translucentStatusBar: Boolean, translucentNavBar: Boolean) {

    private var mTranslucentStatusBar: Boolean
    private var mTranslucentNavBar: Boolean
    var mStatusBarHeight: Int
    private var mActionBarHeight: Int
    private var mHasNavigationBar: Boolean
    var mNavigationBarHeight: Int
    private var mNavigationBarWidth: Int
    private var mInPortrait: Boolean
    private var mSmallestWidthDp: Float

    init {
        val res = activity.resources
        mInPortrait = res.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        mSmallestWidthDp = getSmallestWidthDp(activity)
        mStatusBarHeight = getInternalDimensionSize(res, STATUS_BAR_HEIGHT_RES_NAME)
        mActionBarHeight = getActionBarHeight(activity)
        mNavigationBarHeight = getNavigationBarHeight(activity)
        mNavigationBarWidth = getNavigationBarWidth(activity)
        mHasNavigationBar = mNavigationBarHeight > 0
        mTranslucentStatusBar = translucentStatusBar
        mTranslucentNavBar = translucentNavBar
    }

    @TargetApi(14)
    private fun getActionBarHeight(context: Context): Int {
        var result = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            val tv = TypedValue()
            context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)
            result = TypedValue.complexToDimensionPixelSize(tv.data, context.resources.displayMetrics)
        }
        return result
    }

    @TargetApi(14)
    private fun getNavigationBarHeight(context: Context): Int {
        val res = context.resources
        val result = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (hasNavBar(context)) {
                val key: String = if (mInPortrait) {
                    NAV_BAR_HEIGHT_RES_NAME
                } else {
                    NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME
                }
                return getInternalDimensionSize(res, key)
            }
        }
        return result
    }

    @TargetApi(14)
    private fun getNavigationBarWidth(context: Context): Int {
        val res = context.resources
        val result = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (hasNavBar(context)) {
                return getInternalDimensionSize(res, NAV_BAR_WIDTH_RES_NAME)
            }
        }
        return result
    }

    @TargetApi(14)
    private fun hasNavBar(context: Context): Boolean {
        val res = context.resources
        val resourceId = res.getIdentifier(SHOW_NAV_BAR_RES_NAME, "bool", "android")
        return if (resourceId != 0) {
            var hasNav = res.getBoolean(resourceId) // check override flag (see static block)
            if ("1" == SystemBarTintManager.sNavBarOverride) {
                hasNav = false
            } else if ("0" == SystemBarTintManager.sNavBarOverride) {
                hasNav = true
            }
            hasNav
        } else { // fallback
            !ViewConfiguration.get(context).hasPermanentMenuKey()
        }
    }

    /**
     * 通过反射获取值
     */
    private fun getInternalDimensionSize(res: Resources, key: String): Int {
        var result = 0
        val resourceId = res.getIdentifier(key, "dimen", "android")
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId)
        }
        return result
    }

    private fun getSmallestWidthDp(context: Context): Float {
        var metrics: DisplayMetrics? = DisplayMetrics()
        metrics = context.getResources().displayMetrics
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // context.getDefaultDisplay().getRealMetrics(metrics);
        } else {
            // TODO this is not correct, but we don't really care pre-kitkat
            //  context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        }
        val widthDp = metrics.widthPixels / metrics.density
        val heightDp = metrics.heightPixels / metrics.density
        return widthDp.coerceAtMost(heightDp)
    }

    /**
     * Should a navigation bar appear at the bottom of the screen in the current
     * device configuration? A navigation bar may appear on the right side of
     * the screen in certain configurations.
     *
     * @return True if navigation should appear at the bottom of the screen, False otherwise.
     */
    fun isNavigationAtBottom(): Boolean {
        return mSmallestWidthDp >= 600 || mInPortrait
    }

    /**
     * Get the height of the system status bar.
     *
     * @return The height of the status bar (in pixels).
     */
    fun getStatusBarHeight(): Int {
        return mStatusBarHeight
    }

    /**
     * Get the height of the action bar.
     *
     * @return The height of the action bar (in pixels).
     */
    fun getActionBarHeight(): Int {
        return mActionBarHeight
    }

    /**
     * Does this device have a system navigation bar?
     *
     * @return True if this device uses soft key navigation, False otherwise.
     */
    fun hasNavigtionBar(): Boolean {
        return mHasNavigationBar
    }

    /**
     * Get the height of the system navigation bar.
     *
     * @return The height of the navigation bar (in pixels). If the device does not have
     * soft navigation keys, this will always return 0.
     */
    fun getNavigationBarHeight(): Int {
        return mNavigationBarHeight
    }

    /**
     * Get the width of the system navigation bar when it is placed vertically on the screen.
     *
     * @return The width of the navigation bar (in pixels). If the device does not have
     * soft navigation keys, this will always return 0.
     */
    fun getNavigationBarWidth(): Int {
        return mNavigationBarWidth
    }

    /**
     * Get the layout inset for any system UI that appears at the top of the screen.
     *
     * @param withActionBar True to include the height of the action bar, False otherwise.
     * @return The layout inset (in pixels).
     */
    fun getPixelInsetTop(withActionBar: Boolean): Int {
        return (if (mTranslucentStatusBar) mStatusBarHeight else 0) + if (withActionBar) mActionBarHeight else 0
    }

    /**
     * Get the layout inset for any system UI that appears at the bottom of the screen.
     *
     * @return The layout inset (in pixels).
     */
    fun getPixelInsetBottom(): Int {
        return if (mTranslucentNavBar && isNavigationAtBottom()) {
            mNavigationBarHeight
        } else {
            0
        }
    }

    /**
     * Get the layout inset for any system UI that appears at the right of the screen.
     *
     * @return The layout inset (in pixels).
     */
    fun getPixelInsetRight(): Int {
        return if (mTranslucentNavBar && !isNavigationAtBottom()) {
            mNavigationBarWidth
        } else {
            0
        }
    }

    companion object {
        private const val STATUS_BAR_HEIGHT_RES_NAME: String = "status_bar_height"
        private const val NAV_BAR_HEIGHT_RES_NAME: String = "navigation_bar_height"
        private const val NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME: String = "navigation_bar_height_landscape"
        private const val NAV_BAR_WIDTH_RES_NAME: String = "navigation_bar_width"
        private const val SHOW_NAV_BAR_RES_NAME: String = "config_showNavigationBar"
    }

}