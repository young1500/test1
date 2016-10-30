package com.hawk.funday.support.sdk.bean;

import java.io.Serializable;

public class UploadImageResultBean implements Serializable {

    private static final long serialVersionUID = -6073524249201534350L;

    private int width;

    private int height;

    private long contentLength;

    private String url;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLengh) {
        this.contentLength = contentLengh;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
