package com.hawk.funday.support.sdk.bean;

import org.aisen.android.component.orm.annotation.PrimaryKey;
import org.aisen.android.ui.fragment.adapter.IPagingAdapter;

import java.io.Serializable;

/**
 * Created by wangdan on 16/8/17.
 */
public class PostBean implements Serializable, IPagingAdapter.ItemTypeData {

    private static final long serialVersionUID = -4188766171881496758L;

    @PrimaryKey(column = "id")
    private long id;

    private long resourceId;// 分页id

    private int resourceType;

    private long commentCount;

    private String title;

    private PicUrl[] thumbnailUrls;// 缩略图地址

    private PicUrl[] urls;// 原图地址

    private String url;// 文章或者视频链接

    private long duration;// 视频时长

    private String content;// 文章内容

    private String type;//视频连接类型，html和raw和piiic

    private String share;//分享链接

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getResourceId() {
        return resourceId;
    }

    public void setResourceId(long resourceId) {
        this.resourceId = resourceId;
    }

    public int getResourceType() {
        return resourceType;
    }

    public void setResourceType(int resourceType) {
        this.resourceType = resourceType;
    }

    public long getCommentCount() {
        if (commentCount < 0) {
            commentCount = 0;
        }

        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        if (commentCount < 0) {
            commentCount = 0;
        }

        this.commentCount = commentCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public PicUrl[] getThumbnailUrls() {
        return thumbnailUrls;
    }

    public void setThumbnailUrls(PicUrl[] thumbnailUrls) {
        this.thumbnailUrls = thumbnailUrls;
    }

    public PicUrl[] getUrls() {
        return urls;
    }

    public void setUrls(PicUrl[] urls) {
        this.urls = urls;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public int itemType() {
        return getResourceType();
    }

    private boolean isfavorite; ////是否被收藏

    public boolean isFavorite() {
        return isfavorite;
    }

    public void setFavorite(boolean favorite) {
        isfavorite = favorite;
    }

    private FundayUserBean user;

    public FundayUserBean getUser() {
        return user;
    }

    public void setUser(FundayUserBean user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getShare() {
        return share;
    }

    public void setShare(String share) {
        this.share = share;
    }
}
