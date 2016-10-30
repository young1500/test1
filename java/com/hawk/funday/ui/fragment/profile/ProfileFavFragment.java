package com.hawk.funday.ui.fragment.profile;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.hawk.funday.R;
import com.hawk.funday.support.sdk.FundaySDK;
import com.hawk.funday.support.sdk.bean.FundayUserBean;
import com.hawk.funday.support.sdk.bean.PostBean;
import com.hawk.funday.support.sdk.bean.PostsBean;
import com.hawk.funday.support.utils.ViewUtils;
import com.hawk.funday.ui.fragment.posts.APostListFragment;

import org.aisen.android.network.task.TaskException;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;

/**
 * 个人收藏内容页面
 *
 * Created by wangdan on 16/8/25.
 */
public class ProfileFavFragment extends APostListFragment {

    public static ProfileFavFragment newInstance(FundayUserBean user) {
        Bundle args = new Bundle();
        args.putSerializable("user", user);

        ProfileFavFragment profileFavFragment = new ProfileFavFragment();
        profileFavFragment.setArguments(args);
        return profileFavFragment;
    }

    private FundayUserBean mUser;
    private PostsBean cachePostsBean;

    @Override
    public int inflateContentView() {
        return R.layout.ui_fg_profile_fav;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = savedInstanceState == null ? (FundayUserBean) getArguments().getSerializable("user")
                : (FundayUserBean) savedInstanceState.getSerializable("user");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);
    }

    @Override
    protected IItemViewCreator<PostBean> configFooterViewCreator() {
        return ViewUtils.configFooterViewCreator(getActivity(), this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("user", mUser);
    }

    @Override
    public IItemViewCreator<PostBean> configItemViewCreator() {
        return new IItemViewCreator<PostBean>() {

            @Override
            public View newContentView(LayoutInflater layoutInflater, ViewGroup viewGroup, int viewType) {
                return layoutInflater.inflate(R.layout.item_profile_post, viewGroup, false);
            }

            @Override
            public IITemView<PostBean> newItemView(View view, int viewType) {
                return new ProfilePostItemView(getActivity(), view);
            }

        };
    }

    @Override
    public void requestData(RefreshMode refreshMode) {
        new FeaturedTask(refreshMode != RefreshMode.update ? RefreshMode.reset : refreshMode).execute();
    }

    class FeaturedTask extends APostsTask {

        public FeaturedTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected PostsBean workInBackground(RefreshMode refreshMode, String prePage, String nextPage, Void... voids) throws TaskException {
            int offset = 0;

            if (mode == RefreshMode.update && !TextUtils.isEmpty(nextPage)) {
                offset = Integer.parseInt(nextPage);
            }

//            PostsBean beans = FundaySDK.newInstance(FundayUtils.isLoginedUser(mUser.getId()) ? getTaskCacheMode(this) : ABizLogic.CacheMode.disable).getProfileFavs(mUser.getId(), offset);
            PostsBean beans = FundaySDK.newInstance().getProfileFavs(mUser.getId(), offset);
            if (beans != null && beans.isFromCache()) {
                cachePostsBean = beans;
            }

            beans.setEndPaging(beans.getOffset() == -1);
            return beans;
        }

        @Override
        protected void onSuccess(PostsBean postsBean) {
            super.onSuccess(postsBean);

            if (!postsBean.isFromCache() && cachePostsBean != null) {
                for (PostBean postBean : cachePostsBean.getResources()) {
                    getAdapterItems().remove(postBean);
                }
                getAdapter().notifyDataSetChanged();
            }
        }
    }

    @Override
    protected RecyclerView.LayoutManager configLayoutManager() {
        return new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
    }

}
