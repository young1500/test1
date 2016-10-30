package com.hawk.funday.support.db;

import android.content.Context;
import android.util.Log;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.component.orm.SqliteUtility;
import org.aisen.android.component.orm.SqliteUtilityBuilder;

/**
 * Created by wangdan on 16/8/30.
 */
public class FundayDB {

    static final String DB_NAME = "fundaydb";
    static final String DB_CACHE_NAME = "fundaycachedb";

    static final int DB_VERSION = 1;
    
    public static void setInitDB(Context context) {
        try {
            Log.w("LScreenDB", "初始化 db versionCode = " + DB_VERSION);

            new SqliteUtilityBuilder().configVersion(DB_VERSION).configDBName(DB_NAME).build(context);
            new SqliteUtilityBuilder().configVersion(DB_VERSION).configDBName(DB_CACHE_NAME).build(context);
        } catch (Throwable e) {
            Logger.printExc(FundayDB.class, e);
        }
    }

    /**
     * 请不要直接使用DB，根据各自的业务方法开发API供外面调用，便于维护
     *
     * @return
     */
    static SqliteUtility getDB() throws Exception {
        return SqliteUtility.getInstance(DB_NAME);
    }

    /**
     * 专门用于缓存数据的DB
     *
     * @return
     */
    public static SqliteUtility getCacheDB() throws Exception  {
        return SqliteUtility.getInstance(DB_CACHE_NAME);
    }
    
}
