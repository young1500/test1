package com.hawk.funday.support.sdk.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author yong.zeng
 * @Description:
 * @date 2016/10/12 14 53
 * @copyright TCL-MIE
 */
public class APPConfsBean extends BaseBean implements Serializable {

    private List<String> elapse;

    public List<String> getElapse() {
        return elapse;
    }

    public void setElapse(List<String> elapse) {
        this.elapse = elapse;
    }

}
