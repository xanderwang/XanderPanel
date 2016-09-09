package com.xander.panel;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by wangxiaoyang on 16-9-7.
 */

public class PagerRecycleView extends RecyclerView {

    private int pageIndex = 0;
    private ActionMenu actionMenu;
    private PagerRecycleAdapter recycleAdapter;

    public PagerRecycleView(Context context) {
        super(context);
    }

    public PagerRecycleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PagerRecycleView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setPageIndexAndAdapter(int pageIndex, ActionMenu actionMenu) {
        this.pageIndex = pageIndex;
        this.actionMenu = actionMenu;
        recycleAdapter = new PagerRecycleAdapter();
        setAdapter(recycleAdapter);
    }


    class PagerRecycleAdapter extends RecyclerView.Adapter<MenuHolder> {

        public PagerRecycleAdapter() {

        }

        @Override
        public int getItemCount() {
            if (null != actionMenu) {
                return actionMenu.size();
            }
            return 0;
        }

        @Override
        public MenuHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.xander_panel_menu_grid_item, parent, false);
            MenuHolder menuHolder = new MenuHolder(view);
            return menuHolder;
        }

        @Override
        public void onBindViewHolder(MenuHolder holder, int position) {
            holder.bindActionMenuItem((ActionMenuItem) actionMenu.getItem(position));
        }
    }

    class MenuHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView menuIcon;
        private TextView menuTitle;
        private ActionMenuItem actionMenuItem;

        public MenuHolder(View itemView) {
            super(itemView);
            menuIcon = (ImageView) itemView.findViewById(R.id.panel_menu_icon);
            menuTitle = (TextView) itemView.findViewById(R.id.panel_menu_title);
            itemView.setOnClickListener(this);
        }

        public void bindActionMenuItem(ActionMenuItem menuItem) {
            this.actionMenuItem = menuItem;
            if (null == menuItem.getIcon()) {
                menuIcon.setVisibility(GONE);
            } else {
                menuIcon.setImageDrawable(menuItem.getIcon());
                menuIcon.setVisibility(VISIBLE);
            }
            if (TextUtils.isEmpty(menuItem.getTitle())) {
                menuTitle.setVisibility(GONE);
            } else {
                menuTitle.setText(menuItem.getTitle());
                menuTitle.setVisibility(VISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            if (actionMenuItem != null) {
                actionMenuItem.invoke();
            }
        }
    }

}
