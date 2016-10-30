package com.hawk.funday.support.paging;

import com.hawk.funday.support.sdk.bean.PostBean;
import com.hawk.funday.support.sdk.bean.PostsBean;

import org.aisen.android.support.paging.IPaging;

/**
 * Created by yijie.ma on 2016/9/5.
 */
public class FeaturePaging implements IPaging<PostBean, PostsBean> {

    private static final long serialVersionUID = 2857854440055871747L;
    private String downRefreshId;
    private String upRefreshId;

    @Override
    public void processData(PostsBean postsBean, PostBean postBean, PostBean t1) {

    }

    public String getDownRefreshId() {
        return downRefreshId;
    }

    public void setDownRefreshId(String downRefreshId) {
        this.downRefreshId = downRefreshId;
    }

    public String getUpRefreshId() {
        return upRefreshId;
    }

    public void setUpRefreshId(String upRefreshId) {
        this.upRefreshId = upRefreshId;
    }

    @Override
    public String getPreviousPage() {
        return downRefreshId;
    }

    @Override
    public String getNextPage() {
        return upRefreshId;
    }

}
