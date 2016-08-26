package com.sequenia.threads.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by yuri on 08.08.2016.
 */
public class PrefUtils {
    private static final String TAG = "PrefUtils ";
    public static final String TAG_CLIENT_ID = "TAG_CLIENT_ID";
    public static final String IS_CLIENT_ID_SET_TAG = "IS_CLIENT_ID_SET_TAG";
    public static final String DEFAULT_TITLE_TAG = "DEFAULT_TITLE_TAG";
    public static final String CLIENT_NAME = "DEFAULT_CLIENT_NAMETITLE_TAG";

    private PrefUtils() {
    }

    public static void setClientName(Context ctx, String ClientName) {
        if (ClientName == null) throw new IllegalStateException("ClientName is null");
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString(PrefUtils.class + CLIENT_NAME, ClientName).commit();
    }

    public static String getClientName(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString(PrefUtils.class + CLIENT_NAME, "");
    }

    public static void setClientId(Context ctx, String clientId) {
        if (clientId == null) throw new IllegalStateException("clientId is null");
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString(PrefUtils.class + TAG_CLIENT_ID, clientId).commit();
    }

    public static String getClientID(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString(PrefUtils.class + TAG_CLIENT_ID, null);
    }

    public static void setDefaultTitle(Context ctx, String defTitle) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString(PrefUtils.class + DEFAULT_TITLE_TAG, defTitle).commit();
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

    public static void setIncomingStyle(Context ctx, Bundle b) {
        if (ctx == null || b == null) {
            Log.i(TAG, "setIncomingStyle: ctx or bundle is null");
            return;
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        editor.putInt("logoResId", b.getInt("logoResId")).apply();
        editor.putInt("textColorResId", b.getInt("textColorResId")).apply();
        editor.putString("titleText", b.getString("titleText")).apply();
        editor.putString("subtitleText", b.getString("subtitleText")).apply();
        editor.putFloat("titleSize", b.getFloat("titleSize")).apply();
        editor.putFloat("subtitleSize", b.getFloat("subtitleSize")).apply();
    }

    public static Bundle getIncomingStyle(Context ctx) {
        Bundle b = new Bundle();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        b.putInt("logoResId", sp.getInt("logoResId", 0));
        b.putInt("textColorResId", sp.getInt("textColorResId", 0));
        b.putString("titleText", sp.getString("titleText", ""));
        b.putString("subtitleText", sp.getString("subtitleText", ""));
        b.putFloat("titleSize", sp.getFloat("titleSize", 0.0f));
        b.putFloat("subtitleSize", sp.getFloat("subtitleSize", 0.0f));
        return b;
    }
}
