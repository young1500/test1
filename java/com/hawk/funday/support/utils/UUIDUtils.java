package com.hawk.funday.support.utils;

import android.content.Context;
import android.text.TextUtils;

import com.hawk.funday.base.AppContext;
import com.hawk.funday.support.sdk.FundaySDK;
import com.hawk.funday.support.sdk.bean.UUIDBean;

import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;

/**
 * @author Liyang Sun
 * @Description:
 * @date 2016/9/2 17:19
 * @copyright HAWK
 */
public class UUIDUtils {

    private static final String TAG = "UUIDUtils";
    private static final String SP_UUID = "com.hawk.funday.sp.uuid";

    public static void requestUUID(final Context context) {

        final String aid = FundayUtils.getAID(context);
        final String did = FundayUtils.getIMSI(context);

        new WorkTask<Void, Void, Void>() {

            @Override
            public Void workInBackground(Void... voids) throws TaskException {
                UUIDBean bean = FundaySDK.newInstance().getUUID(aid, did);
                if (bean != null && !TextUtils.isEmpty(bean.getUuid())) {
                    ActivityHelper.putShareData(context, SP_UUID, bean.getUuid());
                    AppContext.setUuid(bean.getUuid());
                } else {
                    Logger.d(TAG, "UUID is null!!");
                }

                return null;
            }
        }.execute();
    }

    public static String getUUID(Context context) {
        return ActivityHelper.getShareData(context, SP_UUID);
    }

}
