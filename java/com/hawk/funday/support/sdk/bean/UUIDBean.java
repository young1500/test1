package com.hawk.funday.support.sdk.bean;

import java.io.Serializable;

/**
 * @author Liyang Sun
 * @Description:
 * @date 2016/9/8 13:56
 * @copyright HAWK
 */
public class UUIDBean implements Serializable {

    private static final long serialVersionUID = -278891466968376835L;

    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}
