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
import com.sequenia.threads.controllers.PushNotificationFormatter;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.utils.MessageFormatter;
import com.sequenia.threads.utils.PrefUtils;

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
         /*   List<ChatItem> list = MessageFormatter.formatMessages(il);
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) instanceof ConsultPhrase || list.get(i) instanceof ConsultConnectionMessage)
                    unreadMessages.add(list.get(i));
            }
            final NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this);
            notificationBuilder
                    .setSmallIcon(PrefUtils.getPushIconResid(getApplication()));
            if (unreadMessages.size() == 1) {
                String phrase = null;
                String notif = "";
                if (unreadMessages.get(0) instanceof ConsultPhrase) {
                    ConsultPhrase cp = (ConsultPhrase) unreadMessages.get(0);
                    phrase = cp.getPhrase();
                    boolean hasFile = cp.hasFile();
                    if (!TextUtils.isEmpty(phrase)) {
                        notif = phrase;
                        if (hasFile) {
                            notif = notif + " " + getString(R.string.with_file);
                        }
                    } else {
                        notif = notif + " " + getString(R.string.file_received);
                    }
                } else if (unreadMessages.get(0) instanceof ConsultConnectionMessage) {
                    ConsultConnectionMessage ccm = (ConsultConnectionMessage) unreadMessages.get(0);
                    if (!ccm.getSex()
                            && ccm.getConnectionType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_JOINED)) {
                        notif = ccm.getName() + " " + getString(R.string.connected_female);
                    } else if (!ccm.getSex()
                            && ccm.getConnectionType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_LEFT)) {
                        notif = ccm.getName() + " " + getString(R.string.left_female);
                    } else if (!ccm.getSex() && ccm.getConnectionType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_JOINED)) {
                        notif = ccm.getName() + " " + getString(R.string.connected);
                    } else if (ccm.getSex() && ccm.getConnectionType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_LEFT)) {
                        notif = ccm.getName() + " " + getString(R.string.left_dialog);
                    }
                }
                notificationBuilder
                        .setContentTitle(getString(R.string.one_new_message))
                        .setContentText(notif);

            } else if (unreadMessages.size() <= 5 && unreadMessages.size() != 0) {
                String notif = "";
                NotificationCompat.InboxStyle inboxStyle =
                        new NotificationCompat.InboxStyle();
                for (ChatItem ci : unreadMessages) {
                    notif = "";
                    if (ci instanceof ConsultPhrase) {
                        ConsultPhrase cp = (ConsultPhrase) ci;
                        String phrase = cp.getPhrase();
                        boolean hasFile = cp.hasFile();
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
                    } else if (ci instanceof ConsultConnectionMessage) {
                        ConsultConnectionMessage ccm = (ConsultConnectionMessage) unreadMessages.get(0);
                        if (!ccm.getSex()
                                && ccm.getConnectionType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_JOINED)) {
                            notif = ccm.getName() + " " + getString(R.string.connected_female);
                        } else if (!ccm.getSex()
                                && ccm.getConnectionType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_LEFT)) {
                            notif = ccm.getName() + " " + getString(R.string.left_female);
                        } else if (!ccm.getSex() && ccm.getConnectionType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_JOINED)) {
                            notif = ccm.getName() + " " + getString(R.string.connected);
                        } else if (ccm.getSex() && ccm.getConnectionType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_LEFT)) {
                            notif = ccm.getName() + " " + getString(R.string.left_dialog);
                        }
                    }
                    inboxStyle.addLine(notif);
                }
                String title = unreadMessages.size() == 1 ? getString(R.string.one_new_message) : unreadMessages.size() + " " + getString(R.string.new_messages);
                inboxStyle.setBuilder(notificationBuilder);
                inboxStyle.setBigContentTitle(title);
                notificationBuilder
                        .setStyle(inboxStyle);

            } else if (unreadMessages.size() > 5) {
                int filesnum = 0;
                for (ChatItem cp : unreadMessages) {
                    if (cp instanceof ConsultPhrase && ((ConsultPhrase) cp).hasFile()) filesnum++;
                }
                notificationBuilder
                        .setContentTitle(unreadMessages.size() + " " + getString(R.string.new_messages))
                        .setContentText(filesnum == 0 ? getString(R.string.you_have_unread_messages) : getString(R.string.you_have_unread_messages) + " " + getString(R.string.with_file));
            }*/
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
            List<String> out = pushNotificationFormatter.format(unreadMessages, incoming);
            final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            final NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat
                            .Builder(this)
                            .setSmallIcon(PrefUtils.getPushIconResid(getApplication()));

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
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pend = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
            notificationBuilder.setContentIntent(pend);
            notificationBuilder.setAutoCancel(true);
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
            PendingIntent pend = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            nc.setContentIntent(pend);
            nc.setSmallIcon(PrefUtils.getPushIconResid(getApplication()));
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
