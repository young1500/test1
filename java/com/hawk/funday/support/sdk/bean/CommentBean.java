package com.hawk.funday.support.sdk.bean;

import org.aisen.android.component.orm.annotation.PrimaryKey;

import java.io.Serializable;

/**
 * @Description: CommentBean 评论数据对象
 * @author  qiangtai.huang
 * @date  2016/8/22
 * @copyright TCL-HAWK
 */
public class CommentBean implements Serializable {

    private static final long serialVersionUID = 476910704501775313L;

    @PrimaryKey(column = "id")
    private long id;///评论ID

    private String content; ////评论的内容

    private long publishTime; /////评论的事件戳

    private PostBean resource;

    private FundayUserBean user;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(long publishTime) {
        this.publishTime = publishTime;
    }

    public FundayUserBean getUser() {
        return user;
    }

    public void setUser(FundayUserBean user) {
        this.user = user;
    }

    public PostBean getResource() {
        return resource;
    }

    public void setResource(PostBean resource) {
        this.resource = resource;
    }
}
