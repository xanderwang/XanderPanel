package com.xander.panel

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import java.io.File
import java.util.*

object ShareTools {

    fun share(context: Context, text: String, images: Array<String>, componentName: ComponentName?) {
        val shareIntent = createShareIntent(text, images)
        componentName?.let {
            shareIntent.component = it
        }
        context.startActivity(shareIntent)
    }

    /**
     * 查询手机内所有支持分享图片的应用，并将其打印出来
     *
     * @param context 上下文
     * @return
     */
    @SuppressLint("WrongConstant")
    fun createShareActionMenu(context: Context, text: String, images: Array<String>,
            filterPackages: Array<String>): ActionMenu {
        val shareIntent = createShareIntent(text, images)
        val pManager = context.packageManager
        val activities = pManager.queryIntentActivities(shareIntent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
        val actionMenu = ActionMenu(context)
        val appCount = activities.size
        for (index in 0 until appCount) {
            val appTitle = activities[index].loadLabel(pManager) as String
            val appIcon = activities[index].loadIcon(pManager)
            val actionMenuItem = ActionMenuItem(context, 1, index, index, index, appTitle)
            actionMenuItem.setIcon(appIcon)
            val activityInfo = activities[index].activityInfo
            val componentName = ComponentName(activityInfo.packageName, activityInfo.name)
            actionMenuItem.componentName = componentName
            actionMenu.add(actionMenuItem)
        }
        return actionMenu
    }

    /**
     * 创建需要分享的 Intent
     * @param text 需要分享的文字
     * @param images 需要分享的图片
     * @return
     */
    private fun createShareIntent(text: String, images: Array<String>): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.addCategory(Intent.CATEGORY_DEFAULT)
        if (images.isNotEmpty()) {
            shareIntent.type = "image/*"
            if (images.size > 1) {
                shareIntent.action = Intent.ACTION_SEND_MULTIPLE
                val imageUris = ArrayList<Uri>()
                for (image in images) {
                    val imageFile = File(image)
                    imageUris.add(Uri.fromFile(imageFile))
                }
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
            } else {
                shareIntent.action = Intent.ACTION_SEND
                val imageFile = File(images[0])
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile))
            }
        } else {
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, text)
        }
        return shareIntent
    }
}