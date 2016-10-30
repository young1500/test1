package com.hawk.funday.support.sdk.bean;


import java.io.Serializable;

/**
 * @author Liyang Sun
 * @Description:
 * @date 2016/8/25 16:30
 * @copyright HAWK
 */

public class AccountBean implements Serializable {

    private FundayUserBean user;

    private String token;

    private String openId;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getOpenId() { // 从TokenObj中的User对象中拿到openId
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public long getUserId() { // 从user（FunDayUserBean）中拿到userId
        if (user != null && user.getId() != 0) {
            return user.getId();
        }

        return 0;
    }

    public FundayUserBean getUser() {
        return user;
    }

    public void setUser(FundayUserBean user) {
        this.user = user;
    }

}
