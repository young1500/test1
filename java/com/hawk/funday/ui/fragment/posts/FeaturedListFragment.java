package com.hawk.funday.ui.fragment.posts;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hawk.funday.R;
import com.hawk.funday.base.Consts;
import com.hawk.funday.support.paging.FeaturePaging;
import com.hawk.funday.support.sdk.FundaySDK;
import com.hawk.funday.support.sdk.bean.PostBean;
import com.hawk.funday.support.sdk.bean.PostsBean;
import com.tma.analytics.TmaAgent;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.support.paging.IPaging;

import java.util.List;

/**
 * Featured列表
 *
 * Created by wangdan on 16/8/17.
 */
public class FeaturedListFragment extends APostListFragment {

    private static final String TAG = "FeaturedListFragment";

    public static FeaturedListFragment newInstance() {
        return new FeaturedListFragment();
    }

    @ViewInject (id = R.id.lay_funbar)
    LinearLayout mFunbarLy;
    @ViewInject (id = R.id.funbar_tv)
    TextView mFunBarMsg;
    @ViewInject (id = R.id.funbar_iv)
    ImageView mFunBarDismiss;
    @ViewInject (id = R.id.funbar_cancel_ly)
    RelativeLayout mFunbarCancelLy;

    private PostsBean postsBean;
    private int nextTimeToastCount = 0;

    @Override
    public int inflateContentView() {
        return R.layout.ui_fg_featured;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        mFunbarCancelLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (postsBean != null) {
                    dismissFunbar();
                }
            }
        });
    }

    @Override
    protected void setupRefreshConfig(RefreshConfig config) {
        super.setupRefreshConfig(config);

        config.positionKey = "MainFeaturePost";
    }

    @Override
    protected int favDrawableResId() {
        return R.drawable.ic_funday_homepage_likes_selected_feature;
    }

    @Override
    public void requestData(RefreshMode refreshMode) {
        new FeaturedTask(refreshMode).execute();

        if (refreshMode == RefreshMode.refresh) {
            TmaAgent.onEvent(getActivity(), Consts.Event.Event_post_featured_refresh);
        }
        else if (refreshMode == RefreshMode.update) {
            TmaAgent.onEvent(getActivity(), Consts.Event.Event_post_featured_loadmore);
        }
    }

    @Override
    protected IPaging<PostBean, PostsBean> newPaging() {
        return new FeaturePaging();
    }

    class FeaturedTask extends APostsTask {

        public FeaturedTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected PostsBean workInBackground(RefreshMode refreshMode, String prePage, String nextPage, Void... voids) throws TaskException {

            String direction;

            if (mode == RefreshMode.update){
                direction = "UP";
            } else {
                direction = "DOWN";
            }

            String refreshId;
            if (mode == RefreshMode.update && !TextUtils.isEmpty(nextPage)) {
                refreshId = nextPage;
            } else if (mode == RefreshMode.refresh && !TextUtils.isEmpty(nextPage)){
                refreshId = prePage;
            } else {
                refreshId = prePage;
            }

            PostsBean result = FundaySDK.newInstance(getTaskCacheMode(this)).getFeatured(refreshId, direction);

            FeaturePaging paging = (FeaturePaging) getPaging();

            if (result.fromCache()) {
                String[] pageIndex = result.getPagingIndex();

                paging.setDownRefreshId(pageIndex[0]);
                paging.setUpRefreshId(pageIndex[1]);
            }
            else {
                if (mode == RefreshMode.reset) {//保存refreshId
                    paging.setDownRefreshId(result.getRefreshId());
                    paging.setUpRefreshId(result.getRefreshId());
                } else if(mode == RefreshMode.update){
                    if (!"0".equals(result.getRefreshId())) {
                        paging.setUpRefreshId(result.getRefreshId());
                    }
                } else {
                    if (!"0".equals(result.getRefreshId())) {
                        paging.setDownRefreshId(result.getRefreshId());
                    }
                }
            }

            if (result.getOffset() == -1) {
                result.setEndPaging(true);
            }

            return result;
        }

        @Override
        protected List<PostBean> parseResult(PostsBean postsResultBean) {
            if (mode == RefreshMode.refresh && postsResultBean.getInterval() == 0 && postsResultBean.getResources().size() > 0){
                getAdapter().getDatas().clear();
            }
            return super.parseResult(postsResultBean);
        }

        @Override
        protected void onSuccess(PostsBean postsBean) {
            super.onSuccess(postsBean);

            // 显示下次内容更新时间提示
            if(postsBean != null && mode == RefreshMode.refresh && postsBean.getInterval() > 0 && getActivity() != null) {
                showNextToast(postsBean);
            }
        }
    }

    private void showNextToast(final PostsBean postsBean) {
        // 已经显示了下次更新时间Toast
        if (FeaturedListFragment.this.postsBean != null) {
            return;
        }

        FeaturedListFragment.this.postsBean = postsBean;
        nextTimeToastCount = 0;
        // 计数刷新显示时间
        mHandler.removeCallbacks(showNextToastTimeRunnable);
        mHandler.post(showNextToastTimeRunnable);

        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, -mFunbarLy.getHeight(), 0);
        translateAnimation.setDuration(500);
        mFunbarLy.startAnimation(translateAnimation);
        mFunbarLy.setVisibility(View.VISIBLE);
    }

    private Handler mHandler = new Handler();
    Runnable showNextToastTimeRunnable = new Runnable() {

        @Override
        public void run() {
            if (postsBean == null) {
                return;
            }

            if (getActivity() != null) {
                mFunBarMsg.setText(getActivity().getResources().getString(R.string.show_interval) + getNextTime(postsBean.getInterval()));
            }

            if (nextTimeToastCount++ < 3) {
                postsBean.setInterval(postsBean.getInterval() - 1000);

                mHandler.removeCallbacks(showNextToastTimeRunnable);
                mHandler.postDelayed(showNextToastTimeRunnable, 1000);
            }
            else {
                dismissFunbar();
            }
        }

    };

    private void dismissFunbar() {
        nextTimeToastCount = Integer.MAX_VALUE;
        mHandler.removeCallbacks(showNextToastTimeRunnable);
        postsBean = null;

        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, -mFunbarLy.getHeight());
        translateAnimation.setDuration(500);
        mFunbarLy.startAnimation(translateAnimation);
        mFunbarLy.setVisibility(View.INVISIBLE);
    }

    private String getNextTime(long interval) {
        long hh = 0;
        long mm = 0;
        long ss = 0;
        Logger.d(TAG, "Next Funny time will come in: " + interval);
        if (interval - 1000 * 60 * 60 > 0) {
            hh = interval / (60 * 60 * 1000);
        }
        if (interval - 1000 * 60 > 0) {
            mm = (interval % (60 * 60 * 1000)) / (60 * 1000);
        }
        if (interval - 1000 > 0) {
            ss = ((interval % (60 * 60 * 1000)) % (60 * 1000)) / 1000;
        }

        StringBuffer sb = new StringBuffer();

        // 00:00:00格式拼接时间
        if (hh == 0) {
            sb.append("00");
        }
        else if (hh > 9) {
            sb.append(hh);
        }
        else {
            sb.append("0").append(hh);
        }

        sb.append(":");

        if (mm == 0) {
            sb.append("00");
        }
        else if (mm > 9) {
            sb.append(mm);
        }
        else {
            sb.append("0").append(mm);
        }

        sb.append(":");

        if (ss == 0) {
            sb.append("00");
        }
        else if (ss > 9) {
            sb.append(ss);
        }
        else {
            sb.append("0").append(ss);
        }

        return " " + sb.toString();
    }

}
