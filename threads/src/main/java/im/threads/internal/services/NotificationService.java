package im.threads.internal.services;

import android.app.Notification;
import android.app.NotificationChannel;
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
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.mfms.android.push_lite.repo.push.remote.model.PushMessage;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.ThreadsLib;
import im.threads.internal.Config;
import im.threads.internal.activities.TranslucentActivity;
import im.threads.internal.database.DatabaseHolder;
import im.threads.internal.formatters.IncomingMessageParser;
import im.threads.internal.formatters.MarshmallowPushMessageFormatter;
import im.threads.internal.formatters.NougatMessageFormatter;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.CompletionHandler;
import im.threads.internal.model.ConsultChatPhrase;
import im.threads.internal.picasso_url_connection_only.Picasso;
import im.threads.internal.picasso_url_connection_only.Target;
import im.threads.internal.utils.CircleTransformation;
import im.threads.internal.utils.FileUtils;
import im.threads.internal.utils.TargetNoError;
import im.threads.internal.utils.ThreadsLogger;
import im.threads.internal.utils.Tuple;
import im.threads.view.ChatFragment;

import static android.text.TextUtils.isEmpty;

/**
 * Отображает пуш уведомление, о котором скачана полная информация.
 */
public class NotificationService extends Service {

    private static final String TAG = "NotificationService";
    private static final String CHANNEL_ID = "im.threads.internal.services.NotificationService.CHANNEL_ID";

    public static final String ACTION_ALL_MESSAGES_WERE_READ = "com.sequenia.threads.services.ThreadsPushServerIntentService.ACTION_MESSAGES_WERE_READ";
    public static final String ACTION_ADD_UNREAD_MESSAGE = "com.sequenia.threads.services.ThreadsPushServerIntentService.ACTION_ADD_UNREAD_MESSAGE";
    public static final String ACTION_ADD_UNSENT_MESSAGE = "com.sequenia.threads.services.ThreadsPushServerIntentService.ACTION_ADD_UNSENT_MESSAGE";
    public static final String ACTION_REMOVE_NOTIFICATION = "com.sequenia.threads.services.ThreadsPushServerIntentService.ACTION_REMOVE_NOTIFICATION";
    public static final String ACTION_ADD_UNREAD_MESSAGE_TEXT = "com.sequenia.threads.services.ThreadsPushServerIntentService.ACTION_ADD_UNREAD_MESSAGE_TEXT";
    public static final String EXTRA_OPERATOR_URL = "com.sequenia.threads.services.ThreadsPushServerIntentService.EXTRA_OPERATOR_URL";
    public static final String EXTRA_APP_MARKER = "appMarker";

    private static final int UNREAD_MESSAGE_PUSH_ID = 0;
    private static final int UNSENT_MESSAGE_PUSH_ID = 1;

    private ArrayList<ChatItem> unreadMessages = new ArrayList<>();
    private MyBroadcastReceiver mBroadcastReceiver;
    Handler h = new Handler(Looper.getMainLooper());
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private ChatStyle style;

    public NotificationService() {
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
                    new IntentFilter(NotificationService.ACTION_ALL_MESSAGES_WERE_READ));
        }
        if (intent == null) {
            return START_STICKY;
        }
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Threads Channel", NotificationManager.IMPORTANCE_DEFAULT);
            nm.createNotificationChannel(channel);
        }
        final ArrayList<PushMessage> il =
                intent.getParcelableArrayListExtra(ACTION_ADD_UNREAD_MESSAGE);
        if (intent.getAction() != null && intent.getAction().equals(ACTION_REMOVE_NOTIFICATION)) {
            dismissUnreadMessagesNotification();
        } else if (intent.getAction() != null && intent.getAction().equals(ACTION_ADD_UNREAD_MESSAGE_TEXT)) {
            final String message = intent.getStringExtra(ACTION_ADD_UNREAD_MESSAGE_TEXT);
            if (Build.VERSION.SDK_INT < 24) {
                final Notification notification = getMstyleNotif(intent, null, message);
                notifyUnreadMessagesCountChanged(nm, notification);
            } else {
                getNstyleNotif(intent, null, new CompletionHandler<Notification>() {
                    @Override
                    public void onComplete(final Notification notification) {
                        notifyUnreadMessagesCountChanged(nm, notification);
                    }

                    @Override
                    public void onError(final Throwable e, final String message, final Notification data) {

                    }
                }, message);
            }
        } else if (il != null) {
            final List<ChatItem> items = IncomingMessageParser.formatMessages(il);
            if (Build.VERSION.SDK_INT < 24) {
                final Notification notification = getMstyleNotif(intent, items, null);
                notifyUnreadMessagesCountChanged(nm, notification);
            } else {
                getNstyleNotif(intent, items, new CompletionHandler<Notification>() {
                    @Override
                    public void onComplete(final Notification notification) {
                        notifyUnreadMessagesCountChanged(nm, notification);
                    }

                    @Override
                    public void onError(final Throwable e, final String message, final Notification data) {

                    }
                }, null);
            }
        } else if (intent.getAction() != null && intent.getAction().equals(ACTION_ADD_UNSENT_MESSAGE)) {
            final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
            notificationBuilder.setContentTitle(getString(R.string.threads_message_were_unsent));
            final PendingIntent pend = getChatIntent(intent.getStringExtra(EXTRA_APP_MARKER));
            final int iconResId = style.defPushIconResId;
            notificationBuilder.setSmallIcon(iconResId);
            notificationBuilder.setContentIntent(pend);
            notificationBuilder.setAutoCancel(true);
            h.postDelayed(() -> nm.notify(UNSENT_MESSAGE_PUSH_ID, notificationBuilder.build()), 1500);
        }
        return START_STICKY;
    }

    private boolean needsShowNotification() {
        return !ChatFragment.isShown();
    }

    private void notifyUnreadMessagesCountChanged(final NotificationManager nm, final Notification notification) {
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

    private void dismissUnreadMessagesNotification() {
        unreadMessages.clear();
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(UNREAD_MESSAGE_PUSH_ID);
    }

    Notification getMstyleNotif(final Intent intent, final List<ChatItem> items, final String message) {
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
            if (!isEmpty(operatorUrl)) {
                showMstyleOperatorAvatar(FileUtils.convertRelativeUrlToAbsolute(operatorUrl), pushSmall, pushBig);
                showMstyleSmallIcon(pushSmall, pushBig);
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
        } else {
            final Tuple<Boolean, MarshmallowPushMessageFormatter.PushContents> pushText = new
                    MarshmallowPushMessageFormatter(this, unreadMessages, items)
                    .getFormattedMessageAsPushContents();
            String avatarPath = null;
            for (int i = unreadMessages.size() - 1; i >= 0; i--) {
                if (unreadMessages.get(i) instanceof ConsultChatPhrase) {
                    avatarPath = ((ConsultChatPhrase) unreadMessages.get(i)).getAvatarPath();
                    break;
                }
            }
            if (!isEmpty(avatarPath)) {
                showMstyleOperatorAvatar(FileUtils.convertRelativeUrlToAbsolute(avatarPath), pushSmall, pushBig);
                showMstyleSmallIcon(pushSmall, pushBig);
            } else {
                final Bitmap icon = BitmapFactory.decodeResource(getResources(), style.defPushIconResId);
                pushSmall.setImageViewBitmap(R.id.icon_large, icon);
                pushBig.setImageViewBitmap(R.id.icon_large, icon);
                pushSmall.setImageViewBitmap(R.id.icon_small_corner, null);
                pushBig.setImageViewBitmap(R.id.icon_small_corner, null);
            }
            pushSmall.setTextViewText(R.id.consult_name, pushText.second.consultName + ":");
            pushSmall.setTextViewText(R.id.text, pushText.second.contentDescription.trim());

            pushBig.setTextViewText(R.id.consult_name, pushText.second.consultName + ":");
            pushBig.setTextViewText(R.id.text, pushText.second.contentDescription.trim());

            if (pushText.second.isOnlyImages) {
                pushSmall.setViewVisibility(R.id.attach_image, View.VISIBLE);
                pushBig.setViewVisibility(R.id.attach_image, View.VISIBLE);
                final Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.insert_photo_grey_48x48);
                pushSmall.setImageViewBitmap(R.id.attach_image, b);
                pushBig.setImageViewBitmap(R.id.attach_image, b);
            } else if (pushText.second.isWithAttachments) {
                pushSmall.setViewVisibility(R.id.attach_image, View.VISIBLE);
                pushBig.setViewVisibility(R.id.attach_image, View.VISIBLE);
                final Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.attach_file_grey_48x48);
                pushSmall.setImageViewBitmap(R.id.attach_image, b);
                pushBig.setImageViewBitmap(R.id.attach_image, b);
            } else {
                pushSmall.setViewVisibility(R.id.attach_image, View.GONE);
                pushBig.setViewVisibility(R.id.attach_image, View.GONE);
            }
            if (pushText.first) {
                builder.setCustomBigContentView(pushBig);
                final PendingIntent buttonPend = getFastAnswerIntent();
                pushBig.setOnClickPendingIntent(R.id.reply, buttonPend);
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
            final int smallIconViewId = getResources().getIdentifier("right_icon", "id", android.R.class.getPackage().getName());
            notification.contentView.setViewVisibility(smallIconViewId, View.INVISIBLE);
        } catch (final Exception e) {
            ThreadsLogger.e(TAG, "getMstyleNotif", e);
        }
        return notification;
    }

    private void showMstyleOperatorAvatar(final String operatorAvatarUrl, final RemoteViews pushSmall, final RemoteViews pushBig) {
        final Target target = new Target() {
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
        };
        Picasso.with(this)
                .load(operatorAvatarUrl)
                .transform(new CircleTransformation())
                .into(target);
    }

    private void showMstyleSmallIcon(final RemoteViews pushSmall, final RemoteViews pushBig) {
        final Target smallPicTarget = new Target() {
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
        };
        Picasso.with(this)
                .load(style.defPushIconResId)
                .transform(new CircleTransformation())
                .into(smallPicTarget);
    }


    void getNstyleNotif(final Intent intent, final List<ChatItem> items, final CompletionHandler<Notification> completionHandler, final String message) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        final String appMarker = intent.getStringExtra(EXTRA_APP_MARKER);
        builder.setShowWhen(true);
        if (Build.VERSION.SDK_INT > 23) {
            builder.setColor(getColor(style.nougatPushAccentColorResId));
        }
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
                builder.addAction(0, getString(R.string.threads_answer), getFastAnswerIntent());
                completionHandler.onComplete(builder.build());

            });
        } else {
            final Tuple<Boolean, NougatMessageFormatter.PushContents> out
                    = new NougatMessageFormatter(this, unreadMessages, items).getFormattedMessageAsPushContents();
            final NougatMessageFormatter.PushContents pushContents = out.second;
            builder.setContentTitle(pushContents.titleText);
            if (pushContents.hasImage || pushContents.hasPlainFiles || pushContents.phrasesCount <= 1) {
                builder.setContentText(pushContents.contentText);
            }
            if (pushContents.hasAvatar) {
                final TargetNoError avatarTarget = new TargetNoError() {
                    @Override
                    public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
                        builder.setLargeIcon(bitmap);
                    }
                };
                final String avatarPath = FileUtils.convertRelativeUrlToAbsolute(pushContents.avatarPath);
                Picasso
                        .with(this)
                        .load(avatarPath)
                        .transform(new CircleTransformation())
                        .into(avatarTarget);
            }
            if (!pushContents.hasImage && !pushContents.hasPlainFiles) {
                builder.setSmallIcon(style.defPushIconResId);
                executor.execute(() -> {
                    builder.setContentIntent(getChatIntent(appMarker));
                    if (out.first) {
                        builder.addAction(0, getString(R.string.threads_answer), getFastAnswerIntent());
                    }
                    completionHandler.onComplete(builder.build());

                });
                return;
            }
            if (pushContents.hasImage
                    && !pushContents.hasPlainFiles
                    && pushContents.imagesCount == 1) {
                final NotificationCompat.BigPictureStyle pictureStyle
                        = new android.support.v4.app.NotificationCompat.BigPictureStyle();
                executor.execute(() -> {
                    try {
                        final URLConnection url = new URL(pushContents.lastImagePath).openConnection();
                        final Bitmap b = BitmapFactory.decodeStream(url.getInputStream());
                        pictureStyle.bigPicture(b);
                        builder.setSmallIcon(R.drawable.insert_photo_grey_48x48);
                        builder.setStyle(pictureStyle);
                        builder.setContentIntent(getChatIntent(appMarker));
                        if (out.first) {
                            builder.addAction(0, getString(R.string.threads_answer), getFastAnswerIntent());
                        }
                        completionHandler.onComplete(builder.build());
                    } catch (final IOException e) {
                        ThreadsLogger.e(TAG, "getNstyleNotif", e);
                    }
                });
                return;
            }
            if (pushContents.hasPlainFiles) {
                builder.setSmallIcon(R.drawable.attach_file_grey_48x48);
            } else {
                builder.setSmallIcon(R.drawable.insert_photo_grey_48x48);
            }
            executor.execute(() -> {
                builder.setContentIntent(getChatIntent(appMarker));
                if (out.first) {
                    builder.addAction(0, getString(R.string.threads_answer), getFastAnswerIntent());
                }
                completionHandler.onComplete(builder.build());
            });
        }
    }

    private PendingIntent getFastAnswerIntent() {
        final Intent buttonIntent = new Intent(this, TranslucentActivity.class);
        buttonIntent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(
                this,
                1
                , buttonIntent
                , PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent getChatIntent(String appMarker) {
        return Config.instance.pendingIntentCreator.create(this, appMarker);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(NotificationService.ACTION_ALL_MESSAGES_WERE_READ)) {
                dismissUnreadMessagesNotification();
            }
        }
    }
}
