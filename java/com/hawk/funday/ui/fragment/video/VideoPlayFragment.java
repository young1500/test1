package com.hawk.funday.ui.fragment.video;


import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hawk.funday.R;
import com.hawk.funday.base.Consts;
import com.hawk.funday.support.js.JsInterface;
import com.hawk.funday.support.sdk.bean.PostBean;
import com.hawk.funday.support.utils.JsUtils;
import com.hawk.funday.ui.activity.base.ContainerActivity;
import com.tma.analytics.TmaAgent;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.activity.container.FragmentArgs;
import org.aisen.android.ui.fragment.ABaseFragment;

import java.io.IOException;

/**
 * Created by yijie.ma on 2016/8/25.
 */
public class VideoPlayFragment extends ABaseFragment implements View.OnClickListener {

    private PostBean mVideoBean;
    private Context mContext;
    private boolean mVideoHasClick;
    private BaseActivity mBaseActivity;

    @ViewInject(id = R.id.video_title)
    TextView mVideoTitle;//视频标题
    @ViewInject(id = R.id.video_quit)
    ImageView mVideoQuit;//退出按钮
    @ViewInject(id = R.id.video_webview)
    WebView mVideoWebView;
    @ViewInject(id = R.id.viewCover)
    View viewCover;
    @ViewInject(id = R.id.video_hint)
    TextView mVideoHint;
    @ViewInject(id = R.id.video_webview_progress)
    ProgressBar videoWebviewProgress;
    @ViewInject(id = R.id.video_title_item)
    LinearLayout mTitleAndQuit;


    public static void launch(Activity from, PostBean videoBean) {
        FragmentArgs args = new FragmentArgs();
        args.add("videoBean", videoBean);

        ContainerActivity.launch(from, VideoPlayFragment.class, args);
    }

    @Override
    public int inflateContentView() {
        return R.layout.ui_fg_web_video;
    }

    @Override
    public int setActivityTheme() {
        return R.style.VideoPlayerTheme;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);
        mVideoTitle.setText(mVideoBean.getTitle());

        mVideoQuit.setOnClickListener(this);

        mVideoWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP
                        && !mVideoBean.getUrl().contains(JsInterface.YOUTUBE)){//非youtube视频暂时采用点击视频切换标题的隐藏或出现
                    if (mTitleAndQuit.getVisibility() == View.VISIBLE){
                        mTitleAndQuit.setVisibility(View.GONE);
                    }else {
                        mTitleAndQuit.setVisibility(View.VISIBLE);
                    }
                }
                return false;
            }
        });

        setWebView(mVideoBean.getUrl());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVideoBean = savedInstanceState == null ? (PostBean) getArguments().getSerializable("videoBean")
                : (PostBean) savedInstanceState.getSerializable("videoBean");
        mBaseActivity = (BaseActivity) getActivity();
        if (mBaseActivity == null) {
            return;
        }
        mContext = mBaseActivity.getApplicationContext();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mBaseActivity == null) {
            return;
        }
        mBaseActivity.getToolbar().setVisibility(View.GONE);
        mBaseActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mBaseActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("videoBean", mVideoBean);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVideoWebView.destroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_quit:
                Activity self = getActivity();
                if (self != null) {
                    self.finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mVideoWebView.onResume();

        TmaAgent.onPageStart(Consts.Page.Page_video_player_h5);
        TmaAgent.onResume(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();

        mVideoWebView.onPause();

        TmaAgent.onPageEnd(Consts.Page.Page_video_player_h5);
        TmaAgent.onPause(getActivity());
    }

    private void setWebView(String url) {

        String ua = mVideoWebView.getSettings().getUserAgentString();
        mVideoWebView.getSettings().setUserAgentString(ua);

        mVideoWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();//处理https的html视频
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Logger.e("video", "onPageFinished");
                videoWebviewProgress.setVisibility(View.GONE);
                viewCover.setVisibility(View.GONE);
                if (url.contains(JsInterface.YOUTUBE)){//目前只有youtube进行了适配
                    initJs();
                    hideTitle();
                    autoPlayJs();
                    autoHideTitle();
                }
            }
        });
        mVideoWebView.getSettings().setJavaScriptEnabled(true);
        mVideoWebView.getSettings().setDomStorageEnabled(true);
        mVideoWebView.addJavascriptInterface(new JsInterface(getActivity()), "jsToJava");
        mVideoWebView.addJavascriptInterface(this, "toWebView");
        mVideoWebView.loadUrl(url);
    }

    private void initJs(){
        String js;
        try {
            js = JsUtils.readFromAssets(mContext, "js/youtube.js");
            js = "var newscript = document.getElementById('tcl_inject_script');" +
                    "if (newscript == null || newscript == undefined) {" +
                    "var newscript = document.createElement('script');" +
                    "newscript.id='tcl_inject_script';" +
                    "newscript.text=\"" + js + "\";" +
                    "document.body.appendChild(newscript); }";
            js = "javascript: " + js;
            mVideoWebView.loadUrl(js);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void hideTitle(){
        mVideoWebView.loadUrl("javascript: hideTitle();");
        mVideoWebView.loadUrl("javascript: hideShareBtn();");
    }

    private void autoPlayJs(){
        if (!mVideoHasClick){
            mVideoWebView.loadUrl("javascript: autoPlay();");
            mVideoHasClick = true;
        }
    }

    private void autoHideTitle() {
        mVideoWebView.loadUrl("javascript: autoHideTitle()");
//        mVideoWebView.loadUrl("javascript: outputSource()");//用于输出整个HTML
    }

    @JavascriptInterface
    public void showOrHideTitle(final String s){
        runUIRunnable(new Runnable() {//此方法是异步线程调用的，所以要切换回主线程去操作标题
            @Override
            public void run() {
                if (s.contains("ytp-autohide")){
                    mTitleAndQuit.setVisibility(View.GONE);
                }else if(s.contains("playing-mode ytp-touch-mode")){
                    mTitleAndQuit.setVisibility(View.VISIBLE);
                }
            }

        });

    }
}
