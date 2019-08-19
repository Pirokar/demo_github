package im.threads.internal.permissions;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Менеджер разрешений
 */
class PermissionsChecker {

    private PermissionsChecker() {
    }

    /**
     * @param permissions разрешения
     * @return true, если хотя бы одно разрешение отклонено
     */
    static boolean permissionsDenied(Activity activity, String... permissions) {
        for (String permission : permissions) {
            if (permissionDenied(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true, если для всех отклоненных разрешений нажато - больше не показывать
     */
    static boolean clickedNeverAskAgain(Activity activity, String... permissions) {
        for (String permission : permissions) {
            // Если отклонено, но не нажато БОЛЬШЕ НЕ ПОКАЗЫВАТЬ, нужно вернуть false
            boolean denied = permissionDenied(activity, permission);              // Отклонено ли
            boolean never = clickedNeverAskAgain(activity, permission); // Нажато ли - больше не показывать
            if (denied && !never) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param permission разрешение
     * @return true, если разрешение отклонено
     */
    private static boolean permissionDenied(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED;
    }

    // Если БОЛЬШЕ НЕ ПОКАЗЫВАТЬ не нажимали
    private static boolean clickedNeverAskAgain(Activity activity, String permission) {
        return !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }
}
