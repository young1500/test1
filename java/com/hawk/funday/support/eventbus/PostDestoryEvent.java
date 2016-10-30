package com.hawk.funday.support.eventbus;

import com.hawk.funday.support.sdk.bean.PostBean;

/**
 * Created by wangdan on 16/9/19.
 */
public class PostDestoryEvent {

    private PostBean post;

    public PostDestoryEvent(PostBean post) {
        this.post = post;
    }

    public PostBean getPost() {
        return post;
    }

}
