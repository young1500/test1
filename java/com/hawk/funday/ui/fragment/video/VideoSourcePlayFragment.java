package com.hawk.funday.ui.fragment.video;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.hawk.funday.R;
import com.hawk.funday.base.Consts;
import com.hawk.funday.support.sdk.bean.PostBean;
import com.hawk.funday.ui.activity.base.ContainerActivity;
import com.tma.analytics.TmaAgent;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.activity.container.FragmentArgs;
import org.aisen.android.ui.fragment.ABaseFragment;

/**
 * Created by yijie.ma on 2016/8/30.
 */
public class VideoSourcePlayFragment extends ABaseFragment implements View.OnClickListener{

    private PostBean mVideoBean;

    private SouceMediaController mMediaController;
    private Uri mVideoUri;
    private int mPausedProgress;
    private boolean mIsPlaying;
    private Activity mSelfActivity;


    @ViewInject(id = R.id.videoView)
    VideoView videoView;
    @ViewInject(id = R.id.video_webview_progress)
    ProgressBar videoWebviewProgress;
    @ViewInject(id = R.id.video_title)
    TextView mVideoTitle;//视频标题
    @ViewInject(id = R.id.video_quit)
    ImageView mVideoQuit;//退出按钮
    @ViewInject(id = R.id.video_title_item)
    LinearLayout mTitleAndQuit;

    public static void launch(Activity from, PostBean videoBean) {
        FragmentArgs args = new FragmentArgs();
        args.add("videoBean", videoBean);

        ContainerActivity.launch(from, VideoSourcePlayFragment.class, args);
    }

    @Override
    public int inflateContentView() {
        return R.layout.ui_video_player;
    }

    @Override
    public int setActivityTheme() {
        return R.style.VideoPlayerTheme;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);
        mVideoQuit.setOnClickListener(this);
        mVideoTitle.setText(mVideoBean.getTitle());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVideoBean = savedInstanceState == null ? (PostBean) getArguments().getSerializable("videoBean")
                : (PostBean) savedInstanceState.getSerializable("videoBean");
        mIsPlaying = savedInstanceState == null ? true : savedInstanceState.getBoolean(LAST_PLAYED_STATE);
        mPausedProgress = savedInstanceState == null ? 0 : savedInstanceState.getInt(LAST_PLAYED_TIME);

        mSelfActivity = getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //全屏
        BaseActivity baseActivity = (BaseActivity) getActivity();
        if (baseActivity == null){
            return;
        }
        baseActivity.getToolbar().setVisibility(View.GONE);
        baseActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        baseActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    final static String LAST_PLAYED_TIME = "last_play_position";
    final static String LAST_PLAYED_STATE = "last_play_state";
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("videoBean", mVideoBean);
        outState.putInt(LAST_PLAYED_TIME, mPausedProgress);
        outState.putBoolean(LAST_PLAYED_STATE, mIsPlaying);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.video_quit:
                if (mSelfActivity != null){
                    mSelfActivity.finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (videoView.isPlaying()){
            mIsPlaying = true;
            videoView.pause();
        }else {
            mIsPlaying = false;
        }

        mPausedProgress = videoView.getCurrentPosition();
        videoWebviewProgress.setVisibility(View.VISIBLE);

        TmaAgent.onPageEnd(Consts.Page.Page_video_player_native);
        TmaAgent.onPause(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        setVideo(Uri.parse(mVideoBean.getUrl()));
        if (mPausedProgress != 0){
            videoView.seekTo(mPausedProgress);
        }
        if (mIsPlaying){
            videoView.start();
        }else {
            videoView.pause();
        }
        if (videoView.isPlaying()){
            videoWebviewProgress.setVisibility(View.GONE);
        }

        TmaAgent.onPageStart(Consts.Page.Page_video_player_native);
        TmaAgent.onResume(getActivity());
    }

    private void setVideo(Uri uri){
        if (mSelfActivity == null){
            return;
        }
        mMediaController = new SouceMediaController(mSelfActivity);
        mVideoUri = uri;
        mMediaController.setAnchorView(videoView);
        videoView.setMediaController(mMediaController);
        videoView.requestFocus();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {//准备播放
                videoWebviewProgress.setVisibility(View.GONE);
                if (mIsPlaying){
                    videoView.seekTo(mPausedProgress);
                    videoView.start();
                }else {
                    videoView.seekTo(mPausedProgress);

                    videoView.pause();
                }
                Logger.e("videoView", "onPrepared");
            }
        });


        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {//视频加载失败
                Logger.e("videoView", "onError");
                return false;
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Logger.e("videoView", "onCompletion");
                videoView.seekTo(1);
            }
        });

        videoView.setVideoURI(uri);
    }

    class SouceMediaController extends MediaController{

        public SouceMediaController(Context context){
            super(context);
        }

        public SouceMediaController(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public void hide() {
            super.hide();
            mTitleAndQuit.setVisibility(INVISIBLE);
        }

        @Override
        public void show() {
            super.show();
            mTitleAndQuit.setVisibility(VISIBLE);
        }
    }
}
