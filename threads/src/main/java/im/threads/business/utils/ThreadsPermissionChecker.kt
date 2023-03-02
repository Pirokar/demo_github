package im.threads.business.utils

import android.Manifest
import android.content.Context
import android.os.Build
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
    fun isReadExternalPermissionGranted(context: Context?): Boolean {
        return if (context != null) {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            } else { arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE) }
            for (permission in permissions) {
                if (PermissionChecker.checkSelfPermission(context, permission) != PermissionChecker.PERMISSION_GRANTED) {
                    return false
                }
            }
            return true
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
