package com.hawk.funday.ui.fragment.posts;

import android.text.TextUtils;

import com.hawk.funday.R;
import com.hawk.funday.base.Consts;
import com.hawk.funday.support.paging.PostTimelinePaging;
import com.hawk.funday.support.sdk.FundaySDK;
import com.hawk.funday.support.sdk.bean.PostBean;
import com.hawk.funday.support.sdk.bean.PostsBean;
import com.tma.analytics.TmaAgent;

import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.paging.IPaging;

import java.util.List;

/**
 * New列表
 *
 * Created by wangdan on 16/8/26.
 */
public class NewListFragment extends APostListFragment {

    public static NewListFragment newInstance() {
        return new NewListFragment();
    }

    @Override
    protected void setupRefreshConfig(RefreshConfig config) {
        super.setupRefreshConfig(config);

        config.positionKey = "MainNewPost";
    }

    @Override
    protected int favDrawableResId() {
        return R.drawable.ic_funday_homepage_likes_selected_new;
    }

    @Override
    protected IPaging<PostBean, PostsBean> newPaging() {
        return new PostTimelinePaging();
    }

    @Override
    public void requestData(RefreshMode refreshMode) {
        new NewTask(refreshMode).execute();

        if (refreshMode == RefreshMode.refresh) {
            TmaAgent.onEvent(getActivity(), Consts.Event.Event_post_new_refresh);
        }
        else if (refreshMode == RefreshMode.update) {
            TmaAgent.onEvent(getActivity(), Consts.Event.Event_post_new_loadmore);
        }
    }

    class NewTask extends APostsTask {

        public NewTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected PostsBean workInBackground(RefreshMode refreshMode, String prePage, String nextPage, Void... voids) throws TaskException {
            long topId = 0;
            long bottomId = 0;

            if (mode == RefreshMode.refresh && !TextUtils.isEmpty(prePage)) {
                topId = Long.parseLong(prePage);
            }
            else if (mode == RefreshMode.update && !TextUtils.isEmpty(nextPage)) {
                bottomId = Long.parseLong(nextPage);
            }

            PostsBean result = FundaySDK.newInstance(getTaskCacheMode(this)).getNew(topId, bottomId);
            result.setEndPaging(result.getResources().size() == 0 && mode == RefreshMode.update);
            return result;
        }

        @Override
        protected List<PostBean> parseResult(PostsBean postsResultBean) {
            if (mode == RefreshMode.refresh && postsResultBean.getResources().size() == postsResultBean.getPageSize()){
                getAdapter().getDatas().clear();
            }
            return super.parseResult(postsResultBean);
        }

    }

}
