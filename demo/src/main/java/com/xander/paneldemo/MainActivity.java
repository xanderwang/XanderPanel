package com.xander.paneldemo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.xander.panel.PanelInterface;
import com.xander.panel.XanderPanel;

public class MainActivity extends AppCompatActivity {

    private int mButtonsId[] = {
            R.id.top_with_controller,
            R.id.bottom_with_controller,
            R.id.top_custom_view,
            R.id.bottom_custom_view,
            R.id.sheet,
            R.id.top_list_menu,
            R.id.bottom_list_menu,
            R.id.top_grid_menu,
            R.id.bottom_grid_menu,
            R.id.bottom_list_share,
            R.id.bottom_grid_share
    };

    private Context mContext;
    private LayoutInflater mInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mInflater = LayoutInflater.from(mContext);

        for (int i = 0; i < mButtonsId.length; i++) {
            View v = findViewById(mButtonsId[i]);
            v.setOnClickListener(mOnClickListener);
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            XanderPanel.Builder mBuilder = new XanderPanel.Builder(MainActivity.this);
            switch (v.getId()) {
                case R.id.top_with_controller:
                    mBuilder.setTitle("Title")
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage("I am Message!!!")
                            .setGravity(Gravity.TOP)
                            .setController("Cancel", "Ok", new PanelInterface.PanelControllerListener() {
                                @Override
                                public void onPanelNagetiiveClick(XanderPanel panel) {
                                    toast("onPanelNagetiiveClick");
                                }

                                @Override
                                public void onPanelPositiveClick(XanderPanel panel) {
                                    toast("onPanelPositiveClick");
                                }
                            })
                            .setCanceledOnTouchOutside(true);
                    break;
                case R.id.bottom_with_controller:
                    mBuilder.setTitle("Title")
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage("I am Message!!!")
                            .setGravity(Gravity.BOTTOM)
                            .setController("Cancel", "Ok", new PanelInterface.PanelControllerListener() {
                                @Override
                                public void onPanelNagetiiveClick(XanderPanel panel) {
                                    toast("onPanelNagetiiveClick");
                                }

                                @Override
                                public void onPanelPositiveClick(XanderPanel panel) {
                                    toast("onPanelPositiveClick");
                                }
                            })
                            .setCanceledOnTouchOutside(true);
                    break;
                case R.id.sheet:
                    mBuilder.setSheet(
                            new String[]{"I", "am", "sheet", "item"},
                            true,
                            new PanelInterface.SheetListener() {
                                @Override
                                public void onSheetItemClick(int position) {
                                    toast("click sheet item " + position);
                                }

                                @Override
                                public void onSheetCancelClick() {
                                    toast("sheet cancel");
                                }
                            }
                    );
                    break;
                case R.id.top_list_menu:
                    mBuilder.list()
                            .setMenu(R.menu.main_menu, new PanelInterface.PanelMenuListener() {
                                @Override
                                public void onMenuClick(MenuItem menuItem) {
                                    toast("List MenuItem click " + menuItem.getTitle());
                                }
                            })
                            .setGravity(Gravity.TOP)
                            .setCanceledOnTouchOutside(true);
                    break;
                case R.id.bottom_list_menu:
                    mBuilder.list()
                            .setMenu(R.menu.main_menu, new PanelInterface.PanelMenuListener() {
                                @Override
                                public void onMenuClick(MenuItem menuItem) {
                                    toast("List MenuItem click " + menuItem.getTitle());
                                }
                            })
                            .setGravity(Gravity.BOTTOM)
                            .setCanceledOnTouchOutside(true);
                    break;
                case R.id.top_grid_menu:
                    mBuilder.grid(2, 3)
                            .setMenu(R.menu.main_menu, new PanelInterface.PanelMenuListener() {
                                @Override
                                public void onMenuClick(MenuItem menuItem) {
                                    toast("Grid MenuItem click " + menuItem.getTitle());
                                }
                            })
                            .setGravity(Gravity.TOP)
                            .setCanceledOnTouchOutside(true);
                    break;
                case R.id.bottom_grid_menu:
                    mBuilder.grid(2, 3)
                            .setMenu(R.menu.main_menu, new PanelInterface.PanelMenuListener() {
                                @Override
                                public void onMenuClick(MenuItem menuItem) {
                                    toast("Grid MenuItem click " + menuItem.getTitle());
                                }
                            })
                            .setGravity(Gravity.BOTTOM)
                            .setCanceledOnTouchOutside(true);
                    break;
                case R.id.bottom_list_share:
                    mBuilder.list()
                            .shareText("test share")
                            .setGravity(Gravity.BOTTOM)
                            .setCanceledOnTouchOutside(true);
                    break;
                case R.id.bottom_grid_share:
                    mBuilder.grid(2, 3)
                            .shareText("test share")
                            .shareImages(new String[]{"/sdcard/s1.png","/sdcard/s2.png"})
                            .setGravity(Gravity.BOTTOM)
                            .setCanceledOnTouchOutside(true);
                    break;
                case R.id.top_custom_view:
                    mBuilder.setGravity(Gravity.TOP);
                    mBuilder.setCanceledOnTouchOutside(true);
                    View mCustomView = mInflater.inflate(R.layout.custom_layout, null);
                    mBuilder.setView(mCustomView);
                    break;
                case R.id.bottom_custom_view:
                    mBuilder.setCanceledOnTouchOutside(true);
                    mBuilder.setGravity(Gravity.BOTTOM);
                    View mCustomViewBottom = mInflater.inflate(R.layout.custom_layout, null);
                    mBuilder.setView(mCustomViewBottom);
                    break;
            }
            XanderPanel xanderPanel = mBuilder.create();
            xanderPanel.show();
        }
    };

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}
