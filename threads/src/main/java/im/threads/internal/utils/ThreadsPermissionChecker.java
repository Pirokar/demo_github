package im.threads.internal.utils;

import android.Manifest;
import android.content.Context;

import androidx.core.content.PermissionChecker;

/**
 * util class with static methods to test runtime permissions;
 */
public final class ThreadsPermissionChecker {

    public static boolean isCameraPermissionGranted(Context ctx) {
        return PermissionChecker.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED;
    }

    public static boolean isWriteExternalPermissionGranted(Context ctx) {
        return PermissionChecker.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED;
    }

    public static boolean isReadExternalPermissionGranted(Context ctx) {
        return PermissionChecker.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED;
    }
}
