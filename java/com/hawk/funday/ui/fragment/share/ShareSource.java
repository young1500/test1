package com.hawk.funday.ui.fragment.share;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.Log;

import com.hawk.funday.base.Consts;
import com.hawk.funday.component.imageloader.core.ImageLoader;
import com.hawk.funday.support.sdk.bean.PostBean;
import com.hawk.funday.ui.fragment.detail.CommentListFragment;

import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.ui.fragment.APagingFragment;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by yijie.ma on 2016/9/7.
 */
public class ShareSource {
    private static final int EACH_PAGE_COUNT = 6;
    private List<ResolveInfo> mAppsToShare;//可以分享的App
    private static ArrayList<ShareInfo> mShareInfoList;//要分享的内容
    public static final int PRIORITY_FACEBOOK = 1;
    public static final int PRIORITY_SAVE = 3;
    public static final int MY_PRIORITY_COPY_LINK = 2;//不是系统自带的复制链接


    public ArrayList<ShareInfo> getmShareInfoList() {
        return mShareInfoList;
    }

    public ShareSource(APagingFragment parent, final Context context, PostBean postBean) {
        mAppsToShare = new ArrayList<>();
        mShareInfoList = new ArrayList<>();
        boolean flag=false;
        mAppsToShare = getAppListToShare(context, "text/plain");
        if (mAppsToShare.size() == 0) {
            return;
        }
        Comparator<ShareInfo> comp = new Comparator<ShareInfo>() {

            public int compare(ShareInfo o1, ShareInfo o2) {
                if(o1.resolveInfo.activityInfo==null){
                    return -1;
                }
                if(o2.resolveInfo.activityInfo==null){
                    return -1;
                }
                o1.times=ActivityHelper.getIntShareData(context,o1.resolveInfo.activityInfo.name);
                o2.times=ActivityHelper.getIntShareData(context,o2.resolveInfo.activityInfo.name);
                if(o1.times==o2.times){
                    return 0;
                }
                return o1.times>o2.times?1:-1;
            }
        };

        for (int i = 0; i < mAppsToShare.size(); i++) {
            ShareInfo realShareInfo = new ShareInfo();
            realShareInfo.resolveInfo = mAppsToShare.get(i);
            String packageName = realShareInfo.resolveInfo.activityInfo.packageName;
            if (packageName.equals("com.facebook.katana")) {
                realShareInfo.priority = PRIORITY_FACEBOOK;
            }  else if (realShareInfo.resolveInfo.activityInfo.toString().contains("clipboard.SendTextToClipboardActivity")) {//屏蔽掉系统自带的复制链接
                continue;
            }
            realShareInfo.type = postBean.getResourceType();
            realShareInfo.postBean = postBean;
            mShareInfoList.add(realShareInfo);
        }
        Collections.sort(mShareInfoList, comp);

        //手动构建复制链接选项
        ShareInfo copyLinkShareInfo = new ShareInfo();
        copyLinkShareInfo.priority = MY_PRIORITY_COPY_LINK;
        copyLinkShareInfo.postBean = postBean;
        copyLinkShareInfo.resolveInfo = new ResolveInfo();
        copyLinkShareInfo.resolveInfo.priority = MY_PRIORITY_COPY_LINK;
        mShareInfoList.add(copyLinkShareInfo);
        //添加保存选项
        if (parent instanceof CommentListFragment){//详情页才有保存选项
            if (postBean.getResourceType() == Consts.MediaType.image || postBean.getResourceType() == Consts.MediaType.gif){
                File save = ImageLoader.getInstance().getDiskCache().get(postBean.getUrls()[0].getUrl());
                if (save.exists()){
                    ShareInfo savePicShareInfo = new ShareInfo();//手动构建保存图片的选项
                    savePicShareInfo.priority = PRIORITY_SAVE;
                    savePicShareInfo.postBean = postBean;
                    savePicShareInfo.resolveInfo=new ResolveInfo();
                    savePicShareInfo.resolveInfo.priority=PRIORITY_SAVE;
                    mShareInfoList.add(savePicShareInfo);
                }
            }
        }
        Collections.reverse(mShareInfoList);
    }

    public List<ResolveInfo> getAppListToShare(Context context, String type) {
        Intent intent = new Intent(Intent.ACTION_SEND, null);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType(type);
        PackageManager pManager = context.getPackageManager();
        mAppsToShare = pManager.queryIntentActivities(intent,PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        return mAppsToShare;
    }
    public static class ShareInfo implements Serializable {
        public ResolveInfo resolveInfo;
        public int priority;
        public PostBean postBean;
        public int type;
        public int times;
    }
}
