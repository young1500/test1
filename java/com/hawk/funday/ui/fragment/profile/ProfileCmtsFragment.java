package com.hawk.funday.ui.fragment.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.hawk.funday.R;
import com.hawk.funday.base.Consts;
import com.hawk.funday.support.eventbus.CommentEvent;
import com.hawk.funday.support.eventbus.ICommentSubscriber;
import com.hawk.funday.support.eventbus.IPostFavoriteSubscriber;
import com.hawk.funday.support.eventbus.PostFavoriteEvent;
import com.hawk.funday.support.paging.OffsetPaging;
import com.hawk.funday.support.sdk.FundaySDK;
import com.hawk.funday.support.sdk.bean.CommentBean;
import com.hawk.funday.support.sdk.bean.CommentsBean;
import com.hawk.funday.support.sdk.bean.FundayUserBean;
import com.hawk.funday.support.utils.FundayUtils;
import com.hawk.funday.support.utils.ViewUtils;
import com.hawk.funday.ui.fragment.detail.CommentListFragment;
import com.wcc.framework.notification.NotificationCenter;

import org.aisen.android.network.biz.ABizLogic;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.fragment.ARecycleViewSwipeRefreshFragment;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;

import java.io.Serializable;
import java.util.List;

/**
 * 个人发表的评论列表
 *
 * Created by wangdan on 16/8/25.
 */
public class ProfileCmtsFragment extends ARecycleViewSwipeRefreshFragment<CommentBean, CommentsBean, Serializable> {

    public static ProfileCmtsFragment newInstance(FundayUserBean user) {
        Bundle args = new Bundle();
        args.putSerializable("user", user);

        ProfileCmtsFragment fragment = new ProfileCmtsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private FundayUserBean mUser;
    private CommentsBean cacheCommentsBean;

    @Override
    protected void setupSwipeRefreshLayout() {
        super.setupSwipeRefreshLayout();

        ViewUtils.setupSwipeRefreshLayout(getSwipeRefreshLayout());
    }

    @Override
    protected IItemViewCreator<CommentBean> configFooterViewCreator() {
        return ViewUtils.configFooterViewCreator(getActivity(), this);
    }

    @Override
    public int inflateContentView() {
        return R.layout.ui_fg_profile_cmts;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = savedInstanceState == null ? (FundayUserBean) getArguments().getSerializable("user")
                                           : (FundayUserBean) savedInstanceState.getSerializable("user");

        NotificationCenter.defaultCenter().subscriber(CommentEvent.class, commentSubscriber);
        NotificationCenter.defaultCenter().subscriber(PostFavoriteEvent.class, postFavoriteSubscriber);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        NotificationCenter.defaultCenter().unsubscribe(CommentEvent.class, commentSubscriber);
        NotificationCenter.defaultCenter().unsubscribe(PostFavoriteEvent.class, postFavoriteSubscriber);
    }

    IPostFavoriteSubscriber postFavoriteSubscriber = new IPostFavoriteSubscriber() {

        @Override
        public void onEvent(PostFavoriteEvent event) {
            for (int i = 0; i < getAdapterItems().size(); i++) {
                if (getAdapterItems().get(i).getResource().getResourceId() == event.getTag().getResourceId()) {
                    getAdapterItems().get(i).getResource().setFavorite(event.isFav());

                    break;
                }
            }
        }

    };

    ICommentSubscriber commentSubscriber = new ICommentSubscriber() {

        @Override
        public void onEvent(CommentEvent event) {
            // 删除评论
            if (FundayUtils.removeCommentStatus(getAdapterItems(), event.getComment())) {
                getAdapter().notifyDataSetChanged();
            }

            // 修改评论数
            for (int i = 0; i < getAdapterItems().size(); i++) {
                if (getAdapterItems().get(i).getResource().getResourceId() == event.getPost().getResourceId()) {
                    getAdapterItems().get(i).getResource().setCommentCount(event.getPost().getCommentCount());

                    break;
                }
            }
        }

    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("user", mUser);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);

        if (position < getAdapterItems().size()) {
            CommentListFragment.launch(getActivity(), getAdapterItems().get(position).getResource(), false);
        }
    }

    @Override
    public IItemViewCreator configItemViewCreator() {
        return new IItemViewCreator() {
            @Override
            public View newContentView(LayoutInflater layoutInflater, ViewGroup viewGroup, int i) {
                return layoutInflater.inflate(R.layout.item_profile_cmt, viewGroup, false);
            }

            @Override
            public IITemView newItemView(View view, int i) {
                return new ProfileCmtItemView(getActivity(), view);
            }

        };
    }

    @Override
    protected IPaging<CommentBean, CommentsBean> newPaging() {
        return new OffsetPaging<>();
    }

    @Override
    public void requestData(RefreshMode refreshMode) {
        new ProfileCmtTask(refreshMode != RefreshMode.update ? RefreshMode.reset : RefreshMode.update).execute();
    }

    class ProfileCmtTask extends APagingTask<Void, Void, CommentsBean> {

        public ProfileCmtTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected List<CommentBean> parseResult(CommentsBean commentsBean) {
            return commentsBean.getComments();
        }

        @Override
        protected CommentsBean workInBackground(RefreshMode refreshMode, String s, String s1, Void... voids) throws TaskException {
            int offset = 0;

            if (!TextUtils.isEmpty(s1)) {
                offset = Integer.parseInt(s1);
            }

            ABizLogic.CacheMode cacheMode = FundayUtils.isLoginedUser(mUser.getId()) ? getTaskCacheMode(this) : ABizLogic.CacheMode.disable;
            CommentsBean beans = FundaySDK.newInstance().getProfileCmts(mUser.getId(), offset);
            if (beans != null && beans.isFromCache()) {
                cacheCommentsBean = beans;
            }

            if (beans != null) {
                beans.setEndPaging(beans.getOffset() == -1);
            }
            return beans;
        }

        @Override
        protected void onSuccess(CommentsBean commentsBean) {
            super.onSuccess(commentsBean);

            if (!commentsBean.isFromCache() && cacheCommentsBean != null) {
                for (CommentBean commentBean : cacheCommentsBean.getComments()) {
                    getAdapterItems().remove(commentBean);
                }
                getAdapter().notifyDataSetChanged();
            }

        }
    }

    @Override
    public boolean onToolbarDoubleClick() {
        if (FundayUtils.checkTabsFragmentCanRequestData(this)) {
            getRefreshView().scrollToPosition(0);
            requestDataDelaySetRefreshing(Consts.REQUEST_DATA_DELAY);

            return true;
        }

        return false;
    }

}
