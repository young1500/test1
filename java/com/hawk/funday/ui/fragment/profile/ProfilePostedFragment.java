package com.hawk.funday.ui.fragment.profile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.hawk.funday.R;
import com.hawk.funday.base.AppContext;
import com.hawk.funday.support.db.PostPublisherDB;
import com.hawk.funday.support.eventbus.IPostDestorySubscriber;
import com.hawk.funday.support.eventbus.PostDestoryEvent;
import com.hawk.funday.support.sdk.FundaySDK;
import com.hawk.funday.support.sdk.bean.FundayUserBean;
import com.hawk.funday.support.sdk.bean.PicUrl;
import com.hawk.funday.support.sdk.bean.PostBean;
import com.hawk.funday.support.sdk.bean.PostsBean;
import com.hawk.funday.support.sdk.bean.UploadBean;
import com.hawk.funday.support.utils.FundayUtils;
import com.hawk.funday.support.utils.ViewUtils;
import com.hawk.funday.sys.service.UploadService;
import com.hawk.funday.ui.fragment.posts.APostListFragment;
import com.hawk.funday.ui.fragment.posts.PostFragment;
import com.wcc.framework.notification.NotificationCenter;

import org.aisen.android.network.task.TaskException;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;

import java.util.ArrayList;
import java.util.List;

/**
 * 个人发布的Post
 *
 * Created by wangdan on 16/8/22.
 */
public class ProfilePostedFragment extends APostListFragment {

    public static int FAILED_VIEWTYPE = 20160906;
    private static final String TAG = "ProfilePostedFragment";

    public static ProfilePostedFragment newInstance(FundayUserBean user) {
        Bundle args = new Bundle();
        args.putSerializable("user", user);

        ProfilePostedFragment profilePostedFragment = new ProfilePostedFragment();
        profilePostedFragment.setArguments(args);
        return profilePostedFragment;
    }

    private FundayUserBean mUser;
    private PostsBean cachePostsBean;

    @Override
    public int inflateContentView() {
        return R.layout.ui_fg_profile_posted;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = savedInstanceState == null ? (FundayUserBean) getArguments().getSerializable("user")
                                           : (FundayUserBean) savedInstanceState.getSerializable("user");

        NotificationCenter.defaultCenter().subscriber(PostDestoryEvent.class, postDestorySubscriber);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        NotificationCenter.defaultCenter().unsubscribe(PostDestoryEvent.class, postDestorySubscriber);
    }

    IPostDestorySubscriber postDestorySubscriber = new IPostDestorySubscriber() {

        @Override
        public void onEvent(PostDestoryEvent event) {
            if (FundayUtils.postDestory(getAdapterItems(), event.getPost())) {
                getAdapter().notifyDataSetChanged();
            }
        }

    };

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        if (FundayUtils.isLoginedUser(mUser.getId())) {
            findViewById(R.id.btnNewPost).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    PostFragment.launch(getActivity());
                }

            });
        } else {
            findViewById(R.id.btnNewPost).setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // 如果用户点击的是草稿箱中的数据，那么不让用户点击进入详情
        if (getAdapterItems().get(position).itemType() == FAILED_VIEWTYPE) {
        } else {
            super.onItemClick(parent, view, position, id);
        }
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
                if (viewType == FAILED_VIEWTYPE) {
                    return layoutInflater.inflate(R.layout.item_profile_post_failed, viewGroup, false);
                }

                return layoutInflater.inflate(R.layout.item_profile_post, viewGroup, false);
            }

            @Override
            public IITemView<PostBean> newItemView(View view, int viewType) {
                if (viewType == FAILED_VIEWTYPE) {
                    return new ProfilePostFailedItemView(getActivity(), view);
                }

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

//            PostsBean beans = FundaySDK.newInstance(FundayUtils.isLoginedUser(mUser.getId()) ? getTaskCacheMode(this) : ABizLogic.CacheMode.disable).getProfilePosted(mUser.getId(), offset);
            PostsBean beans = FundaySDK.newInstance().getProfilePosted(mUser.getId(), offset);
            if (beans != null && beans.isFromCache()) {
                cachePostsBean = beans;
            }

            if (AppContext.getLoginedAccount() != null &&
                    mUser.getId() == AppContext.getLoginedAccount().getUserId()) {
                if (mode == RefreshMode.reset) {
                    List<PostBean> draftList = getPostFailedBeans();
                    if (draftList != null) {
                        if (beans != null) {
                            beans.getResources().addAll(0, draftList);
                        }
                    }
                }
            }

            if (beans != null) {
                beans.setEndPaging(beans.getOffset() == -1);
            }
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

    // 从数据库中取出来发送失败的条目
    private List<PostBean> getPostFailedBeans() {
        List<PostBean> list = new ArrayList<>();

        List<UploadBean> uploadList = PostPublisherDB.select();
        if (uploadList != null && uploadList.size() > 0) {
            for (int i = 0; i < uploadList.size(); i ++) {
                int state = uploadList.get(i).getState();
                if (state == 0 || state == 1) {
                    int localId = uploadList.get(i).getId();
//                    int resourceType = uploadList.get(i).getResourceType();
                    int resourceType = FAILED_VIEWTYPE;
                    String picPath = uploadList.get(i).getFilePath();
                    String title = uploadList.get(i).getTitle();

                    // 解析获取的图片信息
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    Bitmap bitmap = BitmapFactory.decodeFile(picPath, options);
//                    int picWidth = bitmap.getWidth();
//                    int picHeight = bitmap.getHeight();
                    int picWidth = options.outWidth;
                    int picHeight = options.outHeight;
                    long picContentLength = 0;
                    if (bitmap!=null && !bitmap.isRecycled())
                        bitmap.recycle();


                    // 存储图片数组
                    PicUrl[] picUrls = new PicUrl[1];
                    PicUrl picUrl = new PicUrl();
                    picUrl.setWidth(picWidth);
                    picUrl.setHeight(picHeight);
                    picUrl.setContentLength(picContentLength);
                    picUrl.setUrl("file://" + picPath);
                    picUrls[0] = picUrl;

                    // 存储Post
                    PostBean postBean = new PostBean();
                    postBean.setId(localId);
                    postBean.setResourceType(resourceType);
                    postBean.setThumbnailUrls(picUrls);
                    postBean.setUrls(picUrls); // 缩略图数组和大图数组都用原图
                    postBean.setTitle(title);

                    list.add(postBean);
                }
            }
            return list;
        }

        return null;
    }

    private PostBean getPostFailedBeanById(int id) {
        if (getPostFailedBeans() != null) {
            for (PostBean postBean : getPostFailedBeans()) {
                long resId = postBean.getId();
                if (resId == id) {
                    return postBean;
                }
            }
        }

        return null;
    }

    private int getBitmapSize(Bitmap bitmap){
        if (bitmap != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //API 19
                return bitmap.getAllocationByteCount();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) { //API 12
                return bitmap.getByteCount();
            }
            return bitmap.getRowBytes() * bitmap.getHeight(); //earlier version
        } else {
            return 0;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(UploadService.ACTION_UPLOAD_START);
        filter.addAction(UploadService.ACTION_UPLOAD_SUCCESS);
        filter.addAction(UploadService.ACTION_UPLOAD_FAILURE);
        filter.addAction(UploadService.ACTION_UPLOAD_DELETE);
        getActivity().registerReceiver(mPostServiceReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(mPostServiceReceiver);
    }

    BroadcastReceiver mPostServiceReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (intent.hasExtra("resId") && intent.getIntExtra("resId", -1) != -1) {
                int resId = intent.getIntExtra("resId", -1);
                PostBean postBean = null;
                ArrayList<PostBean> arrayList = getAdapterItems();

                for (PostBean p : arrayList) {
                    long id = p.getId();
                    if (resId == id) {
                        postBean = p;
                    }
                }

                if (postBean != null) {
                    switch (action) {
                        case UploadService.ACTION_UPLOAD_START:
                            arrayList.remove(postBean);
                            getAdapter().notifyDataSetChanged();
                            break;
                        case UploadService.ACTION_UPLOAD_DELETE:
                            arrayList.remove(postBean);
                            getAdapter().notifyDataSetChanged();
                    }
                }

                if (action.equals(UploadService.ACTION_UPLOAD_SUCCESS)) {
                    requestData(RefreshMode.refresh);
                }

                if (action.equals(UploadService.ACTION_UPLOAD_FAILURE)) {
                    arrayList.add(0, getPostFailedBeanById(resId)); // 收到失败广播后，找到这个ID的bean塞到list最前面
                    getAdapter().notifyDataSetChanged();
                }
            }
        }
    };

}
