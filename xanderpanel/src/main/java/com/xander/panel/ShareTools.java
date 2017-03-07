package com.xander.panel;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ShareTools {

    private static final String TAG = "ShareTools";


    public static void share( Context context , String text, String[] images, ComponentName componentName ) {
        Intent shareIntent = createShareIntent(text,images);
        shareIntent.setComponent(componentName);
        context.startActivity(shareIntent);
    }

    /**
     * 查询手机内所有支持分享图片的应用，并将其打印出来
     *
     * @param context 上下文
     * @return
     */
    public static ActionMenu createShareActionMenu(
            Context context, String text, String[] images, String[] filterPackgees) {
        Intent shareIntent = createShareIntent(text, images);
        PackageManager pManager = context.getPackageManager();
        List<ResolveInfo> activitys = pManager.queryIntentActivities(
                shareIntent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
        );
        ActionMenu actionMenu = new ActionMenu(context);
        int appCount = activitys.size();
        for (int i = 0; i < appCount; i++) {
            String appTitle = (String) activitys.get(i).loadLabel(pManager);
            Drawable appIcon = activitys.get(i).loadIcon(pManager);
            ActionMenuItem actionMenuItem = new ActionMenuItem(context, 1, i, i,i, appTitle);
            actionMenuItem.setIcon(appIcon);
            ComponentName componentName = new ComponentName(
                    activitys.get(i).activityInfo.packageName,
                    activitys.get(i).activityInfo.name
            );
            actionMenuItem.setComponentName(componentName);
            actionMenu.add(actionMenuItem);
        }
        return actionMenu;
    }

    /**
     * 分享
     * @param text 需要分享的文字
     * @param images 需要分享的图片
     * @return
     */
    @NonNull
    private static Intent createShareIntent(String text, String[] images) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addCategory(Intent.CATEGORY_DEFAULT);
        if (null != images && images.length > 0) {
            shareIntent.setType("image/*");
            if (images.length > 1) {
                shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                ArrayList<Uri> imageUris = new ArrayList<>();
                for (String image : images) {
                    File imageFile = new File(image);
                    imageUris.add(Uri.fromFile(imageFile));
                }
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
            } else {
                shareIntent.setAction(Intent.ACTION_SEND);
                File imageFile = new File(images[0]);
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
            }
        } else {
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        }
        return shareIntent;
    }

}
