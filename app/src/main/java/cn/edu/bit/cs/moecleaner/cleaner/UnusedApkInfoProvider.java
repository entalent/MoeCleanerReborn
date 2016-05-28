package cn.edu.bit.cs.moecleaner.cleaner;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.File;
import java.io.FileFilter;
import java.util.Set;

import cn.edu.bit.cs.moecleaner.util.PackageInfoUtil;

/**
 * Created by entalent on 2016/5/24.
 */
public class UnusedApkInfoProvider extends TypedFileInfoProvider{
    Context context;
    PackageManager pm;

    public UnusedApkInfoProvider(File rootDirectory, Context context) {
        super(rootDirectory);
        this.context = context;
        pm = context.getPackageManager();
        this.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if(pathname.isDirectory() || pathname.getName().endsWith(".apk")) {
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    public boolean isFilteredFile(File file) {
        if((!file.canRead()) || (!file.isFile()) || (!file.getName().endsWith(".apk")))
            return false;
        PackageInfo packageInfo = PackageInfoUtil.getApkPackageInfo(pm, file);
        if(packageInfo != null)
            return PackageInfoUtil.isValidPackageName(pm, packageInfo.packageName, PackageManager.GET_ACTIVITIES);
        return false;
    }
}
