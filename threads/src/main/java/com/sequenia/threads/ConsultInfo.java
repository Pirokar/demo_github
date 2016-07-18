package com.sequenia.threads;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by yuri on 14.07.2016.
 */
public class ConsultInfo {
    private static final String TAG = "ConsultInfo ";
    public static final String OPERATOR_STATUS = "OPERATOR_STATUS";
    public static final String OPERATOR_NAME = "OPERATOR_NAME";
    public static final String OPERATOR_TITLE = "OPERATOR_TITLE";
    public static final String OPERATOR_PHOTO = "OPERATOR_PHOTO";
    public static final String OPERATOR_ID = "OPERATOR_ID";

    private ConsultInfo() {
    }

    public static void setConsultInfo(String consultId, Bundle info, Context ctx) {
        if (ctx == null) return;
        SharedPreferences.Editor editor = ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit();
        editor.putString(OPERATOR_STATUS + consultId, info.getString("operatorStatus"));
        editor.putString(OPERATOR_NAME + consultId, info.getString("operatorName"));
        if (info.getString("alert") != null) {
            String title = info.getString("alert").split(" ")[0];
            editor.putString(OPERATOR_TITLE + consultId, info.getString(title));
        }
        editor.putString(OPERATOR_PHOTO + consultId, info.getString("operatorPhoto"));
        editor.apply();
    }

    public static void setCurrentConsultInfo(String consultId, Bundle info, Context ctx) {
        if (ctx == null) return;
        SharedPreferences.Editor editor = ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit();
        editor.putString(OPERATOR_STATUS + consultId, info.getString("operatorStatus")).commit();
        editor.putString(OPERATOR_NAME + consultId, info.getString("operatorName")).commit();
        if (info.getString("alert") != null) {
            String title = info.getString("alert").split(" ")[0];
            editor.putString(OPERATOR_TITLE + consultId, title).commit();
        }
        editor.putString(OPERATOR_PHOTO + consultId, info.getString("operatorPhoto")).commit();
        setCurrentConsultId(consultId, ctx);
    }

    public static void setCurrentConsultId(String consultId, Context ctx) {
        ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit().putString(OPERATOR_ID, consultId).commit();
    }

    public static String getConsultName(Context ctx, String id) {
        if (ctx == null) return null;
        return ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE).getString(OPERATOR_NAME + id, null);
    }

    public static String getCurrentConsultName(Context ctx) {
        if (ctx == null) {
            Log.e(TAG, "getCurrentConsultName context is null");
            return null;
        }
        Log.e(TAG, "getCurrentConsultName");// TODO: 18.07.2016
        Log.e(TAG, "current consult id is " + getCurrentConsultId(ctx));// TODO: 18.07.2016
        return getConsultName(ctx, getCurrentConsultId(ctx));
    }

    public static String getConsultStatus(Context ctx, String id) {
        if (ctx == null) return null;
        return ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE).getString(OPERATOR_STATUS + id, null);
    }

    public static String getConsultTitle(Context ctx, String id) {
        if (ctx == null) return null;
        return ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE).getString(OPERATOR_TITLE + id, null);
    }

    public static String getCurrentConsultTitle(Context ctx) {
        if (ctx == null) return null;
        return getConsultTitle(ctx, getCurrentConsultId(ctx));
    }

    public static String getConsultPhoto(Context ctx, String id) {
        if (ctx == null) return null;
        return ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE).getString(OPERATOR_PHOTO + id, null);
    }

    public static String getCurrentConsultPhoto(Context ctx) {
        if (ctx == null) return null;
        return getConsultPhoto(ctx, getCurrentConsultId(ctx));
    }


    public static String getCurrentConsultId(Context ctx) {
        if (ctx == null) return null;
        return ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE).getString(OPERATOR_ID, null);
    }

    public static void setCurrentConsultLeft(Context ctx) {
        if (ctx == null) return;
        ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit().putString(OPERATOR_ID, null).apply();
    }


    public static boolean isConsultConnected(Context ctx) {
        if (ctx == null) return false;
        String id = ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE).getString(OPERATOR_ID, null);
        return id != null;
    }


}
