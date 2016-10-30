package com.hawk.funday.support.sdk.bean;

import com.hawk.funday.support.paging.IOffsetResult;

import org.aisen.android.support.bean.ResultBean;

import java.io.Serializable;

/**
 * Funday基础Bean
 *
 * Created by wangdan on 16/8/18.
 */
public class BaseBean extends ResultBean implements Serializable, IOffsetResult {

    private static final long serialVersionUID = 2414186251403297106L;

    private int code;

    private String msg;

    private int offset;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

}
