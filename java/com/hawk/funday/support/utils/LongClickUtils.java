package com.hawk.funday.support.utils;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;

/**
 * @author liyang.sun
 * @Description:
 * @date 2016/4/12 16:04
 * @copyright TCL-MIE
 */
public class LongClickUtils {

    public static void setLongClick(final Handler handler,
                                    final View longClickView, final long delayMillis,
                                    final OnLongClickListener longClickListener) {
        longClickView.setOnTouchListener(new OnTouchListener() {
            private int TOUCH_MAX = 50;
            private int mLastMotionX;
            private int mLastMotionY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        handler.removeCallbacks(r);
                        break;
                    case MotionEvent.ACTION_DOWN:
                        handler.removeCallbacks(r);
                        handler.postDelayed(r, delayMillis);
                        break;
                }
                return false;
            }

            private Runnable r = new Runnable() {

                @Override
                public void run() {
                    if (longClickListener != null) {
                        longClickListener.onLongClick(longClickView);
                    }
                }
            };
        });
    }
}