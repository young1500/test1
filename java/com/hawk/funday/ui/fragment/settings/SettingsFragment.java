package com.hawk.funday.ui.fragment.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hawk.funday.R;
import com.hawk.funday.base.AppContext;
import com.hawk.funday.base.Consts;
import com.hawk.funday.support.utils.TokenUtils;
import com.hawk.funday.ui.activity.base.ContainerActivity;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;

/**
 * @author Liyang Sun
 * @Description:
 * @date 2016/8/24 17:05
 * @copyright HAWK
 */

public class SettingsFragment extends ABaseFragment implements View.OnClickListener {
    @ViewInject(id = R.id.follow_fb)
    TextView mFollowFb;
    @ViewInject(id = R.id.follow_insta)
    TextView mFollowInsta;
    @ViewInject(id = R.id.feedback)
    TextView mFeedback;
    @ViewInject(id = R.id.about_funday)
    TextView mAboutFunday;
    @ViewInject(id = R.id.log_out)
    TextView mLogOut;

    private static final String TAG = "SettingsFragment";
    private TokenUtils mTokenUtils;

    public static void launch(Activity from) {
        ContainerActivity.launch(from, SettingsFragment.class, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(false);
        activity.getSupportActionBar().setTitle("");
    }

    @Override
    public int inflateContentView() {
        return R.layout.ui_fg_settings;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        mTokenUtils = new TokenUtils(getActivity());
        if (AppContext.getLoginedAccount() != null &&
                AppContext.getLoginedAccount().getUser() != null &&
                    !TextUtils.isEmpty(AppContext.getLoginedAccount().getUser().getName())) {
            mLogOut.setVisibility(View.VISIBLE);
            Logger.d(TAG + "_Account", "User has Logined, Logout Item VISIBLE");
        }

        mFollowFb.setOnClickListener(this);
        mFollowInsta.setOnClickListener(this);
        mFeedback.setOnClickListener(this);
        mAboutFunday.setOnClickListener(this);
        mLogOut.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.follow_fb:
                Uri uriFb = Uri.parse(Consts.FB_URL);
                Intent intentFb = new Intent(Intent.ACTION_VIEW, uriFb);
                startActivity(intentFb);
                break;
            case R.id.follow_insta:
                Uri uriInsta = Uri.parse(Consts.INSTA_URL);
                Intent intentInsta = new Intent(Intent.ACTION_VIEW, uriInsta);
                startActivity(intentInsta);
                break;
            case R.id.feedback:
                FeedbackFragment.launch(getActivity());
                break;
            case R.id.about_funday:
//                MainStats.logClick(StatEvent.SettingsParams.ABOUT, new Bundle());
                AboutFundayFragment.launch(getActivity());
                break;
            case R.id.log_out:
                logout();
                break;

            default:
                break;
        }
    }

    private void logout() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.dialog_logout_title)
                .positiveText(R.string.dialog_logout_ok)
                .negativeText(R.string.dialog_logout_cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        new TokenUtils(getActivity()).clearLoginInfo();
                        mLogOut.setVisibility(View.GONE);
                        Logger.d(TAG + "_Account", "User Logout :(");
                    }
                }).show();
    }
}
