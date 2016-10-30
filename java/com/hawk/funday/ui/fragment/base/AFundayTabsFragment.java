package com.hawk.funday.ui.fragment.base;

import android.os.Bundle;
import android.support.design.widget.TabLayout;

import com.tma.analytics.TmaAgent;

import org.aisen.android.support.bean.TabItem;
import org.aisen.android.ui.fragment.ATabsTabLayoutFragment;
import org.aisen.android.ui.widget.AsToolbar;

/**
 * Created by wangdan on 16/9/12.
 */
public abstract class AFundayTabsFragment extends ATabsTabLayoutFragment<TabItem> implements TabLayout.OnTabSelectedListener {

    private long lastClickTime = 0;

    private TabItem selectedItem;

    @Override
    protected void setupTabLayout(Bundle savedInstanceSate, TabLayout tabLayout) {
        super.setupTabLayout(savedInstanceSate, tabLayout);

        tabLayout.addOnTabSelectedListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getTabItems().size() > getViewPager().getCurrentItem()) {
            onPageStart(getTabItems().get(getViewPager().getCurrentItem()));
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (getTabItems().size() > getViewPager().getCurrentItem()) {
            onPageEnd(getTabItems().get(getViewPager().getCurrentItem()));
        }
    }

    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);

        if (selectedItem != null) {
            onPageEnd(selectedItem);
        }

        onPageStart(getTabItems().get(position));
    }

    protected abstract String tab2page(TabItem tabItem);

    private void onPageStart(TabItem item) {
        selectedItem = item;

        TmaAgent.onPageStart(tab2page(item));
        TmaAgent.onResume(getActivity());
    }

    private void onPageEnd(TabItem item) {
        TmaAgent.onPageEnd(tab2page(item));
        TmaAgent.onPause(getActivity());
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        if (lastClickTime != 0) {
            if (System.currentTimeMillis() - lastClickTime <= 500) {
                performDoublcClick();
            }
        }

        lastClickTime = System.currentTimeMillis();
    }

    protected void performDoublcClick() {
        if (getActivity() instanceof AsToolbar.OnToolbarDoubleClick)
            ((AsToolbar.OnToolbarDoubleClick) getActivity()).onToolbarDoubleClick();
    }

}
