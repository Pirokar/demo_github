package com.sequenia.threads.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.pushserver.android.PushMessage;

import org.json.JSONException;
import org.json.JSONObject;

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
    public static final String SEARCHING_CONSULT = "SEARCHING_CONSULT";

    private ConsultInfo() {
    }

    public static void setSearchingConsult(boolean isSearching, Context ctx) {
        ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit().putBoolean(ConsultInfo.class+ SEARCHING_CONSULT, isSearching).commit();
    }

    public static boolean istSearchingConsult(Context ctx) {
        if (ctx == null) return false;
        return ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE).getBoolean(ConsultInfo.class+ SEARCHING_CONSULT, false);
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

    public static void setCurrentConsultInfo(String consultId
            , String operatorStatus
            , String operatorName
            , String operatorTitle
            , String operatorPhoto
            , Context ctx) {
        if (ctx == null) return;
        SharedPreferences.Editor editor = ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit();
        editor.putString(OPERATOR_STATUS + consultId, operatorStatus).commit();
        editor.putString(OPERATOR_NAME + consultId, operatorName).commit();
        editor.putString(OPERATOR_TITLE + consultId, operatorTitle).commit();
        editor.putString(OPERATOR_PHOTO + consultId, operatorPhoto).commit();
        setCurrentConsultId(consultId, ctx);
    }
    public static void setCurrentConsultInfo(PushMessage pushMessage, Context ctx) throws JSONException {
        if (ctx == null) return;
        JSONObject fullMessage = new JSONObject(pushMessage.getFullMessage());
        JSONObject operatorInfo = fullMessage.getJSONObject("operator");
        final String name = operatorInfo.getString("name");
        String status = operatorInfo.getString("status");
        String photoUrl = operatorInfo.getString("photoUrl");
        final String title = ConsultInfo.getCurrentConsultTitle(ctx);
        String id = operatorInfo.getString("id");
        SharedPreferences.Editor editor = ctx.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit();
        editor.putString(OPERATOR_STATUS + id, status).commit();
        editor.putString(OPERATOR_NAME + id, name).commit();
        editor.putString(OPERATOR_TITLE + id, title).commit();
        editor.putString(OPERATOR_PHOTO + id, photoUrl).commit();
        editor.putString(OPERATOR_ID + id, operatorInfo.getString("id")).commit();
        setCurrentConsultId(name, ctx);
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
