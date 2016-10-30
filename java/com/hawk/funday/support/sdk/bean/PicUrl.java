package com.hawk.funday.support.sdk.bean;

import java.io.Serializable;

/**
 * Created by wangdan on 16/8/18.
 */
public class PicUrl implements Serializable {

    private static final long serialVersionUID = 3006882069717135318L;

    private int width;

    private int height;

    private long contentLength;

    private String url;

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
