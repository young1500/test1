package com.hawk.funday.support.analytics;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.hawk.funday.BuildConfig;
import com.hawk.funday.base.file.ServiceContext;
import com.hawk.funday.support.utils.Singleton;
import com.hawk.funday.support.utils.TelephonyManagerUtil;
import com.wcc.framework.log.NLog;
import com.wcc.framework.network.NetworkHelper;

import org.aisen.android.common.utils.Logger;

import java.util.Locale;

/**
 * 统计工具类
 * Created by wenbiao.xie on 2016/7/13.
 */
public final class Stats {

    private final static String TAG = "Firebase";
    private final static int SESSION_TIMEOUT_DURATION = 5 * 60 * 1000;
    private final static int MIN_SESSION_DURATION = 30 * 1000;

    final static Singleton<FirebaseAnalytics> INSTANCE = new Singleton<FirebaseAnalytics>() {
        @Override
        protected FirebaseAnalytics create() {
            ServiceContext sc = ServiceContext.get();
            if (sc == null)
                throw new IllegalStateException("global context not init!");

            Context context = sc.getApplicationContext();
            FirebaseAnalytics fa = FirebaseAnalytics.getInstance(context);
            init(context, fa);
            return fa;
        }
    };

    private static void init(Context context, FirebaseAnalytics fa) {

        fa.setMinimumSessionDuration(MIN_SESSION_DURATION);
        fa.setSessionTimeoutDuration(SESSION_TIMEOUT_DURATION);

        // 调试阶段不启用日志收集
        if (!BuildConfig.ENABLE_ANALYTICS) {
            fa.setAnalyticsCollectionEnabled(false);
        } else {
            fa.setAnalyticsCollectionEnabled(true);

            String deviceId = TelephonyManagerUtil.getDeviceId(context);
            if (!TextUtils.isEmpty(deviceId)) {
                fa.setUserId(deviceId);
                if (TelephonyManagerUtil.isIMEI(deviceId)) {
                    fa.setUserProperty(StatEvent.UserProperty.IMEI, deviceId);
                }
                fa.setUserProperty(StatEvent.UserProperty.LANGUAGE,
                        context.getResources().getConfiguration().locale.toString());

                String channel = getMetaData(context, "CHANNEL");
                fa.setUserProperty(StatEvent.UserProperty.CHANNEL, channel != null? channel: "DEFAULT");

                Point size = getScreenSize(context);
                fa.setUserProperty(StatEvent.UserProperty.SCREEN,
                        String.format(Locale.US, "%dx%d", size.x, size.y));
            } else {
                NLog.w(TAG, "no valid user id for firebase");
            }
        }
    }

    private static NetworkHelper.NetworkStatus getNetworkType() {
        NetworkHelper helper = NetworkHelper.sharedHelper();
        if (helper != null) {
            return helper.getNetworkStatus();
        }

        ServiceContext sc = ServiceContext.get();
        if (sc == null)
            throw new IllegalStateException("global context not init!");

        NetworkHelper.NetworkStatus result = NetworkHelper.NetworkStatus.NetworkNotReachable;
        Context context = sc.getApplicationContext();
        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info == null || !info.isAvailable()) {
            result = NetworkHelper.NetworkStatus.NetworkNotReachable;
        } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            result = NetworkHelper.NetworkStatus.NetworkReachableViaWWAN;
        } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            result = NetworkHelper.NetworkStatus.NetworkReachableViaWiFi;
        } else if (info.getType() == ConnectivityManager.TYPE_BLUETOOTH) {
            result = NetworkHelper.NetworkStatus.NetworkReachableViaBlueTooth;
        }

        return result;
    }

    public static void logEvent(String name) {
        logEvent(name, new Bundle());
    }

    /**
     * 统计事件
     *
     * @param name   事件名
     * @param params 事件参数
     */
    public static void logEvent(String name, Bundle params) {
        try {
            FirebaseAnalytics fa = INSTANCE.get();
            if (params == null || !params.containsKey(StatEvent.CommonParams.NETWORK)) {
                if (params == null)
                    params = new Bundle();

                NetworkHelper.NetworkStatus status = getNetworkType();
                params.putString(StatEvent.CommonParams.NETWORK, status.getShortName());
            }
            fa.logEvent(name, params);
            Logger.d(TAG, "Submit logEvent, name: " + name);
        } catch (Exception e) {
            NLog.printStackTrace(e);
        }

    }

    /**
     * 设置统计用户ID
     *
     * @param id 用户ID
     */
    public static void setUserId(String id) {
        try {
            FirebaseAnalytics fa = INSTANCE.get();
            fa.setUserId(id);
        } catch (Exception e) {
            NLog.printStackTrace(e);
        }
    }

    /**
     * 统计用户的基本属性，支持自由扩展
     *
     * @param name  属性名
     * @param value 属性值
     */
    public static void setUserProperty(String name, String value) {
        try {
            FirebaseAnalytics fa = INSTANCE.get();
            fa.setUserProperty(name, value);
        } catch (Exception e) {
            NLog.printStackTrace(e);
        }
    }

    public static String getMetaData(Context context, String name) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo;
        Object value = null;
        try {

            applicationInfo = packageManager.getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (applicationInfo != null && applicationInfo.metaData != null) {
                value = applicationInfo.metaData.get(name);
            }

        } catch (PackageManager.NameNotFoundException e) {
            NLog.printStackTrace(e);
            NLog.w("ContextUtils",
                    "Could not read the name(%s) in the manifest file.", name);
            return null;
        }

        return value == null ? null : value.toString();
    }

    public static Point getScreenSize(Context context) {
        if (Build.VERSION.SDK_INT < 17) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            return new Point(metrics.widthPixels, metrics.heightPixels);
        } else {
            Display display= ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            Point size = new Point();
            display.getRealSize(size);
            return size;
        }
    }
}
