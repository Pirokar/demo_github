package im.threads.business.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

object MediaHelper {
    @JvmStatic
    fun grantPermissionsForImageUri(context: Context, intent: Intent?, imageUri: Uri?) {
        if (intent != null) {
            val resInfoList = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resInfoList.isNotEmpty()) {
                resInfoList.forEach { resolveInfo ->
                    val packageName = resolveInfo.activityInfo.packageName
                    context.grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
            }
        }
    }
}
