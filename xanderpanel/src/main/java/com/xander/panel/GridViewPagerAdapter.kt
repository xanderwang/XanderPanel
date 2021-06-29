package com.xander.panel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.PagerAdapter
import java.util.*

/**
 * Created by wangxiaoyang on 16-9-7.
 */
class GridViewPagerAdapter(private val context: Context, PAGER_ITEM_ROW: Int, PAGER_ITEM_COL: Int) : PagerAdapter() {

    private var PAGER_ITEM_ROW = 2
    private var PAGER_ITEM_COL = 4
    private var PAGER_ITEM_COUNT = PAGER_ITEM_ROW * PAGER_ITEM_COL

    private val recyclerViewList: MutableList<PagerRecycleView> = ArrayList()
    private lateinit var actionMenus: ActionMenu

    init {
        this.PAGER_ITEM_COL = PAGER_ITEM_COL
        this.PAGER_ITEM_ROW = PAGER_ITEM_ROW
        PAGER_ITEM_COUNT = PAGER_ITEM_ROW * PAGER_ITEM_COL
    }

    fun setPAGER_ITEM_COL(PAGER_ITEM_COL: Int) {
        this.PAGER_ITEM_COL = PAGER_ITEM_COL
        PAGER_ITEM_COUNT = PAGER_ITEM_ROW * PAGER_ITEM_COL
    }

    fun setPAGER_ITEM_ROW(PAGER_ITEM_ROW: Int) {
        this.PAGER_ITEM_ROW = PAGER_ITEM_ROW
        PAGER_ITEM_COUNT = PAGER_ITEM_ROW * PAGER_ITEM_COL
    }

    fun setActionMenus(actionMenu: ActionMenu, parentView: ViewGroup) {
        actionMenus = actionMenu
        resetRecyclerViews(parentView)
    }

    private fun resetRecyclerViews(parentView: ViewGroup) {
        recyclerViewList.clear()
        val inflater = LayoutInflater.from(context)
        val pagerCount = (actionMenus.size() + PAGER_ITEM_COUNT - 1) / PAGER_ITEM_COUNT
        for (i in 0 until pagerCount) {
            createRecyclerView(i, parentView, inflater)
        }
    }

    private fun createRecyclerView(pageIndex: Int, parentView: ViewGroup, inflater: LayoutInflater) {
        val pagerRecycleView = inflater.inflate(R.layout.xander_panel_menu_gridviewpager_item, parentView,
                false) as PagerRecycleView
        val pageItemActionMenu = ActionMenu(context)
        val allMenuSize = actionMenus.size()
        val pageItemStart = pageIndex * PAGER_ITEM_COUNT
        val pageItemEnd = pageItemStart + PAGER_ITEM_COUNT
        var index = pageItemStart
        while (index < pageItemEnd && index < allMenuSize) {
            pageItemActionMenu.add(actionMenus.getItem(index) as ActionMenuItem)
            index++
        }
        pagerRecycleView.setPageIndexAndAdapter(pageIndex, pageItemActionMenu)
        val layoutManager = GridLayoutManager(context, PAGER_ITEM_COL, GridLayoutManager.VERTICAL, false)
        pagerRecycleView.layoutManager = layoutManager
        recyclerViewList.add(pagerRecycleView)
    }

    override fun destroyItem(container: ViewGroup, position: Int, viewObj: Any) {
        // super.destroyItem(container, position, object);
        if (viewObj is PagerRecycleView) {
            container.removeView(viewObj as PagerRecycleView?)
        }
    }

    override fun getCount(): Int {
        return recyclerViewList.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        // return super.instantiateItem(container, position);
        val recycleView = recyclerViewList[position]
        container.addView(recycleView)
        return recycleView
    }

    override fun isViewFromObject(view: View, viewObj: Any): Boolean {
        return view === viewObj
    }

}