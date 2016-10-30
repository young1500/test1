package com.hawk.funday.support.sdk.bean;

import java.io.Serializable;

/**
 * @author Liyang Sun
 * @Description:
 * @date 2016/9/19 19:07
 * @copyright HAWK
 */
public class TokenUserBean implements Serializable {

    private String accountName;

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

}
