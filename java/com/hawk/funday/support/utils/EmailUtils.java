package com.hawk.funday.support.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;

import com.hawk.funday.R;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: EmailUtils 邮箱使用工具
 * @author  qiangtai.huang
 * @date  2016/9/21
 * @copyright TCL-HAWK
 */
public class EmailUtils {
    public static void sendEmail(Context context,String emailAddr,String title,String body) throws ActivityNotFoundException{
        Intent data=new Intent(Intent.ACTION_SENDTO);
        data.setData(Uri.parse("mailto:"+emailAddr));
        data.putExtra(Intent.EXTRA_SUBJECT, title);
        data.putExtra(Intent.EXTRA_TEXT, body);
        context.startActivity(data);

    }
    public static void sendFeedBackEmail(Activity context, String emailAddr, String title)throws ActivityNotFoundException{
        Point point = new Point();///手机屏幕像素尺寸
        context.getWindowManager().getDefaultDisplay().getSize(point);
        String size=point.x+"*"+point.y;
        String sysVersion=android.os.Build.VERSION.RELEASE;
        StringBuilder bodyBuilder=new StringBuilder();
        bodyBuilder.append("\n\n__________________________\n");
        String temp=context.getResources().getString(R.string.phone_model);
        bodyBuilder.append(temp);
        if (!temp.endsWith(":"))
            bodyBuilder.append(":");
        bodyBuilder.append( Build.MODEL);
        bodyBuilder.append("\n");
        temp=context.getResources().getString(R.string.display_size);
        bodyBuilder.append(temp);
        if (!temp.endsWith(":"))
            bodyBuilder.append(":");
        bodyBuilder.append(size);
        bodyBuilder.append("\n");
        temp=context.getResources().getString(R.string.system_language);
        bodyBuilder.append(temp);
        if (!temp.endsWith(":"))
            bodyBuilder.append(":");
        bodyBuilder.append(Locale.getDefault().getLanguage());
        bodyBuilder.append("-");
        bodyBuilder.append(Locale.getDefault().getDisplayLanguage());
        bodyBuilder.append("\n");
        temp=context.getResources().getString(R.string.version_release);
        bodyBuilder.append(temp);
        if (!temp.endsWith(":"))
            bodyBuilder.append(":");
        bodyBuilder.append(sysVersion);
        sendEmail(context,emailAddr,title,bodyBuilder.toString());
    }
    /**
      * @Description:
      * @param emailAddr 邮件地址
      * @return  判断地址格式正确与否的结果
      */

    public static boolean checkEmailAddressFormate(String emailAddr){
        Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}");
        Matcher matcher = p.matcher(emailAddr);
        return matcher.matches();
    }
}
