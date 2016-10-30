package com.hawk.funday.support.paging;

import com.hawk.funday.support.sdk.bean.PostBean;
import com.hawk.funday.support.sdk.bean.PostsBean;

import org.aisen.android.support.paging.IPaging;

/**
 * Created by wangdan on 16/8/26.
 */
public class PostTimelinePaging implements IPaging<PostBean, PostsBean> {

    private static final long serialVersionUID = 448141080418890598L;

    private long topId = 0;
    private long bottomId = 0;

    @Override
    public void processData(PostsBean postsBean, PostBean postBean, PostBean t1) {
        if (postBean != null && t1 != null) {
            topId = postBean.getId();
            bottomId = t1.getId();
        }
    }

    @Override
    public String getPreviousPage() {
        if (topId > 0) {
            return String.valueOf(topId);
        }

        return null;
    }

    @Override
    public String getNextPage() {
        if (bottomId > 0) {
            return String.valueOf(bottomId);
        }

        return null;
    }
}
