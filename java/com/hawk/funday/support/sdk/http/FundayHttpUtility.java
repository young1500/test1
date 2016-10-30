package com.hawk.funday.support.sdk.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hawk.funday.support.sdk.bean.BaseBean;
import com.hawk.funday.support.utils.APILogReportUtils;

import org.aisen.android.common.setting.Setting;
import org.aisen.android.network.http.DefHttpUtility;
import org.aisen.android.network.http.HttpConfig;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;

/**
 * Created by wangdan on 16/8/19.
 */
public class FundayHttpUtility extends DefHttpUtility {
    @Override
    public <T> T doGet(HttpConfig config, Setting action, Params urlParams, Class<T> responseCls) throws TaskException {
        long currentTime=System.currentTimeMillis();

        T t = super.doGet(config, action, urlParams, responseCls);

        try {
            APILogReportUtils.saveLog(action, System.currentTimeMillis() - currentTime);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return  t;
    }

    @Override
    public <T> T doPost(HttpConfig config, Setting action, Params urlParams, Params bodyParams, Object requestObj, Class<T> responseCls) throws TaskException {
        long currentTime = System.currentTimeMillis();

        T t = super.doPost(config, action, urlParams, bodyParams, requestObj, responseCls);

        try {
            APILogReportUtils.saveLog(action, System.currentTimeMillis() - currentTime);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return  t;
    }

    @Override
    public <T> T doPostFiles(HttpConfig config, Setting action, Params urlParams, Params bodyParams, MultipartFile[] files, Class<T> responseCls) throws TaskException {
        long currentTime = System.currentTimeMillis();
        T t = super.doPostFiles(config, action, urlParams, bodyParams, files, responseCls);

        try {
            APILogReportUtils.saveLog(action,System.currentTimeMillis() - currentTime);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return  t;
    }

    @Override
    protected <T> T parseResponse(String resultStr, Class<T> responseCls) throws TaskException {
        try {
            JSONObject jsonObject = JSON.parseObject(resultStr);

            if (jsonObject.getInteger("code") != 200)
                throw new TaskException(jsonObject.getInteger("code") + "", jsonObject.getString("msg"));

            T result = super.parseResponse(jsonObject.getString("data"), responseCls);

            if (result instanceof BaseBean) {
                BaseBean baseBean = (BaseBean) result;
                if (jsonObject.containsKey("offset"))
                    baseBean.setOffset(jsonObject.getInteger("offset"));
                if (jsonObject.containsKey("code"))
                    baseBean.setCode(jsonObject.getInteger("code"));
                if (jsonObject.containsKey("msg"))
                    baseBean.setMsg(jsonObject.getString("msg"));
            }

            return result;
        } catch (Throwable e) {
            if (e instanceof TaskException) {
                throw e;
            }
            throw new TaskException(TaskException.TaskError.resultIllegal.toString());
        }
    }

}
