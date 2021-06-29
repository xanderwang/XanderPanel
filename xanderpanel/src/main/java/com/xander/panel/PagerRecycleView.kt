package com.xander.panel

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by wangxiaoyang on 16-9-7.
 */
class PagerRecycleView : RecyclerView {

    private var pageIndex = 0

    private var recycleAdapter: PagerRecycleAdapter = PagerRecycleAdapter(context)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    fun setPageIndexAndAdapter(pageIndex: Int, actionMenu: ActionMenu) {
        this.pageIndex = pageIndex
        recycleAdapter.actionMenu = actionMenu
        adapter = recycleAdapter
        recycleAdapter.notifyDataSetChanged()
    }
}

private class MenuHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private val menuIcon: ImageView = itemView.findViewById<View>(R.id.panel_menu_icon) as ImageView
    private val menuTitle: TextView = itemView.findViewById<View>(R.id.panel_menu_title) as TextView

    lateinit var actionMenuItem: ActionMenuItem

    init {
        itemView.setOnClickListener(this)
    }

    fun bindActionMenuItem(menuItem: ActionMenuItem) {
        actionMenuItem = menuItem
        if (null == menuItem.icon) {
            menuIcon.visibility = RecyclerView.GONE
        } else {
            menuIcon.setImageDrawable(menuItem.icon)
            menuIcon.visibility = RecyclerView.VISIBLE
        }
        if (TextUtils.isEmpty(menuItem.title)) {
            menuTitle.visibility = RecyclerView.GONE
        } else {
            menuTitle.text = menuItem.title
            menuTitle.visibility = RecyclerView.VISIBLE
        }
    }

    override fun onClick(v: View) {
        actionMenuItem.invoke()
    }
}


private class PagerRecycleAdapter(private val context: Context) : RecyclerView.Adapter<MenuHolder>() {

    lateinit var actionMenu: ActionMenu

    override fun getItemCount(): Int {
        return actionMenu.size()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.xander_panel_menu_grid_item, parent, false)
        return MenuHolder(view)
    }

    override fun onBindViewHolder(holder: MenuHolder, position: Int) {
        holder.bindActionMenuItem(actionMenu.getItem(position) as ActionMenuItem)
    }
}