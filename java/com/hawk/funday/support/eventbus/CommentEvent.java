package com.hawk.funday.support.eventbus;

import com.hawk.funday.support.sdk.bean.CommentBean;
import com.hawk.funday.support.sdk.bean.PostBean;

/**
 * Post评论发送改变时的事件
 *
 * Created by wangdan on 16/9/19.
 */
public class CommentEvent {

    public enum Type {
        create, destory
    }

    private final PostBean post;

    private final CommentBean comment;

    private final Type type;

    public CommentEvent(Type type, PostBean post, CommentBean comment) {
        this.type = type;
        this.post = post;
        this.comment = comment;
    }

    public CommentBean getComment() {
        return comment;
    }

    public PostBean getPost() {
        return post;
    }

    public Type getType() {
        return type;
    }

}
