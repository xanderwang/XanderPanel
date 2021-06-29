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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import java.util.*

class ActionMenu(private val context: Context) : Menu {

    private var isQwerty = false

    private var items: ArrayList<ActionMenuItem> = ArrayList()

    private fun getContext(): Context {
        return context
    }

    override fun add(titleStr: CharSequence): MenuItem {
        return add(0, 0, 0, titleStr)
    }

    override fun add(titleRes: Int): MenuItem {
        return add(0, 0, 0, titleRes)
    }

    override fun add(groupId: Int, itemId: Int, order: Int, titleRes: Int): MenuItem {
        return add(groupId, itemId, order, context.resources.getString(titleRes))
    }

    override fun add(groupId: Int, itemId: Int, order: Int, titleStr: CharSequence): MenuItem {
        val item = ActionMenuItem(getContext(), groupId, itemId, 0, order, titleStr)
        items.add(findInsertIndex(items, getOrdering(order)), item)
        return item
    }

    fun add(item: ActionMenuItem): MenuItem {
        items.add(findInsertIndex(items, getOrdering(item.order)), item)
        return item
    }

    override fun addIntentOptions(groupId: Int, itemId: Int, order: Int, caller: ComponentName,
            specifics: Array<Intent>, intent: Intent, flags: Int, outSpecificItems: Array<MenuItem>): Int {
        val pm = context.packageManager
        val resolveInfoList = pm.queryIntentActivityOptions(caller, specifics, intent, 0)
        val size = resolveInfoList.size
        if (flags and Menu.FLAG_APPEND_TO_GROUP == 0) {
            removeGroup(groupId)
        }
        for (index in 0 until size) {
            val resolveInfo = resolveInfoList[index]
            val resolveIntent = Intent(
                    if (resolveInfo.specificIndex < 0) intent else specifics[resolveInfo.specificIndex])
            resolveIntent.component = ComponentName(resolveInfo.activityInfo.applicationInfo.packageName,
                    resolveInfo.activityInfo.name)
            val item = add(groupId, itemId, order, resolveInfo.loadLabel(pm))
            item.setIcon(resolveInfo.loadIcon(pm))
            item.intent = resolveIntent
            if (resolveInfo.specificIndex >= 0) {
                outSpecificItems[resolveInfo.specificIndex] = item
            }
        }
        return size
    }

    override fun addSubMenu(title: CharSequence?): SubMenu? {
        // TODO Implement submenus
        return null
    }

    override fun addSubMenu(titleRes: Int): SubMenu? {
        // TODO Implement submenus
        return null
    }

    override fun addSubMenu(groupId: Int, itemId: Int, order: Int, title: CharSequence?): SubMenu? {
        // TODO Implement submenus
        return null
    }

    override fun addSubMenu(groupId: Int, itemId: Int, order: Int, titleRes: Int): SubMenu? {
        // TODO Implement submenus
        return null
    }

    override fun clear() {
        items.clear()
    }

    override fun close() {}

    private fun findItemIndex(id: Int): Int {
        val items = items
        val itemCount = items.size
        for (index in 0 until itemCount) {
            if (items[index].itemId == id) {
                return index
            }
        }
        return -1
    }

    override fun findItem(id: Int): MenuItem? {
        val index = findItemIndex(id)
        return if (index < 0) {
            null
        } else items[index]
    }

    override fun getItem(index: Int): MenuItem {
        return items[index]
    }

    override fun hasVisibleItems(): Boolean {
        val items = items
        val itemCount = items.size
        for (i in 0 until itemCount) {
            if (items[i].isVisible) {
                return true
            }
        }
        return false
    }

    private fun findItemWithShortcut(keyCode: Int, event: KeyEvent?): ActionMenuItem? {
        // TODO Make this smarter.
        val qwerty = isQwerty
        val items = items
        val itemCount = items.size
        for (i in 0 until itemCount) {
            val item = items[i]
            val shortcut = if (qwerty) item.alphabeticShortcut else item.numericShortcut
            if (keyCode == shortcut.code) {
                return item
            }
        }
        return null
    }

    override fun isShortcutKey(keyCode: Int, event: KeyEvent?): Boolean {
        return findItemWithShortcut(keyCode, event) != null
    }

    override fun performIdentifierAction(id: Int, flags: Int): Boolean {
        val index = findItemIndex(id)
        return if (index < 0) {
            false
        } else items[index].invoke()
    }

    override fun performShortcut(keyCode: Int, event: KeyEvent?, flags: Int): Boolean {
        val item = findItemWithShortcut(keyCode, event) ?: return false
        return item.invoke()
    }

    override fun removeGroup(groupId: Int) {
        val items = items
        var itemCount = items.size
        var i = 0
        while (i < itemCount) {
            if (items.get(i).groupId == groupId) {
                items.removeAt(i)
                itemCount--
            } else {
                i++
            }
        }
    }

    override fun removeItem(id: Int) {
        val index = findItemIndex(id)
        if (index < 0) {
            return
        }
        items.removeAt(index)
    }

    override fun setGroupCheckable(group: Int, checkable: Boolean, exclusive: Boolean) {
        val items = items
        val itemCount = items.size
        for (i in 0 until itemCount) {
            val item = items[i]
            if (item.groupId == group) {
                item.isCheckable = checkable
                item.setExclusiveCheckable(exclusive)
            }
        }
    }

    override fun setGroupEnabled(group: Int, enabled: Boolean) {
        val items = items
        val itemCount = items.size
        for (i in 0 until itemCount) {
            val item = items[i]
            if (item.groupId == group) {
                item.isEnabled = enabled
            }
        }
    }

    override fun setGroupVisible(group: Int, visible: Boolean) {
        val items = items
        val itemCount = items.size
        for (i in 0 until itemCount) {
            val item = items[i]
            if (item.groupId == group) {
                item.isVisible = visible
            }
        }
    }

    override fun setQwertyMode(isQwerty: Boolean) {
        this.isQwerty = isQwerty
    }

    override fun size(): Int {
        return items.size
    }

    fun clone(size: Int): ActionMenu {
        val out = ActionMenu(getContext())
        out.items = ArrayList(items.subList(0, size))
        return out
    }

    fun removeInvisible() {
        val iterator = items.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (!item.isVisible) iterator.remove()
        }
    }

    companion object {
        /**
         * This is the part of an order integer that the user can provide.
         */
        const val USER_MASK = 0x0000ffff

        /**
         * Bit shift of the user portion of the order integer.
         */
        const val USER_SHIFT = 0

        /**
         * This is the part of an order integer that supplies the category of the item.
         */
        const val CATEGORY_MASK = -0x10000

        /**
         * Bit shift of the category portion of the order integer.
         */
        const val CATEGORY_SHIFT = 16

        /**
         * Flag which stops the Menu being closed when a sub menu is opened
         */
        const val FLAG_KEEP_OPEN_ON_SUBMENU_OPENED = 4

        private val sCategoryToOrder: IntArray = intArrayOf(1,  /* No category */
                4,  /* CONTAINER */
                5,  /* SYSTEM */
                3,  /* SECONDARY */
                2,  /* ALTERNATIVE */
                0)

        private fun findInsertIndex(items: ArrayList<ActionMenuItem>, ordering: Int): Int {
            for (i in items.indices.reversed()) {
                val item = items[i]
                if (item.order <= ordering) {
                    return i + 1
                }
            }
            return 0
        }

        /**
         * Returns the ordering across all items. This will grab the category from
         * the upper bits, find out how to order the category with respect to other
         * categories, and combine it with the lower bits.
         *
         * @param categoryOrder The category order for a particular item (if it has
         * not been or/add with a category, the default category is
         * assumed).
         * @return An ordering integer that can be used to order this item across
         * all the items (even from other categories).
         */
        private fun getOrdering(categoryOrder: Int): Int {
            val index = categoryOrder and CATEGORY_MASK shr CATEGORY_SHIFT
            require(!(index < 0 || index >= sCategoryToOrder.size)) { "order does not contain a valid category." }
            return sCategoryToOrder.get(index) shl CATEGORY_SHIFT or (categoryOrder and USER_MASK)
        }
    }

}