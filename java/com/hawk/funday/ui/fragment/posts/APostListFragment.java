package com.hawk.funday.ui.fragment.posts;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.hawk.funday.R;
import com.hawk.funday.base.Consts;
import com.hawk.funday.support.eventbus.CommentEvent;
import com.hawk.funday.support.eventbus.ICommentSubscriber;
import com.hawk.funday.support.eventbus.IPostDestorySubscriber;
import com.hawk.funday.support.eventbus.IPostFavoriteSubscriber;
import com.hawk.funday.support.eventbus.PostDestoryEvent;
import com.hawk.funday.support.eventbus.PostFavoriteEvent;
import com.hawk.funday.support.paging.OffsetPaging;
import com.hawk.funday.support.sdk.bean.PostBean;
import com.hawk.funday.support.sdk.bean.PostsBean;
import com.hawk.funday.support.utils.FundayUtils;
import com.hawk.funday.support.utils.ViewUtils;
import com.hawk.funday.ui.activity.base.MainActivity;
import com.hawk.funday.ui.activity.base.OnMainFloatingCallback;
import com.hawk.funday.ui.fragment.base.BizFragment;
import com.hawk.funday.ui.fragment.detail.CommentListFragment;
import com.wcc.framework.notification.NotificationCenter;

import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.fragment.ARecycleViewSwipeRefreshFragment;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.android.ui.widget.AsToolbar;

import java.io.Serializable;
import java.util.List;

/**
 * Posts的基类
 *
 * Created by wangdan on 16/8/26.
 */
public abstract class APostListFragment extends ARecycleViewSwipeRefreshFragment<PostBean, PostsBean, Serializable>
                                            implements AsToolbar.OnToolbarDoubleClick {

    private OnMainFloatingCallback mainFloatingCallback;

    @Override
    public int inflateContentView() {
        return R.layout.ui_recycleview_swiperefresh;
    }

    @Override
    protected void setupSwipeRefreshLayout() {
        super.setupSwipeRefreshLayout();

        ViewUtils.setupSwipeRefreshLayout(getSwipeRefreshLayout());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NotificationCenter.defaultCenter().subscriber(PostFavoriteEvent.class, postFavoriteSubscriber);
        NotificationCenter.defaultCenter().subscriber(CommentEvent.class, postCmtSubscriber);
        NotificationCenter.defaultCenter().subscriber(PostDestoryEvent.class, postDestorySubscriber);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        NotificationCenter.defaultCenter().unsubscribe(PostFavoriteEvent.class, postFavoriteSubscriber);
        NotificationCenter.defaultCenter().unsubscribe(CommentEvent.class, postCmtSubscriber);
        NotificationCenter.defaultCenter().unsubscribe(PostDestoryEvent.class, postDestorySubscriber);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            BizFragment.createBizFragment(getActivity()).getFabAnimator().attachToRecyclerView(getRefreshView(), null, null);
        }

        if (getActivity() instanceof OnMainFloatingCallback) {
            mainFloatingCallback = (OnMainFloatingCallback) getActivity();
        }
    }

    @Override
    protected IItemViewCreator<PostBean> configFooterViewCreator() {
        return ViewUtils.configFooterViewCreator(getActivity(), this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);

        if (position < getAdapterItems().size()) {
            CommentListFragment.launch(getActivity(), getAdapterItems().get(position), false, favDrawableResId());
        }
    }

    protected int favDrawableResId() {
        return R.drawable.ic_funday_homepage_likes_selected_feature;
    }

    @Override
    protected IPaging<PostBean, PostsBean> newPaging() {
        return new OffsetPaging();
    }

    @Override
    public IItemViewCreator<PostBean> configItemViewCreator() {
        return new IItemViewCreator<PostBean>() {

            @Override
            public View newContentView(LayoutInflater layoutInflater, ViewGroup viewGroup, int viewType) {
                return layoutInflater.inflate(R.layout.item_post, viewGroup, false);
            }

            @Override
            public IITemView<PostBean> newItemView(View view, int viewType) {
                return new PostImageItemView(getActivity(), view, APostListFragment.this);
            }

        };
    }

    public abstract class APostsTask extends APagingTask<Void, Void, PostsBean> {

        public APostsTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected List<PostBean> parseResult(PostsBean postsResultBean) {
            return postsResultBean.getResources();
        }

        @Override
        protected void onFailure(TaskException exception) {
            super.onFailure(exception);

            showMessage(exception.getMessage());
        }

    }

    Handler mHandler = new Handler();

    Runnable mShowFloatingRunnable = new Runnable() {

        @Override
        public void run() {
            if (mainFloatingCallback != null) {
                mainFloatingCallback.onShow();
            }
        }

    };

    @Override
    protected void setupRefreshView(Bundle savedInstanceSate) {
        super.setupRefreshView(savedInstanceSate);

        RecyclerView recyclerView = getRefreshView();
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    mHandler.removeCallbacks(mShowFloatingRunnable);
                    if (mainFloatingCallback != null && RecyclerView.SCROLL_STATE_IDLE == newState) {
                        mHandler.postDelayed(mShowFloatingRunnable, mainFloatingCallback.duration());
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int firstCompleted = manager.findFirstVisibleItemPosition();
                    int lastCompleted = manager.findLastVisibleItemPosition();
                    if (firstCompleted == lastCompleted){
                        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(manager.findFirstCompletelyVisibleItemPosition());
                        if (viewHolder instanceof IItemPlayer) {
                            ((IItemPlayer) viewHolder).onPlay();
                        }
                    } else {
                        for (int i = firstCompleted; i<= lastCompleted; i++){
                            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
                            if (viewHolder instanceof IItemPlayer) {
                                ((IItemPlayer) viewHolder).onPlay();
                            }
                        }
                    }

                    //屏幕显示范围以外的释放掉Gif
                    for (int i = 0; i<firstCompleted; i++){
                        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
                        if (viewHolder instanceof IItemPlayer) {
                            ((IItemPlayer) viewHolder).onStop();
                        }
                    }
                    for (int i = lastCompleted; i < manager.getChildCount(); i++){
                        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
                        if (viewHolder instanceof IItemPlayer) {
                            ((IItemPlayer) viewHolder).onStop();
                        }
                    }
                }
            });
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

    IPostDestorySubscriber postDestorySubscriber = new IPostDestorySubscriber() {

        @Override
        public void onEvent(PostDestoryEvent event) {
            if (FundayUtils.postDestory(getAdapterItems(), event.getPost())) {
                getAdapter().notifyDataSetChanged();
            }
        }

    };

    IPostFavoriteSubscriber postFavoriteSubscriber = new IPostFavoriteSubscriber() {

        @Override
        public void onEvent(PostFavoriteEvent event) {
            int position = FundayUtils.refreshPostFavStatus(getAdapterItems(), event.getTag());
            if (position != -1) {
                RecyclerView.ViewHolder viewHolder = getRefreshView().findViewHolderForAdapterPosition(position);
                if (viewHolder != null && viewHolder instanceof PostItemView) {
                    ((PostItemView) viewHolder).setFavBtnBackground(event.isFav());
                }
            }
        }

    };

    ICommentSubscriber postCmtSubscriber = new ICommentSubscriber() {

        @Override
        public void onEvent(CommentEvent event) {
            int position = FundayUtils.refreshPostFavStatus(getAdapterItems(), event.getPost());
            if (position != -1) {
                RecyclerView.ViewHolder viewHolder = getRefreshView().findViewHolderForAdapterPosition(position);
                if (viewHolder != null && viewHolder instanceof PostItemView) {
                    ((PostItemView) viewHolder).setCmtNum(event.getPost());
                }
            }
        }

    };

    public interface IItemPlayer {

        void onPlay();

        void onStop();

    }

}
