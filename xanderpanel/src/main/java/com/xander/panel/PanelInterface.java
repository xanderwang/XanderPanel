package com.xander.panel;


import android.view.MenuItem;

/**
 * Created by wangxiaoyang on 16/5/29.
 */
public class PanelInterface {

    public interface PanelMenuListener {
        void onMenuClick(MenuItem menuItem);
    }

    public interface PanelDismissListener {
        void onPanelDismiss( XanderPanel panel );
    }

    public interface PanelShowListener {
        void onPanelShow( XanderPanel panel );
    }

    public interface SheetListener {
        void onSheetItemClick(int position);
        void onSheetCancelClick();
    }

    public interface PanelControllerListener {
        void onPanelNagetiiveClick(XanderPanel panel);
        void onPanelPositiveClick(XanderPanel panel);
    }

}
