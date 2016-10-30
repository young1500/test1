package com.hawk.funday.base;

import android.text.TextUtils;
import android.util.Log;

import com.hawk.funday.BuildConfig;
import com.hawk.funday.base.file.DirType;
import com.hawk.funday.base.file.FileContext;
import com.hawk.funday.component.imageloader.cache.disc.impl.UnlimitedDiscCache;
import com.hawk.funday.component.imageloader.core.ImageLoader;
import com.hawk.funday.component.imageloader.core.ImageLoaderConfiguration;
import com.hawk.funday.support.analytics.FirebaseAgent;
import com.hawk.funday.support.db.FundayDB;
import com.hawk.funday.support.imageloader.FundayImageDecoder;
import com.hawk.funday.support.sdk.bean.AccountBean;
import com.hawk.funday.support.utils.FundayExceptionDelegate;
import com.hawk.funday.support.utils.TokenUtils;
import com.hawk.funday.support.utils.UUIDUtils;
import com.hawk.funday.ui.activity.base.FundayActivityHelper;
import com.squareup.okhttp.OkHttpClient;
import com.tcl.example.tcllogin.LoginTclManager;
import com.tencent.bugly.crashreport.CrashReport;
import com.tma.analytics.AnalyticsConfig;
import com.tma.analytics.TLogger;
import com.tma.analytics.TmaAgent;
import com.tma.analytics.TmaAnalyticsConfig;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.CrashHandler;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.ui.activity.basic.BaseActivity;

import java.io.File;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by wangdan on 16/8/17.
 */
public class FundayApplication extends GlobalContext {

    private final static String TAG = "FundayApplication";

    private static final String SS_KEY_GLOBAL = "74284763";

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化Exception
        TaskException.config(new FundayExceptionDelegate());
        // 初始化DB
        FundayDB.setInitDB(this);
        // 配置ActivityHelper
        BaseActivity.setHelper(FundayActivityHelper.class);
        // 初始化AccountBean
        setupAccountBean();
        // 初始化UUID
        setupUUID();
        // 配置文件系统
        Log.v(TAG, "setupFileManager");
        setupFileManager();
        // 配置OkHttp
        Log.v(TAG, "setupOkHttp");
        setupOkHttp();
        // 打开Debug日志
        Logger.DEBUG = BuildConfig.LOG_DEBUG;
        // 设置crash
        Log.v(TAG, "setupCrash");
        setupCrash();
        //初始化图片加载组件
        Log.v(TAG, "initPicLoader");
        initPicLoader();
        // 初始化bugly
        setupBugly();
        // 初始化TCL Login
        setupTCLLogin();
        // 初始化统计
        setupAnalytics();
    }

    private void setupTCLLogin() {
        LoginTclManager.getInstance().init(this, SS_KEY_GLOBAL);
    }

    // 设置crash组件
    public void setupCrash() {
        if (BuildConfig.LOG_DEBUG) {
            CrashHandler.setupCrashHandler(this);
        }
    }

    // 设置网络请求
    private void setupOkHttp() {
        configOkHttpClient(CONN_TIMEOUT, READ_TIMEOUT);
        if (getOkHttpClient() != null) {
            OkHttpClient okHttpClient = getOkHttpClient();
            try {
                TrustManager tm = new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                    }

                    public void checkServerTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                };

                SSLContext e = SSLContext.getInstance("TLS");
                e.init(null, new TrustManager[]{tm}, null);
                okHttpClient.setSslSocketFactory(e.getSocketFactory());
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
        }
    }

    // 设置文件系统
    private void setupFileManager() {
        FileContext.initInstance(this);
    }

    //初始化图片加载组件
    private void initPicLoader(){
        File cacheDir = FileContext.get().getDirectoryManager().getDir(DirType.image.value());
        File reserveCacheDir = FileContext.get().getDirectoryManager().getDir(DirType.cache.value());
        reserveCacheDir.mkdirs();
        cacheDir.mkdirs();
        if (cacheDir.isDirectory() && cacheDir.canWrite()){
            Logger.e("cacheDir", cacheDir.getAbsolutePath());
        }
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .diskCache(new UnlimitedDiscCache(cacheDir,reserveCacheDir))
                .imageDecoder(new FundayImageDecoder())
                .build();
        ImageLoader.getInstance().init(config);
    }

    // 初始化AccountBean
    private void setupAccountBean() {
        AccountBean accountBean = new TokenUtils(getApplicationContext()).getAccountBean();
        if (accountBean != null && accountBean.getUser() != null &&
                accountBean.getToken() != null) {
            AppContext.setLoginedAccountBean(accountBean);
            Logger.d(TAG + "_Account", "setupUserAccount, userName: " + accountBean.getUser().getName()
                    + "; userToken: " + accountBean.getToken());
        } else {
            Logger.d(TAG + "_Account", "setupUserAccount fail, No user Logined");
        }
    }

    // 初始化UUIDBean
    private void setupUUID() {
        String UUID = UUIDUtils.getUUID(this);
        if (!TextUtils.isEmpty(UUID)) {
            AppContext.setUuid(UUID);
            Logger.d(TAG, "setup UUID successful, UUID: " + UUID);
        }
    }

    // 初始化bugly
    private void setupBugly() {
        try {
            CrashReport.initCrashReport(this, BuildConfig.BUGLY_APP_ID, BuildConfig.DEBUG);
            Logger.d(TAG, "setupBugly, ID: " + BuildConfig.BUGLY_APP_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 初始化统计
    private void setupAnalytics() {
        TLogger.DEBUG = BuildConfig.LOG_DEBUG;
        TmaAnalyticsConfig config = new TmaAnalyticsConfig(this, new FirebaseAgent(this));
        AnalyticsConfig.ACTIVITY_DURATION_OPEN = false;
        TmaAgent.startWithConfigure(this, config);
    }

}
