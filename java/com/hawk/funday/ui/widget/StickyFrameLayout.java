package com.hawk.funday.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by wangdan on 16/9/21.
 */
public class StickyFrameLayout extends FrameLayout {

    public StickyFrameLayout(Context context) {
        super(context);
    }

    public StickyFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StickyFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        try {
            super.onLayout(changed, left, top, right, bottom);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}
