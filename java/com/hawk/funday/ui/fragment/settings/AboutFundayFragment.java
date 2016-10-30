package com.hawk.funday.ui.fragment.settings;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hawk.funday.BuildConfig;
import com.hawk.funday.R;
import com.hawk.funday.support.utils.LongClickUtils;
import com.hawk.funday.ui.activity.base.ContainerActivity;

import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;

/**
 * @author Liyang Sun
 * @Description:
 * @date 2016/8/24 20:40
 * @copyright HAWK
 */

public class AboutFundayFragment extends ABaseFragment {
    @ViewInject (id = R.id.version_name)
    TextView mVersionName;
    @ViewInject (id = R.id.funday_logo)
    ImageView mLogo;

    public static void launch (Activity from) {
        ContainerActivity.launch(from, AboutFundayFragment.class, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(false);
        activity.getSupportActionBar().setTitle(R.string.title_about);
    }

    @Override
    public boolean onBackClick() {
        return super.onBackClick();
    }

    @Override
    public boolean onHomeClick() {
        return super.onHomeClick();
    }

    @Override
    public int inflateContentView() {
        return R.layout.ui_fg_about;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        String versionName = SystemUtils.getVersionName(getActivity());
        mVersionName.setText(versionName);

        mLogo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    final CharSequence log = BuildConfig.BUILD_TYPE + BuildConfig.VERSION_NAME + BuildConfig.VERSION_CODE;
                    if (BuildConfig.LOG_DEBUG) {
                        showMessage(log);
                    }

                    LongClickUtils.setLongClick(new Handler(), mLogo, 3000, new View.OnLongClickListener() {

                        @Override
                        public boolean onLongClick(View v) {
                            showMessage(log);
                            return true;
                        }
                    });
                }catch (Exception e) {
                        e.printStackTrace();
                }
            }
        });
    }
}
