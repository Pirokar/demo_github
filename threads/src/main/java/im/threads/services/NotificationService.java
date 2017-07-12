package im.threads.services;

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

import im.threads.R;
import im.threads.activities.ChatActivity;
import im.threads.activities.TranslucentActivity;
import im.threads.controllers.ChatController;
import im.threads.formatters.MarshmellowPushMessageFormatter;
import im.threads.formatters.MessageFormatter;
import im.threads.formatters.NugatMessageFormatter;
import im.threads.fragments.ChatFragment;
import im.threads.model.ChatItem;
import im.threads.model.ChatStyle;
import im.threads.model.CompletionHandler;
import im.threads.model.ConsultChatPhrase;
import im.threads.picasso_url_connection_only.Picasso;
import im.threads.picasso_url_connection_only.Target;
import im.threads.utils.CircleTransform;
import im.threads.utils.PrefUtils;
import im.threads.utils.TargetNoError;
import im.threads.utils.Tuple;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.text.TextUtils.isEmpty;

/**
 * Отображает пуш уведомление, о котором скачана полная информация.
 * Created by yuri on 17.08.2016.
 */
public class NotificationService extends Service {

    private static final String TAG = "NotificationService ";

    public static final String ACTION_ALL_MESSAGES_WERE_READ = "com.sequenia.threads.services.IncomingMessagesIntentService.ACTION_MESSAGES_WERE_READ";
    public static final String ACTION_ADD_UNREAD_MESSAGE = "com.sequenia.threads.services.IncomingMessagesIntentService.ACTION_ADD_UNREAD_MESSAGE";
    public static final String ACTION_ADD_UNSENT_MESSAGE = "com.sequenia.threads.services.IncomingMessagesIntentService.ACTION_ADD_UNSENT_MESSAGE";

    private static final int UNREAD_MESSAGE_PUSH_ID = 0;
    private static final int UNSENT_MESSAGE_PUSH_ID = 1;

    private ArrayList<ChatItem> unreadMessages = new ArrayList<>();
    private myBroadcastReceiver mBroadcastReceiver;
    Handler h = new Handler(Looper.getMainLooper());
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private ChatStyle style;

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

        if (style == null) {
            style = PrefUtils.getIncomingStyle(this);
        }

        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new myBroadcastReceiver();
            getApplicationContext().registerReceiver(mBroadcastReceiver, new IntentFilter(NotificationService.ACTION_ALL_MESSAGES_WERE_READ));
        }

        if (intent == null) {
            return START_STICKY;
        }

        ArrayList<com.pushserver.android.PushMessage> il = intent.getParcelableArrayListExtra(ACTION_ADD_UNREAD_MESSAGE);

        if (il != null) {
            final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
            List<ChatItem> items = MessageFormatter.formatMessages(il);

            if (Build.VERSION.SDK_INT < 24) {
                Notification notification = getMstyleNotif(notificationBuilder, items);
                notification.defaults |= Notification.DEFAULT_SOUND;
                notification.defaults |= Notification.DEFAULT_VIBRATE;
                if(needsShowNotification()) {
                    nm.notify(UNREAD_MESSAGE_PUSH_ID, notification);
                    ChatController.notifyUnreadMessagesCountChanged(NotificationService.this);
                }
            } else {
                getNstyleNotif(notificationBuilder, items, new CompletionHandler<Notification>() {
                    @Override
                    public void onComplete(final Notification data) {
                        if(needsShowNotification()) {
                            data.defaults |= Notification.DEFAULT_SOUND;
                            data.defaults |= Notification.DEFAULT_VIBRATE;
                            nm.notify(UNREAD_MESSAGE_PUSH_ID, data);
                            ChatController.notifyUnreadMessagesCountChanged(NotificationService.this);
                        }
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
            PendingIntent pend = getChatIntent();
            if (style.defPushIconResid != ChatStyle.INVALID) {
                final int iconResId = style.defPushIconResid;
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

    private boolean needsShowNotification() {
        return !ChatFragment.isShown();
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
    }

    Notification getMstyleNotif(
            android.support.v7.app.NotificationCompat.Builder builder, List<ChatItem> items) {

        Tuple<Boolean, MarshmellowPushMessageFormatter.PushContents> pushText = new
                MarshmellowPushMessageFormatter(this, unreadMessages, items)
                .getFormattedMessageAsPushContents();

        final RemoteViews pushSmall = new RemoteViews(getPackageName(), R.layout.remote_push_small);
        final RemoteViews pushBig = new RemoteViews(getPackageName(), R.layout.remote_push_expanded);

        String avatarPath = null;
        for (int i = unreadMessages.size() - 1; i >= 0; i--) {
            if (isEmpty(avatarPath)) {
                if (unreadMessages.get(i) instanceof ConsultChatPhrase) {
                    avatarPath = ((ConsultChatPhrase) unreadMessages.get(i)).getAvatarPath();
                    break;
                }
            }
        }

        if (!isEmpty(avatarPath)) {
            Picasso
                    .with(this)
                    .load(avatarPath)
                    .transform(new CircleTransform())
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            pushSmall.setImageViewBitmap(R.id.icon_large, bitmap);
                            pushBig.setImageViewBitmap(R.id.icon_large, bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            Bitmap big = BitmapFactory.decodeResource(getResources(), R.drawable.blank_avatar_round);
                            pushSmall.setImageViewBitmap(R.id.icon_large, big);
                            pushBig.setImageViewBitmap(R.id.icon_large, big);
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });

            Target smallPicTarger = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {//round icon in corner
                    pushSmall.setImageViewBitmap(R.id.icon_small_corner, bitmap);
                    pushBig.setImageViewBitmap(R.id.icon_small_corner, bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    Bitmap big = BitmapFactory.decodeResource(getResources(), R.drawable.blank_avatar_round);
                    pushSmall.setImageViewBitmap(R.id.icon_small_corner, big);
                    pushBig.setImageViewBitmap(R.id.icon_small_corner, big);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };

            if (style != null
                    && style.defPushIconResid != ChatStyle.INVALID) {
                Picasso
                        .with(this)
                        .load(style.defPushIconResid)
                        .transform(new CircleTransform())
                        .into(smallPicTarger);
            } else {
                Picasso
                        .with(this)
                        .load(R.drawable.defult_big_icon)
                        .transform(new CircleTransform())
                        .into(smallPicTarger);
            }
        } else {
            if (style != null && style.defPushIconResid != ChatStyle.INVALID) {
                Bitmap icon = BitmapFactory.decodeResource(getResources(), style.defPushIconResid);
                pushSmall.setImageViewBitmap(R.id.icon_large, icon);
                pushBig.setImageViewBitmap(R.id.icon_large, icon);
                pushSmall.setImageViewBitmap(R.id.icon_small_corner,null);
                pushBig.setImageViewBitmap(R.id.icon_small_corner,null);
            } else {
                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.defult_big_icon);
                pushSmall.setImageViewBitmap(R.id.icon_large, icon);
                pushBig.setImageViewBitmap(R.id.icon_large, icon);
                pushSmall.setImageViewBitmap(R.id.icon_small_corner,null);
                pushBig.setImageViewBitmap(R.id.icon_small_corner,null);
            }
        }
        if (style != null && style.defTitleResId != ChatStyle.INVALID) {
            builder.setContentTitle(getString(style.defTitleResId));
            pushSmall.setTextViewText(R.id.title, getString(style.defTitleResId));
            pushBig.setTextViewText(R.id.title, getString(style.defTitleResId));
        } else {
            builder.setContentTitle(getString(R.string.title_default));
            pushSmall.setTextViewText(R.id.title, getString(R.string.title_default));
            pushBig.setTextViewText(R.id.title, getString(R.string.title_default));
        }
        int color = style != null && style.pushBackgroundColorResId != ChatStyle.INVALID ? style.pushBackgroundColorResId : android.R.color.white;
        if (style != null && style.pushBackgroundColorResId != ChatStyle.INVALID) {
            pushSmall.setInt(R.id.root_small, "setBackgroundColor", getResources().getColor(color));
            pushBig.setInt(R.id.root_big, "setBackgroundColor", getResources().getColor(color));
        }
        if (style != null && style.incomingMessageTextColor != ChatStyle.INVALID) {
            pushSmall.setInt(R.id.text, "setTextColor", getResources().getColor(style.incomingMessageTextColor));
            pushBig.setInt(R.id.text, "setTextColor", getResources().getColor(style.incomingMessageTextColor));
        }
        builder
                .setColor(getResources().getColor(color))
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
            if (style != null && style.nugatPushAccentColorResId != ChatStyle.INVALID)
                builder.setColor(getColor(style.nugatPushAccentColorResId));
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
            if (style != null && style.defPushIconResid != ChatStyle.INVALID) {
                builder.setSmallIcon(style.defPushIconResid);
            } else {
                builder.setSmallIcon(R.drawable.done);
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
        buttonIntent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent buttonPend = PendingIntent.getActivity(
                this,
                1
                , buttonIntent
                , PendingIntent.FLAG_CANCEL_CURRENT);
        return buttonPend;
    }


    private PendingIntent getChatIntent() {
        return ChatController.getPendingIntentCreator().createPendingIntent(this);
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
