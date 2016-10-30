package com.hawk.funday.base.file;

import android.content.Context;

import com.wcc.framework.fs.Directory;
import com.wcc.framework.fs.DirectoryContext;
import com.wcc.framework.util.TimeConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by yijie.ma on 2016/8/22.
 */
public class ApplicationDirectoryContext extends DirectoryContext {

    public ApplicationDirectoryContext(Context context, String externalPath) {
        super(context, externalPath);
    }

    @Override
    protected Collection<Directory> initDirectories() {
        List<Directory> children = new ArrayList<Directory>();

        children.add(newDirectory(DirType.log));
        children.add(newDirectory(DirType.image));
        children.add(newDirectory(DirType.crash));
        children.add(newDirectory(DirType.cache));
        children.add(newDirectory(DirType.video));
        return children;
    }

    private Directory newDirectory(DirType type) {
        Directory child = new Directory(type.toString(), null);
        child.setType(type.value());
        if (type.equals(DirType.cache))
        {
            child.setForCache(true);
            child.setExpiredTime(TimeConstants.ONE_DAY_MS);
        }

        return child;
    }
}
