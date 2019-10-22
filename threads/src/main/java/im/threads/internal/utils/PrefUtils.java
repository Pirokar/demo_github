package im.threads.internal.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import im.threads.internal.Config;
import im.threads.ChatStyle;

public final class PrefUtils {
    private static final String TAG = "PrefUtils ";
    private static final String TAG_CLIENT_ID = "TAG_CLIENT_ID";
    private static final String TAG_CLIENT_ID_ENCRYPTED = "TAG_CLIENT_ID_ENCRYPTED";
    private static final String CLIENT_ID_SIGNATURE_KEY = "CLIENT_ID_SIGNATURE";
    private static final String TAG_NEW_CLIENT_ID = "TAG_NEW_CLIENT_ID";
    private static final String IS_CLIENT_ID_SET_TAG = "IS_CLIENT_ID_SET_TAG";
    private static final String CLIENT_NAME = "DEFAULT_CLIENT_NAMETITLE_TAG";
    private static final String EXTRA_DATA = "EXTRA_DATE";
    private static final String LAST_COPY_TEXT = "LAST_COPY_TEXT";
    private static final String APP_STYLE = "APP_STYLE";
    private static final String TAG_THREAD_ID = "THREAD_ID";
    private static final String SERVER_URL_META_INFO = "im.threads.getServerUrl";
    private static final String APP_MARKER_KEY = "APP_MARKER";

    private PrefUtils() {
    }

    public static void setLastCopyText(String text) {
        getDefaultSharedPreferences()
                .edit()
                .putString(PrefUtils.class + LAST_COPY_TEXT, text)
                .commit();
    }

    public static String getLastCopyText() {
        return getDefaultSharedPreferences().getString(PrefUtils.class + LAST_COPY_TEXT, null);
    }

    public static void setUserName(String clientName) {
        getDefaultSharedPreferences()
                .edit()
                .putString(PrefUtils.class + CLIENT_NAME, clientName)
                .commit();
    }

    public static String getUserName() {
        return getDefaultSharedPreferences().getString(PrefUtils.class + CLIENT_NAME, "");
    }

    public static void setData(String data) {
        getDefaultSharedPreferences()
                .edit()
                .putString(PrefUtils.class + EXTRA_DATA, data)
                .commit();
    }

    public static String getData() {
        return getDefaultSharedPreferences().getString(PrefUtils.class + EXTRA_DATA, "");
    }

    public static void setNewClientId(@NonNull String clientId) {
        getDefaultSharedPreferences()
                .edit()
                .putString(PrefUtils.class + TAG_NEW_CLIENT_ID, clientId)
                .commit();
    }

    public static String getNewClientID() {
        return getDefaultSharedPreferences().getString(PrefUtils.class + TAG_NEW_CLIENT_ID, null);
    }

    public static void setClientId(@NonNull String clientId) {
        getDefaultSharedPreferences()
                .edit()
                .putString(PrefUtils.class + TAG_CLIENT_ID, clientId)
                .commit();
    }

    public static String getClientID() {
        return getDefaultSharedPreferences().getString(PrefUtils.class + TAG_CLIENT_ID, "");
    }

    public static void setClientIdEncrypted(boolean clientIdEncrypted) {
        getDefaultSharedPreferences()
                .edit()
                .putBoolean(PrefUtils.class + TAG_CLIENT_ID_ENCRYPTED, clientIdEncrypted)
                .commit();
    }

    public static boolean getClientIDEncrypted() {
        return getDefaultSharedPreferences().getBoolean(PrefUtils.class + TAG_CLIENT_ID_ENCRYPTED, false);
    }

    public static void setClientIdSignature(String clientIdSignature) {
        getDefaultSharedPreferences()
                .edit()
                .putString(PrefUtils.class + CLIENT_ID_SIGNATURE_KEY, clientIdSignature)
                .commit();
    }

    public static String getClientIdSignature() {
        return getDefaultSharedPreferences().getString(PrefUtils.class + CLIENT_ID_SIGNATURE_KEY, "");
    }

    public static void setThreadId(Long threadId) {
        if (threadId == null) {
            throw new IllegalStateException("threadId must not be null");
        }
        getDefaultSharedPreferences()
                .edit()
                .putLong(PrefUtils.class + TAG_THREAD_ID, threadId)
                .commit();
    }

    public static Long getThreadID() {
        return getDefaultSharedPreferences().getLong(PrefUtils.class + TAG_THREAD_ID, -1L);
    }

    public static void setClientIdWasSet(boolean isSet) {
        getDefaultSharedPreferences()
                .edit()
                .putBoolean(PrefUtils.class + IS_CLIENT_ID_SET_TAG, isSet)
                .apply();
    }

    public static boolean isClientIdSet() {
        return getDefaultSharedPreferences().getBoolean(PrefUtils.class + IS_CLIENT_ID_SET_TAG, false);
    }

    public static boolean isClientIdNotEmpty() {
        return !getClientID().isEmpty();
    }

    public static void setIncomingStyle(@NonNull ChatStyle style) {
        getDefaultSharedPreferences()
                .edit()
                .putString(APP_STYLE, new Gson().toJson(style))
                .commit();
    }

    @Nullable
    public static ChatStyle getIncomingStyle() {
        ChatStyle style = null;
        try {
            SharedPreferences sharedPreferences = getDefaultSharedPreferences();
            if (sharedPreferences.getString(APP_STYLE, null) != null) {
                String sharedPreferencesString = sharedPreferences.getString(APP_STYLE, null);
                style = new Gson().fromJson(sharedPreferencesString, ChatStyle.class);
            }
        } catch (IllegalStateException | JsonSyntaxException ex) {
            ThreadsLogger.w(TAG, "getIncomingStyle failed: ", ex);
        }
        return style;
    }

    public static String getServerUrlMetaInfo() {
        return getMetaData(SERVER_URL_META_INFO);
    }

    public static void setAppMarker(String appMarker) {
        getDefaultSharedPreferences()
                .edit()
                .putString(PrefUtils.class + APP_MARKER_KEY, appMarker)
                .commit();
    }

    public static String getAppMarker() {
        String appMarker = getDefaultSharedPreferences().getString(PrefUtils.class + APP_MARKER_KEY, "");
        return appMarker.length() > 0 ? appMarker : null;
    }

    @Nullable
    private static String getMetaData(String key) {
        try {
            Context context = Config.instance.context;
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            return bundle.getString(key);
        } catch (PackageManager.NameNotFoundException e) {
            ThreadsLogger.e(TAG, "Failed to load self applicationInfo - that's really weird. ", e);
            return null;
        }
    }

    private static SharedPreferences getDefaultSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(Config.instance.context);
    }
}
