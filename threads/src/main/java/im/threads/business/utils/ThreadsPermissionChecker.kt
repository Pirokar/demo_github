package im.threads.business.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
            val isApi34OrMore = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
            val permissions = if (isApi34OrMore) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                )
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            } else { arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE) }

            val indexOfVisualUserPermission = if (isApi34OrMore) {
                permissions.indexOf(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            } else {
                -1
            }
            if (indexOfVisualUserPermission > 0 &&
                PermissionChecker.checkSelfPermission(context, permissions[indexOfVisualUserPermission]) == PermissionChecker.PERMISSION_GRANTED
            ) {
                return true
            }

            for (i in permissions.indices) {
                val grantResult = PermissionChecker.checkSelfPermission(context, permissions[i])

                if (grantResult == PackageManager.PERMISSION_DENIED && i != indexOfVisualUserPermission) {
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
