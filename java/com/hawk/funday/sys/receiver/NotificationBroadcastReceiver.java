package com.hawk.funday.sys.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hawk.funday.base.AppContext;
import com.hawk.funday.support.db.PostPublisherDB;
import com.hawk.funday.support.sdk.bean.UploadBean;
import com.hawk.funday.sys.service.UploadService;

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int notifyId=-1;
        long userID=-1;
        if (intent.hasExtra("notifyId")) {
            notifyId=intent.getIntExtra("notifyId",-1);
        }
        if (intent.hasExtra("userId")) {
            userID=intent.getLongExtra("userId",-1);
        }
            if (action.equals(UploadService.ACTION_RETRY)) {
                Intent startServiceIntent = new Intent(context,UploadService.class);////5.0版本后要显示设置包名
                String title=intent.getStringExtra("title");
                String imagePath=intent.getStringExtra("imagePath");
                startServiceIntent.putExtra("title", title);
                startServiceIntent.putExtra("imagePath", imagePath);
                startServiceIntent.putExtra("userId",userID);
                //startServiceIntent.setPackage(context.getPackageName());
                if (AppContext.getLoginedAccount()!=null&&userID>-1&&AppContext.getLoginedAccount().getUserId()==userID) {
                    int result = initUpoadBean(notifyId,title, imagePath);
                    if (result != notifyId) {
                        cancelNotify(context,notifyId);
                    }
                    startServiceIntent.putExtra("notifyId", notifyId);
                    context.startService(startServiceIntent);
                }else {
                    cancelNotify(context,notifyId);
                }
            }  else if (action.equals(UploadService.ACTION_UPLOAD_DELETE)&&intent.hasExtra("resId")){
                notifyId=intent.getIntExtra("resId",-1);
                cancelNotify(context,notifyId);;
            }else if (action.equals(UploadService.ACTION_CANCEL)){
                cancelNotify(context,notifyId);

                if (AppContext.getLoginedAccount() != null &&
                        AppContext.getLoginedAccount().getUserId() == userID){
                    PostPublisherDB.deleteById(notifyId);
                }
            }

    }

    private void cancelNotify(Context context,int notfyId)
    {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notfyId);
    }
    private int initUpoadBean(int redID,String title,String filePath){
        UploadBean bean = PostPublisherDB.selectById(redID);

        if (bean == null) {
            bean = new UploadBean();
            bean.setResourceType(getResourceType(filePath));
            bean.setTitle(title);
            bean.setFilePath(filePath);
            bean.setState(0);

            PostPublisherDB.insert(bean);

            redID = bean.getId();
        }

        return redID;

    }
    private int getResourceType(String fileName)
    {
        String temp=fileName.toLowerCase();
        if (temp.endsWith(".gif"))
        {
            return 2;
        }else if (temp.endsWith(".jpg")||temp.endsWith(".png")||temp.endsWith(".jpeg")||temp.endsWith(".bmp"))
        {
            return 1;
        }
        return 0;
    }
}
