package com.hawk.funday.support.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.wcc.framework.crypt.MD5;
import com.wcc.framework.util.PrefsUtils;
import com.wcc.framework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * Created by wenbiao.xie on 2016/7/22.
 */
public class TelephonyManagerUtil {
    private static final String PREF_DEVICE_ID = "IMEI_CACHE";
    /*
	 * 唯一的设备ID： GSM手机的 IMEI 和 CDMA手机的 MEID. Return null if device ID is not
	 * available.
	 */
    public static String getDeviceId(Context context){
        boolean needCached = false;
        String id = null;
        boolean ignore = false;
        if (aboveApiLevel(Build.VERSION_CODES.M)) {
            String cached = getPrefDeviceId(context);
            if (!TextUtils.isEmpty(cached))
                return cached;

            int perms = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE);
            if (perms == PackageManager.PERMISSION_GRANTED) {
                needCached = true;
            } else {
                ignore = true;
            }
        }

        if (!ignore) {
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            id = tm.getDeviceId();
        }

        if (TextUtils.isEmpty(id)) {
            String aid = getSecureAndroidID(context);
            if (!TextUtils.isEmpty(aid))
                id = "AID_" + aid;
        }

        if (TextUtils.isEmpty(id)) {
            String uuid = UUID.randomUUID().toString();
            try {
                byte[] byes = MD5.encode16(uuid, "utf-8");
                id = "UUID_" + StringUtils.bytesToHexes(byes);
            } catch (UnsupportedEncodingException e) {

            }
        }

        if (!TextUtils.isEmpty(id) && needCached) {
            savePrefDeviceId(context, id);
        }

        return id;
    }

    public static boolean isIMEI(String deviceId) {
        if (TextUtils.isEmpty(deviceId))
            return false;

        return deviceId.startsWith("AID_") || deviceId.startsWith("UUID_");
    }

    public static String getPrefDeviceId(Context context) {
        return PrefsUtils.loadPrefString(context, PREF_DEVICE_ID);
    }

    public static void savePrefDeviceId(Context context, String value) {
        PrefsUtils.savePrefString(context, PREF_DEVICE_ID, value);
    }

    public static boolean aboveApiLevel(int paramInt) {
        return getApiLevel() >= paramInt;
    }

    public static int getApiLevel() {
        return Build.VERSION.SDK_INT;
    }

    public static String getSecureAndroidID(Context paramContext) {
        return Settings.Secure.getString(paramContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }
}
