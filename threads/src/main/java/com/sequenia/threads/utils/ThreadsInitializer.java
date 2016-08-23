package com.sequenia.threads.utils;

import android.Manifest;
import android.content.Context;
import android.preference.PreferenceManager;

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
        return instance;
    }

    private ThreadsInitializer(Context ctx) {
        isInited = ctx.getSharedPreferences(this.getClass().toString(), Context.MODE_PRIVATE).getBoolean("init", false);
        this.ctx = ctx;
        isInited = PrefUtils.isClientIdSet(ctx);
    }

    public synchronized boolean init() throws IllegalArgumentException {
        if (PermissionChecker.isCoarseLocationPermissionGranted(ctx) && PermissionChecker.isReadSmsPermissionGranted(ctx) && PermissionChecker.isReadPhoneStatePermissionGranted(ctx)) {
            PushController.getInstance(ctx).init();
            return true;
        }
        return false;
    }

    public boolean isInited() {
        return PrefUtils.isClientIdSet(ctx);
    }

    public String getStatus() {
        if (PrefUtils.isClientIdSet(ctx)) {
            return "Threads is initialised";
        }
        HashMap<String, Boolean> map = new HashMap<>();
        map.put(Manifest.permission.ACCESS_COARSE_LOCATION, PermissionChecker.isCoarseLocationPermissionGranted(ctx));
        map.put(Manifest.permission.READ_SMS, PermissionChecker.isReadSmsPermissionGranted(ctx));
        map.put(Manifest.permission.READ_PHONE_STATE, PermissionChecker.isReadPhoneStatePermissionGranted(ctx));
        return "threads is not inited\n permissions " + map;
    }
}
