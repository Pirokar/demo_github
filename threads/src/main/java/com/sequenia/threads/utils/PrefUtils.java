package com.sequenia.threads.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.annotation.XmlRes;
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
    public static final String PUSH_ICON = "DEFAULT_PUSH_ICON";
    public static final String PUSH_TITLE = "PUSH_TITLE";
    public static final String LAST_COPY_TEXT = "LAST_COPY_TEXT";
    public static final String GA_TRACKER_ID = "GA_TRACKER_ID";

    private PrefUtils() {
    }

    public static String getGaTrackerId(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString(PrefUtils.class + GA_TRACKER_ID, null);
    }

    public static void setGaTrackerId(Context ctx, String GATrackerId) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString(PrefUtils.class + GA_TRACKER_ID, GATrackerId).commit();
    }

    public static void setLastCopyText(Context ctx, String text) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString(PrefUtils.class + LAST_COPY_TEXT, text).commit();
    }

    public static String getLastCopyText(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString(PrefUtils.class + LAST_COPY_TEXT, null);
    }

    public static void setPushTitle(Context ctx, @StringRes int resId) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putInt(PrefUtils.class + PUSH_TITLE, resId).commit();
    }

    public static
    @StringRes
    int getPushTitle(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getInt(PrefUtils.class + PUSH_TITLE, -1);
    }

    public static void setUserName(Context ctx, String ClientName) {
        if (ClientName == null) throw new IllegalStateException("ClientName is null");
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString(PrefUtils.class + CLIENT_NAME, ClientName).commit();
    }

    public static String getUserName(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString(PrefUtils.class + CLIENT_NAME, "");
    }

    public static void setPushIconResid(Context ctx, @DrawableRes int iconResId) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putInt(PrefUtils.class + PUSH_ICON, iconResId).commit();
    }

    public static int getPushIconResid(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getInt(PrefUtils.class + PUSH_ICON, -1);
    }

    public static void setClientId(Context ctx, String clientId) {
        if (clientId == null) throw new IllegalStateException("clientId is null");
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString(PrefUtils.class + TAG_CLIENT_ID, clientId).commit();
    }

    public static String getClientID(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString(PrefUtils.class + TAG_CLIENT_ID, null);
    }

    public static void setDefaultChatTitle(Context ctx, @StringRes int defTitle) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putInt(PrefUtils.class + DEFAULT_TITLE_TAG, defTitle).commit();
    }

    public static
    @StringRes
    int getDefaultChatTitle(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getInt(PrefUtils.class + DEFAULT_TITLE_TAG, -1);
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
        editor.putInt("titleText", b.getInt("titleText")).apply();
        editor.putInt("contentText", b.getInt("contentText")).apply();
        editor.putFloat("titleSize", b.getFloat("titleSize")).apply();
        editor.putFloat("subtitleSize", b.getFloat("subtitleSize")).apply();
    }

    public static Bundle getIncomingStyle(Context ctx) {
        Bundle b = new Bundle();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        b.putInt("logoResId", sp.getInt("logoResId", 0));
        b.putInt("textColorResId", sp.getInt("textColorResId", 0));
        b.putInt("titleText", sp.getInt("titleText", -1));
        b.putInt("contentText", sp.getInt("contentText", -1));
        b.putFloat("titleSize", sp.getFloat("titleSize", 0.0f));
        b.putFloat("subtitleSize", sp.getFloat("subtitleSize", 0.0f));
        return b;
    }
}
