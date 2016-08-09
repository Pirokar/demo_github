package com.sequenia.threads.utils;

import android.content.Context;

/**
 * Created by yuri on 08.08.2016.
 */
public class PrefUtils {
    private static final String TAG = "PrefUtils ";
    public static final String TAG_CLIENT_ID = "TAG_CLIENT_ID";
    public static final String IS_CLIENT_ID_SET_TAG = "IS_CLIENT_ID_SET_TAG";

    private PrefUtils() {
    }

    public static void setClientId(Context ctx, String clientId) {
        if (clientId == null) throw new IllegalStateException("clientId is null");
        ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit().putString(TAG_CLIENT_ID, clientId).apply();
    }

    public static String getClientID(Context ctx) {
        return ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE).getString(TAG_CLIENT_ID, null);
    }

    public static void setClientIdWasSet(boolean isSet,Context ctx){
        ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit().putBoolean(IS_CLIENT_ID_SET_TAG, isSet).apply();
    }

    public static boolean isClientIdSet(Context ctx){
      return ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE).getBoolean(IS_CLIENT_ID_SET_TAG, false);
    }

}
