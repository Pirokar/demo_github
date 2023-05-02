package im.threads.ui.permissions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Менеджер разрешений
 */
internal object PermissionsChecker {
    /**
     * @param permissions разрешения
     * @return true, если хотя бы одно разрешение отклонено
     */
    fun permissionsDenied(activity: Activity, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (permissionDenied(activity, permission)) {
                return true
            }
        }
        return false
    }

    /**
     * @return true, если для всех отклоненных разрешений нажато - больше не показывать
     */
    fun clickedNeverAskAgain(activity: Activity, vararg permissions: String): Boolean {
        for (permission in permissions) {
            val denied = permissionDenied(activity, permission)
            val never = clickedNeverAskAgain(activity, permission)
            if (denied && !never) {
                return false
            }
        }
        return true
    }

    /**
     * @param permission разрешение
     * @return true, если разрешение отклонено
     */
    private fun permissionDenied(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED
    }

    // Если "БОЛЬШЕ НЕ ПОКАЗЫВАТЬ" не нажимали
    private fun clickedNeverAskAgain(activity: Activity, permission: String): Boolean {
        return !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
}
