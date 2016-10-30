package com.hawk.funday.ui.activity.base;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hawk.funday.R;
import com.hawk.funday.base.AppContext;
import com.hawk.funday.base.AppSettings;
import com.hawk.funday.base.Consts;
import com.hawk.funday.component.imageloader.core.DisplayImageOptions;
import com.hawk.funday.component.imageloader.core.ImageLoader;
import com.hawk.funday.component.imageloader.core.assist.LoadedFrom;
import com.hawk.funday.component.imageloader.core.display.BitmapDisplayer;
import com.hawk.funday.component.imageloader.core.imageaware.ImageAware;
import com.hawk.funday.support.sdk.FundaySDK;
import com.hawk.funday.support.sdk.bean.AccountBean;
import com.hawk.funday.support.sdk.bean.FundayUserBean;
import com.hawk.funday.support.utils.APILogReportUtils;
import com.hawk.funday.support.utils.AppConfigsUtils;
import com.hawk.funday.support.utils.EmailUtils;
import com.hawk.funday.support.utils.FundayUtils;
import com.hawk.funday.support.utils.TokenUtils;
import com.hawk.funday.support.utils.UUIDUtils;
import com.hawk.funday.ui.fragment.base.BizFragment;
import com.hawk.funday.ui.fragment.main.MainFragment;
import com.hawk.funday.ui.fragment.posts.PostFragment;
import com.hawk.funday.ui.fragment.profile.ProfilePagerFragment;
import com.hawk.funday.ui.fragment.profile.RegisterProfileEditFragment;
import com.hawk.funday.ui.fragment.settings.AboutFundayFragment;
import com.hawk.funday.ui.fragment.settings.FeedbackFragment;
import com.tma.analytics.TmaAgent;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;

/**
 * 首页
 *
 * Created by wangdan on 16/8/17.
 */
public class MainActivity extends BaseActivity implements OnMainFloatingCallback {

    private static String TAG = "MainActivity";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @ViewInject(id = R.id.btn_add)
    FloatingActionButton mAddBtn; ///编辑上传内容按钮
    @ViewInject(id = R.id.imgAvatar)
    ImageView imgAvatar;

    private BizFragment bizFragment;

    public static void launch(Activity from) {
        from.startActivity(new Intent(from, MainActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_main);

        AppConfigsUtils.loadAppConfigs();
        APILogReportUtils.doRepost();

        TmaAgent.onEvent(this, Consts.Event.Event_main_launch);

        attchEvent();

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
//        getSupportActionBar().setLogo(android.R.drawable.title_bar);
        getSupportActionBar().setTitle(R.string.app_name);
//        getSupportActionBar().setTitle("");

        bizFragment = BizFragment.createBizFragment(this);
        bizFragment.createFabAnimator(mAddBtn);
        bizFragment.getFabAnimator().setDuration(200);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().replace(R.id.fragmentContainer, MainFragment.newInstance(), "MainFragment").commit();
        }

        requestUUID();
        verifyAccount(); // 检查登陆是否有效

        findViewById(R.id.layAvatar).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TmaAgent.onEvent(MainActivity.this, Consts.Event.Event_profile_main_click);

                onLogin();
            }

        });

        reSetVideoTips();//重启播放视频会弹出流量提示
    }

    /** 当UUID为空，或者UUID为0000000000时请求UUID
       当AID和DID同时为空时，服务器UUID返回0000000000
       当AID和DID只有一个为空时，服务器可以返回UUID */
    private void requestUUID() {
        if (TextUtils.isEmpty(UUIDUtils.getUUID(MainActivity.this))) {
            Logger.d(TAG, "UUIDUtils.getUUID() is null, request UUID");
            UUIDUtils.requestUUID(MainActivity.this); // 请求UUID
        } else if (UUIDUtils.getUUID(MainActivity.this).equals("0000000000")) {
            Logger.d(TAG, "UUIDUtils.getUUID() is 0000000000, request UUID");
            UUIDUtils.requestUUID(MainActivity.this); // 请求UUID
        }
    }

    // 检查登陆是否有效
    private void verifyAccount() {
        if (AppContext.getLoginedAccount() == null) {
            return;
        }

        final String openId = AppContext.getLoginedAccount().getOpenId();
        final String token = AppContext.getLoginedAccount().getToken();

        new WorkTask<Void, Void, Integer>() {

            @Override
            public Integer workInBackground(Void... voids) throws TaskException {
                FundayUserBean fundayUserBean = FundaySDK.newInstance().getUserByToken(openId, token);
                return fundayUserBean.getCode();
            }

            @Override
            protected void onSuccess(Integer integer) {
                super.onSuccess(integer);

                if (integer == 200) {
                    Logger.d(TAG + "_Account", "Verify Account Success");
                } else {
                    new TokenUtils(MainActivity.this).clearLoginInfo();
                    Logger.d(TAG + "_Account", "Verify Account Failed, Logout Account, Result Code: " + String.valueOf(integer));
                }

                showUserAvatar();
            }

        }.execute();
    }

    private void attchEvent(){
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BizFragment.createBizFragment(MainActivity.this).checkUserPermission(new BizFragment.OnUserPermissionCallback() {
                    @Override
                    public void onSuccess(AccountBean account) {
                        TmaAgent.onEvent(MainActivity.this, Consts.Event.Event_upload_btn_click);

                        PostFragment.launch(MainActivity.this);
                    }

                    @Override
                    public void onFaild() {

                    }
                });

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        showUserAvatar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.logout).setVisible(AppContext.getLoginedAccount() != null);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fbFollow:
                TmaAgent.onEvent(this, Consts.Event.Event_settings_facebook_click);

                Uri uriFb = Uri.parse(Consts.FB_URL);
                Intent intentFb = new Intent(Intent.ACTION_VIEW, uriFb);
                startActivity(intentFb);
                break;
            case R.id.instaFollow:
                TmaAgent.onEvent(this, Consts.Event.Event_settings_instagram_click);

                Uri uriInsta = Uri.parse(Consts.INSTA_URL);
                Intent intentInsta = new Intent(Intent.ACTION_VIEW, uriInsta);
                startActivity(intentInsta);
                break;
            case R.id.feedback:
                TmaAgent.onEvent(this, Consts.Event.Event_settings_feedback_click);
                onFeedBack();
                break;
            case R.id.about:
                TmaAgent.onEvent(this, Consts.Event.Event_settings_about_click);
                AboutFundayFragment.launch(this);
                break;
            case R.id.logout:
                onLogout();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    private void onFeedBack(){
        try{
            EmailUtils.sendFeedBackEmail(MainActivity.this,"dailytubeservice@gmail.com","DailyTube Feedback");
        }catch (ActivityNotFoundException acE){
            FeedbackFragment.launch(this);
        } catch (Exception e) {
           Logger.e(TAG,e);
        }
    }
    private void onLogout() {
        new MaterialDialog.Builder(this)
                .title(R.string.dialog_logout_title)
                .positiveText(R.string.dialog_logout_ok)
                .negativeText(R.string.dialog_logout_cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        new TokenUtils(MainActivity.this).clearLoginInfo();

                        TmaAgent.onEvent(MainActivity.this, Consts.Event.Event_profile_logout);

                        showUserAvatar();
                    }

                }).show();
    }

    private void onLogin() {
        BizFragment bizFragment = BizFragment.createBizFragment(this);
        if (bizFragment != null) {
            bizFragment.checkUserPermission(new BizFragment.OnUserPermissionCallback() {

                @Override
                public void onSuccess(AccountBean account) {
                    Logger.d(TAG + "_Account", "Logined, Start ProfilePagerFragment");
                    ProfilePagerFragment.launch(MainActivity.this, account.getUser());
                }

                @Override
                public void onFaild() {
//                    showMessage(R.string.login_failed);
                    Logger.d(TAG + "_Account", "Start ProfilePagerFragment Fail");
                }

            }, true);
        }
    }

    private void showUserAvatar() {
        if (AppContext.getLoginedAccount() != null &&
                AppContext.getLoginedAccount().getUser() != null &&
                !TextUtils.isEmpty(AppContext.getLoginedAccount().getUser().getAvatar())) {
            String userAvatarUrl = AppContext.getLoginedAccount().getUser().getAvatar();

            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .showImageOnLoading(getResources().getDrawable(R.mipmap.ic_funday_default_avadar))
                    .displayer(new BitmapDisplayer() {
                        @Override
                        public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
                            bitmap = FundayUtils.setImageCorner(bitmap, bitmap.getWidth() / 2);
                            ((ImageView) imageAware.getWrappedView()).setImageBitmap(bitmap);
                        }
                    })
                    .build();

            ImageLoader.getInstance().displayImage(userAvatarUrl, imgAvatar, options);
        } else {
            imgAvatar.setImageResource(R.mipmap.ic_funday_default_avadar);
        }
    }

    @Override
    public void onShow() {
        if (bizFragment != null && bizFragment.getFabAnimator() != null) {
            bizFragment.getFabAnimator().show();
        }
    }

    @Override
    public long duration() {
        return 1500;
    }

    private void reSetVideoTips(){
        AppSettings.setFlowRemind(this, true);
    }

    private boolean canFinish = false;

    @Override
    public boolean onBackClick() {
        if (!canFinish) {
            canFinish = true;

            showMessage(R.string.main_hint_exit);

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    canFinish = false;
                }

            }, 1500);

            return true;
        }

        return super.onBackClick();
    }

}
