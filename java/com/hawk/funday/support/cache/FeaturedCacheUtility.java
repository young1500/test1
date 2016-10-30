package com.hawk.funday.support.cache;

import android.content.Context;
import android.text.TextUtils;

import com.hawk.funday.base.Consts;
import com.hawk.funday.support.db.FundayDB;
import com.hawk.funday.support.sdk.bean.PostBean;
import com.hawk.funday.support.sdk.bean.PostsBean;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.setting.Setting;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.common.utils.CacheTimeUtils;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.component.orm.extra.Extra;
import org.aisen.android.network.biz.IResult;
import org.aisen.android.network.cache.ICacheUtility;
import org.aisen.android.network.http.Params;

import java.util.List;

/**
 * FeaturedList缓存
 *
 * Created by wangdan on 16/9/6.
 */
public class FeaturedCacheUtility implements ICacheUtility {

    private static final String TAG = "SqliteUtility_featured";

    private static final String KEY_DOWN_REFRESH_ID = "com.hawk.funday.cache.feature.KEY_DOWN_REFRESH_ID";
    private static final String KEY_UP_REFRESH_ID = "com.hawk.funday.cache.feature.KEY_UP_REFRESH_ID";
    private static final String KEY_OFFSET = "com.hawk.funday.cache.feature.KEY_OFFSET";
    private static final String KEY_TIME = "com.hawk.funday.cache.feature.KEY_TIME";

    @Override
    public IResult findCacheData(Setting setting, Params params) {
        Context context = GlobalContext.getInstance();
        if (context == null) {
            return null;
        }
        String refreshId = params.getParameter("refreshId");

        // 第一次初始化
        if (TextUtils.isEmpty(refreshId)) {
            String downRefreshId = ActivityHelper.getShareData(context, KEY_DOWN_REFRESH_ID);
            String upRefreshId = ActivityHelper.getShareData(context, KEY_UP_REFRESH_ID);
            int offset = ActivityHelper.getIntShareData(context, KEY_OFFSET);
//            List<PostBean> list = FundayDB.getCacheDB().select(getDBExtra(), PostBean.class);
            List<PostBean> list = null;
            try {
                list = FundayDB.getCacheDB().select(PostBean.class, "com_m_common_key = ?", new String[]{"featured"}, null, null, "com_m_common_owner desc", null);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (list != null && !TextUtils.isEmpty(downRefreshId) && !TextUtils.isEmpty(upRefreshId)) {
                PostsBean result = new PostsBean();
                result.setResources(list);
                result.setFromCache(true);
                result.setOutofdate(CacheTimeUtils.isOutofdate(context, KEY_TIME, Consts.Cache.time));
                result.setOffset(offset);
                result.setPagingIndex(new String[]{ downRefreshId, upRefreshId });

                Logger.v(TAG, "findcache, list size = %d, offset = %d, downRefreshId = %s, upRefreshId = %s",
                                list.size(), offset, downRefreshId, upRefreshId);

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
        if (result.getResources() == null || result.getResources().size() == 0
                || "0".equals(result.getRefreshId())) {
            return;
        }

        String direction = params.getParameter("direction");
        String refreshId = params.getParameter("refreshId");

        try {
            // 第一次初始化
            if (TextUtils.isEmpty(refreshId) ||
                    // 下拉刷新
                    "down".equals(direction.toLowerCase())) {
                // 清理缓存
                FundayDB.getCacheDB().deleteAll(getDBExtra(result.getRefreshId()), PostBean.class);
                CacheTimeUtils.saveTime(context, KEY_TIME);
                Logger.w(TAG, "clear cache");
                FundayDB.getCacheDB().insert(getDBExtra(result.getRefreshId()), result.getResources());
                Logger.v(TAG, "insert size = %d", result.getResources().size());

                ActivityHelper.putShareData(context, KEY_DOWN_REFRESH_ID, result.getRefreshId());
                ActivityHelper.putShareData(context, KEY_UP_REFRESH_ID, result.getRefreshId());
                Logger.v(TAG, "refreshId = %s", result.getRefreshId());
                ActivityHelper.putIntShareData(context, KEY_OFFSET, result.getOffset());
                Logger.v(TAG, "offset = %d", result.getOffset());
            }
            // 加载更多
            else if ("up".equals(direction.toLowerCase())) {
                FundayDB.getCacheDB().insert(getDBExtra(result.getRefreshId()), result.getResources());
                Logger.v(TAG, "up insert size = %d", result.getResources().size());

                ActivityHelper.putShareData(context, KEY_UP_REFRESH_ID, result.getRefreshId());
                Logger.v(TAG, "up refreshId = %s", result.getRefreshId());
                ActivityHelper.putIntShareData(context, KEY_OFFSET, result.getOffset());
                Logger.v(TAG, "up offset = %d", result.getOffset());

                CacheTimeUtils.saveTime(context, KEY_TIME);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Extra getDBExtra(String mRefreshId) {
        return new Extra(mRefreshId, "featured");
    }

}
