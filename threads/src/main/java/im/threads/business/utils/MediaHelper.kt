package im.threads.business.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

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

    @JvmStatic
    fun getAllPhotos(context: Context): Cursor? {
        val projection = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media._ID
        )
        val sortOrder = MediaStore.Images.Media.DATE_TAKEN + " desc"
        val selection = MediaStore.Images.Media.MIME_TYPE + " = ? OR " + MediaStore.Images.Media.MIME_TYPE + " = ?"
        val selectionArgs = arrayOf(
            "image/png",
            "image/jpeg"
        )
        return context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder)
    }
}
