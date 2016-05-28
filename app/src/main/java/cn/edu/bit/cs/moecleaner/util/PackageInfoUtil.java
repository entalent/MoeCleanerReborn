package cn.edu.bit.cs.moecleaner.util;

import android.content.Context;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.RemoteException;
import android.text.format.Formatter;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Semaphore;

/**
 * Created by entalent on 2016/5/2.
 */
public class PackageInfoUtil {
    public static boolean isValidPackageName(PackageManager pm, String packageName, int flag) {
        if(packageName == null || packageName.isEmpty())
            return false;
        try {
            pm.getPackageInfo(packageName, flag);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static PackageInfo getApkPackageInfo(PackageManager pm, File apkFile) {
        try {
            PackageInfo info = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), pm.GET_ACTIVITIES);
            return info;
        } catch (Exception e) {
            return null;
        }
    }

    static long totalSize = 0;

    public static synchronized long getPkgTotalSize(final Context context, final String pkgName)
            throws NoSuchMethodException,
            InvocationTargetException,
            IllegalAccessException,
            InterruptedException {
        Method method = PackageManager.class.getMethod("getPackageSizeInfo",
                new Class[] { String.class, IPackageStatsObserver.class });

        final Semaphore sem = new Semaphore(1);
        sem.drainPermits();
        //在回调中获取缓存，数据，apk大小
        method.invoke(context.getPackageManager(), new Object[] {
                pkgName,
                new IPackageStatsObserver.Stub() {
                    @Override
                    public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
                            throws RemoteException {
                        /*
                        System.out.println(
                            "cache size = " + Formatter.formatFileSize(context, pStats.cacheSize) +
                            "\ndata size = " + Formatter.formatFileSize(context, pStats.dataSize) +
                            "\nprogram size = " + Formatter.formatFileSize(context, pStats.codeSize));
                                        */
                        totalSize = pStats.cacheSize + pStats.dataSize + pStats.codeSize;
                        sem.release();
                    }
                }
        });
        //用信号量等待回调完成，返回结果
        sem.acquire(1);
        return totalSize;
    }
}
