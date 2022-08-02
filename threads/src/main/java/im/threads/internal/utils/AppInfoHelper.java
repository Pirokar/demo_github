package im.threads.internal.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import im.threads.BuildConfig;
import im.threads.internal.Config;
import im.threads.internal.domain.logger.LoggerEdna;

public final class AppInfoHelper {
    public static String getAppVersion() {
        PackageInfo pInfo = null;
        try {
            pInfo = Config.instance.context.getPackageManager().getPackageInfo(Config.instance.context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            LoggerEdna.error("getAppVersion", e);
        }
        return pInfo != null ? pInfo.versionName : "";
    }

    public static String getLibVersion() {
        return BuildConfig.VERSION_NAME;
    }

    public static String getAppName() {
        ApplicationInfo applicationInfo = Config.instance.context.getApplicationInfo();
        if (applicationInfo != null) {
            try {
                return applicationInfo.loadLabel(Config.instance.context.getPackageManager()).toString();
            } catch (Exception e) {
                LoggerEdna.error("getAppName", e);
            }
        }
        return "Unknown";
    }

    public static String getAppId() {
        ApplicationInfo applicationInfo = Config.instance.context.getApplicationInfo();
        if (applicationInfo != null) {
            try {
                return applicationInfo.packageName;
            } catch (Exception e) {
                LoggerEdna.error("getAppId", e);
            }
        }
        return "";
    }
}
