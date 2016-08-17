package com.sequenia.threads.utils;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by yuri on 08.08.2016.
 */
public class PrefUtils {
    private static final String TAG = "PrefUtils ";
    public static final String TAG_CLIENT_ID = "TAG_CLIENT_ID";
    public static final String IS_CLIENT_ID_SET_TAG = "IS_CLIENT_ID_SET_TAG";
    public static final String DEFAULT_TITLE_TAG = "DEFAULT_TITLE_TAG";

    private PrefUtils() {
    }

    public static void setClientId(Context ctx, String clientId) {
        if (clientId == null) throw new IllegalStateException("clientId is null");
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString(PrefUtils.class + TAG_CLIENT_ID, clientId).apply();
    }

    public static String getClientID(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString(PrefUtils.class + TAG_CLIENT_ID, null);
    }

    public static void setDefaultTitle(Context ctx, String defTitle) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString(PrefUtils.class + DEFAULT_TITLE_TAG, defTitle).apply();
    }

    public static String getDefaultTitle(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString(PrefUtils.class + DEFAULT_TITLE_TAG, null);
    }

    public static void setClientIdWasSet(boolean isSet, Context ctx) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putBoolean(PrefUtils.class + IS_CLIENT_ID_SET_TAG, isSet).apply();
    }

    public static boolean isClientIdSet(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(PrefUtils.class + IS_CLIENT_ID_SET_TAG, false);
    }
}
