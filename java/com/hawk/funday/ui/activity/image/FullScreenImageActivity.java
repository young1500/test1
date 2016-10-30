package com.hawk.funday.ui.activity.image;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.hawk.funday.R;
import com.hawk.funday.component.imageloader.core.ImageLoader;
import com.hawk.funday.component.imageloader.core.assist.FailReason;
import com.hawk.funday.component.imageloader.core.listener.ImageLoadingListener;
import com.hawk.funday.support.sdk.bean.PostBean;

import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;

/**
 * Created by yijie.ma on 2016/9/1.
 */
public class FullScreenImageActivity extends BaseActivity {


    @ViewInject(id = R.id.full_srceen_image)
    ImageView mFullSrceenImage;
    @ViewInject(id = R.id.video_webview_progress)
    ProgressBar mVideoWebviewProgress;

    public static final String IMAGE_BEAN = "image_bean";

    private PostBean mImageBean;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public static void launch(Activity from, PostBean postBean) {
        Intent intent = new Intent(from, PreviewImageActivity.class);
        intent.putExtra(IMAGE_BEAN, postBean);
        from.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setFullScreen();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ui_fullscreen_image);
        mImageBean = savedInstanceState == null ? (PostBean) getIntent().getSerializableExtra(IMAGE_BEAN)
                : (PostBean) savedInstanceState.getSerializable(IMAGE_BEAN);

        showImage();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(IMAGE_BEAN, mImageBean);
    }

    private void setFullScreen() {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
    }

    private void showImage(){
        ImageLoader.getInstance().displayImage(mImageBean.getUrls()[0].getUrl(), mFullSrceenImage, null, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mVideoWebviewProgress.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        }, null);

    }
}
