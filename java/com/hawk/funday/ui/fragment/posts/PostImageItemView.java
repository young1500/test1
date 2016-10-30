package com.hawk.funday.ui.fragment.posts;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.hawk.funday.R;
import com.hawk.funday.base.Consts;
import com.hawk.funday.component.imageloader.core.ImageLoader;
import com.hawk.funday.component.imageloader.core.assist.ImageSize;
import com.hawk.funday.support.permissions.FlowRemindAction;
import com.hawk.funday.support.sdk.bean.PicUrl;
import com.hawk.funday.support.sdk.bean.PostBean;
import com.hawk.funday.ui.fragment.detail.CommentListFragment;
import com.hawk.funday.ui.fragment.video.VideoPlayFragment;
import com.hawk.funday.ui.fragment.video.VideoSourcePlayFragment;
import com.hawk.funday.ui.fragment.video.YoutubeFragment;
import com.tma.analytics.TmaAgent;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.support.action.IAction;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.APagingFragment;

import java.io.File;
import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by wangdan on 16/8/18.
 */
public class PostImageItemView extends PostItemView implements View.OnClickListener, PostGIFProgressManager.OnGIFProgressListener, APostListFragment.IItemPlayer {

    @ViewInject(id = R.id.item_gif_button)
    ImageView itemGifButton;
    @ViewInject(id = R.id.item_video_gif_progress)
    ProgressBar itemVideoGifProgress;
    @ViewInject(id = R.id.item_video_button)
    ImageView itemVideoButton;
    @ViewInject(id = R.id.loadProgress)
    ImageView progressView;
    @ViewInject(id = R.id.layImageContainer)
    RelativeLayout layImageContainer;

    private long mClickTime = System.currentTimeMillis();

    public PostImageItemView(Activity context, View itemView, APagingFragment ownerFragment) {
        super(context, itemView, ownerFragment);
    }

    @Override
    public void onBindData(View view, final PostBean postBean, int l) {
        super.onBindData(view, postBean, l);

        // 如果是GIF，且正在下载，需要显示进度
        if (postBean.getResourceType() == Consts.MediaType.gif) {
            if (postBean.getUrls() != null &&
                    postBean.getUrls().length > 0 &&
                    PostGIFProgressManager.bindLoading(postBean.getUrls()[0].getUrl(), this)) {
                setProgressVisiable(View.VISIBLE);
            } else {
                setProgressVisiable(View.GONE);
                itemGifButton.setTag(postBean);
                itemGifButton.setOnClickListener(this);
                itemGifButton.setVisibility(View.VISIBLE);
            }
        } else if (postBean.getResourceType() == Consts.MediaType.video) {
            itemVideoButton.setVisibility(View.VISIBLE);
            itemVideoButton.setTag(postBean);
            itemVideoButton.setOnClickListener(this);
            itemGifButton.setVisibility(View.GONE);
        }

        gifImageView.setTag(postBean);
        gifImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        mClickTime = System.currentTimeMillis() - mClickTime;
        if (mClickTime < 300) {
            return;
        } else {
            mClickTime = System.currentTimeMillis();
        }
        super.onClick(v);
        PostBean postBean = (PostBean) v.getTag();
        switch (v.getId()) {
            case R.id.item_gif_button:
                // 点击播放按钮
                onGIFClicked(postBean);
                break;
            case R.id.imgPicGif:
                if (postBean.getResourceType() == Consts.MediaType.gif) {
                    // 点击图片
                    if (gifImageView.isActivated()) {
                        onBindImage(postBean);
                        gifImageView.setActivated(false);
                        itemGifButton.setVisibility(View.VISIBLE);
                    } else {
                        onGIFClicked(postBean);
                    }
                } else if (postBean.getResourceType() == Consts.MediaType.video) {
                    //点击视频
                    onVideoClicked(postBean);
                } else {
                    onImageClicked(postBean);
                }
                break;
            case R.id.item_video_button:
                onVideoClicked(postBean);
                break;
        }
    }

    /**
     * 图片被点击
     *
     * @param bean
     */
    protected void onImageClicked(PostBean bean) {
        CommentListFragment.launch((Activity) mContext, bean, false);
    }

    /**
     * 视频点击事件
     *
     * @param bean
     */
    protected void onVideoClicked(final PostBean bean) {

        new IAction(ownerFragment.getActivity(), new FlowRemindAction(ownerFragment.getActivity(), bean.getDuration())){
            @Override
            public void doAction() {
                String videoId = null;
                if (bean.getUrl().startsWith("http://www.youtube.com/embed/")) {
                    videoId = bean.getUrl().substring(0, bean.getUrl().indexOf("?"));
                    videoId = videoId.replace("http://www.youtube.com/embed/", "");

                    final String finalVideoId = videoId;

                    final YoutubeFragment fragment = YoutubeFragment.getFragment(getContext());
                    if (fragment != null) {
                        final View view = fragment.getView();
                        if (view != null) {
                            view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {

                                @Override
                                public void onViewAttachedToWindow(View v) {

                                }

                                @Override
                                public void onViewDetachedFromWindow(View v) {
                                    Logger.d("Youtube", "onViewDetachedFromWindow");

                                    view.removeOnAttachStateChangeListener(this);
                                    fragment.stop();

                                    if (view.getParent() != null && view.getParent() instanceof ViewGroup) {
                                        ((ViewGroup) view.getParent()).removeView(view);

                                        FrameLayout container = (FrameLayout) getContext().findViewById(R.id.layYoutube);
                                        container.addView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                                    }
                                }

                            });
                        }

                        if (view.getParent() != null && view.getParent() instanceof ViewGroup) {
                            ((ViewGroup) view.getParent()).removeView(view);

                            layImageContainer.addView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));

                            fragment.play(finalVideoId);
                        }

                        return;
                    }
                }

                if (bean.getType() == null || bean.getType().equals("")) {//如果为空就定义为网站url
                    bean.setType("HTML");
                }
                if (bean.getType().equals("RAW")) {//如果是资源url
                    TmaAgent.onEvent(getContext(), Consts.Event.Event_post_video_native_play);

                    VideoSourcePlayFragment.launch((Activity) getContext(), bean);
                } else if (bean.getType().equals("HTML")) {//如果是网站url
                    TmaAgent.onEvent(getContext(), Consts.Event.Event_post_video_h5_play);

                    VideoPlayFragment.launch(getContext(), bean);
                }

            }
        }.run();


    }

    /**
     * 设置进度条可见
     *
     * @param visiable
     */
    private void setProgressVisiable(int visiable) {
//        itemVideoGifProgress.setVisibility(visiable);
        AnimationDrawable drawable = (AnimationDrawable) progressView.getDrawable();
        itemGifButton.setVisibility(View.GONE);
        progressView.setVisibility(visiable);
        if (visiable == View.GONE) {
            drawable.stop();
        } else {
            drawable.start();
        }
    }

    /**
     * 设置进度条状态
     *
     * @param current
     * @param total
     */
    private void setProgress(int current, int total) {
        itemVideoGifProgress.setMax(total);
        itemVideoGifProgress.setProgress(current);
    }

    /**
     * GIF点击事件
     *
     * @param bean
     */
    protected void onGIFClicked(PostBean bean) {
        if (bean.getUrls() != null && bean.getUrls().length > 0) {
            final PicUrl pic = bean.getUrls()[0];
            final PostGIFProgressManager.OnGIFProgressListener listener = this;
            File file = ImageLoader.getInstance().getDiskCache().get(pic.getUrl());
            if (file != null && file.exists()) {//若GIF下载过则直接显示
                TmaAgent.onEvent(getContext(), Consts.Event.Event_post_gif_play);

                try {
                    itemGifButton.setVisibility(View.GONE);
                    gifImageView.setActivated(true);
                    gifImageView.setImageDrawable(new GifDrawable(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {//没下载过则显示加载动画，开始下载GIF
                setProgressVisiable(View.VISIBLE);
                PostGIFProgressManager.loadImage(pic.getUrl(), listener);
            }
        }
    }

    @Override
    public void onGifSuccess(String imageUri) {
        if (imageUri.equals(image())) {
            // 隐藏进度条
            TmaAgent.onEvent(getContext(), Consts.Event.Event_post_gif_play);

            setProgressVisiable(View.GONE);
            itemGifButton.setVisibility(View.GONE);
            gifImageView.setActivated(true);
            File file = ImageLoader.getInstance().getDiskCache().get(imageUri);
            if (file != null && file.exists()) {
                try {
                    gifImageView.setImageDrawable(new GifDrawable(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                onGifFailed(imageUri);
            }
        }
    }

    @Override
    public void onGifFailed(String imageUri) {
        if (imageUri.equals(image())) {
            setProgressVisiable(View.GONE);
            itemGifButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onGifProgressUpdate(String imageUri, View view, int current, int total) {
        setProgressVisiable(View.VISIBLE);
    }

    @Override
    public String image() {
        if (mBean != null && mBean.getUrls() != null && mBean.getUrls().length > 0) {
            return mBean.getUrls()[0].getUrl();
        }

        return null;
    }

    @Override
    public void onPlay() {

//        if (mBean.getResourceType() == Consts.MediaType.gif) {
//
//            float tabLayoutbottom = 0.0f;//TabLayout下边界的位置
//            int[] location = new int[2];
//            float showTop, showBottom;//要播放gif的上下边界范围
//            float gifTop;//gif的显示位置
//            TabLayout tabLayout = null;
//            GifImageView gifImageView = this.gifImageView;
//
//            if (mContext instanceof Activity){
//                tabLayout = (TabLayout) ((Activity)mContext).findViewById(R.id.tabLayout);
//                tabLayout.getLocationOnScreen(location);
//                tabLayoutbottom = location[1] + tabLayout.getHeight();
//            }
//            showTop = tabLayoutbottom - gifImageView.getMeasuredHeight() * (1 - GIF_SHOW_RANGE);
//            showBottom = mScreenHeight - gifImageView.getMeasuredHeight() * GIF_SHOW_RANGE;
//
//            gifImageView.getLocationOnScreen(location);
//            gifTop = location[1];
//
//            if (showTop < gifTop && gifTop < showBottom){//符合这个范围的才播放gif，否则显示缩略图
//                gifStart();
//            }else {
//                gifEnd();
//            }
//        }

    }

    @Override
    public void onStop() {
        gifEnd();
    }

    public static class GIFImageSize extends ImageSize {

        public GIFImageSize(int width, int height) {
            super(width, height);
        }

    }

    private void gifStart() {
        onGIFClicked(mBean);
    }

    private void gifEnd() {
        if (mBean.getResourceType() == Consts.MediaType.gif) {
            if (gifImageView.isActivated()) {
                onBindImage(mBean);
                gifImageView.setActivated(false);
                itemGifButton.setVisibility(View.VISIBLE);
            }
        }
    }

}
