package com.hawk.funday.support.utils;

import android.content.Context;
import android.text.TextUtils;

import com.wcc.framework.util.IOUtils;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by yijie.ma on 2016/8/26.
 */
public class JsUtils {

    /*
     * 获取js文件
     */
    public static String readFromAssets(Context context, String fileName) throws IOException {
        InputStream is = null;
        try {
            is = context.getAssets().open(fileName);
            BufferedInputStream bis = new BufferedInputStream(is);
            String content = IOUtils.readLeft(bis, "utf-8");
            return content;
        } finally {
            if (is != null){
                try {
                    is.close();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
