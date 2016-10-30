package com.hawk.funday.support.sdk.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wangdan on 16/8/17.
 */
public class PostsBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -6535672211595049820L;

    private String refreshId;

    private int pageSize;

    private long interval;

    private List<PostBean> resources;

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public List<PostBean> getResources() {
        return resources;
    }

    public void setResources(List<PostBean> resources) {
        this.resources = resources;
    }

    public String getRefreshId() {
        return refreshId;
    }

    public void setRefreshId(String refreshId) {
        this.refreshId = refreshId;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
