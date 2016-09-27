package com.sequenia.threads.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.sequenia.threads.R;
import com.sequenia.threads.activities.ChatActivity;
import com.sequenia.threads.activities.TranslucentActivity;
import com.sequenia.threads.formatters.NugatMessageFormatter;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.CompletionHandler;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.picasso_url_connection_only.Picasso;
import com.sequenia.threads.picasso_url_connection_only.Target;
import com.sequenia.threads.utils.CircleTransform;
import com.sequenia.threads.formatters.MessageFormatter;
import com.sequenia.threads.utils.PrefUtils;
import com.sequenia.threads.formatters.MarshmellowPushMessageFormatter;
import com.sequenia.threads.utils.TargetNoError;
import com.sequenia.threads.utils.Tuple;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private ExecutorService executor = Executors.newSingleThreadExecutor();

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
            final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            final NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat
                            .Builder(this);
            List<ChatItem> items = MessageFormatter.formatMessages(il);
            Notification notification = null;
            if (Build.VERSION.SDK_INT < 24) {
                notification = getMstyleNotif(notificationBuilder, items);
                final Notification finalNotification = notification;
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        nm.notify(UNREAD_MESSAGE_PUSH_ID, finalNotification);
                    }
                };
                unreadMessagesRunnables.add(r);
                h.postDelayed(r, 3000);
            } else {
                getNstyleNotif(notificationBuilder, items, new CompletionHandler<Notification>() {
                    @Override
                    public void onComplete(final Notification data) {
                        Log.e(TAG, "oncomplete");// TODO: 15.09.2016  
                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                nm.notify(UNREAD_MESSAGE_PUSH_ID, data);
                            }
                        };
                        unreadMessagesRunnables.add(r);
                        h.postDelayed(r, 3000);
                    }

                    @Override
                    public void onError(Throwable e, String message, Notification data) {

                    }
                });
            }
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

    Notification getMstyleNotif(
            android.support.v7.app.NotificationCompat.Builder builder
            , List<ChatItem> items) {
        Tuple<Boolean, MarshmellowPushMessageFormatter.PushContents> pushText = new
                MarshmellowPushMessageFormatter(this
                , unreadMessages
                , items)
                .getFormattedMessageAsPushContents();
        final RemoteViews pushSmall = new RemoteViews(getPackageName(), R.layout.remote_push_small);
        final RemoteViews pushBig = new RemoteViews(getPackageName(), R.layout.remote_push_expanded);

        String avatarPath = null;
        for (int i = unreadMessages.size() - 1; i >= 0; i--) {
            if (avatarPath == null) {
                if (unreadMessages.get(i) instanceof ConsultConnectionMessage) {
                    avatarPath = ((ConsultConnectionMessage) unreadMessages.get(i)).getAvatarPath();
                } else if (unreadMessages.get(i) instanceof ConsultPhrase) {
                    avatarPath = ((ConsultPhrase) unreadMessages.get(i)).getAvatarPath();
                }
            }
        }
        if (avatarPath != null) {
            Target avatarTarget = new TargetNoError() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    pushSmall.setImageViewBitmap(R.id.image, bitmap);
                    pushBig.setImageViewBitmap(R.id.image, bitmap);
                }
            };
            Picasso
                    .with(this)
                    .load(avatarPath)
                    .transform(new CircleTransform())
                    .into(avatarTarget);

            Target smallPicTarger = new TargetNoError() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    pushSmall.setImageViewBitmap(R.id.image_small, bitmap);
                    pushBig.setImageViewBitmap(R.id.image_small, bitmap);
                }
            };
            Picasso
                    .with(this)
                    .load(PrefUtils.getPushIconResid(this))
                    .transform(new CircleTransform())
                    .into(smallPicTarger);
        } else {
            if (PrefUtils.getPushIconResid(this) != -1) {
                Bitmap icon = BitmapFactory.decodeResource(getResources(), PrefUtils.getPushIconResid(this));
                pushSmall.setImageViewBitmap(R.id.image, icon);
                pushBig.setImageViewBitmap(R.id.image, icon);
            } else {

            }
        }
        if (PrefUtils.getPushTitle(this) != -1) {
            builder.setContentTitle(getString(PrefUtils.getPushTitle(this)));
            pushSmall.setTextViewText(R.id.title, getString(PrefUtils.getPushTitle(this)));
            pushBig.setTextViewText(R.id.title, getString(PrefUtils.getPushTitle(this)));
        }

        builder
                .setColor(getResources().getColor(android.R.color.white))
                .setSmallIcon(R.drawable.empty);
        pushSmall.setTextViewText(R.id.consult_name, pushText.second.consultName + ":");
        pushSmall.setTextViewText(R.id.text, pushText.second.contentDescription.trim());

        pushBig.setTextViewText(R.id.consult_name, pushText.second.consultName + ":");
        pushBig.setTextViewText(R.id.text, pushText.second.contentDescription.trim());

        if (pushText.second.isOnlyImages) {
            pushSmall.setViewVisibility(R.id.attach_image, View.VISIBLE);
            pushBig.setViewVisibility(R.id.attach_image, View.VISIBLE);
            Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.insert_photo_grey_48x48);
            pushSmall.setImageViewBitmap(R.id.attach_image, b);
            pushBig.setImageViewBitmap(R.id.attach_image, b);
        } else if (pushText.second.isWithAttachments) {
            pushSmall.setViewVisibility(R.id.attach_image, View.VISIBLE);
            pushBig.setViewVisibility(R.id.attach_image, View.VISIBLE);
            Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.attach_file_grey_48x48);
            pushSmall.setImageViewBitmap(R.id.attach_image, b);
            pushBig.setImageViewBitmap(R.id.attach_image, b);
        } else if (!pushText.second.isWithAttachments) {
            pushSmall.setViewVisibility(R.id.attach_image, View.GONE);
            pushBig.setViewVisibility(R.id.attach_image, View.GONE);
        }
        pushBig.setTextViewText(R.id.reply, getString(R.string.reply));
        builder.setContent(pushSmall);


        PendingIntent pend = getChatIntent();
        builder.setContentIntent(pend);
        builder.setAutoCancel(true);
        builder.setContentIntent(pend);
        if (pushText.first) {
            builder.setCustomBigContentView(pushBig);
            PendingIntent buttonPend = getFastAnswerIntent();
            pushBig.setOnClickPendingIntent(R.id.reply, buttonPend);
        }
        Notification notification = builder.build();
        try {
            int smallIconViewId = getResources().getIdentifier("right_icon", "id", android.R.class.getPackage().getName());
            notification.contentView.setViewVisibility(smallIconViewId, View.INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return notification;
    }

    void getNstyleNotif(
            final android.support.v7.app.NotificationCompat.Builder builder
            , List<ChatItem> items, final CompletionHandler<Notification> completionHandler) {
        builder.setShowWhen(true);
        if (Build.VERSION.SDK_INT > 23) {
            builder.setColor(getColor(android.R.color.holo_red_light));
        }
        final Tuple<Boolean, NugatMessageFormatter.PushContents> out
                = new NugatMessageFormatter(this, unreadMessages, items).getFormattedMessageAsPushContents();
        final NugatMessageFormatter.PushContents pushContents = out.second;
        if (pushContents.hasAvatar) {
            TargetNoError avatarTarget = new TargetNoError() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    builder.setLargeIcon(bitmap);
                }
            };
            Picasso
                    .with(this)
                    .load(pushContents.avatarPath)
                    .transform(new CircleTransform())
                    .into(avatarTarget);
        }
        builder.setContentTitle(pushContents.titleText);
        if (!pushContents.hasImage && !pushContents.hasPlainFiles && pushContents.phrasesCount > 1) {
        } else {
            builder.setContentText(pushContents.contentText);
        }
        if (!pushContents.hasImage && !pushContents.hasPlainFiles) {
            if (PrefUtils.getPushIconResid(this) != -1) {
                builder.setSmallIcon(PrefUtils.getPushIconResid(this));
            } else {
                builder.setSmallIcon(R.drawable.sample);
            }
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    builder.setContentIntent(getChatIntent());
                    if (out.first) {
                        builder.addAction(0, getString(R.string.answer), getFastAnswerIntent());
                    }
                    completionHandler.onComplete(builder.build());

                }
            });

            return;
        }
        if (pushContents.hasImage
                && !pushContents.hasPlainFiles
                && pushContents.imagesCount == 1) {
            final NotificationCompat.BigPictureStyle pictureStyle
                    = new android.support.v4.app.NotificationCompat.BigPictureStyle();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        URLConnection url = new URL(pushContents.lastImagePath).openConnection();
                        Bitmap b = BitmapFactory.decodeStream(url.getInputStream());
                        pictureStyle.bigPicture(b);
                        builder.setContentIntent(getChatIntent());
                        builder.setSmallIcon(R.drawable.insert_photo_grey_48x48);
                        builder.setStyle(pictureStyle);
                        builder.setContentIntent(getChatIntent());
                        builder.setContentIntent(getChatIntent());
                        if (out.first) {
                            builder.addAction(0, getString(R.string.answer), getFastAnswerIntent());
                        }
                        completionHandler.onComplete(builder.build());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            return;
        }
        if ((pushContents.hasImage && pushContents.hasPlainFiles)
                || (pushContents.hasPlainFiles && !pushContents.hasImage)) {
            builder.setSmallIcon(R.drawable.attach_file_grey_48x48);
        } else if (pushContents.hasImage && !pushContents.hasPlainFiles) {
            builder.setSmallIcon(R.drawable.insert_photo_grey_48x48);
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                builder.setContentIntent(getChatIntent());
                if (out.first) {
                    builder.addAction(0, getString(R.string.answer), getFastAnswerIntent());
                }
                completionHandler.onComplete(builder.build());
            }
        });
        return;
    }

    private PendingIntent getFastAnswerIntent() {
        Intent buttonIntent = new Intent(this, TranslucentActivity.class);
        buttonIntent.setFlags(/*Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        |*/ Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                /*| Intent.FLAG_ACTIVITY_CLEAR_TASK*/);
        PendingIntent buttonPend = PendingIntent.getActivity(
                this,
                1
                , buttonIntent
                , PendingIntent.FLAG_CANCEL_CURRENT);
        return buttonPend;
    }


    private PendingIntent getChatIntent() {
        Intent i = new Intent(this, ChatActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pend = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        return pend;
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
