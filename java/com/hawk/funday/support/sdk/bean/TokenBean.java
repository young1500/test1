package com.hawk.funday.support.sdk.bean;

import java.io.Serializable;

/**
 * @author Liyang Sun
 * @Description:
 * @date 2016/9/19 19:00
 * @copyright HAWK
 */
public class TokenBean implements Serializable {

    private String token;
    private TokenUserBean user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public TokenUserBean getUser() {
        return user;
    }

    public void setUser(TokenUserBean user) {
        this.user = user;
    }

}
