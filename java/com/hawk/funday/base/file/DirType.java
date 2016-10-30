package com.hawk.funday.base.file;

/**
 * Created by yijie.ma on 2016/8/22.
 */
public enum  DirType {
    root,
    log,
    image,
    cache,
    crash,
    video;

    public int value()
    {
        return ordinal();
    }

}
