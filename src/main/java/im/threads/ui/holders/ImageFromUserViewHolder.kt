package im.threads.ui.holders

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.isVisible
import im.threads.R
import im.threads.business.imageLoading.ImageLoader
import im.threads.business.imageLoading.ImageModifications
import im.threads.business.models.ChatItem
import im.threads.business.models.FileDescription
import im.threads.business.models.MessageStatus
import im.threads.business.models.UserPhrase
import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.ogParser.OpenGraphParser
import im.threads.business.utils.FileUtils
import im.threads.ui.config.Config
import im.threads.ui.utils.ColorsHelper
import im.threads.ui.widget.textView.BubbleTimeTextView
import io.reactivex.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageFromUserViewHolder(
    parent: ViewGroup,
    private val maskedTransformation: ImageModifications.MaskedModification?,
    highlightingStream: PublishSubject<ChatItem>,
    openGraphParser: OpenGraphParser,
    private val messageErrorProcessor: PublishSubject<Long>
) : BaseImageHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.ecc_item_user_image_from, parent, false),
    highlightingStream,
    openGraphParser,
    false
) {
    private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var timeStamp: Long? = null

    private var loadedUri: String? = null

    private val loaderLayoutRoot: RelativeLayout = itemView.findViewById(R.id.loaderLayoutRoot)
    private val errorText: TextView = itemView.findViewById(R.id.errorText)
    private val fileName: TextView = itemView.findViewById(R.id.fileName)
    private val loader: ImageView = itemView.findViewById(R.id.loader)
    private val timeStampLoading: BubbleTimeTextView = itemView.findViewById(R.id.timeStampLoading)

    init {
        setTextColorToViews(
            arrayOf(fileName),
            style.outgoingMessageTextColor
        )
        loaderLayoutRoot.also { applyBubbleLayoutStyle(it) }
        ColorsHelper.setTextColor(errorText, style.errorMessageTextColor)
        itemView.findViewById<View>(R.id.delimiter).setBackgroundColor(getColorInt(style.outgoingMessageTextColor))
    }

    fun onBind(
        userPhrase: UserPhrase,
        highlighted: Boolean,
        clickRunnable: Runnable,
        longClickRunnable: Runnable
    ) {
        timeStamp = userPhrase.timeStamp
        showBubbleByCurrentStatus(userPhrase.fileDescription)
        subscribeForHighlighting(userPhrase, itemView)
        image.setOnClickListener { clickRunnable.run() }
        image.setOnLongClickListener {
            longClickRunnable.run()
            true
        }
        bindImage(userPhrase.fileDescription, userPhrase.sentState)
        bindIsChosen(highlighted, longClickRunnable)
        bindTimeStamp(userPhrase.sentState, userPhrase.timeStamp, userPhrase.fileDescription, longClickRunnable)
    }

    private fun bindIsChosen(isChosen: Boolean, longClickRunnable: Runnable) {
        rootLayout.setOnLongClickListener {
            longClickRunnable.run()
            true
        }
        changeHighlighting(isChosen)
    }

    private fun bindImage(
        fileDescription: FileDescription?,
        messageStatus: MessageStatus
    ) {
        fileDescription?.let {
            if ((it.state === AttachmentStateEnum.PENDING || messageStatus == MessageStatus.SENDING) &&
                statuses[timeStamp] == null || statuses[timeStamp] != MessageStatus.FAILED
            ) {
                showLoaderLayout(it)
            } else if (it.state === AttachmentStateEnum.ERROR || messageStatus == MessageStatus.FAILED ||
                (statuses[timeStamp] != null && statuses[timeStamp] == MessageStatus.FAILED)
            ) {
                showErrorLayout(it)
            } else {
                showCommonLayout(it)
            }
        }
    }

    private fun bindTimeStamp(
        messageStatus: MessageStatus,
        timeStamp: Long,
        fileDescription: FileDescription?,
        longClickRunnable: Runnable
    ) {
        timeStampTextView.setOnLongClickListener {
            longClickRunnable.run()
            true
        }
        val timeStampText = sdf.format(Date(timeStamp))
        timeStampTextView.text = timeStampText
        timeStampLoading.text = timeStampText

        val rightDrawable: Drawable? =
            when (messageStatus) {
                MessageStatus.SENDING -> {
                    val previousStatus = statuses[timeStamp]
                    if (previousStatus == null || previousStatus != MessageStatus.FAILED) {
                        showCommonLayout(fileDescription)
                        getColoredDrawable(
                            style.messageSendingIconResId,
                            style.messageSendingIconColorResId
                        )
                    } else {
                        getColoredDrawable(
                            style.messageFailedIconResId,
                            style.messageFailedIconColorResId
                        )
                    }
                }
                MessageStatus.SENT, MessageStatus.ENQUEUED -> {
                    showCommonLayout(fileDescription)
                    getColoredDrawable(
                        style.messageSentIconResId,
                        style.messageSentIconColorResId
                    )
                }
                MessageStatus.DELIVERED -> {
                    showCommonLayout(fileDescription)
                    getColoredDrawable(
                        style.messageDeliveredIconResId,
                        style.messageDeliveredIconColorResId
                    )
                }
                MessageStatus.READ -> {
                    showCommonLayout(fileDescription)
                    getColoredDrawable(
                        style.messageReadIconResId,
                        style.messageReadIconColorResId
                    )
                }
                MessageStatus.FAILED -> {
                    if (fileDescription != null) showErrorLayout(fileDescription)
                    scrollToErrorIfAppearsFirstTime()
                    getColoredDrawable(
                        style.messageFailedIconResId,
                        style.messageFailedIconColorResId
                    )
                }
            }
        timeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null)
        timeStampLoading.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null)
        statuses[timeStamp] = messageStatus
    }

    private fun getColoredDrawable(@DrawableRes res: Int, @ColorRes color: Int): Drawable? {
        val drawable = AppCompatResources.getDrawable(itemView.context, res)
        drawable?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            ContextCompat.getColor(itemView.context, color),
            BlendModeCompat.SRC_ATOP
        )
        return drawable
    }

    private fun applyBubbleLayoutStyle(layout: ViewGroup) {
        layout.background = AppCompatResources.getDrawable(
            itemView.context,
            style.outgoingMessageBubbleBackground
        )
        layout.background.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                getColorInt(style.outgoingMessageBubbleColor),
                BlendModeCompat.SRC_ATOP
            )
        setPaddings(false, layout)
        setLayoutMargins(false, layout)
        layout.invalidate()
        layout.requestLayout()
    }

    private fun showLoaderLayout(fileDescription: FileDescription) {
        loaderLayoutRoot.isVisible = true
        imageLayout.isVisible = false
        errorText.isVisible = false
        fileName.text = fileDescription.incomingName
        initAnimation(loader, false)
    }

    private fun showErrorLayout(fileDescription: FileDescription?) {
        errorText.isVisible = true
        loaderLayoutRoot.isVisible = true
        imageLayout.isVisible = false
        showErrorBubble()

        if (fileDescription != null) {
            loader.setImageResource(getErrorImageResByErrorCode(fileDescription.errorCode))
            ColorsHelper.setTint(itemView.context, loader, Config.getInstance().chatStyle.messageNotSentErrorImageColor)
            fileName.text = FileUtils.getFileName(fileDescription)
            val errorString = getString(getErrorStringResByErrorCode(fileDescription.errorCode))
            errorText.text = errorString
            rotateAnim.cancel()
        }
    }

    private fun showErrorBubble() {
        loaderLayoutRoot.background.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                getColorInt(style.messageNotSentBubbleBackgroundColor),
                BlendModeCompat.SRC_ATOP
            )
    }

    private fun showNormalBubble() {
        loaderLayoutRoot.background.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                getColorInt(style.outgoingMessageBubbleColor),
                BlendModeCompat.SRC_ATOP
            )
    }

    private fun scrollToErrorIfAppearsFirstTime() {
        val previousStatus = statuses[timeStamp]
        if (previousStatus == null || previousStatus != MessageStatus.FAILED) {
            timeStamp?.let { messageErrorProcessor.onNext(it) }
        }
    }

    private fun showBubbleByCurrentStatus(fileDescription: FileDescription?) {
        val previousStatus = statuses[timeStamp]
        if (previousStatus == null || previousStatus != MessageStatus.FAILED) {
            showCommonLayout(fileDescription)
        } else if (fileDescription != null) {
            showErrorLayout(fileDescription)
        }
    }

    private fun showCommonLayout(fileDescription: FileDescription?) {
        imageLayout.isVisible = true
        errorText.isVisible = false
        loaderLayoutRoot.isVisible = false
        rotateAnim.cancel()
        showNormalBubble()
        if (fileDescription != null) {
            val isDownloadError = fileDescription.isDownloadError
            val uri = fileDescription.fileUri
            val path = fileDescription.downloadPath
            val fileUri = uri?.toString() ?: path
            if (!fileUri.isNullOrEmpty() && !isDownloadError) {
                ImageLoader.get()
                    .autoRotateWithExif(true)
                    .load(fileUri)
                    .scales(ImageView.ScaleType.FIT_XY, ImageView.ScaleType.CENTER_CROP)
                    .modifications(maskedTransformation)
                    .errorDrawableResourceId(style.imagePlaceholder)
                    .into(image)
                loadedUri = uri.toString()
            } else {
                image.setImageResource(style.imagePlaceholder)
            }
            moveTimeToImageLayout()
        }
    }
}
