package com.hawk.funday.sys.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.hawk.funday.R;
import com.hawk.funday.base.AppContext;
import com.hawk.funday.base.Consts;
import com.hawk.funday.support.db.PostPublisherDB;
import com.hawk.funday.support.sdk.FundaySDK;
import com.hawk.funday.support.sdk.bean.ImageMetadata;
import com.hawk.funday.support.sdk.bean.PostRequestBean;
import com.hawk.funday.support.sdk.bean.UploadBean;
import com.hawk.funday.support.sdk.bean.UploadImageResultBean;
import com.hawk.funday.support.sdk.bean.UploadImageResultsBean;
import com.hawk.funday.support.utils.FileUtils;
import com.hawk.funday.support.utils.GifUtils;
import com.hawk.funday.support.utils.UnitUtils;
import com.hawk.funday.sys.receiver.NotificationBroadcastReceiver;
import com.hawk.funday.ui.activity.base.ContainerActivity;
import com.hawk.funday.ui.fragment.profile.ProfilePagerFragment;
import com.tma.analytics.TmaAgent;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.Utils;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.network.http.OnFileProgress;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.ui.activity.container.FragmentArgs;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: UploadService 文件上传service
 * @author  qiangtai.huang
 * @date  2016/8/29
 * @copyright TCL-HAWK
 */
public class UploadService extends IntentService {
    private final String TAG="UploadService";
    public static final  String ACTION_RETRY = "com.hawk.funday.ui.service.retry";
    public static final  String ACTION_CANCEL = "com.hawk.funday.ui.service.cancel";
    public static final  String ACTION_UPLOAD_START = "com.hawk.funday.ui.service.start";
    public static final  String ACTION_UPLOAD_SUCCESS = "com.hawk.funday.ui.service.sucess";
    public static final  String ACTION_UPLOAD_FAILURE = "com.hawk.funday.ui.service.failure";
    public static final  String ACTION_UPLOAD_DELETE = "com.hawk.funday.ui.service.delete";
    private  Notification mNotify;
    private  NotificationManager mNotifyManage;
    private  NotificationCompat.Builder mNotifyBuilder;
    public UploadService(){
        super("UploadService");
    }

    public static void  luanchService(Context context,String title,String filePath,int notifyId,long userId){
        Intent startServiceIntent = new Intent(context, UploadService.class);
        startServiceIntent.putExtra("title",title);
        startServiceIntent.putExtra("imagePath",filePath);
        startServiceIntent.putExtra("notifyId",notifyId);
        startServiceIntent.putExtra("userId",userId);
        startServiceIntent.setPackage(context.getPackageName());
        context.startService(startServiceIntent);
    }
    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        //setNotify(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        handleIntent(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

    }
    private void handleIntent( Intent intent){
        final Intent tempData=intent;
        new WorkTask<Void, Long, UploadImageResultBean>() {

            @Override
            public UploadImageResultBean workInBackground(Void... voids) throws TaskException {
                long progress = 0;
                long total = 1;
                final Intent data=tempData;
                publishProgress(progress, total);
                FileUtils utils = new FileUtils();
                byte[] fileBytes = null;
                String imagePath = data.getStringExtra("imagePath");
                String temp = imagePath.toLowerCase();
                if (temp.endsWith(".gif")) {
                    fileBytes = utils.file2byte(imagePath);
                } else {
                    fileBytes = utils.compressImage(imagePath);
                }
               if (fileBytes==null)
                    throw  new TaskException("压缩图片异常");
                UploadImageResultsBean beans = FundaySDK.newInstance().uploadImage(fileBytes, new OnFileProgress() {
                    @Override
                    public void onProgress(long l, long l1) {
                        //////此处的进度值是单个文件的进度值，对于上传多个文件时需要定义一个总的所有的文件长度，然后对每次上传进度值累加前次进度值
                        publishProgress(l,l1);
                    }
                });
                if (temp.endsWith(".gif")) {
                    TmaAgent.onEvent(UploadService.this, Consts.Event.Event_upload_gif);
                } else {
                    TmaAgent.onEvent(UploadService.this, Consts.Event.Event_upload_pic);
                }
                if (beans!=null &&beans.getData()!=null &&beans.getData().size()>0) {
                    UploadImageResultBean result=beans.getData().get(0);
                    Logger.i(TAG, result.getUrl());
                    updateUploadRecord(data.getIntExtra("notifyId", -1), result.getUrl());;
                    PostRequestBean postBean=new PostRequestBean();
                    postBean.setContent("");
                    postBean.setTitle(data.getStringExtra("title"));
                    List<ImageMetadata> imageMetadatas=new ArrayList<ImageMetadata>();

                    for(UploadImageResultBean bean : beans.getData())
                    {
                        /*if (stringBuf.length()>0)
                            stringBuf.append(",");
                        stringBuf.append(bean.getUrl());*/
                        ImageMetadata imageMetadata=new ImageMetadata();
                        imageMetadata.setContentLength(bean.getContentLength());
                        imageMetadata.setWidth(bean.getWidth());
                        imageMetadata.setHeight(bean.getHeight());
                        imageMetadata.setUrl(bean.getUrl());
                        imageMetadatas.add(imageMetadata);
                    }
                    postBean.setUrls(imageMetadatas);
                    FundaySDK.newInstance().doPublish(postBean);

                    publishProgress((long)fileBytes.length, (long)fileBytes.length);
                    return result;
                }else {
                    throw new TaskException("上传文件结果无法解析");
                }

            }

            @Override
            protected void onPrepare() {
                super.onPrepare();
                final  Intent data=tempData;
                sendProcessBroadcast(data,ACTION_UPLOAD_START);
                try
                {
                    //setUploadNotify(data,0);
                    setUploadNotify(data,0,0);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
            @Override
            protected void onProgressUpdate(Long... values) {
                super.onProgressUpdate(values);
                long progress = values[0];
                long total = values[1];
                final  Intent data=tempData;
                try{

                    setUploadNotify(data,(int)total,(int)progress);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
            @Override
            protected void onFailure(TaskException exception) {
                super.onFailure(exception);
                final  Intent data=tempData;
                if (exception.getCode().equals(TaskException.TaskError.noneNetwork.toString())) {
                    ViewUtils.showMessage(getApplication(), exception.getMessage());
                }else {
                    ViewUtils.showMessage(getApplication(), R.string.post_failure);
                }
                sendProcessBroadcast(data,ACTION_UPLOAD_FAILURE);
                //setErrorNotify(data);
                setErrorNotify(data,"");
            }

            @Override
            protected void onSuccess(UploadImageResultBean uploadImageResultBean) {
                super.onSuccess(uploadImageResultBean);
                final  Intent data=tempData;
                deleteUploadRecord(data.getIntExtra("notifyId", -1));
                ViewUtils.showMessage(getApplication(),R.string.post_success);
                setSuccessNotify(data);
                sendProcessBroadcast(data,ACTION_UPLOAD_SUCCESS);
            }
        }.execute();
    }

    /**
      * @Description:
      * @param recordId 记录id
     *  @param url 文件上传后 获取的url
      * @return
      */

    private void updateUploadRecord(int recordId,String url){
        if (AppContext.getLoginedAccount() != null&&recordId>=0){
            UploadBean bean= PostPublisherDB.selectById(recordId);
            if (bean!=null) {
                bean.setState(1);
                bean.setFileUrl(url);

                PostPublisherDB.update(bean);
            }
        }
    }
    private void  deleteUploadRecord(int recordId){
        PostPublisherDB.deleteById(recordId);
    }

    private void setUploadNotify(Intent data,int max,int uploadRate){
        int notifyId = data.getIntExtra("notifyId", -1);
        if (mNotifyBuilder==null||max==0) {
            String imgPath=data.getStringExtra("imagePath");

            Bitmap largIcon = getLargIcon( imgPath);
            String title=data.hasExtra("title") ? data.getStringExtra("title") : null;
            if (title==null||title.trim().length()==0)
                title=  getResources() != null ? getResources().getString(R.string.uploading_new_post) : null;
            initNotifyBuiler(R.mipmap.logo,title,title,title,largIcon);
        }
        mNotifyBuilder.setProgress(max,uploadRate,false);
        String progressValue=UnitUtils.byteUnit2Str(uploadRate,false,max>1048576)+"/"+UnitUtils.byteUnit2Str(max,true,false);
        mNotifyBuilder.setContentText(progressValue);
        Notification notification=mNotifyBuilder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags|=Notification.FLAG_ONGOING_EVENT;
        notification.flags|=Notification.FLAG_INSISTENT;
        sendNotify(notifyId,notification);
    }
    private void setErrorNotify(Intent data,String content){
        int notifyId= data.getIntExtra("notifyId",-1);
        long userID=data.getLongExtra("userId",-1);
        String imgPath=data.getStringExtra("imagePath");
        ///点击重试
        Intent retryIntent = new Intent(UploadService.this, NotificationBroadcastReceiver.class);
        retryIntent.setAction(ACTION_RETRY);
        retryIntent.putExtra("notifyId", notifyId);
        retryIntent.putExtra("title", data.getStringExtra("title"));
        retryIntent.putExtra("imagePath", imgPath);
        retryIntent.putExtra("userId", userID);
        PendingIntent retryPIntent = PendingIntent.getBroadcast(this,notifyId, retryIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        ////滑动删除响应
        Intent cancelIntent = new Intent(ACTION_CANCEL);
        cancelIntent.putExtra("notifyId", notifyId);
        cancelIntent.putExtra("userId", userID);

        PendingIntent cancelPIntent = PendingIntent.getBroadcast(this, notifyId, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        String tip = getResources() != null ? getResources().getString(R.string.uploaded_failure) : null;

        String contentTxt=getResources() != null ? getResources().getString(R.string.notify_touch_hint) : null;
        Bitmap largIcon =getLargIcon( imgPath);
       if (Build.VERSION.SDK_INT<Build.VERSION_CODES.JELLY_BEAN){
            initCustomerNotifyBuiler(R.layout.lay_failure_notify,R.mipmap.ic_launcher,tip,tip,contentTxt,largIcon,retryPIntent);
       }else{
            initNotifyBuiler(R.mipmap.logo, tip, tip, contentTxt, largIcon);
            mNotifyBuilder.setWhen(0);
           ////添加按钮事件
           mNotifyBuilder.addAction(android.R.drawable.ic_menu_recent_history,getResources().getString(R.string.retry),retryPIntent);
           mNotifyBuilder.setPriority( Notification.PRIORITY_MAX);
        }
        mNotifyBuilder.setAutoCancel(true);
        mNotifyBuilder.setContentIntent(getToProfileIntent(notifyId));
        mNotifyBuilder.setDeleteIntent(cancelPIntent);
        sendNotify(notifyId);
    }
    private void  initNotifyBuiler(int samllIcon,String ticker,String title,String contentTxt,Bitmap largIcon){
        mNotifyBuilder = new NotificationCompat.Builder(UploadService.this);
        mNotifyBuilder.setSmallIcon(getSmallIcon());
        mNotifyBuilder.setTicker(ticker);
        mNotifyBuilder.setContentTitle(title);
        mNotifyBuilder.setContentText(contentTxt);
        if (largIcon!=null)
             mNotifyBuilder.setLargeIcon(largIcon);
    }
    private void  initCustomerNotifyBuiler(int viewLayoutId,int samllIcon,String ticker,String title,String contentTxt,Bitmap largIcon,PendingIntent pendingIntent){

        RemoteViews remoteViews=new RemoteViews(getApplication()
                .getPackageName(), viewLayoutId);
        remoteViews.setTextViewText(R.id.tv_title,title);
        //setImageView(remoteViews,imagePath);
        if (largIcon!=null)
            remoteViews.setImageViewBitmap(R.id.img_largicon,largIcon);
        remoteViews.setOnClickPendingIntent(R.id.btn_retry, pendingIntent);
        mNotifyBuilder=  new NotificationCompat.Builder(UploadService.this)
                          .setContent(remoteViews)
                          .setTicker(ticker)
                          .setSmallIcon(samllIcon);

    }
    private void setSuccessNotify(Intent data){
        if (data==null)
            return;
        int notifyId= data.getIntExtra("notifyId",-1);
        String imgPath=data.getStringExtra("imagePath");
        String tip = getResources() != null ? getResources().getString(R.string.uploaded_success) : null;
        String contentTxt=getResources() != null ? getResources().getString(R.string.notify_touch_hint) : null;
        Bitmap largIcon =getLargIcon( imgPath);
        initNotifyBuiler(R.mipmap.logo,tip,tip,contentTxt,largIcon);
        PendingIntent pendingIntent=getToProfileIntent(notifyId);
        mNotifyBuilder.setAutoCancel(true);
        if (pendingIntent!=null)
            mNotifyBuilder.setContentIntent(pendingIntent);
        sendNotify(notifyId);
    }
    private void sendNotify(int notifyId,Notification notification){
        if (mNotifyManage==null)
            mNotifyManage=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyManage.notify(notifyId,notification);
    }
    private void sendNotify(int notifyId){
        Notification notification=mNotifyBuilder.build();
        sendNotify(notifyId,notification);
    }
    private Bitmap  getIconBitmap(Context context,String imagePath){
        FileUtils utils=new FileUtils();
        int sizeW= Utils.dip2px(context,120);
        Bitmap bm=utils.getBitmap(imagePath,sizeW,sizeW,true);
        return bm;
    }
    private PendingIntent getToProfileIntent(int notifyID){
        if (AppContext.getLoginedAccount()==null)
            return  null;
        Intent intent=new Intent(this.getApplicationContext(),ContainerActivity.class);
        FragmentArgs args=new FragmentArgs();
        args.add("user", AppContext.getLoginedAccount().getUser());
        intent.putExtra("args", args);
        intent.putExtra("className", ProfilePagerFragment.class.getName());
        PendingIntent pendingIntent=PendingIntent.getActivity(UploadService.this,notifyID,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        return  pendingIntent;
    }

    private void sendProcessBroadcast(Intent data,String action){
        int resId= data.getIntExtra("notifyId",-1);
        Intent newIntent=new Intent(action);
        newIntent.putExtra("resId",resId);
        sendBroadcast(newIntent);
    }
    private Bitmap getLargIcon(String imagePath){
        String tempName=imagePath.toLowerCase();

        try {
            Bitmap largIcon=null;
            FileUtils fileUtils=new FileUtils();
            final int edgeLen=120;
            if (tempName.endsWith(".gif")) {
                GifUtils gifUtils=new GifUtils();
                gifUtils.read(new FileInputStream(new File(imagePath)));
                largIcon=gifUtils.getBitmap();
            } else {
                largIcon = getIconBitmap(this.getApplication().getApplicationContext(), imagePath);
            }
            if (largIcon.getHeight()!=largIcon.getWidth() ||largIcon.getHeight()>edgeLen){
                largIcon=fileUtils.centerSquareScaleBitmap(largIcon,edgeLen);
            }
            if (largIcon!=null)
                  Logger.e(TAG,"notify图标高宽："+largIcon.getHeight()+"/"+largIcon.getWidth());
            return largIcon;
        }catch (Exception e){
            Logger.e(TAG,e);
        }
        return null;
    }

    private Integer getSmallIcon() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP ? R.mipmap.icon_small_white : R.mipmap.icon_small_colorful;
    }
}
