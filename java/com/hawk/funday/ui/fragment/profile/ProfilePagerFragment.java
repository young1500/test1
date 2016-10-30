package com.hawk.funday.ui.fragment.profile;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hawk.funday.R;
import com.hawk.funday.base.AppContext;
import com.hawk.funday.base.Consts;
import com.hawk.funday.base.PageDeepManager;
import com.hawk.funday.component.imageloader.core.DisplayImageOptions;
import com.hawk.funday.component.imageloader.core.ImageLoader;
import com.hawk.funday.support.sdk.bean.AccountBean;
import com.hawk.funday.support.sdk.bean.FundayUserBean;
import com.hawk.funday.support.utils.FundayUtils;
import com.hawk.funday.ui.activity.base.ContainerActivity;
import com.hawk.funday.ui.activity.base.MainActivity;
import com.hawk.funday.ui.fragment.base.AFundayTabsFragment;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.support.bean.TabItem;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.activity.container.FragmentArgs;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * 个人首页
 *
 * Created by wangdan on 16/8/22.
 */
public class ProfilePagerFragment extends AFundayTabsFragment {

    @ViewInject (id = R.id.profile_user_avatar)
    ImageView mUserAvatar;
    @ViewInject (id = R.id.profile_user_name)
    TextView mUserName;
    @ViewInject (id = R.id.profile_detail_edit_icon)
    ImageView mEditIcon;
    @ViewInject (id = R.id.profile_detail_user_name_ly)
    LinearLayout mUserNameLy;
    @ViewInject (id = R.id.avadar_background)
    ImageView mAvadarBg;

    private static final String TAG = "ProfilePagerFragment";

    public static void launch(Activity from, FundayUserBean user) {
        FragmentArgs args = new FragmentArgs();
        args.add("user", user);

        ContainerActivity.launch(from, ProfilePagerFragment.class, args);
    }

    private FundayUserBean mUser;

    @Override
    public int inflateContentView() {
        return R.layout.ui_fg_profile;
    }

    @Override
    protected void setupTabLayout(Bundle savedInstanceSate, TabLayout tabLayout) {
        super.setupTabLayout(savedInstanceSate, tabLayout);

        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        getViewPager().setOffscreenPageLimit(3);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = savedInstanceState == null ? (FundayUserBean) getArguments().getSerializable("user")
                                           : (FundayUserBean) savedInstanceState.getSerializable("user");

        PageDeepManager.addFragment(ProfilePagerFragment.this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PageDeepManager.removeFragment(ProfilePagerFragment.this);
    }

    @Override
    public boolean onBackClick() {
        return super.onBackClick();
    }

    @Override
    public boolean onHomeClick() {
        try {
            if (FundayUtils.isLoginedUser(mUser.getId())) {
                if (getActivity() != null) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    ComponentName componentName = intent.resolveActivity(getActivity().getPackageManager());
                    if (componentName != null) {

                        ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE);
                        List<ActivityManager.RunningTaskInfo> taskInfoList = activityManager.getRunningTasks(10);
                        List<ComponentName> componentNameList = new ArrayList<>();
                        for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
                            componentNameList.add(taskInfo.baseActivity);
                        }
                        if (componentNameList.contains(componentName)) {
                            return super.onHomeClick();
                        } else {
                            startActivity(intent);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.onHomeClick();
    }

    @Override
    protected void setupContentView(LayoutInflater inflater, ViewGroup contentView, Bundle savedInstanceState) {
        super.setupContentView(inflater, contentView, savedInstanceState);

        contentView.setBackgroundColor(Color.WHITE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(false);
        activity.getSupportActionBar().setTitle(R.string.title_profile);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("user", mUser);
    }

    @Override
    protected ArrayList<TabItem> generateTabs() {
        ArrayList<TabItem> tabItems = new ArrayList<>();

        tabItems.add(new TabItem("0", getString(R.string.tab_profile_posts)));
        tabItems.add(new TabItem("1", getString(R.string.tab_profile_comments)));
        tabItems.add(new TabItem("2", getString(R.string.tab_profile_favorite)));

        return tabItems;
    }

    @Override
    protected Fragment newFragment(TabItem tabItem) {
        switch (Integer.parseInt(tabItem.getType())) {
            case 0:
                return ProfilePostedFragment.newInstance(mUser);
            case 1:
                return ProfileCmtsFragment.newInstance(mUser);
            case 2:
                return ProfileFavFragment.newInstance(mUser);
        }

        return ProfilePostedFragment.newInstance(mUser);
    }

    @Override
    protected String tab2page(TabItem tabItem) {
        switch (Integer.parseInt(tabItem.getType())) {
            case 0:
                return Consts.Page.Page_profile_posted;
            case 1:
                return Consts.Page.Page_profile_comments;
            case 2:
                return Consts.Page.Page_profile_favorites;
        }

        return Consts.Page.Page_profile_posted;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceState) {
        super.layoutInit(inflater, savedInstanceState);

//        mEditIcon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MainStats.logClick(StatEvent.ProfilePagerFragment.CLICK_EDIT_PROFILE_PEN);
//
//                ProfileDetailFragment.launch(getActivity());
//            }
//        });

        mUserAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoginedUser()) {
                    ProfileDetailFragment.launch(getActivity());
                }
            }
        });

        mUserNameLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLoginedUser()) {
                    ProfileDetailFragment.launch(getActivity());
                }
            }
        });

        setAccountLayout();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isUserProfileChanged()) {
            setAccountLayout();
        }
    }

    private boolean isLoginedUser() {
        AccountBean account = AppContext.getLoginedAccount();
        if (account != null) {
            return account.getUser() != null &&
                    mUser.getId() == account.getUser().getId();
        }

        return false;
    }

    private boolean isUserProfileChanged() {
        AccountBean account = AppContext.getLoginedAccount();
        if (account != null && account.getUser() != null) {
            if (!TextUtils.isEmpty(account.getUser().getAvatar()) &&
                    !mUser.getAvatar().equals(account.getUser().getAvatar())) {
                return true;
            }
            if (!TextUtils.isEmpty(account.getUser().getName()) &&
                    !mUser.getName().equals(account.getUser().getName())) {
                return true;
            }
        }

        return false;
    }

    private void setAccountLayout() {
        String userName = "";
        String avatar = null;

        if (mUser != null && mUser.getId() != 0) {
            // 如果用户为已登陆用户，则显示已登录用户详细信息
            if (isLoginedUser() && AppContext.getLoginedAccount().getUser() != null){
                Logger.d(TAG + "_Account", "UserId is: " + mUser.getId());

                avatar = AppContext.getLoginedAccount().getUser().getAvatar();
                userName = AppContext.getLoginedAccount().getUser().getName();
                mUser.setName(userName);
                mUser.setAvatar(avatar);
                mEditIcon.setVisibility(View.VISIBLE);
            } else { // 如果不是已登录用户，则显示传递过来的用户信息
                userName = mUser.getName();
                avatar = mUser.getAvatar();
                Logger.d(TAG + "_Account", "UserId is: " + mUser.getId());
            }
        }

        mUserName.setText(!TextUtils.isEmpty(userName) ? userName : "");
        if (!TextUtils.isEmpty(avatar)) {
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .showImageOnLoading(getResources().getDrawable(R.mipmap.ic_funday_profile_pager_head))
                    .build();
            ImageLoader.getInstance().displayImage(avatar, mUserAvatar, options);
            ImageLoader.getInstance().displayImage(avatar, mAvadarBg, options);
        }
        else {
            mUserAvatar.setImageResource(R.mipmap.ic_funday_profile_pager_head);
            mAvadarBg.setImageResource(R.mipmap.ic_funday_profile_pager_head);
        }
    }
}
