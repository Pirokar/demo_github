package im.threads.internal.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

/**
 * util class with static methods to test runtime permissions;
 */
public final class PermissionChecker {

    public static boolean isCameraPermissionGranted(@NonNull Context ctx) {
        return android.support.v4.content.PermissionChecker.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isWriteExternalPermissionGranted(@NonNull Context ctx) {
        return android.support.v4.content.PermissionChecker.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isReadExternalPermissionGranted(@NonNull Context ctx) {
        return android.support.v4.content.PermissionChecker.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
}
