/*
 * Copyright (C) 2010 The Android Open Source Project
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
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.ActionProvider
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import androidx.annotation.LayoutRes

class ActionMenuItem(private val context: Context, private val groupId: Int, private val id: Int,
        private val categoryOrder: Int, private val order: Int, private var title: CharSequence) : MenuItem {

    private var titleCondensed: CharSequence = ""

    private var iconResId = NO_ICON
    private var iconDrawable: Drawable? = null

    private var mFlags = ENABLED

    private var shortcutAlphabeticChar = '0'
    private var shortcutNumericChar = '0'

    private var itemIntent: Intent? = null
    var componentName: ComponentName? = null

    private var clickListener: MenuItem.OnMenuItemClickListener? = null

    override fun getAlphabeticShortcut(): Char {
        return shortcutAlphabeticChar
    }

    override fun getGroupId(): Int {
        return groupId
    }

    override fun getIcon(): Drawable? {
        return iconDrawable
    }

    override fun getIntent(): Intent? {
        return itemIntent
    }

    override fun getItemId(): Int {
        return id
    }

    override fun getMenuInfo(): ContextMenuInfo? {
        return null
    }

    override fun getNumericShortcut(): Char {
        return shortcutNumericChar
    }

    override fun getOrder(): Int {
        return order
    }

    override fun getSubMenu(): SubMenu? {
        return null
    }

    override fun getTitle(): CharSequence {
        return title
    }

    override fun getTitleCondensed(): CharSequence {
        return if (titleCondensed.isNotEmpty()) titleCondensed else title
    }

    override fun hasSubMenu(): Boolean {
        return false
    }

    override fun isCheckable(): Boolean {
        return mFlags and CHECKABLE != 0
    }

    override fun isChecked(): Boolean {
        return mFlags and CHECKED != 0
    }

    override fun isEnabled(): Boolean {
        return mFlags and ENABLED != 0
    }

    override fun isVisible(): Boolean {
        return mFlags and HIDDEN == 0
    }

    override fun setAlphabeticShortcut(alphaChar: Char): MenuItem {
        shortcutAlphabeticChar = alphaChar
        return this
    }

    override fun setCheckable(checkable: Boolean): MenuItem {
        mFlags = mFlags and CHECKABLE.inv() or if (checkable) CHECKABLE else 0
        return this
    }

    fun setExclusiveCheckable(exclusive: Boolean): ActionMenuItem {
        mFlags = mFlags and EXCLUSIVE.inv() or if (exclusive) EXCLUSIVE else 0
        return this
    }

    override fun setChecked(checked: Boolean): MenuItem {
        mFlags = mFlags and CHECKED.inv() or if (checked) CHECKED else 0
        return this
    }

    override fun setEnabled(enabled: Boolean): MenuItem {
        mFlags = mFlags and ENABLED.inv() or if (enabled) ENABLED else 0
        return this
    }

    override fun setIcon(icon: Drawable): MenuItem {
        iconDrawable = icon
        iconResId = NO_ICON
        return this
    }

    @SuppressLint("ResourceType")
    override fun setIcon(iconRes: Int): MenuItem {
        iconResId = iconRes
        if (iconRes > 0) iconDrawable = context.resources?.getDrawable(iconRes)
        return this
    }

    override fun setIntent(intent: Intent?): MenuItem {
        itemIntent = intent
        return this
    }

    override fun setNumericShortcut(numericChar: Char): MenuItem {
        shortcutNumericChar = numericChar
        return this
    }

    override fun setOnMenuItemClickListener(menuItemClickListener: MenuItem.OnMenuItemClickListener): MenuItem {
        clickListener = menuItemClickListener
        return this
    }

    override fun setShortcut(numericChar: Char, alphaChar: Char): MenuItem {
        shortcutNumericChar = numericChar
        shortcutAlphabeticChar = alphaChar
        return this
    }

    override fun setTitle(titleStr: CharSequence): MenuItem {
        title = titleStr
        return this
    }

    override fun setTitle(titleResId: Int): MenuItem {
        title = context.resources?.getString(titleResId) ?: ""
        return this
    }

    override fun setTitleCondensed(title: CharSequence): MenuItem {
        titleCondensed = title
        return this
    }

    override fun setVisible(visible: Boolean): MenuItem {
        mFlags = mFlags and HIDDEN.inv() or if (visible) 0 else HIDDEN
        return this
    }

    operator fun invoke(): Boolean {
        val clickResult = clickListener?.onMenuItemClick(this) ?: false
        if (clickResult) {
            return true
        }
        if (itemIntent != null) {
            context.startActivity(itemIntent)
            return true
        }
        return false
    }

    override fun setShowAsAction(show: Int) { // Do nothing. ActionMenuItems always show as action buttons.
    }

    override fun getActionView(): View? {
        return null
    }

    override fun setActionProvider(actionProvider: ActionProvider): MenuItem {
        throw UnsupportedOperationException()
    }

    override fun getActionProvider(): ActionProvider {
        throw UnsupportedOperationException()
    }

    override fun setActionView(view: View): MenuItem {
        return this
    }

    override fun setActionView(@LayoutRes resId: Int): MenuItem {
        return this
    }

    override fun setOnActionExpandListener(listener: MenuItem.OnActionExpandListener?): MenuItem {
        throw UnsupportedOperationException()
    }

    override fun setShowAsActionFlags(actionEnum: Int): MenuItem {
        return this
    }

    override fun expandActionView(): Boolean {
        return false
    }

    override fun collapseActionView(): Boolean {
        return false
    }

    override fun isActionViewExpanded(): Boolean {
        return false
    }

    companion object {
        private const val NO_ICON = 0
        private const val CHECKABLE = 0x00000001
        private const val CHECKED = 0x00000002
        private const val EXCLUSIVE = 0x00000004
        private const val HIDDEN = 0x00000008
        private const val ENABLED = 0x00000010
    }
}