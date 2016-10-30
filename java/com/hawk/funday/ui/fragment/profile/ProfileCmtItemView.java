package com.hawk.funday.ui.fragment.profile;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hawk.funday.R;
import com.hawk.funday.component.imageloader.core.DisplayImageOptions;
import com.hawk.funday.component.imageloader.core.ImageLoader;
import com.hawk.funday.support.sdk.bean.CommentBean;
import com.hawk.funday.support.sdk.bean.PicUrl;
import com.hawk.funday.support.utils.FundayUtils;

import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;

/**
 * 个人评论列表Item
 *
 * Created by wangdan on 16/8/25.
 */
public class ProfileCmtItemView extends ARecycleViewItemView<CommentBean> {

    @ViewInject(id = R.id.img)
    ImageView imageView;
    @ViewInject(id = R.id.txtContent)
    TextView txtContent;
    @ViewInject(id = R.id.txtCreate)
    TextView txtCreate;

    public ProfileCmtItemView(Activity context, View itemView) {
        super(context, itemView);
    }

    @Override
    public void onBindData(View view, CommentBean commentBean, int i) {
        int gap = view.getContext().getResources().getDimensionPixelSize(R.dimen.profile_cmts_gap);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(gap / 2, gap / 2, gap / 2, gap / 2);
        view.setLayoutParams(params);

        if (itemPosition() == 0) {
            params.setMargins(gap, gap, gap, gap / 2);
            view.setLayoutParams(params);
        }
        else if (itemPosition() == itemSize() - 1) {
            params.setMargins(gap, gap / 2, gap, gap);
            view.setLayoutParams(params);
        } else {
            params.setMargins(gap, gap / 2, gap, gap / 2);
            view.setLayoutParams(params);
        }

        PicUrl pic = commentBean.getResource().getThumbnailUrls()[0];
        DisplayImageOptions.Builder bulider = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .showImageOnLoading(R.mipmap.bg_timeline_loading)
                .showImageOnFail(R.mipmap.bg_timeline_fail);
        ImageLoader.getInstance().displayImage(pic.getUrl(), imageView, bulider.build());
        txtContent.setText(commentBean.getContent());
        txtCreate.setText(FundayUtils.convDate(commentBean.getPublishTime()));
    }

}
