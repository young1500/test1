package com.hawk.funday.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * Created by wangdan on 16/8/24.
 */
public class PostWebView extends WebView {

    public PostWebView(Context context) {
        super(context);
    }

    public PostWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PostWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
