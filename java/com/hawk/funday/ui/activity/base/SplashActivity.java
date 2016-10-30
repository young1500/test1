package com.hawk.funday.ui.activity.base;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatDelegate;

import com.hawk.funday.R;

import org.aisen.android.ui.activity.basic.BaseActivity;

/**
 * 欢迎页
 *
 * Created by wangdan on 16/8/17.
 */
public class SplashActivity extends BaseActivity {

    private static final int GO_MAIN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.ui_splash);
        if (!SplashActivity.this.isTaskRoot()) {//此段代码用来解决用手势识别打开程序会启动另一个该程序(SplashActivity的LauncherMode=singleTask)
            finish();
            return;
        }
        mHandler.sendEmptyMessageDelayed(GO_MAIN, 1500);
    }

    private void toMain() {
        MainActivity.launch(this);

        finish();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case GO_MAIN:
                    toMain();
                    break;
                default:
                    break;
            }
        }
    };

}
