package com.hawk.funday.support.cache;

import android.content.Context;
import android.text.TextUtils;

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
 * Created by yijie.ma on 2016/9/7.
 */
public class VideoCacheUtility implements ICacheUtility {

    private static final String TAG = "SqliteUtility_video";

    private static final String KEY_TIME = "com.hawk.funday.cache.video.KEY_TIME";

    @Override
    public IResult findCacheData(Setting setting, Params params) {
        Context context = GlobalContext.getInstance();
        if (context == null) {
            return null;
        }
        String topId = params.getParameter("topId");
        String bottomId = params.getParameter("bottomId");

        if (TextUtils.isEmpty(topId) && TextUtils.isEmpty(bottomId)) {//不是刷新也不是加载更多，才去读缓存
            List<PostBean> list = null;
            try {
                list = FundayDB.getCacheDB().select(PostBean.class, "com_m_common_key = ?", new String[]{"video"}, null, null, "id desc", null);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (list != null && list.size()!=0) {
                PostsBean result = new PostsBean();
                result.setResources(list);
                result.setFromCache(true);
                result.setOutofdate(CacheTimeUtils.isOutofdate(context, KEY_TIME, Consts.Cache.time));

                Logger.v(TAG, "findcache, list size = %d",
                        list.size());
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
        String topId = params.getParameter("topId");
        String bottomId = params.getParameter("bottomId");

        try {
            if (!TextUtils.isEmpty(topId) && !topId.equals("0")) {// 下拉刷新
                if (result.getPageSize() == result.getResources().size()) {
                    // 清理缓存
                    FundayDB.getCacheDB().deleteAll(getDBExtra(), PostBean.class);
                    Logger.w(TAG, "clear cache");
                }
                FundayDB.getCacheDB().insert(getDBExtra(), result.getResources());
                Logger.v(TAG, "insert size = %d", result.getResources().size());
                CacheTimeUtils.saveTime(context, KEY_TIME);

            }
            // 加载更多
            else if (!TextUtils.isEmpty(bottomId) && !bottomId.equals("0")) {
                FundayDB.getCacheDB().insert(getDBExtra(), result.getResources());
                Logger.v(TAG, "up insert size = %d", result.getResources().size());
                CacheTimeUtils.saveTime(context, KEY_TIME);


            } else {// 第一次初始化
                // 清理缓存
                FundayDB.getCacheDB().deleteAll(getDBExtra(), PostBean.class);
                CacheTimeUtils.saveTime(context, KEY_TIME);
                Logger.w(TAG, "clear cache");

                FundayDB.getCacheDB().insert(getDBExtra(), result.getResources());
                Logger.v(TAG, "insert size = %d", result.getResources().size());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Extra getDBExtra() {
        return new Extra(null, "video");
    }
}
