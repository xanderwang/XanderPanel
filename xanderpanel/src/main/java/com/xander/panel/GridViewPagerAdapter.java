package com.xander.panel;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangxiaoyang on 16-9-7.
 */

public class GridViewPagerAdapter extends PagerAdapter {

    private Context context;
    private BaseAdapter baseAdapter;
    private int PAGER_ITEM_ROW = 2;
    private int PAGER_ITEM_COL = 4;
    private int PAGER_ITEM_COUNT = PAGER_ITEM_ROW * PAGER_ITEM_COL;
    private List<RecyclerView> recyclerViewList = new ArrayList<>();

    public GridViewPagerAdapter(Context context, int PAGER_ITEM_COL, int PAGER_ITEM_ROW) {
        this.context = context;
        this.PAGER_ITEM_COL = PAGER_ITEM_COL;
        this.PAGER_ITEM_ROW = PAGER_ITEM_ROW;
    }

    private void createrecyclerView(int pageIndex){



    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    @Override
    public int getCount() {
        return recyclerViewList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return false;
    }
}
