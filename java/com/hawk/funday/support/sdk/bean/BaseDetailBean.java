package com.hawk.funday.support.sdk.bean;

/**
 * @Description: BaseDetailBean 多媒体的基础信息对象
 * @author  qiangtai.huang
 * @date  2016/8/23
 * @copyright TCL-HAWK
 */
public class  BaseDetailBean extends BaseBean {
     private long id;
     private  long resourceId;
     private  int commentCount; ////评论数量
     private  String title; /////标题
     private  ThumbnailBean thumbnail; ///预览图数据对象

    public long getResourceId() {
        return resourceId;
    }

    public void setResourceId(long resourceId) {
        this.resourceId = resourceId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ThumbnailBean getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(ThumbnailBean thumbnail) {
        this.thumbnail = thumbnail;
    }
}
