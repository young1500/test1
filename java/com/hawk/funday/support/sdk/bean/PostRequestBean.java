package com.hawk.funday.support.sdk.bean;

import java.util.List;


public class PostRequestBean {
    private String title;
    private String content;
    private List<ImageMetadata> urls;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<ImageMetadata> getUrls() {
        return urls;
    }

    public void setUrls(List<ImageMetadata> urls) {
        this.urls = urls;
    }
}
