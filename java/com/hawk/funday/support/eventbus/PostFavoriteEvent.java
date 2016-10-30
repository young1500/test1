package com.hawk.funday.support.eventbus;

import com.hawk.funday.support.sdk.bean.PostBean;

/**
 * Created by wangdan on 16/9/19.
 */
public class PostFavoriteEvent {

    private final PostBean tag;

    private final boolean fav;

    public PostFavoriteEvent(boolean fav, PostBean tag) {
        this.fav = fav;
        this.tag = tag;
    }

    public PostBean getTag() {
        return tag;
    }

    public boolean isFav() {
        return fav;
    }

}
