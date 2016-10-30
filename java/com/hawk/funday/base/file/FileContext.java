package com.hawk.funday.base.file;

import android.content.Context;

import com.hawk.funday.BuildConfig;
import com.wcc.framework.fs.DirectoryManager;

/**
 * Created by yijie.ma on 2016/8/22.
 */
public class FileContext extends ServiceContext {

    public final static String SD_ROOT = BuildConfig.APP_ROOT_DIR;
    public static boolean CONTEXT_INIT_SUCCESS = false;

    public static boolean initInstance(Context context) {
        if (!CONTEXT_INIT_SUCCESS || _instance == null) {
            FileContext gcContext = new FileContext(context);

            _instance = gcContext;
            CONTEXT_INIT_SUCCESS = gcContext.init();
            return CONTEXT_INIT_SUCCESS;
        }
        return true;
    }

    private DirectoryManager mDirectoryManager = null;

    private FileContext(Context context) {
        super(context);
    }

    @Override
    public DirectoryManager getDirectoryManager() {
        return mDirectoryManager;
    }

    private boolean init() {
        DirectoryManager dm = new DirectoryManager(new ApplicationDirectoryContext(getApplicationContext(), SD_ROOT));
        boolean ret = dm.buildAndClean();
        if (!ret) {
            return false;
        }

        registerSystemObject(ServiceName.DIR_MANAGER, dm);
        mDirectoryManager = dm;
        return false;
    }

}

