package com.xander.panel

import android.view.MenuItem

/**
 * Created by wangxiaoyang on 16/5/29.
 */

interface PanelMenuListener {
    fun onMenuClick(menuItem: MenuItem)
}

interface PanelDismissListener {
    fun onPanelDismiss(panel: XanderPanel)
}

interface PanelShowListener {
    fun onPanelShow(panel: XanderPanel)
}

interface SheetListener {
    fun onSheetItemClick(position: Int)
    fun onSheetCancelClick()
}

interface PanelControllerListener {
    fun onPanelNegativeClick(panel: XanderPanel)
    fun onPanelPositiveClick(panel: XanderPanel)
}