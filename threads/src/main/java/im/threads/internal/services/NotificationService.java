package im.threads.internal.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.util.Consumer;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.ThreadsLib;
import im.threads.internal.Config;
import im.threads.internal.activities.QuickAnswerActivity;
import im.threads.internal.database.DatabaseHolder;
import im.threads.internal.formatters.MessageFormatter;
import im.threads.internal.picasso_url_connection_only.Picasso;
import im.threads.internal.picasso_url_connection_only.Target;
import im.threads.internal.utils.CircleTransformation;
import im.threads.internal.utils.FileUtils;
import im.threads.internal.utils.TargetNoError;
import im.threads.internal.utils.ThreadsLogger;
import im.threads.view.ChatFragment;

/**
 * Отображает пуш уведомление, о котором скачана полная информация.
 */
public final class NotificationService extends ThreadsService {

    private static final String TAG = "NotificationService";
    private static final String CHANNEL_ID = "im.threads.internal.services.NotificationService.CHANNEL_ID";

    public static final String BROADCAST_ALL_MESSAGES_WERE_READ = "im.threads.internal.services.NotificationService.BROADCAST_ALL_MESSAGES_WERE_READ";

    private static final String ACTION_REMOVE_NOTIFICATION = "im.threads.internal.services.NotificationService.ACTION_REMOVE_NOTIFICATION";
    private static final String ACTION_ADD_UNREAD_MESSAGE = "im.threads.internal.services.NotificationService.ACTION_ADD_UNREAD_MESSAGE";
    private static final String ACTION_ADD_UNREAD_MESSAGE_LIST = "im.threads.internal.services.NotificationService.ACTION_ADD_UNREAD_MESSAGE_LIST";
    private static final String ACTION_ADD_UNSENT_MESSAGE = "im.threads.internal.services.NotificationService.ACTION_ADD_UNSENT_MESSAGE";

    public static final String EXTRA_MESSAGE = "im.threads.internal.services.NotificationService.EXTRA_MESSAGE";
    public static final String EXTRA_OPERATOR_URL = "im.threads.internal.services.NotificationService.EXTRA_OPERATOR_URL";
    public static final String EXTRA_APP_MARKER = "im.threads.internal.services.NotificationService.EXTRA_APP_MARKER";
    public static final String EXTRA_MESSAGE_CONTENT = "im.threads.internal.services.NotificationService.EXTRA_MESSAGE_CONTENT";

    private static final int UNREAD_MESSAGE_PUSH_ID = 0;
    private static final int UNSENT_MESSAGE_PUSH_ID = 1;

    private MyBroadcastReceiver mBroadcastReceiver;
    private final Handler h = new Handler(Looper.getMainLooper());
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private ChatStyle style;

    public NotificationService() {
    }

    public static void removeNotification(final @NonNull Context context) {
        startService(context, new Intent(context, NotificationService.class)
                .setAction(NotificationService.ACTION_REMOVE_NOTIFICATION));
    }

    public static void addUnreadMessage(final @NonNull Context context, String message, String operatorUrl, String appMarker) {
        startService(context, new Intent(context, NotificationService.class)
                .setAction(NotificationService.ACTION_ADD_UNREAD_MESSAGE)
                .putExtra(NotificationService.EXTRA_MESSAGE, message)
                .putExtra(NotificationService.EXTRA_OPERATOR_URL, operatorUrl)
                .putExtra(NotificationService.EXTRA_APP_MARKER, appMarker));
    }

    public static void addUnreadMessageList(final @NonNull Context context, String appMarker, MessageFormatter.MessageContent messageContent) {
        startService(context, new Intent(context, NotificationService.class)
                .setAction(NotificationService.ACTION_ADD_UNREAD_MESSAGE_LIST)
                .putExtra(NotificationService.EXTRA_APP_MARKER, appMarker)
                .putExtra(NotificationService.EXTRA_MESSAGE_CONTENT, messageContent));
    }

    public static void addUnsentMessage(final @NonNull Context context, String appMarker) {
        startService(context, new Intent(context, NotificationService.class)
                .setAction(NotificationService.ACTION_ADD_UNSENT_MESSAGE)
                .putExtra(NotificationService.EXTRA_APP_MARKER, appMarker));
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        ThreadsLogger.i(TAG, "onStartCommand");
        if (style == null) {
            style = Config.instance.getChatStyle();
        }
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new MyBroadcastReceiver();
            getApplicationContext().registerReceiver(mBroadcastReceiver,
                    new IntentFilter(NotificationService.BROADCAST_ALL_MESSAGES_WERE_READ));
        }
        if (intent == null) {
            return START_STICKY;
        }
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm == null) {
            return START_STICKY;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Threads Channel", NotificationManager.IMPORTANCE_DEFAULT);
            nm.createNotificationChannel(channel);
        }
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case ACTION_REMOVE_NOTIFICATION:
                    nm.cancel(UNREAD_MESSAGE_PUSH_ID);
                    break;
                case ACTION_ADD_UNREAD_MESSAGE:
                    final String message = intent.getStringExtra(EXTRA_MESSAGE);
                    if (Build.VERSION.SDK_INT < 24) {
                        final Notification notification = getPreNStyleNotification(intent, null, message);
                        notifyUnreadMessagesCountChanged(nm, notification);
                    } else {
                        getNStyleNotification(intent, null, notification -> notifyUnreadMessagesCountChanged(nm, notification), message);
                    }
                    break;
                case ACTION_ADD_UNREAD_MESSAGE_LIST:
                    final MessageFormatter.MessageContent messageContent = intent.getParcelableExtra(EXTRA_MESSAGE_CONTENT);
                    if (Build.VERSION.SDK_INT < 24) {
                        final Notification notification = getPreNStyleNotification(intent, messageContent, null);
                        notifyUnreadMessagesCountChanged(nm, notification);
                    } else {
                        getNStyleNotification(intent, messageContent, notification -> notifyUnreadMessagesCountChanged(nm, notification), null);
                    }
                    break;
                case ACTION_ADD_UNSENT_MESSAGE:
                    final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
                    notificationBuilder.setContentTitle(getString(R.string.threads_message_were_unsent));
                    final PendingIntent pend = getChatIntent(intent.getStringExtra(EXTRA_APP_MARKER));
                    final int iconResId = style.defPushIconResId;
                    notificationBuilder.setSmallIcon(iconResId);
                    notificationBuilder.setContentIntent(pend);
                    notificationBuilder.setAutoCancel(true);
                    h.postDelayed(() -> nm.notify(UNSENT_MESSAGE_PUSH_ID, notificationBuilder.build()), 1500);
                    break;
            }
        }
        return START_STICKY;
    }

    private boolean needsShowNotification() {
        return !ChatFragment.isShown();
    }

    private void notifyUnreadMessagesCountChanged(@NonNull final NotificationManager nm, final Notification notification) {
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        if (needsShowNotification()) {
            boolean fixPushCrash = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notification.getSmallIcon() == null) {
                fixPushCrash = true;
            }
            if (!fixPushCrash) {
                nm.notify(UNREAD_MESSAGE_PUSH_ID, notification);
            }
            ThreadsLib.UnreadMessagesCountListener l = Config.instance.unreadMessagesCountListener;
            if (l != null) {
                DatabaseHolder.getInstance().getUnreadMessagesCount(false, l);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBroadcastReceiver != null) {
            getApplicationContext().unregisterReceiver(mBroadcastReceiver);
        }
    }

    private Notification getPreNStyleNotification(final Intent intent, @Nullable final MessageFormatter.MessageContent messageContent, @Nullable final String message) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);

        final RemoteViews pushSmall = new RemoteViews(getPackageName(), R.layout.remote_push_small);
        final RemoteViews pushBig = new RemoteViews(getPackageName(), R.layout.remote_push_expanded);

        builder.setContentTitle(getString(style.defTitleResId));
        pushSmall.setTextViewText(R.id.title, getString(style.defTitleResId));
        pushBig.setTextViewText(R.id.title, getString(style.defTitleResId));

        pushSmall.setImageViewResource(R.id.icon_large_bg, R.drawable.ic_circle_40dp);
        pushBig.setImageViewResource(R.id.icon_large_bg, R.drawable.ic_circle_40dp);

        builder.setColor(getResources().getColor(style.pushBackgroundColorResId));
        pushSmall.setInt(R.id.icon_large_bg, "setColorFilter", getResources().getColor(style.pushBackgroundColorResId));
        pushBig.setInt(R.id.icon_large_bg, "setColorFilter", getResources().getColor(style.pushBackgroundColorResId));

        pushSmall.setInt(R.id.text, "setTextColor", getResources().getColor(style.incomingMessageTextColor));
        pushBig.setInt(R.id.text, "setTextColor", getResources().getColor(style.incomingMessageTextColor));

        builder.setSmallIcon(style.defPushIconResId);
        final boolean unreadMessage = !TextUtils.isEmpty(message);
        if (unreadMessage) {
            final String operatorUrl = intent.getStringExtra(EXTRA_OPERATOR_URL);
            if (!TextUtils.isEmpty(operatorUrl)) {
                showPreNStyleOperatorAvatar(FileUtils.convertRelativeUrlToAbsolute(operatorUrl), pushSmall, pushBig);
                showPreNStyleSmallIcon(pushSmall, pushBig);
            } else {
                final Bitmap icon = BitmapFactory.decodeResource(getResources(), style.defPushIconResId);
                pushSmall.setImageViewBitmap(R.id.icon_large, icon);
                pushBig.setImageViewBitmap(R.id.icon_large, icon);
                pushSmall.setImageViewBitmap(R.id.icon_small_corner, null);
                pushBig.setImageViewBitmap(R.id.icon_small_corner, null);
                pushSmall.setViewVisibility(R.id.consult_name, View.GONE);
                pushBig.setViewVisibility(R.id.consult_name, View.GONE);
                pushSmall.setViewVisibility(R.id.attach_image, View.GONE);
                pushBig.setViewVisibility(R.id.attach_image, View.GONE);
            }
            pushSmall.setTextViewText(R.id.text, message);
            pushBig.setTextViewText(R.id.text, message);
        } else if (messageContent != null) {
            if (!TextUtils.isEmpty(messageContent.avatarPath)) {
                showPreNStyleOperatorAvatar(FileUtils.convertRelativeUrlToAbsolute(messageContent.avatarPath), pushSmall, pushBig);
                showPreNStyleSmallIcon(pushSmall, pushBig);
            } else {
                final Bitmap icon = BitmapFactory.decodeResource(getResources(), style.defPushIconResId);
                pushSmall.setImageViewBitmap(R.id.icon_large, icon);
                pushBig.setImageViewBitmap(R.id.icon_large, icon);
                pushSmall.setImageViewBitmap(R.id.icon_small_corner, null);
                pushBig.setImageViewBitmap(R.id.icon_small_corner, null);
            }
            pushSmall.setTextViewText(R.id.consult_name, messageContent.consultName + ":");
            pushSmall.setTextViewText(R.id.text, messageContent.contentText.trim());

            pushBig.setTextViewText(R.id.consult_name, messageContent.consultName + ":");
            pushBig.setTextViewText(R.id.text, messageContent.contentText.trim());
            if (messageContent.hasPlainFiles) {
                pushSmall.setViewVisibility(R.id.attach_image, View.VISIBLE);
                pushBig.setViewVisibility(R.id.attach_image, View.VISIBLE);
                final Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.attach_file_grey_48x48);
                pushSmall.setImageViewBitmap(R.id.attach_image, b);
                pushBig.setImageViewBitmap(R.id.attach_image, b);
            } else if (messageContent.hasImage) {
                pushSmall.setViewVisibility(R.id.attach_image, View.VISIBLE);
                pushBig.setViewVisibility(R.id.attach_image, View.VISIBLE);
                final Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.insert_photo_grey_48x48);
                pushSmall.setImageViewBitmap(R.id.attach_image, b);
                pushBig.setImageViewBitmap(R.id.attach_image, b);
            } else {
                pushSmall.setViewVisibility(R.id.attach_image, View.GONE);
                pushBig.setViewVisibility(R.id.attach_image, View.GONE);
            }
            if (messageContent.isNeedAnswer) {
                builder.setCustomBigContentView(pushBig);
                pushBig.setOnClickPendingIntent(R.id.reply, QuickAnswerActivity.createPendingIntent(this));
            }
        }
        pushBig.setTextViewText(R.id.reply, getString(R.string.threads_reply));
        builder.setContent(pushSmall);
        final PendingIntent pend = getChatIntent(intent.getStringExtra(EXTRA_APP_MARKER));
        builder.setContentIntent(pend);
        builder.setAutoCancel(true);
        builder.setContentIntent(pend);
        final Notification notification = builder.build();
        try {
            final int smallIconViewId = getResources().getIdentifier("right_icon", "id", getPackageName());
            notification.contentView.setViewVisibility(smallIconViewId, View.INVISIBLE);
        } catch (final Exception e) {
            ThreadsLogger.e(TAG, "getPreNStyleNotification", e);
        }
        return notification;
    }

    private void showPreNStyleOperatorAvatar(final String operatorAvatarUrl, final RemoteViews pushSmall, final RemoteViews pushBig) {
        Picasso.with(this)
                .load(operatorAvatarUrl)
                .transform(new CircleTransformation())
                .into(new Target() {
                          @Override
                          public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
                              pushSmall.setImageViewBitmap(R.id.icon_large, bitmap);
                              pushBig.setImageViewBitmap(R.id.icon_large, bitmap);
                          }

                          @Override
                          public void onBitmapFailed(final Drawable errorDrawable) {
                              final Bitmap big = BitmapFactory.decodeResource(getResources(), R.drawable.threads_operator_avatar_placeholder);
                              pushSmall.setImageViewBitmap(R.id.icon_large, big);
                              pushBig.setImageViewBitmap(R.id.icon_large, big);
                          }

                          @Override
                          public void onPrepareLoad(final Drawable placeHolderDrawable) {
                          }
                      }
                );
    }

    private void showPreNStyleSmallIcon(final RemoteViews pushSmall, final RemoteViews pushBig) {
        Picasso.with(this)
                .load(style.defPushIconResId)
                .transform(new CircleTransformation())
                .into(new Target() {
                          @Override
                          public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {//round icon in corner
                              pushSmall.setImageViewBitmap(R.id.icon_small_corner, bitmap);
                              pushBig.setImageViewBitmap(R.id.icon_small_corner, bitmap);
                          }

                          @Override
                          public void onBitmapFailed(final Drawable errorDrawable) {
                              final Bitmap big = BitmapFactory.decodeResource(getResources(), R.drawable.threads_operator_avatar_placeholder);
                              pushSmall.setImageViewBitmap(R.id.icon_small_corner, big);
                              pushBig.setImageViewBitmap(R.id.icon_small_corner, big);
                          }

                          @Override
                          public void onPrepareLoad(final Drawable placeHolderDrawable) {
                          }
                      }
                );
    }

    @TargetApi(Build.VERSION_CODES.N)
    private void getNStyleNotification(final Intent intent, @Nullable final MessageFormatter.MessageContent messageContent, final Consumer<Notification> completionHandler, @Nullable final String message) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        final String appMarker = intent.getStringExtra(EXTRA_APP_MARKER);
        builder.setShowWhen(true);
        builder.setColor(getColor(style.nougatPushAccentColorResId));
        final boolean unreadMessage = !TextUtils.isEmpty(message);
        if (unreadMessage) {
            builder.setContentText(message);
            builder.setSmallIcon(style.defPushIconResId);
            final String operatorUrl = intent.getStringExtra(EXTRA_OPERATOR_URL);
            if (!TextUtils.isEmpty(operatorUrl)) {
                final TargetNoError avatarTarget = new TargetNoError() {
                    @Override
                    public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
                        builder.setLargeIcon(bitmap);
                    }
                };
                final String avatarPath = FileUtils.convertRelativeUrlToAbsolute(operatorUrl);
                Picasso
                        .with(this)
                        .load(avatarPath)
                        .transform(new CircleTransformation())
                        .into(avatarTarget);
            }
            executor.execute(() -> {
                builder.setContentIntent(getChatIntent(appMarker));
                builder.addAction(0, getString(R.string.threads_answer), QuickAnswerActivity.createPendingIntent(this));
                completionHandler.accept(builder.build());

            });
        } else if (messageContent != null) {
            builder.setContentTitle(messageContent.titleText);
            if (messageContent.hasImage || messageContent.hasPlainFiles || messageContent.phrasesCount <= 1) {
                builder.setContentText(messageContent.contentText);
            }
            if (messageContent.hasAvatar) {
                final TargetNoError avatarTarget = new TargetNoError() {
                    @Override
                    public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
                        builder.setLargeIcon(bitmap);
                    }
                };
                final String avatarPath = FileUtils.convertRelativeUrlToAbsolute(messageContent.avatarPath);
                Picasso
                        .with(this)
                        .load(avatarPath)
                        .transform(new CircleTransformation())
                        .into(avatarTarget);
            }
            if (!messageContent.hasImage && !messageContent.hasPlainFiles) {
                builder.setSmallIcon(style.defPushIconResId);
                executor.execute(() -> {
                    builder.setContentIntent(getChatIntent(appMarker));
                    if (messageContent.isNeedAnswer) {
                        builder.addAction(0, getString(R.string.threads_answer), QuickAnswerActivity.createPendingIntent(this));
                    }
                    completionHandler.accept(builder.build());

                });
                return;
            }
            if (messageContent.hasImage && !messageContent.hasPlainFiles && messageContent.imagesCount == 1) {
                final NotificationCompat.BigPictureStyle pictureStyle = new androidx.core.app.NotificationCompat.BigPictureStyle();
                executor.execute(() -> {
                    try {
                        final URLConnection url = new URL(messageContent.lastImagePath).openConnection();
                        final Bitmap b = BitmapFactory.decodeStream(url.getInputStream());
                        pictureStyle.bigPicture(b);
                        builder.setSmallIcon(R.drawable.insert_photo_grey_48x48);
                        builder.setStyle(pictureStyle);
                        builder.setContentIntent(getChatIntent(appMarker));
                        if (messageContent.isNeedAnswer) {
                            builder.addAction(0, getString(R.string.threads_answer), QuickAnswerActivity.createPendingIntent(this));
                        }
                        completionHandler.accept(builder.build());
                    } catch (final IOException e) {
                        ThreadsLogger.e(TAG, "getNStyleNotification", e);
                    }
                });
                return;
            }
            if (messageContent.hasPlainFiles) {
                builder.setSmallIcon(R.drawable.attach_file_grey_48x48);
            } else {
                builder.setSmallIcon(R.drawable.insert_photo_grey_48x48);
            }
            executor.execute(() -> {
                builder.setContentIntent(getChatIntent(appMarker));
                if (messageContent.isNeedAnswer) {
                    builder.addAction(0, getString(R.string.threads_answer), QuickAnswerActivity.createPendingIntent(this));
                }
                completionHandler.accept(builder.build());
            });
        }
    }

    private PendingIntent getChatIntent(String appMarker) {
        return Config.instance.pendingIntentCreator.create(this, appMarker);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(NotificationService.BROADCAST_ALL_MESSAGES_WERE_READ)) {
                final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if (nm != null) {
                    nm.cancel(UNREAD_MESSAGE_PUSH_ID);
                }
            }
        }
    }
}
