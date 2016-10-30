package com.hawk.funday.base.file;


import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.wcc.framework.fs.DirectoryManager;

import java.io.File;
import java.util.HashMap;

public abstract class ServiceContext {
    protected static ServiceContext _instance = null;
    private final Context mContext;
    private HashMap<String, Object> mServiceMap = new HashMap<>();

    public static ServiceContext get() {
        return _instance;
    }

    protected ServiceContext(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public Context getApplicationContext() {
        return mContext;
    }

    public Object registerSystemObject(String name, Object obj) {
        if (obj == null) {
            return mServiceMap.remove(name);
        } else {
            return mServiceMap.put(name, obj);
        }
    }

    public Object getSystemObject(String name) {
        return mServiceMap.get(name);
    }

    public static File getDirectory(DirType type) {
        DirectoryManager manager = get().getDirectoryManager();
        File file = null;
        if (manager != null)
            file = manager.getDir(type.value());

        if (file == null) {
            final Context context = get().getApplicationContext();
            File[] files = ContextCompat.getExternalFilesDirs(context, type.name());
            file = files[0];

            if (!file.exists()) {
                file.mkdirs();
            }
        }

        return file;
    }

    public static String getDirectoryPath(DirType type) {
        File file = getDirectory(type);
        return file.getAbsolutePath();
    }

    public abstract DirectoryManager getDirectoryManager();
}
