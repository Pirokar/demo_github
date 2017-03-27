package im.threads.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by yuri on 18.05.2016.
 * util class with static methods to test runtime permissions;
 */
public class PermissionChecker {

    public static boolean isCameraPermissionGranted(Context ctx){
        return android.support.v4.content.PermissionChecker.checkSelfPermission(ctx, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED;
    }
    public static boolean isAudioRecordingPermissionGranted(Context ctx){
        return android.support.v4.content.PermissionChecker.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_GRANTED;
    }
    public static boolean isWriteExternalPermissionGranted(Context ctx){
        return android.support.v4.content.PermissionChecker.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;
    }
    public static boolean isReadExternalPermissionGranted(Context ctx){
        return android.support.v4.content.PermissionChecker.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isReadPhoneStatePermissionGranted(Context ctx){
        return android.support.v4.content.PermissionChecker.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE)==PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isReadSmsPermissionGranted(Context ctx){
        return android.support.v4.content.PermissionChecker.checkSelfPermission(ctx, Manifest.permission.READ_SMS)==PackageManager.PERMISSION_GRANTED;
    }
    public static boolean isCoarseLocationPermissionGranted(Context ctx){
        return android.support.v4.content.PermissionChecker.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED;
    }
}
