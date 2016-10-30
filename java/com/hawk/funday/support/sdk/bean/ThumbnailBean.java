package com.hawk.funday.support.sdk.bean;

import java.io.Serializable;

public class ThumbnailBean implements Serializable{
    private int width; ///预览图片宽度
    private int height;////预览图片高度
    private int fileSize; ///文件大小，单位byte
    private String url; ///文件的网络地址

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

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
