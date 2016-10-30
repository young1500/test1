package com.hawk.funday.support.utils;

import com.hawk.funday.support.db.FundayDB;
import com.hawk.funday.support.sdk.FundaySDK;
import com.hawk.funday.support.sdk.bean.APILog;

import org.aisen.android.common.setting.Setting;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;

import java.util.List;

/**
 * 用于API请求日志上报
 *
 * @author yong.zeng
 * @Description:
 * @date 2016/10/12 15 20
 * @copyright TCL-MIE
 */
public class APILogReportUtils {

    // TODO 提供一个保存日志的接口
    public static void saveLog(Setting action, long duration) {

        if(AppConfigsUtils.getAPIConfigs()!=null){
            for(String value : AppConfigsUtils.getAPIConfigs()){
                if(value.equals(action.getValue())){
                    try {
                        FundayDB.getCacheDB().insert(null, new APILog(action.getValue(), duration));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // TODO 上报日志
    public static void doRepost() {

        new WorkTask<Void, Void, Void>() {
            @Override
            public Void workInBackground(Void... voids) throws TaskException {
                try {
                    int count = 5;
                    while (--count > 0) {
                        List<APILog> list = FundayDB.getCacheDB().select(APILog.class, null, null, null, null, "id desc", "10");
                        if (list.size() == 0) {
                            break;
                        }
                        else {
                            FundaySDK.newInstance().upElapse(list);
                            for (APILog log : list) {
                                FundayDB.getCacheDB().deleteById(null, APILog.class, log.getId() + "");
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

}
