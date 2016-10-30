package com.hawk.funday.support.sdk.bean;

import java.util.List;

/**
 * Created by wangdan on 16/8/24.
 */
public class CommentsBean extends BaseBean {

    private static final long serialVersionUID = 1842696736844587397L;

    private List<CommentBean> comments;

    private int pageSize;

    public List<CommentBean> getComments() {
        return comments;
    }

    public void setComments(List<CommentBean> comments) {
        this.comments = comments;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
