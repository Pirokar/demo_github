package im.threads.business.utils

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import im.threads.BuildConfig
import im.threads.business.config.BaseConfig
import im.threads.business.logger.LoggerEdna.error

class AppInfo {
    val appVersion: String
        get() {
            var pInfo: PackageInfo? = null
            try {
                pInfo = BaseConfig.instance.context.packageManager.getPackageInfo(
                    BaseConfig.instance.context.packageName,
                    0
                )
            } catch (e: PackageManager.NameNotFoundException) {
                error("getAppVersion", e)
            }
            return if (pInfo != null) pInfo.versionName else ""
        }

    val libVersion: String
        get() = BuildConfig.VERSION_NAME

    val appName: String
        get() {
            val applicationInfo = BaseConfig.instance.context.applicationInfo
            if (applicationInfo != null) {
                try {
                    return applicationInfo.loadLabel(BaseConfig.instance.context.packageManager)
                        .toString()
                } catch (e: Exception) {
                    error("getAppName", e)
                }
            }
            return "Unknown"
        }

    val appId: String
        get() {
            val applicationInfo = BaseConfig.instance.context.applicationInfo
            if (applicationInfo != null) {
                try {
                    return applicationInfo.packageName
                } catch (e: Exception) {
                    error("getAppId", e)
                }
            }
            return ""
        }
}
