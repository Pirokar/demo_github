package im.threads.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import im.threads.model.ChatStyle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by yuri on 08.08.2016.
 */
public class PrefUtils {
    private static final String TAG = "PrefUtils ";
    public static final String TAG_CLIENT_ID = "TAG_CLIENT_ID";
    public static final String TAG_NEW_CLIENT_ID = "TAG_NEW_CLIENT_ID";
    public static final String IS_CLIENT_ID_SET_TAG = "IS_CLIENT_ID_SET_TAG";
    public static final String CLIENT_NAME = "DEFAULT_CLIENT_NAMETITLE_TAG";
    public static final String LAST_COPY_TEXT = "LAST_COPY_TEXT";
    public static final String GA_TRACKER_ID = "GA_TRACKER_ID";
    public static final String IS_UUID_SET = "IS_UUID_SET";
    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String APP_STYLE = "APP_STYLE";
    public static final String SERVER_URL_META_INFO = "im.threads.getServerUrl";

    private PrefUtils() {
    }

    public static void setDeviceUUidWasSet(Context ctx, boolean isSet) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putBoolean(PrefUtils.class + IS_UUID_SET, isSet).apply();
    }

    public static boolean isDeviceUUidWasSet(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(PrefUtils.class + IS_UUID_SET, false);
    }

    public static String getDeviceAddress(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString(PrefUtils.class + DEVICE_ADDRESS, null);
    }

    public static void setDeviceAddress(Context ctx, String deviceAddress) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString(PrefUtils.class + DEVICE_ADDRESS, deviceAddress).commit();
    }

    public static String getGaTrackerId(Context ctx) {
        //  return PreferenceManager.getDefaultSharedPreferences(ctx).getString(PrefUtils.class + GA_TRACKER_ID, null);
        return "UA-48198875-10";
    }

    public static void setGaTrackerId(Context ctx, boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putBoolean(PrefUtils.class + GA_TRACKER_ID, enabled).commit();
    }

    public static void setLastCopyText(Context ctx, String text) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString(PrefUtils.class + LAST_COPY_TEXT, text).commit();
    }

    public static String getLastCopyText(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString(PrefUtils.class + LAST_COPY_TEXT, null);
    }

    public static void setUserName(Context ctx, String ClientName) {
        if (ClientName == null) throw new IllegalStateException("ClientName is null");
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString(PrefUtils.class + CLIENT_NAME, ClientName).commit();
    }

    public static String getUserName(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString(PrefUtils.class + CLIENT_NAME, "");
    }



    public static void setNewClientId(Context ctx, String clientId) {
        if (clientId == null) throw new IllegalStateException("clientId is null");
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString(PrefUtils.class + TAG_NEW_CLIENT_ID, clientId).commit();
    }

    public static String getNewClientID(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString(PrefUtils.class + TAG_NEW_CLIENT_ID, null);
    }

    public static void setClientId(Context ctx, String clientId) {
        if (clientId == null) throw new IllegalStateException("clientId is null");
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString(PrefUtils.class + TAG_CLIENT_ID, clientId).commit();
    }

    public static String getClientID(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString(PrefUtils.class + TAG_CLIENT_ID, "");
    }

    public static void setClientIdWasSet(boolean isSet, Context ctx) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putBoolean(PrefUtils.class + IS_CLIENT_ID_SET_TAG, isSet).apply();
    }

    public static boolean isClientIdSet(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(PrefUtils.class + IS_CLIENT_ID_SET_TAG, false);
    }

    public static boolean isClientIdNotEmpty(Context ctx) {
        String clientId = getClientID(ctx);
        return !clientId.isEmpty();
    }

    public static void setIncomingStyle(Context ctx, ChatStyle style) {
        if (ctx == null || style == null) {
            Log.i(TAG, "setIncomingStyle: ctx or bundle is null");
            return;
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(style);
            editor.putString(APP_STYLE, Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT));
            editor.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ChatStyle getIncomingStyle(Context ctx) {
        ChatStyle style = null;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (sharedPreferences.getString(APP_STYLE, null) == null) return null;
        else {
            String base64String = sharedPreferences.getString(APP_STYLE, null);
            byte[] object = Base64.decode(base64String, Base64.DEFAULT);
            try {
                ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(object));
                style = (ChatStyle) inputStream.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
       return style;
    }

    public static String getServerUrlMetaInfo(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            try {
                ApplicationInfo ai = context.getPackageManager().getApplicationInfo(activity.getPackageName(), PackageManager.GET_META_DATA);
                Bundle bundle = ai.metaData;
                return bundle.getString(SERVER_URL_META_INFO);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
            } catch (NullPointerException e) {
                Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
            }
        }
        return null;
    }
}
