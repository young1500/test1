package com.hawk.funday.support.sdk;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.hawk.funday.BuildConfig;
import com.hawk.funday.base.AppContext;
import com.hawk.funday.support.sdk.bean.AccountBean;
import com.hawk.funday.support.sdk.http.FundayHttpUtility;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.network.biz.ABizLogic;
import org.aisen.android.network.http.HttpConfig;
import org.aisen.android.network.http.IHttpUtility;
import org.aisen.android.network.http.Params;

import java.util.TimeZone;

/**
 * Created by wangdan on 16/8/17.
 */
public abstract class BaseSDK extends ABizLogic {

    BaseSDK(CacheMode mode) {
        super(mode);
    }

    /**
     * 网络代理
     *
     * @return
     */
    @Override
    protected IHttpUtility configHttpUtility() {
        return new FundayHttpUtility();
    }

    /**
     * 网络请求日志，服务器地址，Headers，Content-Type等信息
     *
     * @return
     */
    @Override
    protected HttpConfig configHttpConfig() {
        HttpConfig config = new HttpConfig();

        config.baseUrl = BuildConfig.BASE_URL;

        return config;
    }

    /**
     * 网络请求日志基础参数
     *
     * @param params
     * @return
     */
    protected Params basicParams(Params params) {
        if (params == null) {
            params = new Params();
        }

        params.addParameter("model", Build.MODEL);
        params.addParameter("os_code", String.valueOf(Build.VERSION.SDK_INT));
        if (GlobalContext.getInstance() != null) {
            Context context = GlobalContext.getInstance();
            params.addParameter("resolution", SystemUtils.getScreenWidth(context) + "_" + SystemUtils.getScreenHeight(context));
            String imsi = getIMSI(context);
            if (!TextUtils.isEmpty(imsi)) {
                params.addParameter("imsi", imsi);
            }
            params.addParameter("language", context.getResources().getConfiguration().locale.toString());
            String networktype = "";
            switch (getConnectedType(context)) {
                case ConnectivityManager.TYPE_MOBILE:
                    networktype = "mobile";
                    break;
                case ConnectivityManager.TYPE_WIFI:
                    networktype = "wifi";
                    break;
                case ConnectivityManager.TYPE_WIMAX:
                    networktype = "wimax";
                    break;
                case ConnectivityManager.TYPE_ETHERNET:
                    networktype = "ethernet";
                    break;
                case ConnectivityManager.TYPE_BLUETOOTH:
                    networktype = "bluetooth";
            }
            params.addParameter("network", networktype);
            params.addParameter("version_code", SystemUtils.getVersionCode(context) + "");
            params.addParameter("version_name", SystemUtils.getVersionName(context));
        }
        params.addParameter("channel", BuildConfig.CHANNEL);
        AccountBean account = AppContext.getLoginedAccount();
        if (account != null) {
            if (!TextUtils.isEmpty(account.getToken()) &&
                    account.getUserId() > 0) {
                params.addParameter("uid", String.valueOf(account.getUserId()));
                params.addParameter("token", account.getToken());
            }
        }
        if (!TextUtils.isEmpty(AppContext.getUuid())) {
            params.addParameter("uuid", AppContext.getUuid());
        }
        if (!TextUtils.isEmpty(getCurrentTimeZone())) {
            params.addParameter("tzone", getCurrentTimeZone());
        }

        return params;
    }

    private static int getConnectedType(Context context) {
        try {
            if (context != null) {
                ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mNetworkInfo = mConnectivityManager
                        .getActiveNetworkInfo();
                if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
                    return mNetworkInfo.getType();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static String getIMSI(Context context) {
        String imsi = null;

        try {
            if (Build.VERSION.SDK_INT < 23) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

                imsi =  telephonyManager.getSubscriberId();
            }
            else {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

                    imsi =  telephonyManager.getSubscriberId();
                }
            }
        } catch (Throwable e) {
            Logger.printExc(BaseSDK.class, e);
        }

        return imsi;
    }

    private String getCurrentTimeZone() { // 获取时区
        TimeZone tz = TimeZone.getDefault();
        return String.valueOf((tz.getRawOffset() + tz.getDSTSavings()) / (3600000f));
    }
}
