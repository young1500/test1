package com.hawk.funday.support.utils;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.hawk.funday.R;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.network.task.IExceptionDeclare;
import org.aisen.android.network.task.TaskException;

/**
 * @author Liyang Sun
 * @Description:
 * @date 2016/9/18 13:44
 * @copyright HAWK
 */
public class FundayExceptionDelegate implements IExceptionDeclare {
    @Override
    public void checkResponse(String response) throws TaskException {
        // 暂时不用实现
    }

    @Override
    public String checkCode(String code) {
        final Context context = GlobalContext.getInstance();
        if (context != null) {
            if (!TextUtils.isEmpty(code)) {
                // timeout
                if (TaskException.TaskError.timeout.toString().equals(code) ||
                        TaskException.TaskError.socketTimeout.toString().equals(code)) {
                    return context.getString(R.string.comm_error_timeout);
                }
                // network none
                else if (TaskException.TaskError.noneNetwork.toString().equals(code) ) {
                    return context.getString(R.string.comm_error_none_network);
                }
            }
        }

        return "";
    }

    public static String getMessage(Activity context, TaskException exception, int defRes) {
        try {
            if (context != null) {
                String message = exception.getMessage();
                if (!TextUtils.isEmpty(message)) {
                    return message;
                }
                else {
                    return context.getString(defRes);
                }
            }
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }

        return "";
    }

}
