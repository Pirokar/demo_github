package com.sequenia.threads;

import android.Manifest;
import android.content.Context;

import com.pushserver.android.PushController;

import java.util.HashMap;

/**
 * Created by yuri on 13.07.2016.
 */
public class ThreadsInitializer {
    private static ThreadsInitializer instance;
    private boolean isInited;
    private Context ctx;

    public static ThreadsInitializer getInstance(Context ctx) {
        if (instance == null) {
            instance = new ThreadsInitializer(ctx);
        }
        instance.init();
        return instance;
    }

    private ThreadsInitializer(Context ctx) {
        isInited = ctx.getSharedPreferences(this.getClass().toString(), Context.MODE_PRIVATE).getBoolean("init", false);
        this.ctx = ctx;
    }

    public boolean init() {
        if (isInited) return true;
        if (!isInited && PermissionChecker.isCoarseLocationPermissionGranted(ctx) && PermissionChecker.isReadSmsPermissionGranted(ctx) && PermissionChecker.isReadPhoneStatePermissionGranted(ctx)) {
            PushController.getInstance(ctx).init();
            isInited = true;
            ctx.getSharedPreferences(this.getClass().toString(), Context.MODE_PRIVATE).edit().putBoolean("init", true).apply();
            return true;
        }
        return false;
    }

    public boolean isInited() {
        return isInited;
    }

    public String getStatus() {
        if (isInited) {
            return "Threads is initialised";
        }
        HashMap<String, Boolean> map = new HashMap<>();
        map.put(Manifest.permission.ACCESS_COARSE_LOCATION, PermissionChecker.isCoarseLocationPermissionGranted(ctx));
        map.put(Manifest.permission.READ_SMS, PermissionChecker.isReadSmsPermissionGranted(ctx));
        map.put(Manifest.permission.READ_PHONE_STATE, PermissionChecker.isReadPhoneStatePermissionGranted(ctx));
        return "threads is not inited\n permissions " + map;
    }
}
