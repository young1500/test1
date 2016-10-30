package com.hawk.funday.support.paging;

import org.aisen.android.support.paging.IPaging;

import java.io.Serializable;

/**
 * Created by wangdan on 16/8/18.
 */
public class OffsetPaging<T extends Serializable, Ts extends IOffsetResult> implements IPaging<T, Ts> {

    private static final long serialVersionUID = -7856476427268282622L;

    private int offset = 0;

    @Override
    public void processData(Ts ts, T t, T t1) {
        if (ts != null) {
            offset = ts.getOffset();
        }
    }

    @Override
    public String getPreviousPage() {
        return null;
    }

    @Override
    public String getNextPage() {
        return String.valueOf(offset);
    }

}
