package com.hawk.funday.ui.activity.image;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.hawk.funday.R;
import com.hawk.funday.component.imageloader.core.ImageLoader;
import com.hawk.funday.support.utils.FileUtils;

import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
/**
 * @Description: PreviewImageActivity 图片预览
 * @author  qiangtai.huang
 * @date  2016/8/23
 * @copyright TCL-HAWK
 */
public class PreviewImageActivity extends BaseActivity{
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public static void launch(Activity activity, String imagePath) {
        Intent intent = new Intent(activity, PreviewImageActivity.class);
        intent.putExtra(IMAGE_PATH,imagePath);
        activity.startActivity(intent);
    }
    public static final String IMAGE_PATH="image_path";
    @ViewInject(id = R.id.img_content)
    private ImageView mImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setFullScreen();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ui_preview_image);
        previewImage();
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
    private void previewImage(){
        final Intent intent=getIntent();
        if (intent!=null && intent.hasExtra(IMAGE_PATH)){
            String path=intent.getStringExtra(IMAGE_PATH);
            if (path!=null&&path.length()>0 && new FileUtils().fileIsExists(path))
            {
                ImageLoader.getInstance().displayImage(path.startsWith("file:///")?path:"file:///"+path,mImageView);
            }
        }
    }
    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
    }

}
