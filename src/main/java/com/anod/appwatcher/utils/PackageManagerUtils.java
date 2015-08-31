package com.anod.appwatcher.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.ArrayMap;

import java.util.ArrayList;
import java.util.List;


/**
 * @author alex
 * @date 9/18/13
 */
public class PackageManagerUtils {
    private PackageManager mPackageManager;
    private ArrayMap<String, InstalledInfo> mInstalledVersionsCache;

    public static class InstalledInfo {
        public int versionCode = 0;
        public String versionName = null;
    }

    public PackageManagerUtils(PackageManager pm) {
        mPackageManager = pm;
        mInstalledVersionsCache = new ArrayMap<>();
    }

    public String getAppTitle(PackageInfo info) {
        return info.applicationInfo.loadLabel(mPackageManager).toString();
    }

    public Bitmap getAppIcon(PackageInfo info) {
        Drawable drawable = info.applicationInfo.loadIcon(mPackageManager);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
// TODO:
        return null;
    }

    public List<PackageInfo> getInstalledApps() {
        List<PackageInfo> packs = mPackageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        List<PackageInfo> downloaded = new ArrayList<>(packs.size());
        for (int i = 0; i < packs.size(); i++)
        {
            PackageInfo packageInfo = packs.get(i);
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            // Skips the system application (packages)
            if ( (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1)
            {
                continue;
            }
            downloaded.add(packageInfo);
        }
        return downloaded;
    }

    public InstalledInfo getInstalledInfo(String packageName) {
        if (mInstalledVersionsCache.containsKey(packageName)) {
            return mInstalledVersionsCache.get(packageName);
        }

        PackageInfo pkgInfo = null;
        try {
            pkgInfo = mPackageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            // skip
        }

        InstalledInfo info = new InstalledInfo();
        if (pkgInfo != null) {
            info.versionCode = pkgInfo.versionCode;
            info.versionName = pkgInfo.versionName;
        }

        mInstalledVersionsCache.put(packageName, info);
        return info;
    }

    public String getAppVersionName(String packageName) {
        InstalledInfo info = getInstalledInfo(packageName);
        return info.versionName;
    }

    public int getAppVersionCode(String packageName) {
        InstalledInfo info = getInstalledInfo(packageName);
        return info.versionCode;
    }

    public boolean isAppInstalled(String packageName) {
        InstalledInfo info = getInstalledInfo(packageName);
        return info.versionCode > 0;
    }
}
