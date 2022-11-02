package im.threads.ui.holders

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
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
import im.threads.business.models.MessageState
import im.threads.business.models.UserPhrase
import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.ogParser.OpenGraphParser
import io.reactivex.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageFromUserViewHolder(
    parent: ViewGroup,
    private val maskedTransformation: ImageModifications.MaskedModification?,
    highlightingStream: PublishSubject<ChatItem>,
    openGraphParser: OpenGraphParser
) : BaseImageHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_user_image_from, parent, false),
    highlightingStream,
    openGraphParser,
    false
) {
    private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

    private var loadedUri: String? = null

    private val loaderLayout: LinearLayout =
        itemView.findViewById<LinearLayout>(R.id.loaderLayout).also { applyBubbleLayoutStyle(it) }

    private val errorText: TextView = itemView.findViewById(R.id.errorText)
    private val fileName: TextView = itemView.findViewById(R.id.fileName)
    private val loader: ImageView = itemView.findViewById(R.id.loader)
    private val rootLayout: LinearLayout = itemView.findViewById(R.id.rootLayout)

    init {
        setTextColorToViews(
            arrayOf(fileName),
            style.outgoingMessageTextColor
        )
    }

    fun onBind(
        userPhrase: UserPhrase,
        highlighted: Boolean,
        clickRunnable: Runnable,
        longClickRunnable: Runnable
    ) {
        subscribeForHighlighting(userPhrase, rootLayout)
        image.setOnClickListener { clickRunnable.run() }
        image.setOnLongClickListener {
            longClickRunnable.run()
            true
        }
        bindImage(userPhrase.fileDescription, userPhrase.sentState)
        bindIsChosen(highlighted, longClickRunnable)
        bindTimeStamp(userPhrase.sentState, userPhrase.timeStamp, longClickRunnable)
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
        messageState: MessageState
    ) {
        fileDescription?.let {
            if (it.state === AttachmentStateEnum.PENDING || messageState == MessageState.STATE_SENDING) {
                showLoaderLayout(it)
            } else if (it.state === AttachmentStateEnum.ERROR) {
                showErrorLayout(it)
            } else {
                showCommonLayout(it)
                moveTimeToImageLayout()
            }
        }
    }

    private fun bindTimeStamp(
        messageState: MessageState,
        timestamp: Long,
        longClickRunnable: Runnable
    ) {
        timeStampTextView.setOnLongClickListener {
            longClickRunnable.run()
            true
        }
        timeStampTextView.text = sdf.format(Date(timestamp))
        val rightDrawable: Drawable? =
            when (messageState) {
                MessageState.STATE_WAS_READ -> getColoredDrawable(
                    R.drawable.threads_image_message_received,
                    R.color.threads_outgoing_message_image_received_icon
                )
                MessageState.STATE_SENT -> getColoredDrawable(
                    R.drawable.threads_message_image_sent,
                    R.color.threads_outgoing_message_image_sent_icon
                )
                MessageState.STATE_NOT_SENT -> getColoredDrawable(
                    R.drawable.threads_message_image_waiting,
                    R.color.threads_outgoing_message_image_not_send_icon
                )
                MessageState.STATE_SENDING -> AppCompatResources.getDrawable(
                    itemView.context,
                    R.drawable.empty_space_24dp
                )
            }
        timeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null)
    }

    private fun getColoredDrawable(@DrawableRes res: Int, @ColorRes color: Int): Drawable? {
        val drawable = AppCompatResources.getDrawable(itemView.context, res)
        drawable?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            ContextCompat.getColor(itemView.context, color),
            BlendModeCompat.SRC_ATOP
        )
        return drawable
    }

    private fun applyBubbleLayoutStyle(layout: LinearLayout) {
        val res = itemView.context.resources
        layout.background = AppCompatResources.getDrawable(
            itemView.context,
            style.outgoingMessageBubbleBackground
        )
        layout.setPadding(
            res.getDimensionPixelSize(style.bubbleOutgoingPaddingLeft),
            res.getDimensionPixelSize(style.bubbleOutgoingPaddingTop),
            res.getDimensionPixelSize(style.bubbleOutgoingPaddingRight),
            res.getDimensionPixelSize(style.bubbleOutgoingPaddingBottom)
        )
        val layoutParams = layout.layoutParams as RelativeLayout.LayoutParams
        layoutParams.setMargins(
            res.getDimensionPixelSize(style.bubbleOutgoingMarginLeft),
            res.getDimensionPixelSize(style.bubbleOutgoingMarginTop),
            res.getDimensionPixelSize(style.bubbleOutgoingMarginRight),
            res.getDimensionPixelSize(style.bubbleOutgoingMarginBottom)
        )
        layout.layoutParams = layoutParams
        layout.background.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                getColorInt(style.outgoingMessageBubbleColor),
                BlendModeCompat.SRC_ATOP
            )
    }

    private fun showLoaderLayout(fileDescription: FileDescription) {
        loaderLayout.isVisible = true
        imageLayout.isVisible = false
        errorText.isVisible = false
        fileName.text = fileDescription.incomingName
        initAnimation(loader, false)
    }

    private fun showErrorLayout(fileDescription: FileDescription) {
        errorText.isVisible = true
        loaderLayout.isVisible = true
        imageLayout.isVisible = false
        loader.setImageResource(getErrorImageResByErrorCode(fileDescription.errorCode))
        fileName.text = fileDescription.incomingName
        val errorString = getString(getErrorStringResByErrorCode(fileDescription.errorCode))
        errorText.text = errorString
        rotateAnim.cancel()
    }

    private fun showCommonLayout(fileDescription: FileDescription) {
        imageLayout.isVisible = true
        errorText.isVisible = false
        loaderLayout.isVisible = false
        rotateAnim.cancel()
        val isDownloadError = fileDescription.isDownloadError
        val uri = fileDescription.fileUri

        if (uri != null && !isDownloadError) {
            ImageLoader.get()
                .autoRotateWithExif(true)
                .load(uri.toString())
                .modifications(maskedTransformation)
                .errorDrawableResourceId(style.imagePlaceholder)
                .into(image)

            loadedUri = uri.toString()
        } else {
            image.setImageResource(style.imagePlaceholder)
        }
    }
}
