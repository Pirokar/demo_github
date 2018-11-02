package im.threads.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import java.util.ArrayList;

import androidx.core.app.ActivityCompat;

/**
 * Created by yuri on 18.05.2016.
 * util class with static methods to test runtime permissions;
 */
public class PermissionChecker {

    public static boolean checkPermissions(Context context) {
        boolean isAccessNetworkStateGranted
                = androidx.core.content.PermissionChecker.checkCallingOrSelfPermission(context, android.Manifest.permission.ACCESS_NETWORK_STATE) == androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

        return isAccessNetworkStateGranted;
    }

    public static void requestPermissionsAndInit(int requestCode, Activity activity) {
        boolean isAccessNetworkStateGranted
                = androidx.core.content.PermissionChecker.checkCallingOrSelfPermission(activity, android.Manifest.permission.ACCESS_NETWORK_STATE) == androidx.core.content.PermissionChecker.PERMISSION_GRANTED;
        ArrayList<String> permissions = new ArrayList<>();
        if (!isAccessNetworkStateGranted) permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
        ActivityCompat.requestPermissions(activity, permissions.toArray(new String[]{}), requestCode);
    }

    public static boolean checkGrantResult(int[] grantResults) {
        int grantedum = 0;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                grantedum++;
            }
        }
        return grantedum == grantResults.length;
    }

    public static boolean isCameraPermissionGranted(Context ctx){
        return androidx.core.content.PermissionChecker.checkSelfPermission(ctx, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isAudioRecordingPermissionGranted(Context ctx){
        return androidx.core.content.PermissionChecker.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isWriteExternalPermissionGranted(Context ctx){
        return androidx.core.content.PermissionChecker.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isReadExternalPermissionGranted(Context ctx){
        return androidx.core.content.PermissionChecker.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isReadPhoneStatePermissionGranted(Context ctx){
        return androidx.core.content.PermissionChecker.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE)==PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isReadSmsPermissionGranted(Context ctx){
        return androidx.core.content.PermissionChecker.checkSelfPermission(ctx, Manifest.permission.READ_SMS)==PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isCoarseLocationPermissionGranted(Context ctx){
        return androidx.core.content.PermissionChecker.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED;
    }
}
