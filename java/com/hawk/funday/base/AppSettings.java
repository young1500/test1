package com.hawk.funday.base;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;

import com.hawk.funday.support.permissions.SdcardPermissionAction;

import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.support.action.IAction;
import org.aisen.android.ui.activity.basic.BaseActivity;

/**
 * 应用配置信息
 *
 * Created by wangdan on 16/8/17.
 */
final public class AppSettings {

    private AppSettings() {

    }

    public static String getImageSavedPath(Activity activity) {
        final StringBuffer path = new StringBuffer("");
        new IAction(activity, new SdcardPermissionAction((BaseActivity) activity, null)) {

            @Override
            public void doAction() {
                path.append(Environment.getExternalStorageDirectory() + "/Funday/") ;
            }

        }.run();
        return path.toString();
    }

    public static long getAlowMaxFlowLength() {
        return 2 * 1024 * 2024;
    }

    public static void setFlowRemind(Context context, boolean enable) {
        ActivityHelper.putBooleanShareData(context, "com.hawk.funday.FlowRemind", enable);
    }

    public static boolean isFlowRemind(Context context) {
        return ActivityHelper.getBooleanShareData(context, "com.hawk.funday.FlowRemind", true);
    }

}
