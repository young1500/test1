package com.hawk.funday.base;

import com.hawk.funday.support.sdk.bean.AccountBean;

import org.aisen.android.common.utils.Logger;

/**
 * 应用上下文
 *
 * Created by wangdan on 16/8/17.
 */
final public class AppContext {
    private static final String TAG = "AppContext";

    private AppContext() {

    }

    private static String uuid;

    private static AccountBean mAccount;

    public static void setLoginedAccountBean(AccountBean account) {
        mAccount = account;
        Logger.d(TAG + "_Account", "Saved Logined AccountBean");
    }

    public static AccountBean getLoginedAccount() {
        return mAccount;
    }

    public static String getUuid() {
        return uuid;
    }

    public static void setUuid(String uuid) {
        AppContext.uuid = uuid;
    }

}
