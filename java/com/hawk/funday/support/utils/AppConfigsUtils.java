package com.hawk.funday.support.utils;

import android.content.SharedPreferences;
import android.util.Log;

import com.hawk.funday.support.sdk.FundaySDK;
import com.hawk.funday.support.sdk.bean.APPConfsBean;

import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;

import java.util.List;

/**
 * 用于管理服务的配置信息工具类
 *
 * @author yong.zeng
 * @Description:
 * @date 2016/10/12 15 20
 * @copyright TCL-MIE
 */
public class AppConfigsUtils {
    private static APPConfsBean confBean;
    /**
     * 刷新服务的配置信息，将配置信息持久化
     *
     */
    public static void loadAppConfigs() {
        new WorkTask<Void, Void, Void>() {
            @Override
            public Void workInBackground(Void... voids) throws TaskException {
                confBean = FundaySDK.newInstance().setConf();
                return null;
            }
        }.execute();
    }

    /**
     * API接口日志统计配置
     *
     * @return
     */
    public static List<String> getAPIConfigs() {
        if(confBean!=null){
           return confBean.getElapse();
        }
        return null;
    }

}
