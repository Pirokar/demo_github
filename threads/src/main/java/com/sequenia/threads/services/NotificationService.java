package com.sequenia.threads.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.sequenia.threads.R;
import com.sequenia.threads.activities.ChatActivity;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.utils.MessageFormatter;

import java.util.ArrayList;

/**
 * Created by yuri on 17.08.2016.
 */
public class NotificationService extends Service {
    private static final String TAG = "NotificationService ";
    public static final String ACTION_ALL_MESSAGES_WERE_READ = "com.sequenia.threads.services.MyServerIntentService.ACTION_MESSAGES_WERE_READ";
    public static final String ACTION_ADD_UNREAD_MESSAGE = "com.sequenia.threads.services.MyServerIntentService.ACTION_ADD_UNREAD_MESSAGE";
    public static final String ACTION_ADD_UNSENT_MESSAGE = "com.sequenia.threads.services.MyServerIntentService.ACTION_ADD_UNSENT_MESSAGE";
    private static final int UNREAD_MESSAGE_PUSH_ID = 0;
    private static final int UNSENT_MESSAGE_PUSH_ID = 1;
    private myBroadcastReceiver mBroadcastReceiver;
    private ArrayList<ConsultPhrase> unreadMessages = new ArrayList<>();
    private ArrayList<Runnable> unreadMessagesRunnables = new ArrayList<>();
    Handler h = new Handler(Looper.getMainLooper());

    public NotificationService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");// TODO: 17.08.2016
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new myBroadcastReceiver();
            getApplicationContext().registerReceiver(mBroadcastReceiver, new IntentFilter(NotificationService.ACTION_ALL_MESSAGES_WERE_READ));
        }
        if (intent==null)return START_STICKY;
        ArrayList<com.pushserver.android.PushMessage> il = intent.getParcelableArrayListExtra(ACTION_ADD_UNREAD_MESSAGE);
        if (il != null) {
            ArrayList<ConsultPhrase> list = MessageFormatter.format(il);
            unreadMessages.addAll(list);
            final NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this);

            notificationBuilder
                    .setSmallIcon(R.drawable.sample);
            if (unreadMessages.size() == 1) {
                String phrase = unreadMessages.get(0).getPhrase();
                boolean hasFile = unreadMessages.get(0).hasFile();
                String notif = "";
                if (!TextUtils.isEmpty(phrase)) {
                    notif = phrase;
                    if (hasFile) {
                        notif = notif + " " + getString(R.string.with_file);
                    }
                } else {
                    notif = notif + " " + getString(R.string.file_received);
                }
                notificationBuilder
                        .setContentTitle(getString(R.string.one_new_message))
                        .setContentText(notif);
            } else if (unreadMessages.size() <= 5 && unreadMessages.size() != 0) {
                Log.e(TAG, "unreadMessages.size() < 5 && unreadMessages.size() != 0");// TODO: 17.08.2016
                String notif = "";
                NotificationCompat.InboxStyle inboxStyle =
                        new NotificationCompat.InboxStyle();
                for (ConsultPhrase cp : unreadMessages) {
                    String phrase = cp.getPhrase();
                    boolean hasFile = cp.hasFile();
                    notif = "";
                    if (!TextUtils.isEmpty(phrase)) {
                        if (phrase.length() > 40) {
                            notif += phrase.substring(0, 40).concat("...");
                        } else {
                            notif += phrase;
                        }
                        if (hasFile) {
                            notif = notif + " " + getString(R.string.with_file);
                        }
                    } else {
                        notif = notif + " " + getString(R.string.file_received);
                    }
                    inboxStyle.addLine(notif);
                }
                String title = unreadMessages.size() == 1 ? getString(R.string.one_new_message) : unreadMessages.size() + " " + getString(R.string.new_messages);
                Log.e(TAG, "" + notif);// TODO: 17.08.2016
                inboxStyle.setBuilder(notificationBuilder);
                inboxStyle.setBigContentTitle(title);
                notificationBuilder
                        .setStyle(inboxStyle);

            } else if (unreadMessages.size() > 5) {
                int filesnum = 0;
                for (ConsultPhrase cp : unreadMessages
                        ) {
                    if (cp.hasFile()) filesnum++;
                }
                notificationBuilder
                        .setContentTitle(unreadMessages.size() + " " + getString(R.string.new_messages))
                        .setContentText(filesnum == 0 ? getString(R.string.you_have_unread_messages) : getString(R.string.you_have_unread_messages) + " " + getString(R.string.with_file));
            }
            final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Intent i = new Intent(this, ChatActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pend = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            Log.e(TAG, "getAppContext = " + getApplicationContext());
            Log.e(TAG, "PendingIntent =" + pend);// TODO: 17.08.2016
            notificationBuilder.setContentIntent(pend);
            notificationBuilder.setAutoCancel(true);
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "run");// TODO: 17.08.2016
                    nm.notify(UNREAD_MESSAGE_PUSH_ID, notificationBuilder.build());
                }
            };
            unreadMessagesRunnables.add(r);
            h.postDelayed(r, 3000);
        } else if (intent.getAction() != null && intent.getAction().equals(ACTION_ADD_UNSENT_MESSAGE)) {
            final NotificationCompat.Builder nc = new NotificationCompat.Builder(this);
            nc.setContentTitle(getString(R.string.message_were_unsent));
            final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Intent i = new Intent(this, ChatActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pend = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            nc.setContentIntent(pend);
            nc.setSmallIcon(R.drawable.sample);
            nc.setAutoCancel(true);
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    nm.notify(UNSENT_MESSAGE_PUSH_ID, nc.build());
                }
            },1500);
        }
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy()");// TODO: 17.08.2016
        super.onDestroy();
        if (mBroadcastReceiver != null)
            getApplicationContext().unregisterReceiver(mBroadcastReceiver);
    }

    private void dismissUnreadMessagesNotification() {
        Log.e(TAG, "dismissUnreadMessagesNotification()");// TODO: 17.08.2016
        unreadMessages.clear();
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(UNREAD_MESSAGE_PUSH_ID);
        for (Runnable r:unreadMessagesRunnables) {
            h.removeCallbacks(r);
        }
        unreadMessagesRunnables.clear();
    }


    private class myBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(NotificationService.ACTION_ALL_MESSAGES_WERE_READ)) {
                Log.e(TAG, "onReceive");// TODO: 17.08.2016
                dismissUnreadMessagesNotification();
            }
        }
    }
}
