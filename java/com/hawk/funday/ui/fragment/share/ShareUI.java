package com.hawk.funday.ui.fragment.share;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.hawk.funday.R;
import com.hawk.funday.base.AppSettings;
import com.hawk.funday.base.Consts;
import com.hawk.funday.component.imageloader.core.ImageLoader;
import com.hawk.funday.support.sdk.bean.PostBean;
import com.hawk.funday.support.utils.FundayUtils;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.APagingFragment;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.android.ui.fragment.adapter.BasicRecycleViewAdapter;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;

import java.io.File;
import java.util.List;

/**
 * Created by yijie.ma on 2016/9/7.
 */
public class ShareUI {

    public static void show( final APagingFragment f, View parent, final PostBean postBean) {
        final Activity context = f.getActivity();
        if (context == null) {
            return;
        }

        final ShareSource shareSource = new ShareSource(f, context, postBean);
        final BottomSheetDialog dialog = new BottomSheetDialog( f.getActivity());
        final View contentView = View.inflate(context, R.layout.view_share_bottom_layout, null);
        final AppBarLayout appBarLayout= (AppBarLayout) contentView.findViewById(R.id.bar_layout_share_bottom);
        final RecyclerView recyclerGridView = (RecyclerView) contentView.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(context, 3);
        recyclerGridView.setLayoutManager(layoutManager);
        final IItemViewCreator<ShareSource.ShareInfo> itemViewCreator = new IItemViewCreator<ShareSource.ShareInfo>() {

            @Override
            public View newContentView(LayoutInflater layoutInflater, ViewGroup viewGroup, int i) {
                return layoutInflater.inflate(R.layout.item_share_view, viewGroup, false);
            }

            @Override
            public IITemView<ShareSource.ShareInfo> newItemView(View view, int i) {
                return new ARecycleViewItemView<ShareSource.ShareInfo>(context, view) {
                    @ViewInject(id = R.id.item_title)
                    TextView mTitle;
                    @ViewInject(id = R.id.item_icon)
                    public ImageView mIcon;

                    @Override
                    public void onBindData(View view, ShareSource.ShareInfo shareInfo, int i) {
                        PackageManager pManager = context.getPackageManager();
                        ResolveInfo appData =shareInfo.resolveInfo;
                        if (ShareSource.MY_PRIORITY_COPY_LINK==appData.priority) {//替换原有图标
                            mIcon.setImageResource(R.mipmap.ic_funday_video_copy_link);
                            mTitle.setText(context.getString(R.string.copy_link));
                        }else if (ShareSource.PRIORITY_SAVE==appData.priority){
                            mIcon.setImageResource(R.mipmap.ic_funday_video_save);
                            mTitle.setText(context.getString(R.string.share_save));
                        }else {
                            mIcon.setImageDrawable(appData.loadIcon(pManager));
                            mTitle.setText(appData.loadLabel(pManager).toString());
                        }
                    }

                };
            }

        };
        final Activity activity = f.getActivity();
        BasicRecycleViewAdapter<ShareSource.ShareInfo> adapter = new BasicRecycleViewAdapter<>(activity, f, itemViewCreator, shareSource.getmShareInfoList());
        recyclerGridView.setAdapter(adapter);
        adapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //点击分享，待完善
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(share, 0);
                ShareSource.ShareInfo shareInfo =shareSource.getmShareInfoList().get(position);
                //统计分享数据
                if(shareInfo.resolveInfo.activityInfo!=null){
                    shareInfo.times=ActivityHelper.getIntShareData(activity,shareInfo.resolveInfo.activityInfo.name,0);
                    shareInfo.times++;
                    ActivityHelper.putIntShareData(activity,shareInfo.resolveInfo.activityInfo.name,shareInfo.times);
                }
                if(!resInfo.isEmpty() &&shareInfo.priority== ShareSource.MY_PRIORITY_COPY_LINK){
                    ClipData myClip;
                    ClipboardManager myClipboard;
                    myClipboard = (ClipboardManager)context.getSystemService(context.CLIPBOARD_SERVICE);
                    String text = shareInfo.postBean.getShare();
                    myClip = ClipData.newPlainText("text", text);
                    myClipboard.setPrimaryClip(myClip);

                    ViewUtils.showMessage(context, R.string.copy_link_success);
                    return;
                }
                if (!resInfo.isEmpty() && shareInfo.priority != ShareSource.PRIORITY_SAVE) {
                    boolean found = false;
                    for (ResolveInfo info : resInfo) {
                        if (info.activityInfo.packageName.toLowerCase().contains(shareInfo.resolveInfo.activityInfo.packageName)) {
                            String shareUrl = shareInfo.postBean.getShare();
                            if (TextUtils.isEmpty(shareUrl)) {
                                shareUrl = "";
                            }
                            String shareTitle = shareInfo.postBean.getTitle();
                            if (TextUtils.isEmpty(shareTitle)) {
                                shareTitle = context.getResources().getString(R.string.default_title);
                            }
                            share.putExtra(Intent.EXTRA_SUBJECT, shareTitle);
                            share.putExtra(Intent.EXTRA_TEXT, shareTitle + " " + shareUrl);
                            share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            share.setPackage(info.activityInfo.packageName);
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        return;
                    if (context != null) {
                        context.startActivity(Intent.createChooser(share, shareInfo.postBean.getTitle()));
                    }
                } else {
                    String savePath = AppSettings.getImageSavedPath(context);

                    boolean isSave = false;

                    String title = shareInfo.postBean.getResourceId() + "";

                    if (shareInfo.postBean.getResourceType() == Consts.MediaType.image) {
                        File save = ImageLoader.getInstance().getDiskCache().get(shareInfo.postBean.getUrls()[0].getUrl());

                        isSave = FundayUtils.saveImageToGallery(context, save, title + ".jpg", savePath);
                    } else if (shareInfo.postBean.getResourceType() == Consts.MediaType.gif) {
                        File save = ImageLoader.getInstance().getDiskCache().get(shareInfo.postBean.getUrls()[0].getUrl());

                        isSave = FundayUtils.saveImageToGallery(context, save, title + ".gif", savePath);
                    }

                    if (isSave) {
                        ViewUtils.showMessage(context, R.string.share_save_success);
                    } else {
                        ViewUtils.showMessage(context, R.string.share_save_fail);
                    }
                }
            }
        });
        dialog.setContentView(contentView);
        //获取behavior来控制appbar的elevation
        View behavorParent = (View) contentView.getParent();
        BottomSheetBehavior behavior = BottomSheetBehavior.from(behavorParent);
        behavior.setPeekHeight(GlobalContext.getInstance().getResources().getDimensionPixelSize(R.dimen.share_bottom_dialog_height));
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    if(newState==BottomSheetBehavior.STATE_EXPANDED){
                        appBarLayout.setElevation(5);
                    }else{
                        appBarLayout.setElevation(0);
                    }
                }
                if(newState==BottomSheetBehavior.STATE_HIDDEN){
                    if(dialog!=null){
                        dialog.dismiss();
                    }
                }
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
        dialog.show();
    }

}
