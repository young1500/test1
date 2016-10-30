package com.hawk.funday.ui.activity.base;

import com.hawk.funday.support.sdk.bean.AccountBean;

/**
 * Created by wangdan on 16/9/21.
 */
public interface OnUserLoginCallback {

    void onSuccess(AccountBean accountBean);

    void onFaild();

}
