package com.xander.panel;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by wangxiaoyang on 16-9-7.
 */

public class PagerRecycleView extends RecyclerView {

    private int pageIndex = 0;

    public PagerRecycleView(Context context) {
        super(context);
    }

    public PagerRecycleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PagerRecycleView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
