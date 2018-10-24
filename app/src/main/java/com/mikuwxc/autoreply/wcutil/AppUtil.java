package com.mikuwxc.autoreply.wcutil;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class AppUtil {
    public static boolean isAppInstalled(Context context, String packageName) {
        List<PackageInfo> pinfo = context.getPackageManager().getInstalledPackages(0);
        List<String> pName = new ArrayList();
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                pName.add(((PackageInfo) pinfo.get(i)).packageName);
            }
        }
        return pName.contains(packageName);
    }

    public static String getAppVersion(Context context, String packageName) throws NameNotFoundException {
        return context.getPackageManager().getPackageInfo(packageName, 0).versionName;
    }
}
