package com.xander.paneldemo

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xander.panel.PanelControllerListener
import com.xander.panel.PanelMenuListener
import com.xander.panel.SheetListener
import com.xander.panel.XanderPanel

class MainActivity : AppCompatActivity() {

    private val mButtonsId: IntArray = intArrayOf(
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
    )
    private lateinit var mContext: Context

    private lateinit var mInflater: LayoutInflater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = this
        mInflater = LayoutInflater.from(mContext)
        for (itemIndex in mButtonsId.indices) {
            val v = findViewById<View>(mButtonsId[itemIndex])
            v.setOnClickListener(mOnClickListener)
        }
    }

    private val mOnClickListener: View.OnClickListener = View.OnClickListener { v ->
        val mBuilder = XanderPanel.Builder(this@MainActivity)
        when (v.id) {
            R.id.top_with_controller -> mBuilder.setTitle("Title").setIcon(R.mipmap.ic_launcher)
                .setMessage("I am Message!!!").setGravity(Gravity.TOP)
                .setController("Cancel", "Ok", object : PanelControllerListener {
                    override fun onPanelNegativeClick(panel: XanderPanel) {
                        toast("onPanelNagetiiveClick")
                    }

                    override fun onPanelPositiveClick(panel: XanderPanel) {
                        toast("onPanelPositiveClick")
                    }
                }).setCanceledOnTouchOutside(true)
            R.id.bottom_with_controller -> mBuilder.setTitle("Title").setIcon(R.mipmap.ic_launcher)
                .setMessage("I am Message!!!").setGravity(Gravity.BOTTOM)
                .setController("Cancel", "Ok", object : PanelControllerListener {
                    override fun onPanelNegativeClick(panel: XanderPanel) {
                        toast("onPanelNagetiiveClick")
                    }

                    override fun onPanelPositiveClick(panel: XanderPanel) {
                        toast("onPanelPositiveClick")
                    }
                }).setCanceledOnTouchOutside(true)
            R.id.sheet -> mBuilder.setSheet(arrayOf("I", "am", "sheet", "item"), true, "I am Cancel",
                    object : SheetListener {
                        override fun onSheetItemClick(position: Int) {
                            toast("click sheet item $position")
                        }

                        override fun onSheetCancelClick() {
                            toast("sheet cancel")
                        }
                    })
            R.id.top_list_menu -> mBuilder.list().setMenu(R.menu.main_menu, object : PanelMenuListener {
                    override fun onMenuClick(menuItem: MenuItem) {
                        toast("List MenuItem click " + menuItem.title)
                    }
                }).setGravity(Gravity.TOP).setCanceledOnTouchOutside(true)
            R.id.bottom_list_menu -> mBuilder.list().setMenu(R.menu.main_menu, object : PanelMenuListener {
                    override fun onMenuClick(menuItem: MenuItem) {
                        toast("List MenuItem click " + menuItem.title)
                    }
                }).setGravity(Gravity.BOTTOM).setCanceledOnTouchOutside(true)
            R.id.top_grid_menu -> mBuilder.grid(2, 3).setMenu(R.menu.main_menu, object : PanelMenuListener {
                    override fun onMenuClick(menuItem: MenuItem) {
                        toast("Grid MenuItem click " + menuItem.title)
                    }
                }).setGravity(Gravity.TOP).setCanceledOnTouchOutside(true)
            R.id.bottom_grid_menu -> mBuilder.grid(2, 3).setMenu(R.menu.main_menu, object : PanelMenuListener {
                    override fun onMenuClick(menuItem: MenuItem) {
                        toast("Grid MenuItem click " + menuItem.title)
                    }
                }).setGravity(Gravity.BOTTOM).setCanceledOnTouchOutside(true)
            R.id.bottom_list_share -> mBuilder.list().shareText("test share").setGravity(Gravity.BOTTOM)
                .setCanceledOnTouchOutside(true)
            R.id.bottom_grid_share -> mBuilder.grid(2, 3).shareText("test share")
                .shareImages(arrayOf("/sdcard/s1.png", "/sdcard/s2.png")).setGravity(Gravity.BOTTOM)
                .setCanceledOnTouchOutside(true)
            R.id.top_custom_view -> {
                mBuilder.setGravity(Gravity.TOP)
                mBuilder.setCanceledOnTouchOutside(true)
                val mCustomView = mInflater.inflate(R.layout.custom_layout, null)
                mBuilder.setView(mCustomView)
            }
            R.id.bottom_custom_view -> {
                mBuilder.setCanceledOnTouchOutside(true)
                mBuilder.setGravity(Gravity.BOTTOM)
                val mCustomViewBottom = mInflater.inflate(R.layout.custom_layout, null)
                mBuilder.setView(mCustomViewBottom)
            }
        }
        val xanderPanel = mBuilder.create()
        xanderPanel.show()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}