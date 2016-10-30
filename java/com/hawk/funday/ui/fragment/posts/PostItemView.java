package com.hawk.funday.ui.fragment.posts;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hawk.funday.R;
import com.hawk.funday.base.Consts;
import com.hawk.funday.component.imageloader.core.DisplayImageOptions;
import com.hawk.funday.component.imageloader.core.ImageLoader;
import com.hawk.funday.component.imageloader.core.assist.ImageSize;
import com.hawk.funday.component.imageloader.core.imageaware.ImageViewAware;
import com.hawk.funday.component.imageloader.utils.MemoryCacheUtils;
import com.hawk.funday.support.eventbus.PostFavoriteEvent;
import com.hawk.funday.support.imageloader.FadeInDisplayer;
import com.hawk.funday.support.sdk.FundaySDK;
import com.hawk.funday.support.sdk.bean.AccountBean;
import com.hawk.funday.support.sdk.bean.PostBean;
import com.hawk.funday.support.utils.FundayExceptionDelegate;
import com.hawk.funday.support.utils.FundayUtils;
import com.hawk.funday.ui.fragment.base.BizFragment;
import com.hawk.funday.ui.fragment.detail.CommentListFragment;
import com.hawk.funday.ui.fragment.share.ShareUI;
import com.tma.analytics.TmaAgent;
import com.wcc.framework.notification.NotificationCenter;
import com.wcc.framework.util.DeviceManager;

import org.aisen.android.common.utils.Utils;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.APagingFragment;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;

import pl.droidsonroids.gif.GifImageView;

/**
 * Post基础ItemView
 * <p/>
 * Created by wangdan on 16/8/18.
 */
public class PostItemView extends ARecycleViewItemView<PostBean> implements View.OnClickListener{

    @ViewInject(id = R.id.txtTitle)
    TextView txtTitle;
    @ViewInject(id = R.id.imgPicGif)
    protected GifImageView gifImageView;
    @ViewInject(id = R.id.txtCmts)
    protected  TextView itemCommentsNum;
    @ViewInject(id = R.id.imgCut)
    View viewCut;
    protected PostBean mBean;
    @ViewInject(id = R.id.btnShare)
    View btnShare;
    @ViewInject(id = R.id.btn_more)
    View btnMore;
    @ViewInject(id = R.id.btnFav)
    LinearLayout btnFav;
    @ViewInject(id = R.id.btnCmt)
    LinearLayout btnCmt;
    @ViewInject(id = R.id.img_fav)
    ImageView imgFav;
    protected int mFavDrawable;
    protected APagingFragment ownerFragment;

    protected Context mContext;
    public PostItemView(Activity context, View itemView, APagingFragment ownerFragment) {
        super(context, itemView);
        mContext = context;
        this.ownerFragment = ownerFragment;
        if (ownerFragment instanceof APostListFragment) {
            mFavDrawable = ((APostListFragment) ownerFragment).favDrawableResId();
        }
    }

    @Override
    public void onBindData(View view, PostBean postBean, int i) {
        if (!TextUtils.isEmpty(postBean.getTitle())){
            txtTitle.setText(postBean.getTitle());
        }
        else {
            txtTitle.setText("");
        }
        setCmtNum(postBean);
        mBean=postBean;
        if (viewCut != null &&
                (postBean.getResourceType() == Consts.MediaType.image || postBean.getResourceType() == Consts.MediaType.gif)) {
            String imageType = postBean.getType();
            if (!TextUtils.isEmpty(imageType) && postBean.getType().equals("PIIIC")) {
                viewCut.setVisibility(View.VISIBLE);
            } else {
                viewCut.setVisibility(View.GONE);
            }
        }
        setBtn(btnShare, postBean);
        setBtn(btnFav, postBean);
        setBtn(btnCmt, postBean);
        setBtn(btnMore, postBean);
        setFavBtnBackground(postBean.isFavorite());
        onImageResize(postBean);
        onBindImage(postBean);
    }

    public void setCmtNum(PostBean bean) {
        if (bean != null)
            itemCommentsNum.setText(String.valueOf(bean.getCommentCount()));
    }

    public void setmFavDrawable(int favDrawable){
        this.mFavDrawable=favDrawable;
    }

    public void setFavBtnBackground(boolean fav){
        if (imgFav != null) {
            if (fav) {
                if (mFavDrawable > 0) {
                    imgFav.setImageResource(mFavDrawable);
                }
                else {
                    imgFav.setImageResource(R.drawable.ic_funday_homepage_likes_selected_new);
                }
            }
            else{
                imgFav.setImageResource(R.mipmap.ic_funday_homepage_likes);
            }
        }

        mBean.setFavorite(fav);
    }
    private void setBtn(View btn, PostBean bean) {
        if (btn != null) {
            btn.setTag(bean);
            btn.setOnClickListener(this);
        }
    }
    protected void onImageResize(PostBean bean) {
        // 预先调整控件的大小
        if (bean.getThumbnailUrls() != null && bean.getThumbnailUrls().length > 0) {
            float width = bean.getThumbnailUrls()[0].getWidth() - Utils.dip2px(getContext(), 8);
            float height = bean.getThumbnailUrls()[0].getHeight();
            float newWidth = DeviceManager.getScreenWidth(getContext());
            float newHeight = (height / width) * newWidth;
            gifImageView.setLayoutParams(new RelativeLayout.LayoutParams((int) newWidth, (int) newHeight));
        }
    }

    protected void onBindImage(PostBean bean) {
        if (bean.getThumbnailUrls() == null || bean.getThumbnailUrls().length == 0 ||
                bean.getThumbnailUrls()[0] == null) {
            return;
        }
        ImageSize imageSize = new ImageSize(bean.getThumbnailUrls()[0].getWidth(), bean.getThumbnailUrls()[0].getHeight());
        String url = bean.getThumbnailUrls()[0].getUrl();
        String key = MemoryCacheUtils.generateKey(url, imageSize);
        Bitmap bitmap = ImageLoader.getInstance().getMemoryCache().get(key);
        if (bitmap != null && !bitmap.isRecycled()) {
            gifImageView.setImageBitmap(bitmap);
        }
        else {
            DisplayImageOptions.Builder bulider = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .displayer(new FadeInDisplayer())
                    .considerExifParams(true)
                    .showImageOnLoading(R.mipmap.bg_timeline_loading)
                    .showImageOnFail(R.mipmap.bg_timeline_fail);
            ImageLoader.getInstance().displayImage(bean.getThumbnailUrls()[0].getUrl(),
                    new ImageViewAware(gifImageView), bulider.build(),
                    null, null, imageSize);
        }
    }

    @Override
    public void onClick(View v) {

        PostBean bean = (PostBean) v.getTag();

        if (v.getId() == R.id.btn_more) {
            onMore(bean);
        }
        else if (v.getId() == R.id.btnShare){
            onShare(v, bean);
        }
        else if (v.getId() == R.id.btnFav){
            onFav(v, bean);
        }
        else if (v.getId() == R.id.btnCmt){
            onCmt(v, bean);
        }
    }

    /**
     * 分享
     *
     * @param v
     * @param bean
     */
    protected void onShare(View v, PostBean bean) {
        ShareUI.show(ownerFragment, v, bean);

        TmaAgent.onEvent(getContext(), Consts.Event.Event_post_share_click);
    }

    /**
     * 更多按钮点击事件
     *
     * @param bean
     */
    protected void onMore(PostBean bean) {

    }

    /**
     * 收藏
     *
     * @param v
     * @param bean
     */
    protected void onFav(View v, PostBean bean) {
        BizFragment.createBizFragment(getContext()).checkUserPermission(new BizFragment.OnUserPermissionCallback() {
            @Override
            public void onSuccess(AccountBean account) {
                handleFavorite(account);
            }

            @Override
            public void onFaild() {

            }
        });
    }

    /**
     * 查看评论
     *
     * @param v
     * @param bean
     */
    protected void onCmt(View v, PostBean bean) {
        CommentListFragment.launch((Activity) mContext, bean, true);

        TmaAgent.onEvent(getContext(), Consts.Event.Event_post_cmts_click);
    }

    /**
     * @return
     * @Description: 添加、取消收藏
     * @params AccountBean 用户对象
     */
    private void handleFavorite(final AccountBean account) {
        new WorkTask<Void, Void, String>() {
            @Override
            protected void onPrepare() {
                super.onPrepare();

                if (mBean.isFavorite()) {
                    TmaAgent.onEvent(getContext(), Consts.Event.Event_post_fav_destory);
                }
                else {
                    TmaAgent.onEvent(getContext(), Consts.Event.Event_post_fav_create);
                }

                mBean.setFavorite(!mBean.isFavorite());

                FundayUtils.animScale(imgFav);

                NotificationCenter.defaultCenter().publish(new PostFavoriteEvent(mBean.isFavorite(), mBean));
            }

            @Override
            public String workInBackground(Void... voids) throws TaskException {
                if (!mBean.isFavorite())
                    return FundaySDK.newInstance().doCancelFavorite(mBean.getResourceId(), mBean.getResourceType(), account.getToken(), account.getOpenId());
                else
                    return FundaySDK.newInstance().doAddFavorite(mBean.getResourceId(), mBean.getResourceType(), account.getToken(), account.getOpenId());
            }

            @Override
            protected void onFinished() {
                super.onFinished();

                ViewUtils.dismissProgressDialog();
            }

            @Override
            protected void onFailure(TaskException exception) {
                super.onFailure(exception);

                mBean.setFavorite(!mBean.isFavorite());

                NotificationCenter.defaultCenter().publish(new PostFavoriteEvent(mBean.isFavorite(), mBean));

                if (mBean.isFavorite()) {
                    ViewUtils.showMessage(getContext(), FundayExceptionDelegate.getMessage(getContext(), exception, R.string.set_favorite_failure));
                }
                else {
                    ViewUtils.showMessage(getContext(), FundayExceptionDelegate.getMessage(getContext(), exception, R.string.cancel_favorite_failure));
                }
            }

        }.execute();
    }

}
