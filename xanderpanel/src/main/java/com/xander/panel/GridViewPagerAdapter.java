package com.xander.panel;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangxiaoyang on 16-9-7.
 */

public class GridViewPagerAdapter extends PagerAdapter {

    private Context context;
    private int PAGER_ITEM_ROW = 2;
    private int PAGER_ITEM_COL = 4;
    private int PAGER_ITEM_COUNT = PAGER_ITEM_ROW * PAGER_ITEM_COL;
    private List<PagerRecycleView> recyclerViewList = new ArrayList<>();
    private ActionMenu actionMenus;

    public GridViewPagerAdapter(Context context, int PAGER_ITEM_ROW,int PAGER_ITEM_COL) {
        this.context = context;
        this.PAGER_ITEM_COL = PAGER_ITEM_COL;
        this.PAGER_ITEM_ROW = PAGER_ITEM_ROW;
        PAGER_ITEM_COUNT = PAGER_ITEM_ROW * PAGER_ITEM_COL;
    }

    public void setPAGER_ITEM_COL(int PAGER_ITEM_COL) {
        this.PAGER_ITEM_COL = PAGER_ITEM_COL;
        PAGER_ITEM_COUNT = PAGER_ITEM_ROW * PAGER_ITEM_COL;
    }

    public void setPAGER_ITEM_ROW(int PAGER_ITEM_ROW) {
        this.PAGER_ITEM_ROW = PAGER_ITEM_ROW;
        PAGER_ITEM_COUNT = PAGER_ITEM_ROW * PAGER_ITEM_COL;
    }

    public void setActionMenus(ActionMenu actionMenu, ViewGroup parentView) {
        actionMenus = actionMenu;
        resetRecyclerViews(parentView);
    }

    private void resetRecyclerViews(ViewGroup parentView) {
        recyclerViewList.clear();
        LayoutInflater inflater = LayoutInflater.from(context);
        int pagerCount = (actionMenus.size() + PAGER_ITEM_COUNT - 1) / PAGER_ITEM_COUNT;
        for (int i = 0; i < pagerCount; i++) {
            createrecyclerView(i, parentView, inflater);
        }
    }

    private void createrecyclerView(int pageIndex, ViewGroup parentView, LayoutInflater inflater) {
        PagerRecycleView pagerRecycleView = (PagerRecycleView) inflater.inflate(
                R.layout.xander_panel_menu_gridviewpager_item,
                parentView,
                false
        );
        ActionMenu pageItemActionMenu = new ActionMenu(context);
        int allMenuSize = actionMenus.size();
        int pageItemStart = pageIndex * PAGER_ITEM_COUNT;
        int pageItemEnd = pageItemStart + PAGER_ITEM_COUNT;
        for (int i = pageItemStart; i < pageItemEnd && i < allMenuSize; i++) {
            pageItemActionMenu.add((ActionMenuItem) actionMenus.getItem(i));
        }
        pagerRecycleView.setPageIndexAndAdapter(pageIndex, pageItemActionMenu);
        GridLayoutManager layoutManager =
                new GridLayoutManager(context, PAGER_ITEM_COL, GridLayoutManager.VERTICAL, false);
        pagerRecycleView.setLayoutManager(layoutManager);
        recyclerViewList.add(pagerRecycleView);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        super.destroyItem(container, position, object);
        if (object instanceof PagerRecycleView) {
            container.removeView((PagerRecycleView) object);
        }
    }

    @Override
    public int getCount() {
        return recyclerViewList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
//        return super.instantiateItem(container, position);
        PagerRecycleView recycleView = recyclerViewList.get(position);
        container.addView(recycleView);
        return recycleView;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
