package com.hawk.funday.support.cache;

import android.content.Context;

import com.hawk.funday.base.Consts;
import com.hawk.funday.support.db.FundayDB;
import com.hawk.funday.support.sdk.bean.CommentBean;
import com.hawk.funday.support.sdk.bean.CommentsBean;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.setting.Setting;
import org.aisen.android.common.utils.CacheTimeUtils;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.component.orm.extra.Extra;
import org.aisen.android.network.biz.IResult;
import org.aisen.android.network.cache.ICacheUtility;
import org.aisen.android.network.http.Params;

import java.util.List;

/**
 * @author Liyang Sun
 * @Description:
 * @date 2016/9/12 15:46
 * @copyright HAWK
 */
public class ProfileCmtsCacheUtility implements ICacheUtility {

    private static final String TAG = "ProfileCmtsCacheUtility";

    private static final String KEY_TIME = "com.hawk.funday.cache.profile.cmts.KEY_TIME";

    @Override
    public IResult findCacheData(Setting setting, Params params) {
        Context context = GlobalContext.getInstance();
        if (context == null) {
            return null;
        }
        String offset = params.getParameter("offset");

        if (offset.equals(String.valueOf(0))) { // offset 为0才加载缓存，不是0不加载
            List<CommentBean> list = null;
            try {
                list = FundayDB.getCacheDB().select(getDBExtra(), CommentBean.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (list != null && list.size() != 0) {
                CommentsBean result = new CommentsBean();
                result.setComments(list);
                result.setFromCache(true);
                result.setOutofdate(CacheTimeUtils.isOutofdate(context, KEY_TIME, Consts.Cache.time));

                Logger.d(TAG, "Find Cache, list size = %d", list.size());
                return result;
            }
        }

        return null;
    }

    @Override
    public void addCacheData(Setting setting, Params params, IResult iResult) {
        Context context = GlobalContext.getInstance();
        if (context == null) {
            return;
        }

        CommentsBean result = (CommentsBean) iResult;
        if (result.getComments() == null || result.getComments().size() == 0) {
            return;
        }

        int offset = result.getOffset();

        try {
            if (offset != 0) {
                // 清理缓存
                if (result.getPageSize() == result.getComments().size()) {
                    FundayDB.getCacheDB().deleteAll(getDBExtra(), CommentBean.class);
                }
                Logger.d(TAG, "Clear cache");
                FundayDB.getCacheDB().insert(getDBExtra(), result.getComments());
                Logger.d(TAG, "Insert size = %d", result.getComments().size());
                CacheTimeUtils.saveTime(context, KEY_TIME);
            } else { // 首次加载
                FundayDB.getCacheDB().deleteAll(getDBExtra(), CommentBean.class);
                Logger.d(TAG, "first clear cache");
                FundayDB.getCacheDB().insert(getDBExtra(), result.getComments());
                Logger.d(TAG, "first insert size = %d", result.getComments().size());
                CacheTimeUtils.saveTime(context, KEY_TIME);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Extra getDBExtra() {
        return new Extra(null, "pcmts");
    }
}
