package com.hawk.funday.support.js;

import android.content.Context;
import android.webkit.JavascriptInterface;

import org.aisen.android.common.utils.Logger;


/**
 * Created by yijie.ma on 2016/9/18.
 */
public class JsInterface {
    private Context mContext;
    private final static String TAG = "JsInterface";

    public final static String YOUTUBE = "youtube";
    public final static String KALTURA = "kaltura";


    public JsInterface(Context context){
        mContext = context;
    }

    @JavascriptInterface
    public void log(String s){
        Logger.e(TAG, s);
    }

    @JavascriptInterface
    public void outputSource(String s, String source){
        Logger.e(TAG, s);
    }
}
