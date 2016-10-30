package com.hawk.funday.support.utils;

import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * @author Liyang Sun
 * @Description:
 * @date 2016/9/18 17:13
 * @copyright HAWK
 */
public class SnackbarUtil {
    public static void showShortSnackbar(View view, CharSequence msg) {
        Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
    }

    public static void showShortSnackbar(View view, int msgId) {
        Snackbar.make(view, msgId, Snackbar.LENGTH_SHORT).show();
    }

    public static void showLongSnackbar(View view, CharSequence msg) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
    }

    public static void showLongSnackbar(View view, int msgId) {
        Snackbar.make(view, msgId, Snackbar.LENGTH_LONG).show();
    }
}
