package com.hawk.funday.support.cache;

import android.content.Context;

import com.hawk.funday.base.Consts;
import com.hawk.funday.support.db.FundayDB;
import com.hawk.funday.support.sdk.bean.PostBean;
import com.hawk.funday.support.sdk.bean.PostsBean;

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
 * @Description: Profile favorites 页面缓存
 * @date 2016/9/12 15:48
 * @copyright HAWK
 */
public class ProfileFavsCacheUtility implements ICacheUtility {

    private static final String TAG = "ProfileFavsCacheUtility";

    private static final String KEY_TIME = "com.hawk.funday.cache.profile.favs.KEY_TIME";

    @Override
    public IResult findCacheData(Setting setting, Params params) {
        Context context = GlobalContext.getInstance();
        if (context == null) {
            return null;
        }
        String offset = params.getParameter("offset");

        if (offset.equals(String.valueOf(0))) { // offset 为0才加载缓存，不是0不加载
            List<PostBean> list = null;
            try {
                list = FundayDB.getCacheDB().select(getDBExtra(), PostBean.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (list != null && list.size() != 0) {
                PostsBean result = new PostsBean();
                result.setResources(list);
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

        PostsBean result = (PostsBean) iResult;
        if (result.getResources() == null || result.getResources().size() == 0) {
            return;
        }

        int offset = result.getOffset();

        try {
            if (offset != 0) {
                // 清理缓存
                if (result.getPageSize() == result.getResources().size()) {
                    FundayDB.getCacheDB().deleteAll(getDBExtra(), PostBean.class);
                }
                Logger.d(TAG, "add clear cache");
                FundayDB.getCacheDB().insert(getDBExtra(), result.getResources());
                Logger.d(TAG, "add insert size = %d", result.getResources().size());
                CacheTimeUtils.saveTime(context, KEY_TIME);
            } else { // 首次加载
                FundayDB.getCacheDB().deleteAll(getDBExtra(), PostBean.class);
                Logger.d(TAG, "first clear cache");
                FundayDB.getCacheDB().insert(getDBExtra(), result.getResources());
                Logger.d(TAG, "first insert size = %d", result.getResources().size());
                CacheTimeUtils.saveTime(context, KEY_TIME);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Extra getDBExtra() {
        return new Extra(null, "pfavs");
    }
}
