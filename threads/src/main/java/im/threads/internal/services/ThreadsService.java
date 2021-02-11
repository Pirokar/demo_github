package im.threads.internal.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import im.threads.R;

public abstract class ThreadsService extends Service {

    protected static final String CHANNEL_ID = "im.threads.internal.services.ThreadsService.CHANNEL_ID";

    private static final int FOREGROUND_ID = 1423;

    private static final String EXTRA_IS_FOREGROUND = "IS_FOREGROUND";

    private static NotificationChannel notificationChannel;

    protected static void startService(final Context context, final Intent intent) {
        try {
            context.startService(intent);
        } catch (IllegalStateException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent.putExtra(EXTRA_IS_FOREGROUND, true));
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && intent.getBooleanExtra(EXTRA_IS_FOREGROUND, false)) {
                createNotificationChannel(getApplicationContext());
                if (notificationChannel == null) {
                    startForeground(FOREGROUND_ID, new Notification());
                } else {
                    startForeground(FOREGROUND_ID, new Notification.Builder(this, notificationChannel.getId()).build());
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    protected void createNotificationChannel(Context context) {
        if (notificationChannel == null) {
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
                if (notificationChannel == null) {
                    notificationChannel = new NotificationChannel(
                            CHANNEL_ID,
                            context.getString(R.string.threads_channel_name),
                            NotificationManager.IMPORTANCE_DEFAULT
                    );
                    notificationManager.createNotificationChannel(notificationChannel);
                }
            }
        }
    }

}
