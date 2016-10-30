package com.hawk.funday.support.js;


import android.webkit.JavascriptInterface;

import com.wcc.framework.log.NLog;


/**
 * Created by yijie.ma on 2016/9/14.
 */
public class JsBridger {
    private final static String TAG = "JsBridger";
    private final String name;
    public static String last_inject_url = null;

    protected JsBridger(String name) {
        this.name = name;
    }

    public JsBridger() {
        this("appJavaHandler");
    }

    public String getName() {
        return name;
    }


    @JavascriptInterface
    public void injectSuccess(String url) {
        NLog.i("JsBridger", "injectSuccess url: %s", url);
        last_inject_url = url;
        onInjectSuccess(url);
    }

    @JavascriptInterface
    public void log(String text) {
        NLog.i("JsBridger", text);
    }

    @JavascriptInterface
    public void onLoad(String url) {
        NLog.i("JsBridger", "onLoad url: %s", url);
    }

    protected void onInjectSuccess(String url) {

    }




}
