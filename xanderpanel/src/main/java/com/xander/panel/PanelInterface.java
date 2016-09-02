package com.xander.panel;


/**
 * Created by wangxiaoyang on 16/5/29.
 */
public class PanelInterface {

    public interface PanelMenuListener {
        void onMenuClick(int position, ActionMenuItem menuItem);
    }

    public interface PanelItemListener {
        void onItemClick(int position);
    }

    public interface PanelDismissListener {
        void onPanelDismiss( XanderPanel panel );
    }

    public interface PanelShowListener {
        void onPanelShow( XanderPanel panel );
    }

    public interface SheetListener {
        void onSheetItemClick(int posi);
        void onSheetCancelClick();
    }

    public interface PanelControllerListener {
        void onPanelCancelClick(XanderPanel panel);
        void onPanelOkClick(XanderPanel panel);
    }

}
