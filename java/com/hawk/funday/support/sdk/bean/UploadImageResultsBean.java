package com.hawk.funday.support.sdk.bean;

import java.util.List;

public class UploadImageResultsBean extends BaseBean {

    private static final long serialVersionUID = 167395204739787525L;

    private List<UploadImageResultBean> data;

    public void setData(List<UploadImageResultBean> data) {
        this.data = data;
    }

    public List<UploadImageResultBean> getData() {
        return data;
    }
}
