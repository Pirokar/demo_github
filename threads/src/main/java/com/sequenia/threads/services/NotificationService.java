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
import com.sequenia.threads.activities.FilesActivity;
import com.sequenia.threads.activities.TranslucentActivity;
import com.sequenia.threads.controllers.PushNotificationFormatter;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.utils.MessageFormatter;
import com.sequenia.threads.utils.PrefUtils;
import com.sequenia.threads.utils.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private ArrayList<ChatItem> unreadMessages = new ArrayList<>();
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
        Log.i(TAG, "onStartCommand");
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new myBroadcastReceiver();
            getApplicationContext().registerReceiver(mBroadcastReceiver, new IntentFilter(NotificationService.ACTION_ALL_MESSAGES_WERE_READ));
        }
        if (intent == null) return START_STICKY;
        ArrayList<com.pushserver.android.PushMessage> il = intent.getParcelableArrayListExtra(ACTION_ADD_UNREAD_MESSAGE);
        if (il != null) {
            List<ChatItem> incoming = MessageFormatter.formatMessages(il);
            PushNotificationFormatter pushNotificationFormatter =
                    new PushNotificationFormatter(getString(R.string.push_connected_female)
                            , getString(R.string.push_connected)
                            , getString(R.string.push_left_female)
                            , getString(R.string.push_left)
                            , getString(R.string.file_received)
                            , getString(R.string.with_file)
                            , getString(R.string.you_have_unread_messages)
                            , getString(R.string.with_files));
            Tuple<Boolean, List<String>> output = pushNotificationFormatter.format(unreadMessages, incoming);
            List<String> out = output.second;
            final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            final NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat
                            .Builder(this);
            if (PrefUtils.getPushIconResid(this) != -1) {
                final int iconResId = PrefUtils.getPushIconResid(this);
                notificationBuilder.setSmallIcon(iconResId);
            } else {
                notificationBuilder.setSmallIcon(R.drawable.empty);
            }
            if (unreadMessages.size() == 1 && out.size() == 1) {
                notificationBuilder
                        .setContentTitle(getString(R.string.one_new_message))
                        .setContentText(out.get(0));
            } else if (unreadMessages.size() != 0 && unreadMessages.size() <= 5) {
                NotificationCompat.InboxStyle inboxStyle =
                        new NotificationCompat.InboxStyle();
                String title = unreadMessages.size() == 1 ? getString(R.string.one_new_message) : unreadMessages.size() + " " + getString(R.string.new_messages);
                inboxStyle.setBuilder(notificationBuilder);
                inboxStyle.setBigContentTitle(title);
                for (String s : out) {
                    inboxStyle.addLine(s);
                }
                notificationBuilder
                        .setStyle(inboxStyle);
            } else if (unreadMessages.size() > 5 && out.size() == 1) {
                notificationBuilder
                        .setContentTitle(unreadMessages.size() + " " + getString(R.string.new_messages))
                        .setContentText(out.get(0));
            }
            Intent i = new Intent(this, ChatActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pend = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
            notificationBuilder.setContentIntent(pend);
            notificationBuilder.setAutoCancel(true);
            if (output.first) {
                Intent buttonIntent = new Intent(this, TranslucentActivity.class);
                buttonIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                buttonIntent.putExtra("1", true);//have no idea why it's not working
                PendingIntent buttonPend = PendingIntent.getActivity(
                        this,
                        1
                        , buttonIntent
                        , PendingIntent.FLAG_CANCEL_CURRENT);
                notificationBuilder.addAction(
                        R.drawable.ic_reply_white_24dp,
                        getString(R.string.answer),
                        buttonPend);
            }
            Runnable r = new Runnable() {
                @Override
                public void run() {
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
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pend = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
            if (PrefUtils.getPushIconResid(this) != -1) {
                final int iconResId = PrefUtils.getPushIconResid(this);
                nc.setSmallIcon(iconResId);
            } else {
                nc.setSmallIcon(R.drawable.empty);
            }
            nc.setContentIntent(pend);
            nc.setAutoCancel(true);
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    nm.notify(UNSENT_MESSAGE_PUSH_ID, nc.build());
                }
            }, 1500);
        }

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBroadcastReceiver != null)
            getApplicationContext().unregisterReceiver(mBroadcastReceiver);
    }

    private void dismissUnreadMessagesNotification() {
        unreadMessages.clear();
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(UNREAD_MESSAGE_PUSH_ID);
        for (Runnable r : unreadMessagesRunnables) {
            h.removeCallbacks(r);
        }
        unreadMessagesRunnables.clear();
    }


    private class myBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(NotificationService.ACTION_ALL_MESSAGES_WERE_READ)) {
                dismissUnreadMessagesNotification();
            }
        }
    }
}
