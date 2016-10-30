package com.hawk.funday.support.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.hawk.funday.base.AppContext;
import com.hawk.funday.support.sdk.bean.AccountBean;

/**
 * @author Liyang Sun
 * @Description:
 * @date 2016/8/22 16:17
 * @copyright HAWK
 */

public class TokenUtils {
    private static final String SP_ACCOUNT_BEAN = "com.hawk.funday.sp.account.bean";
    private static final String SP_TOKEN_UTILS = "com.hawk.funday.sp.token.utils";

    private SharedPreferences mSharedPreferences;

    public TokenUtils(Context context) {
        mSharedPreferences = context.getSharedPreferences(SP_TOKEN_UTILS, Context.MODE_APPEND);
    }

    public TokenUtils(Context context, String fileName) {
        mSharedPreferences = context.getSharedPreferences(fileName, Context.MODE_APPEND);
    }

    /**
     * Save AccountBean To SharedPreference
     */
    public void saveAccountBean(AccountBean accountBean) {
        String json = toJSON(accountBean);
        addStringData(SP_ACCOUNT_BEAN, json);
    }

    /**
     * Get AccountBean From SharedPreference
     */
    public AccountBean getAccountBean () {
        String json = getStringData(SP_ACCOUNT_BEAN);
        AccountBean accountBean = getBeanFromJson(json, AccountBean.class);
        return accountBean;
    }

    private void addStringData(String name, String data) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(name, data);
        editor.commit();
    }

    private String getStringData(String name) {
        return mSharedPreferences.getString(name, "");
    }

    /**
     * Change AccountBean To JSON
     */
    private String toJSON(Object obj){
        String json = JSON.toJSONString(obj);

        return json;
    }

    /**
     * Change JSON To UserBean
     */
    private <T> T getBeanFromJson(String json, Class<T> clazz) {
        try {
            if (!TextUtils.isEmpty(json))
                return JSON.parseObject(json, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void clearLoginInfo() {
        saveAccountBean(null); // 将SP中的AccountBean置空
        AppContext.setLoginedAccountBean(null); // 将上下文中的AccountBean置空
    }

}
