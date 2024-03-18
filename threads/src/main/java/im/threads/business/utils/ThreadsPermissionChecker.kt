package im.threads.business.utils

import android.Manifest
import android.content.Context
import androidx.core.content.PermissionChecker

/**
 * util class with static methods to test runtime permissions;
 */
object ThreadsPermissionChecker {
    @JvmStatic
    fun isCameraPermissionGranted(context: Context?): Boolean {
        return if (context != null) {
            PermissionChecker.checkSelfPermission(context, Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED
        } else {
            false
        }
    }

    @JvmStatic
    fun isWriteExternalPermissionGranted(context: Context?): Boolean {
        return if (context != null) {
            PermissionChecker.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PermissionChecker.PERMISSION_GRANTED
        } else {
            false
        }
    }

    @JvmStatic
    fun isRecordAudioPermissionGranted(context: Context?): Boolean {
        return if (context != null) {
            PermissionChecker.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PermissionChecker.PERMISSION_GRANTED
        } else {
            false
        }
    }
}
