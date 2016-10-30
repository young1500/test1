package com.hawk.funday.support.sdk.bean;

import org.aisen.android.component.orm.annotation.AutoIncrementPrimaryKey;

import java.io.Serializable;

/**
 * @author yong.zeng
 * @Description:
 * @date 2016/10/12 20 15
 * @copyright TCL-MIE
 */
public class APILog implements Serializable {

    @AutoIncrementPrimaryKey(column = "id")
    private long id;

    private String api;

    private long duration;

    public APILog() {

    }

    public APILog(String api, long duration) {
        this.api = api;
        this.duration = duration;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

}
