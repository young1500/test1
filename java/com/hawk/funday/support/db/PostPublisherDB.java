package com.hawk.funday.support.db;

import com.hawk.funday.base.AppContext;
import com.hawk.funday.support.sdk.bean.UploadBean;

import org.aisen.android.component.orm.extra.Extra;

import java.util.ArrayList;
import java.util.List;

/**
 * 发布Post的DB
 *
 * Created by wangdan on 16/9/18.
 */
public class PostPublisherDB {

    static Extra getExtra() {
        return new Extra(AppContext.getLoginedAccount().getUserId() + "", null);
    }

    static boolean check() throws Exception {
        return FundayDB.getDB() != null &&
                AppContext.getLoginedAccount() != null;
    }

    public static void deleteById(long notifyId) {
        try {
            if (check()) {
                FundayDB.getDB().deleteById(getExtra(), UploadBean.class, notifyId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<UploadBean> select() {
        try {
            if (check()) {
                return FundayDB.getDB().select(getExtra(), UploadBean.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public static UploadBean selectById(int redID) {
        try {
            if (check()) {
                return FundayDB.getDB().selectById(getExtra(), UploadBean.class, redID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void insert(UploadBean bean) {
        try {
            if (check()) {
                FundayDB.getDB().insert(getExtra(), bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void update(UploadBean bean) {
        try {
            if (check()) {
                FundayDB.getDB().update(getExtra(), bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
