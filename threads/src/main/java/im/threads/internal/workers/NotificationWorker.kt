package im.threads.internal.workers

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.annotation.RestrictTo
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.util.Consumer
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import im.threads.ChatStyle
import im.threads.R
import im.threads.internal.Config
import im.threads.internal.activities.QuickAnswerActivity
import im.threads.internal.controllers.UnreadMessagesController
import im.threads.internal.formatters.MessageFormatter
import im.threads.internal.imageLoading.ImageLoader
import im.threads.internal.imageLoading.ImageModifications
import im.threads.internal.utils.FileUtils.convertRelativeUrlToAbsolute
import im.threads.internal.utils.ThreadsLogger
import im.threads.internal.utils.WorkerUtils
import im.threads.internal.utils.WorkerUtils.unmarshall
import im.threads.view.ChatFragment
import java.io.IOException
import java.util.Date
import java.util.concurrent.Executors

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
open class NotificationWorker(private val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {

    private val executor = Executors.newSingleThreadExecutor()

    private var notificationChannel: NotificationChannel? = null
    private var style: ChatStyle = Config.instance.chatStyle

    override fun doWork(): Result {
        ThreadsLogger.i(TAG, "doWork")

        val systemService = context.getSystemService(Context.NOTIFICATION_SERVICE)
        val notificationManager: NotificationManager
        if (systemService is NotificationManager) {
            notificationManager = systemService
        } else {
            return Result.failure()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context)
        }

        val action: String? = inputData.getString(NOTIFICATION_ACTION)

        when (action) {
            ACTION_REMOVE_NOTIFICATION -> {
                notificationManager.cancel(UNREAD_MESSAGE_GROUP_PUSH_ID)
                notificationManager.cancel(CAMPAIGN_MESSAGE_PUSH_ID)
            }
            ACTION_ADD_UNREAD_MESSAGE -> {
                val notificationId: Int = inputData.getInt(EXTRA_NOTIFICATION_ID, 0)
                val message: String? = inputData.getString(EXTRA_MESSAGE)
                if (Build.VERSION.SDK_INT < 24) {
                    notifyUnreadMessagesCountChanged(
                        notificationManager,
                        getPreNStyleNotification(inputData, null, message),
                        notificationId
                    )
                } else {
                    getNStyleNotification(
                        inputData,
                        null,
                        { notification: Notification ->
                            notifyUnreadMessagesCountChanged(
                                notificationManager,
                                notification,
                                notificationId
                            )
                        },
                        message
                    )
                }
            }
            ACTION_ADD_UNREAD_MESSAGE_LIST -> {
                val data = inputData.getByteArray(EXTRA_MESSAGE_CONTENT)?.let { unmarshall(it) }
                val messageContent: MessageFormatter.MessageContent =
                    MessageFormatter.MessageContent.CREATOR.createFromParcel(data)

                if (Build.VERSION.SDK_INT < 24) {
                    val notification = getPreNStyleNotification(inputData, messageContent, null)
                    notifyUnreadMessagesCountChanged(
                        notificationManager,
                        notification,
                        Date().hashCode()
                    )
                } else {
                    getNStyleNotification(
                        inputData,
                        messageContent,
                        { notification: Notification ->
                            notifyUnreadMessagesCountChanged(
                                notificationManager,
                                notification,
                                Date().hashCode()
                            )
                        },
                        null
                    )
                }
            }
            ACTION_ADD_UNSENT_MESSAGE -> notifyAboutUnsent(
                notificationManager,
                inputData.getString(EXTRA_APP_MARKER)
            )
            ACTION_ADD_CAMPAIGN_MESSAGE -> notifyAboutCampaign(
                notificationManager,
                inputData.getString(EXTRA_CAMPAIGN_MESSAGE)
            )
        }
        return Result.success()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private final fun createNotificationChannel(context: Context) {
        if (notificationChannel == null) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
            if (notificationManager != null) {
                notificationChannel =
                    notificationManager.getNotificationChannel(CHANNEL_ID)
                if (notificationChannel == null) {
                    notificationChannel = NotificationChannel(
                        CHANNEL_ID,
                        context.getString(R.string.threads_channel_name),
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    notificationChannel?.let {
                        notificationManager.createNotificationChannel(it)
                    }
                }
            }
        }
    }

    private fun notifyUnreadMessagesCountChanged(
        notificationManager: NotificationManager,
        notification: Notification,
        notificationId: Int
    ) {
        notification.defaults = notification.defaults or Notification.DEFAULT_SOUND
        notification.defaults = notification.defaults or Notification.DEFAULT_VIBRATE
        if (needsShowNotification()) {
            var fixPushCrash = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notification.smallIcon == null) {
                fixPushCrash = true
            }
            if (!fixPushCrash) {
                notificationManager.notify(
                    UNREAD_MESSAGE_GROUP_PUSH_ID,
                    NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(notification.icon)
                        .setColor(NotificationCompat.getColor(notification))
                        .setContentIntent(notification.contentIntent)
                        .setAutoCancel(true)
                        .setGroup(GROUP_KEY_PUSH)
                        .setGroupSummary(true)
                        .build()
                )
                notificationManager.notify(notificationId, notification)
            }
            UnreadMessagesController.INSTANCE.incrementUnreadPush()
        }
    }

    private fun getPreNStyleNotification(
        inputData: Data,
        messageContent: MessageFormatter.MessageContent?,
        message: String?
    ): Notification {
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, CHANNEL_ID)
        val pushSmall = RemoteViews(context.packageName, R.layout.remote_push_small)
        val pushBig = RemoteViews(context.packageName, R.layout.remote_push_expanded)
        builder.setContentTitle(context.getString(style.defTitleResId))
        builder.setGroup(GROUP_KEY_PUSH)
        pushSmall.setTextViewText(R.id.title, context.getString(style.defTitleResId))
        pushBig.setTextViewText(R.id.title, context.getString(style.defTitleResId))
        pushSmall.setImageViewResource(R.id.icon_large_bg, R.drawable.ic_circle_40dp)
        pushBig.setImageViewResource(R.id.icon_large_bg, R.drawable.ic_circle_40dp)
        builder.color = context.resources.getColor(style.pushBackgroundColorResId)
        pushSmall.setInt(
            R.id.icon_large_bg,
            "setColorFilter",
            context.resources.getColor(style.pushBackgroundColorResId)
        )
        pushBig.setInt(
            R.id.icon_large_bg,
            "setColorFilter",
            context.resources.getColor(style.pushBackgroundColorResId)
        )
        pushSmall.setInt(
            R.id.text,
            "setTextColor",
            context.resources.getColor(style.incomingMessageTextColor)
        )
        pushBig.setInt(
            R.id.text,
            "setTextColor",
            context.resources.getColor(style.incomingMessageTextColor)
        )
        builder.setSmallIcon(style.defPushIconResId)
        val unreadMessage = !message.isNullOrEmpty()
        if (unreadMessage) {
            val operatorUrl = inputData.getString(EXTRA_OPERATOR_URL)
            if (!operatorUrl.isNullOrEmpty()) {
                showPreNStyleOperatorAvatar(
                    convertRelativeUrlToAbsolute(operatorUrl),
                    pushSmall,
                    pushBig
                )
                showPreNStyleSmallIcon(pushSmall, pushBig)
            } else {
                val icon =
                    BitmapFactory.decodeResource(context.resources, style.defPushIconResId)
                pushSmall.setImageViewBitmap(R.id.icon_large, icon)
                pushBig.setImageViewBitmap(R.id.icon_large, icon)
                pushSmall.setImageViewBitmap(R.id.icon_small_corner, null)
                pushBig.setImageViewBitmap(R.id.icon_small_corner, null)
                pushSmall.setViewVisibility(R.id.consult_name, View.GONE)
                pushBig.setViewVisibility(R.id.consult_name, View.GONE)
                pushSmall.setViewVisibility(R.id.attach_image, View.GONE)
                pushBig.setViewVisibility(R.id.attach_image, View.GONE)
            }
            pushSmall.setTextViewText(R.id.text, message)
            pushBig.setTextViewText(R.id.text, message)
        } else if (messageContent != null) {
            if (!messageContent.avatarPath.isNullOrEmpty()) {
                showPreNStyleOperatorAvatar(
                    convertRelativeUrlToAbsolute(messageContent.avatarPath),
                    pushSmall,
                    pushBig
                )
                showPreNStyleSmallIcon(pushSmall, pushBig)
            } else {
                val icon =
                    BitmapFactory.decodeResource(context.resources, style.defPushIconResId)
                pushSmall.setImageViewBitmap(R.id.icon_large, icon)
                pushBig.setImageViewBitmap(R.id.icon_large, icon)
                pushSmall.setImageViewBitmap(R.id.icon_small_corner, null)
                pushBig.setImageViewBitmap(R.id.icon_small_corner, null)
            }
            pushSmall.setTextViewText(R.id.consult_name, messageContent.consultName + ":")
            pushSmall.setTextViewText(R.id.text, messageContent.contentText.trim { it <= ' ' })
            pushBig.setTextViewText(R.id.consult_name, messageContent.consultName + ":")
            pushBig.setTextViewText(R.id.text, messageContent.contentText.trim { it <= ' ' })
            if (messageContent.hasPlainFiles) {
                pushSmall.setViewVisibility(R.id.attach_image, View.VISIBLE)
                pushBig.setViewVisibility(R.id.attach_image, View.VISIBLE)
                val b =
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.attach_file_grey_48x48
                    )
                pushSmall.setImageViewBitmap(R.id.attach_image, b)
                pushBig.setImageViewBitmap(R.id.attach_image, b)
            } else if (messageContent.hasImage) {
                pushSmall.setViewVisibility(R.id.attach_image, View.VISIBLE)
                pushBig.setViewVisibility(R.id.attach_image, View.VISIBLE)
                val b =
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.insert_photo_grey_48x48
                    )
                pushSmall.setImageViewBitmap(R.id.attach_image, b)
                pushBig.setImageViewBitmap(R.id.attach_image, b)
            } else {
                pushSmall.setViewVisibility(R.id.attach_image, View.GONE)
                pushBig.setViewVisibility(R.id.attach_image, View.GONE)
            }
            if (messageContent.isNeedAnswer) {
                builder.setCustomBigContentView(pushBig)
                pushBig.setOnClickPendingIntent(
                    R.id.reply,
                    QuickAnswerActivity.createPendingIntent(context)
                )
            }
        }
        pushBig.setTextViewText(R.id.reply, context.getString(R.string.threads_reply))
        builder.setContent(pushSmall)
        val pend = getChatIntent(inputData.getString(EXTRA_APP_MARKER))
        builder.setContentIntent(pend)
        builder.setAutoCancel(true)
        builder.setContentIntent(pend)
        val notification = builder.build()
        try {
            val smallIconViewId: Int =
                context.resources.getIdentifier("right_icon", "id", context.packageName)
            notification.contentView.setViewVisibility(smallIconViewId, View.INVISIBLE)
        } catch (e: Exception) {
            ThreadsLogger.e(TAG, "getPreNStyleNotification", e)
        }
        return notification
    }

    private fun showPreNStyleOperatorAvatar(
        operatorAvatarUrl: String?,
        pushSmall: RemoteViews,
        pushBig: RemoteViews
    ) {
        ImageLoader
            .get()
            .load(operatorAvatarUrl)
            .modifications(ImageModifications.CircleCropModification)
            .callback(object : ImageLoader.ImageLoaderCallback {
                override fun onImageLoaded(bitmap: Bitmap) {
                    onImageLoaded(
                        bitmap.toDrawable(context.resources),
                        pushSmall,
                        pushBig,
                        R.id.icon_large
                    )
                }

                override fun onImageLoadError() {
                    onImageLoadError(pushSmall, pushBig, R.id.icon_large)
                }
            })
            .getBitmap(context)
    }

    private fun showPreNStyleSmallIcon(pushSmall: RemoteViews, pushBig: RemoteViews) {
        ImageLoader
            .get()
            .errorDrawableResourceId(style.defPushIconResId)
            .modifications(ImageModifications.CircleCropModification)
            .getBitmapSync(context)?.let {
                pushSmall.setImageViewBitmap(R.id.icon_small_corner, it)
                pushBig.setImageViewBitmap(R.id.icon_small_corner, it)
            }
    }

    private fun onImageLoaded(
        drawable: Drawable,
        pushSmall: RemoteViews,
        pushBig: RemoteViews,
        pushContainerResId: Int
    ) {
        val bitmap = (drawable as BitmapDrawable).bitmap
        pushSmall.setImageViewBitmap(pushContainerResId, bitmap)
        pushBig.setImageViewBitmap(pushContainerResId, bitmap)
    }

    private fun onImageLoadError(
        pushSmall: RemoteViews,
        pushBig: RemoteViews,
        pushContainerResId: Int
    ) {
        val big = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.threads_operator_avatar_placeholder
        )
        pushSmall.setImageViewBitmap(R.id.icon_large, big)
        pushBig.setImageViewBitmap(R.id.icon_large, big)
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun getNStyleNotification(
        inputData: Data,
        messageContent: MessageFormatter.MessageContent?,
        completionHandler: Consumer<Notification>,
        message: String?
    ) {
        var avatarPath: String? = null
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, CHANNEL_ID)
        val appMarker = inputData.getString(EXTRA_APP_MARKER)
        builder.setContentIntent(getChatIntent(appMarker))
        builder.setShowWhen(true)
        builder.setGroup(GROUP_KEY_PUSH)
        builder.color = context.getColor(style.nougatPushAccentColorResId)

        val unreadMessage = !message.isNullOrEmpty()
        if (unreadMessage) {
            avatarPath =
                convertRelativeUrlToAbsolute(inputData.getString(EXTRA_OPERATOR_URL))
            builder.setContentText(message)
            builder.setSmallIcon(style.defPushIconResId)
            builder.addAction(
                0,
                context.getString(R.string.threads_answer),
                QuickAnswerActivity.createPendingIntent(context)
            )
        } else if (messageContent != null) {
            avatarPath = convertRelativeUrlToAbsolute(messageContent.avatarPath)
            builder.setContentTitle(messageContent.titleText)
            if (messageContent.hasImage || messageContent.hasPlainFiles || messageContent.phrasesCount <= 1) {
                builder.setContentText(messageContent.contentText)
            }
            if (messageContent.isNeedAnswer) {
                builder.addAction(
                    0,
                    context.getString(R.string.threads_answer),
                    QuickAnswerActivity.createPendingIntent(context)
                )
            }
            if (!messageContent.hasImage && !messageContent.hasPlainFiles) {
                builder.setSmallIcon(style.defPushIconResId)
            } else if (messageContent.hasPlainFiles) {
                builder.setSmallIcon(R.drawable.attach_file_grey_48x48)
            } else {
                builder.setSmallIcon(R.drawable.insert_photo_grey_48x48)
            }
        }
        val finalAvatarPath = avatarPath
        executor.execute {
            if (messageContent != null) {
                if (messageContent.hasImage && !messageContent.hasPlainFiles && messageContent.imagesCount == 1) {
                    val pictureStyle =
                        NotificationCompat.BigPictureStyle()
                    pictureStyle.bigPicture(getBitmapFromUrl(messageContent.lastImagePath))
                    builder.setStyle(pictureStyle)
                }
            }
            builder.setLargeIcon(getBitmapFromUrl(finalAvatarPath))
            completionHandler.accept(builder.build())
        }
    }

    private fun getBitmapFromUrl(url: String?): Bitmap? {
        return if (url.isNullOrEmpty()) null else try {
            ImageLoader
                .get()
                .load(url)
                .modifications(ImageModifications.CircleCropModification)
                .getBitmapSync(context)
        } catch (e: IOException) {
            ThreadsLogger.e(TAG, "getBitmapFromUrl", e)
            null
        }
    }

    private fun notifyAboutUnsent(notificationManager: NotificationManager, appMarker: String?) {
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, CHANNEL_ID)
        notificationBuilder.setContentTitle(context.getString(R.string.threads_message_were_unsent))
        val pend = getChatIntent(appMarker)
        val iconResId = style.defPushIconResId
        notificationBuilder.setSmallIcon(iconResId)
        notificationBuilder.setContentIntent(pend)
        notificationBuilder.setAutoCancel(true)
        notificationManager.notify(UNSENT_MESSAGE_PUSH_ID, notificationBuilder.build())
    }

    private fun notifyAboutCampaign(notificationManager: NotificationManager, campaign: String?) {
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, CHANNEL_ID)
        notificationBuilder.setContentText(campaign)
        val pend = getChatIntent(null)
        val iconResId = style.defPushIconResId
        notificationBuilder.setSmallIcon(iconResId)
        notificationBuilder.setContentIntent(pend)
        notificationBuilder.setAutoCancel(true)
        notificationManager.notify(CAMPAIGN_MESSAGE_PUSH_ID, notificationBuilder.build())
    }

    private fun getChatIntent(appMarker: String?): PendingIntent? =
        Config.instance.pendingIntentCreator.create(context, appMarker)

    private fun needsShowNotification(): Boolean = !ChatFragment.isShown()

    companion object {

        const val GROUP_KEY_PUSH =
            "im.threads.internal.workers.NotificationWorker.UNREAD_MESSAGE_GROUP"

        const val TAG = "NotificationWorker"
        private const val WORKER_NAME = "im.threads.internal.workers.NotificationWorker"
        private const val NOTIFICATION_ACTION =
            "im.threads.internal.workers.NotificationWorker.Action"
        private const val CHANNEL_ID = "im.threads.internal.workers.NotificationWorker.CHANNEL_ID"

        const val EXTRA_NOTIFICATION_ID =
            "im.threads.internal.workers.NotificationWorker.EXTRA_NOTIFICATION_ID"
        const val EXTRA_MESSAGE = "im.threads.internal.workers.NotificationWorker.EXTRA_MESSAGE"
        const val EXTRA_OPERATOR_URL =
            "im.threads.internal.workers.NotificationWorker.EXTRA_OPERATOR_URL"
        const val EXTRA_APP_MARKER =
            "im.threads.internal.workers.NotificationWorker.EXTRA_APP_MARKER"
        const val EXTRA_MESSAGE_CONTENT =
            "im.threads.internal.workers.NotificationWorker.EXTRA_MESSAGE_CONTENT"
        const val EXTRA_CAMPAIGN_MESSAGE =
            "im.threads.internal.workers.NotificationWorker.EXTRA_CAMPAIGN_MESSAGE"
        private const val ACTION_REMOVE_NOTIFICATION =
            "im.threads.internal.workers.NotificationWorker.ACTION_REMOVE_NOTIFICATION"
        private const val ACTION_ADD_UNREAD_MESSAGE =
            "im.threads.internal.workers.NotificationWorker.ACTION_ADD_UNREAD_MESSAGE"
        private const val ACTION_ADD_UNREAD_MESSAGE_LIST =
            "im.threads.internal.workers.NotificationWorker.ACTION_ADD_UNREAD_MESSAGE_LIST"
        private const val ACTION_ADD_UNSENT_MESSAGE =
            "im.threads.internal.workers.NotificationWorker.ACTION_ADD_UNSENT_MESSAGE"
        private const val ACTION_ADD_CAMPAIGN_MESSAGE =
            "im.threads.internal.workers.NotificationWorker.ACTION_ADD_CAMPAIGN_MESSAGE"

        private const val UNREAD_MESSAGE_GROUP_PUSH_ID = 0
        private const val UNSENT_MESSAGE_PUSH_ID = 1
        private const val CAMPAIGN_MESSAGE_PUSH_ID = 2

        @JvmStatic
        fun removeNotification(context: Context) {
            val inputData = Data.Builder()
                .putString(NOTIFICATION_ACTION, ACTION_REMOVE_NOTIFICATION)
            startWorker(context, inputData)
        }

        @JvmStatic
        fun addUnreadMessage(
            context: Context,
            notificationId: Int,
            message: String?,
            operatorUrl: String?,
            appMarker: String?
        ) {
            val inputData = Data.Builder()
                .putString(NOTIFICATION_ACTION, ACTION_ADD_UNREAD_MESSAGE)
                .putString(EXTRA_APP_MARKER, appMarker)
                .putString(EXTRA_OPERATOR_URL, operatorUrl)
                .putString(EXTRA_MESSAGE, message)
                .putInt(EXTRA_NOTIFICATION_ID, notificationId)
            startWorker(context, inputData)
        }

        @JvmStatic
        fun addUnreadMessageList(
            context: Context,
            appMarker: String?,
            messageContent: MessageFormatter.MessageContent
        ) {
            val inputData = Data.Builder()
                .putString(NOTIFICATION_ACTION, ACTION_ADD_UNREAD_MESSAGE_LIST)
                .putString(EXTRA_APP_MARKER, appMarker)
                .putByteArray(EXTRA_MESSAGE_CONTENT, WorkerUtils.marshall(messageContent))
            startWorker(context, inputData)
        }

        @JvmStatic
        fun addUnsentMessage(context: Context, appMarker: String?) {
            val inputData = Data.Builder()
                .putString(NOTIFICATION_ACTION, ACTION_ADD_UNSENT_MESSAGE)
                .putString(EXTRA_APP_MARKER, appMarker)
            startWorker(context, inputData)
        }

        @JvmStatic
        fun addCampaignMessage(context: Context, campaign: String?) {
            val inputData = Data.Builder()
                .putString(NOTIFICATION_ACTION, ACTION_ADD_CAMPAIGN_MESSAGE)
                .putString(EXTRA_CAMPAIGN_MESSAGE, campaign)
            startWorker(context, inputData)
        }

        private fun startWorker(context: Context, inputData: Data.Builder) {
            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInputData(inputData.build())
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORKER_NAME, ExistingWorkPolicy.KEEP, workRequest)
        }
    }
}
