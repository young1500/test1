package com.hawk.funday.ui.fragment.profile;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hawk.funday.R;
import com.hawk.funday.component.imageloader.core.DisplayImageOptions;
import com.hawk.funday.component.imageloader.core.ImageLoader;
import com.hawk.funday.support.sdk.bean.PicUrl;
import com.hawk.funday.support.sdk.bean.PostBean;
import com.wcc.framework.util.DeviceManager;

import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;

/**
 * 个人Posts、Fav页面的ItemView
 * <p/>
 * Created by wangdan on 16/8/25.
 */
public class ProfilePostItemView extends ARecycleViewItemView<PostBean> {

    @ViewInject(id = R.id.image)
    ImageView imageView;
    @ViewInject(id = R.id.txtTitle)
    TextView txtTitle;

    public ProfilePostItemView(Activity context, View itemView) {
        super(context, itemView);
    }

    @Override
    public void onBindData(View view, PostBean postBean, int i) {
        int gapTB = view.getContext().getResources().getDimensionPixelSize(R.dimen.profile_posts_top_bottom_gap); // 上下间距
        int gapLR = view.getContext().getResources().getDimensionPixelSize(R.dimen.profile_posts_left_right_gap); // 左右间距
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(gapLR / 2, gapTB / 2, gapLR / 2, gapTB / 2);
        view.setLayoutParams(params);

        //预先调整控件的大小
        float width = postBean.getThumbnailUrls()[0].getWidth();
        float height = postBean.getThumbnailUrls()[0].getHeight();
        float newWidth = DeviceManager.getScreenWidth(getContext()) / 2
                 - view.getContext().getResources().getDimensionPixelSize(R.dimen.profile_posts_top_bottom_gap)
                 - view.getContext().getResources().getDimensionPixelSize(R.dimen.profile_posts_left_right_gap) / 2;
        float newHeight = (height / width) * newWidth;
        imageView.setLayoutParams(new RelativeLayout.LayoutParams((int) newWidth, (int) newHeight));

        PicUrl pic = postBean.getThumbnailUrls()[0];
        DisplayImageOptions.Builder bulider = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .showImageOnLoading(R.mipmap.bg_timeline_loading)
                .showImageOnFail(R.mipmap.bg_timeline_fail);
        ImageLoader.getInstance().displayImage(pic.getUrl(), imageView, bulider.build());

        txtTitle.setText(postBean.getTitle());
    }

}
