package com.hawk.funday.support.sdk.bean;


public class ImageMetadata{
    private int width; ///预览图片宽度
    private int height;////预览图片高度
    private long contentLength; ///文件大小，单位byte
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