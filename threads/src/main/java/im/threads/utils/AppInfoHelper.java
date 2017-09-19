package im.threads.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import im.threads.BuildConfig;

public final class AppInfoHelper {

    private static String getAppBundle() {
        return BuildConfig.APPLICATION_ID;
    }

    public static String getAppVersion(Context ctx) {

        PackageInfo pInfo = null;
        try {
            pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pInfo != null ? pInfo.versionName + " (" + pInfo.versionCode + ")" : "";
    }

    public static String getLibVersion() {
        return BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")";
    }

    public static String getAppName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        if (applicationInfo != null) {
            try {
                return applicationInfo.loadLabel(context.getPackageManager()).toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "Unknown";
    }

    public static String getAppId(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        if (applicationInfo != null) {
            try {
                return applicationInfo.packageName;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}

