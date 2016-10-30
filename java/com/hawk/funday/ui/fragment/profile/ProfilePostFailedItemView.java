package com.hawk.funday.ui.fragment.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hawk.funday.R;
import com.hawk.funday.base.AppContext;
import com.hawk.funday.base.Consts;
import com.hawk.funday.component.imageloader.core.DisplayImageOptions;
import com.hawk.funday.component.imageloader.core.ImageLoader;
import com.hawk.funday.support.db.PostPublisherDB;
import com.hawk.funday.support.sdk.bean.PicUrl;
import com.hawk.funday.support.sdk.bean.PostBean;
import com.hawk.funday.support.sdk.bean.UploadBean;
import com.hawk.funday.sys.service.UploadService;
import com.tma.analytics.TmaAgent;
import com.wcc.framework.util.DeviceManager;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;

/**
 * @author Liyang Sun
 * @Description: 个人Posts页面发送失败的ItemView
 * @date 2016/9/6 19:22
 * @copyright HAWK
 */
public class ProfilePostFailedItemView extends ARecycleViewItemView<PostBean> {

    private final static String TAG = "ProfilePostFailedItemView";

    @ViewInject(id = R.id.image)
    ImageView imageView;
    @ViewInject(id = R.id.txtTitle)
    TextView txtTitle;
    @ViewInject(id = R.id.upload_retry)
    ImageView uploadRetry;
    @ViewInject(id = R.id.del_upload)
    ImageView delUpload;
    @ViewInject(id = R.id.retry_ly)
    LinearLayout retryLy;

    private Context mContext;

    public ProfilePostFailedItemView(Activity context, View itemView) {
        super(context, itemView);

        mContext = context;
    }

    @Override
    public void onBindData(View view, PostBean postBean, final int position) {
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
        retryLy.setLayoutParams(new FrameLayout.LayoutParams((int) newWidth, (int) newHeight));

        PicUrl pic = postBean.getThumbnailUrls()[0];
        DisplayImageOptions.Builder bulider = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .showImageOnLoading(R.mipmap.bg_timeline_loading)
                .showImageOnFail(R.mipmap.bg_timeline_fail);
        ImageLoader.getInstance().displayImage(pic.getUrl(), imageView, bulider.build());

        txtTitle.setText(postBean.getTitle());

        uploadRetry.setTag(postBean);
        uploadRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppContext.getLoginedAccount()!=null) {
                    PostBean bean = (PostBean) v.getTag();

                    UploadBean uploadBean = new UploadBean();
                    uploadBean.setTitle(bean.getTitle());
                    uploadBean.setId((int) bean.getId());
                    String filePathHead = "file://";
                    String filePath = bean.getUrls()[0].getUrl().replace(filePathHead, "");
                    uploadBean.setFilePath(filePath);

                    UploadService.luanchService(mContext, uploadBean.getTitle(),
                            uploadBean.getFilePath(), uploadBean.getId(), AppContext.getLoginedAccount().getUserId()); // 触发上传操作

                    Logger.d(TAG, "Retry Upload Failed Item, Title: " + uploadBean.getTitle());

                    TmaAgent.onEvent(getContext(), Consts.Event.Event_upload_draft_retry);
                }
            }
        });

        delUpload.setTag(postBean);
        delUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new MaterialDialog.Builder(mContext)
                        .title(R.string.dialog_delete_draft_title)
                        .positiveText(R.string.dialog_delete_draft_ok)
                        .negativeText(R.string.dialog_delete_draft_cancel)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                PostBean bean = (PostBean) v.getTag();

                                PostPublisherDB.deleteById(bean.getId()); // 从数据库中删除此条目
                                // 发送广播告知删除
                                int notifyId= (int) bean.getId();
                                Intent intent = new Intent(UploadService.ACTION_UPLOAD_DELETE);
                                intent.putExtra("resId",notifyId);
                                mContext.sendBroadcast(intent);
                                Logger.d(TAG, "Delete Upload Failed Item, Title: " + bean.getTitle());

                                TmaAgent.onEvent(getContext(), Consts.Event.Event_upload_draft_delete);
                            }
                        }).show();
            }
        });
    }

}
