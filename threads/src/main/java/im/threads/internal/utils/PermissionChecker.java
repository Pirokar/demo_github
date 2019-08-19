package im.threads.internal.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

/**
 * util class with static methods to test runtime permissions;
 */
public class PermissionChecker {

    public static boolean isCameraPermissionGranted(Context ctx) {
        return android.support.v4.content.PermissionChecker.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isWriteExternalPermissionGranted(Context ctx) {
        return android.support.v4.content.PermissionChecker.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isReadExternalPermissionGranted(Context ctx) {
        return android.support.v4.content.PermissionChecker.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
}
