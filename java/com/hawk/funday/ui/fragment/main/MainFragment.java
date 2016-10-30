package com.hawk.funday.ui.fragment.main;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hawk.funday.R;
import com.hawk.funday.base.Consts;
import com.hawk.funday.ui.activity.base.OnMainFloatingCallback;
import com.hawk.funday.ui.fragment.base.AFundayTabsFragment;
import com.hawk.funday.ui.fragment.posts.FeaturedListFragment;
import com.hawk.funday.ui.fragment.posts.NewListFragment;
import com.hawk.funday.ui.fragment.posts.VideoListFragment;

import org.aisen.android.support.bean.TabItem;

import java.util.ArrayList;

/**
 * 首页
 *
 * Created by wangdan on 16/8/17.
 */
public class MainFragment extends AFundayTabsFragment {

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    private OnMainFloatingCallback mainFloatingCallback;

    @Override
    public int inflateContentView() {
        return R.layout.ui_fg_main_pager;
    }

    @Override
    protected String configLastPositionKey() {
        return "MainPostTabs";
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() instanceof OnMainFloatingCallback) {
            mainFloatingCallback = (OnMainFloatingCallback) getActivity();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setTabLayout((TabLayout) getActivity().findViewById(R.id.tabLayout));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void setupTabLayout(Bundle savedInstanceSate, TabLayout tabLayout) {
        super.setupTabLayout(savedInstanceSate, tabLayout);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        getViewPager().setOffscreenPageLimit(3);

        setTabTextColor(getViewPager().getCurrentItem());
    }

    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);
        setTabTextColor(position);

        if (mainFloatingCallback != null) {
            mainFloatingCallback.onShow();
        }
    }

    @Override
    protected String tab2page(TabItem tabItem) {
        if ("0".equals(tabItem.getType())) {
            return Consts.Page.Page_main_featured;
        }
        else if ("1".equals(tabItem.getType())) {
            return Consts.Page.Page_main_new;
        }
        else if ("2".equals(tabItem.getType())) {
            return Consts.Page.Page_main_video;
        }

        return Consts.Page.Page_main_new;
    }

    private Handler mHandler = new Handler();

    private void setTabTextColor(int index) {
        int color = getResources().getColor(R.color.comm_white);
        int[] selectedArr = new int[]{ R.color.tab_text_featured_selected, R.color.tab_text_new_selected, R.color.tab_text_video_selected };
        final int selectedColor = getResources().getColor(selectedArr[index]);
        getTablayout().setTabTextColors(color, selectedColor);

        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                getTablayout().setSelectedTabIndicatorColor(selectedColor);
            }

        }, 200);
    }

    @Override
    protected ArrayList<TabItem> generateTabs() {
        ArrayList<TabItem> tabItems = new ArrayList<>();

        tabItems.add(new TabItem("0", getString(R.string.tab_featured)));
        tabItems.add(new TabItem("1", getString(R.string.tab_new)));
        tabItems.add(new TabItem("2", getString(R.string.tab_video)));

        return tabItems;
    }

    @Override
    protected Fragment newFragment(TabItem tabItem) {
        if ("0".equals(tabItem.getType())) {
            return FeaturedListFragment.newInstance();
        }
        else if ("1".equals(tabItem.getType())) {
            return NewListFragment.newInstance();
        }
        else if ("2".equals(tabItem.getType())) {
            return VideoListFragment.newInstance();
        }

        return null;
    }

}
