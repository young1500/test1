package com.hawk.funday.support.utils;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import com.hawk.funday.R;
import com.hawk.funday.base.AppContext;
import com.hawk.funday.support.sdk.bean.AccountBean;
import com.hawk.funday.support.sdk.bean.CommentBean;
import com.hawk.funday.support.sdk.bean.PostBean;
import com.hawk.funday.ui.activity.base.ContainerActivity;
import com.hawk.funday.ui.activity.base.MainActivity;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.DateUtils;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.common.utils.Utils;
import org.aisen.android.component.cardmenu.CardMenuOptions;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.ATabsFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 工具类
 *
 * Created by wangdan on 16/8/17.
 */
public class FundayUtils {
    private static final String TAG = "FundayUtils";

    /**
     * dp to px
     *
     * @param context
     * @param dip
     * @return
     */
    public static int convertDIP2PX(Context context, int dip) {
        return Utils.dip2px(context, dip);
    }

    public static String convDate(long timeMillis) {
        try {
            Context context = GlobalContext.getInstance();
            Resources res = context.getResources();

            StringBuffer buffer = new StringBuffer();

            Calendar createCal = Calendar.getInstance();
            createCal.setTime(new Date(timeMillis));

            Calendar currentcal = Calendar.getInstance();
            currentcal.setTimeInMillis(System.currentTimeMillis());

            long diffTime = (currentcal.getTimeInMillis() - createCal.getTimeInMillis()) / 1000;

            // 同一月
            if (currentcal.get(Calendar.MONTH) == createCal.get(Calendar.MONTH)) {
                // 同一天
                if (currentcal.get(Calendar.DAY_OF_MONTH) == createCal.get(Calendar.DAY_OF_MONTH)) {
                    if (diffTime < 3600 && diffTime >= 60) {
                        buffer.append((diffTime / 60) + " " + res.getString(R.string.msg_few_minutes_ago));
                    } else if (diffTime < 60) {
                        buffer.append(res.getString(R.string.msg_now));
                    } else {
                        buffer.append(res.getString(R.string.msg_today)).append(" ").append(DateUtils.formatDate(createCal.getTimeInMillis(), "HH:mm"));
                    }
                }
                // 前一天
                else if (currentcal.get(Calendar.DAY_OF_MONTH) - createCal.get(Calendar.DAY_OF_MONTH) == 1) {
                    buffer.append(res.getString(R.string.msg_yesterday)).append(" ").append(DateUtils.formatDate(createCal.getTimeInMillis(), "HH:mm"));
                }
            }

            if (buffer.length() == 0) {
                buffer.append(DateUtils.formatDate(createCal.getTimeInMillis(), "MM-dd HH:mm"));
            }

            String timeStr = buffer.toString();
            if (currentcal.get(Calendar.YEAR) != createCal.get(Calendar.YEAR)) {
                timeStr = createCal.get(Calendar.YEAR) + " " + timeStr;
            }

            return timeStr;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return "-";
    }

    public static int getThemeColor(Context context) {
        return context.getResources().getColor(R.color.comm_black);
    }

    // 获取AID
    public static String getAID(Context context) {
        String aid = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
        if (!TextUtils.isEmpty(aid)) {
            return aid;
        }

        return null;
    }

    // 获取IMSI（DID）
    public static String getIMSI(Context context) {
        String imsi = null;

        try {
            if (Build.VERSION.SDK_INT < 23) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

                imsi =  telephonyManager.getSubscriberId();
            }
            else {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

                    imsi =  telephonyManager.getSubscriberId();
                }
            }
        } catch (Throwable e) {
            Logger.printExc(UUIDUtils.class, e);
        }

        return imsi;
    }

    public static boolean checkTabsFragmentCanRequestData(Fragment checkedFragment) {
        if (checkedFragment == null || checkedFragment.getActivity() == null)
            return false;

        try {
            ABaseFragment aFragment = null;
            if (checkedFragment.getActivity() instanceof ContainerActivity) {
                aFragment = (ABaseFragment) checkedFragment.getActivity().getFragmentManager().findFragmentByTag(ContainerActivity.FRAGMENT_TAG);
            }
            else if (checkedFragment.getActivity() instanceof MainActivity) {
                aFragment = (ABaseFragment) checkedFragment.getActivity().getFragmentManager().findFragmentByTag("MainFragment");
            }

            if (aFragment != null && aFragment instanceof ATabsFragment) {
                ATabsFragment fragment = (ATabsFragment) aFragment;
                return fragment.getCurrentFragment() == checkedFragment;
            }
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }

        return false;
    }

    public static void updatePostComment(PostBean bean) {

    }

    public static boolean saveImageToGallery(Context context, File bmp, String saveName, String savePath) {
        // 首先保存图片
        File appDir = new File(savePath);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        File file = new File(appDir, saveName);

        if (file.exists()) {
            return true;
        }

        FileOutputStream fos = null;
        FileInputStream fis = null;
        try {
            file.createNewFile();
            fos = new FileOutputStream(file);
            fis = new FileInputStream(bmp);
            byte[] bytes = new byte[1024];
            int readed;
            while ((readed = fis.read(bytes)) != -1) {
                fos.write(bytes, 0, readed);
            }
            fos.close();
            fis.close();

            // 其次把文件插入到系统图库
            SystemUtils.scanPhoto(context, file);

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null)
                    fos.close();
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static void SaveBitmapToFile(Bitmap bitmap, String saveName) {
        File file = new File("/sdcard/myFolder");
        if (!file.exists())
            file.mkdir();

        file = new File("/sdcard/temp.png".trim());
        String fileName = file.getName();
        String mName = saveName;
        String sName = fileName.substring(fileName.lastIndexOf("."));

        String newFilePath = "/sdcard/myFolder" + "/" + mName + "_cropped" + sName;
        file = new File(newFilePath);
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public final static CardMenuOptions getCardMenuOptions(int theme) {
        return new CardMenuOptions(theme,
                        android.support.v7.appcompat.R.attr.actionOverflowMenuStyle,
                        android.support.v7.appcompat.R.layout.abc_action_menu_layout,
                        android.support.v7.appcompat.R.layout.abc_action_menu_item_layout);
    }


    public static boolean isLoginedUser(long userId) {
        AccountBean account = AppContext.getLoginedAccount();
        return account != null && account.getUser() != null && userId == account.getUser().getId();
    }

    public static void animScale(final View likeView) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.5f, 1.0f, 1.5f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(200);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.start();
        likeView.startAnimation(scaleAnimation);
        likeView.postDelayed(new Runnable() {

            @Override
            public void run() {
                ScaleAnimation scaleAnimation = new ScaleAnimation(1.5f, 1.0f, 1.5f, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(200);
                scaleAnimation.setFillAfter(true);
                likeView.startAnimation(scaleAnimation);
            }

        }, 200);
    }

    public static boolean postDestory(List<PostBean> list, PostBean postBean) {
        if (list != null && postBean != null && list.size() > 0) {
            if (list.remove(postBean)) {
                return true;
            }
            else {
                for (int i = 0; i < list.size(); i++) {
                    PostBean bean = list.get(i);
                    if (bean.getResourceId() == postBean.getResourceId()) {
                        list.remove(bean);

                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static int refreshPostFavStatus(List<PostBean> list, PostBean postBean) {
        if (list != null && postBean != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                PostBean bean = list.get(i);
                if (bean.getResourceId() == postBean.getResourceId()) {
                    bean.setFavorite(postBean.isFavorite());
                    bean.setCommentCount(postBean.getCommentCount());

                    return i;
                }
            }
        }

        return -1;
    }

    public static boolean removeCommentStatus(List<CommentBean> list, CommentBean comment) {
        if (list != null && comment != null && list.size() > 0) {
            if (!list.remove(comment)) {
                for (int i = 0; i < list.size(); i++) {
                    CommentBean bean = list.get(i);
                    if (bean.getId() == comment.getId()) {
                        list.remove(bean);

                        return true;
                    }
                }
            }
            else {
                return true;
            }
        }

        return false;
    }

    public static Bitmap setImageCorner(Bitmap source, float roundPx) {

        Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(result);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, source.getWidth(), source.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        paint.setXfermode(null);
        paint.setAlpha(255);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0, 0, paint);
        return result;
    }

}
