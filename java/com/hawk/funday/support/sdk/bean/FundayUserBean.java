package com.hawk.funday.support.sdk.bean;

import java.io.Serializable;

/**
 * Created by wangdan on 16/8/22.
 */
public class FundayUserBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 77190482505517319L;

    private long id;

    private String name;

    private String avatar;

    private String email;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
