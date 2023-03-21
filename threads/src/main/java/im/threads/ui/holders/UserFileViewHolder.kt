package im.threads.ui.holders

import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.isVisible
import im.threads.R
import im.threads.business.models.ChatItem
import im.threads.business.models.FileDescription
import im.threads.business.models.MessageStatus
import im.threads.business.models.UserPhrase
import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.ogParser.OpenGraphParser
import im.threads.business.utils.FileUtils.getFileName
import im.threads.business.utils.toFileSize
import im.threads.ui.utils.gone
import im.threads.ui.views.CircularProgressButton
import io.reactivex.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserFileViewHolder(
    parent: ViewGroup,
    highlightingStream: PublishSubject<ChatItem>,
    openGraphParser: OpenGraphParser,
    private val messageErrorProcessor: PublishSubject<Long>
) : BaseHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.ecc_item_user_chat_file, parent, false),
    highlightingStream,
    openGraphParser
) {
    private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var timeStamp: Long? = null
    private var fileDescription: FileDescription? = null

    private val fileHeaderTextView: TextView = itemView.findViewById(R.id.header)
    private val fileSizeTextView: TextView = itemView.findViewById(R.id.fileSize)
    private val errorTextView: TextView = itemView.findViewById(R.id.errorText)
    private val loader: ImageView = itemView.findViewById(R.id.loader)
    private val rootLayout: LinearLayout = itemView.findViewById(R.id.rootLayout)
    private val bubbleLayout: ConstraintLayout = itemView.findViewById(R.id.bubble)

    private val circularProgressButton =
        itemView.findViewById<CircularProgressButton>(R.id.buttonDownload).apply {
            setBackgroundColorResId(style.outgoingMessageTextColor)
            setUpProgressButton(this)
        }

    private val timeStampTextView = itemView.findViewById<TextView>(R.id.timeStamp).apply {
        setTextColor(getColorInt(style.outgoingImageTimeColor))
        val timeColorBg = getColorInt(style.outgoingImageTimeBackgroundColor)
        background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            timeColorBg,
            BlendModeCompat.SRC_ATOP
        )
        if (style.outgoingMessageTimeTextSize > 0) {
            val textSize = context.resources.getDimension(style.outgoingMessageTimeTextSize)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        }
    }

    init {
        bubbleLayout.apply {
            background =
                AppCompatResources.getDrawable(
                    itemView.context,
                    style.outgoingMessageBubbleBackground
                )
            setPaddings(false, this)
            setLayoutMargins(false, this)
        }
        setTextColorToViews(
            arrayOf(fileHeaderTextView, fileSizeTextView),
            style.outgoingMessageTextColor
        )
    }

    fun onBind(
        userPhrase: UserPhrase,
        buttonClickListener: View.OnClickListener,
        rowClickListener: View.OnClickListener,
        onLongClick: OnLongClickListener,
        isFilterVisible: Boolean
    ) {
        timeStamp = userPhrase.timeStamp
        subscribeForHighlighting(userPhrase, rootLayout)
        userPhrase.fileDescription?.let {
            this.fileDescription = it
            val viewGroup = itemView as ViewGroup
            fileHeaderTextView.text = getFileName(it)
            fileSizeTextView.text = it.size.toFileSize()
            fileSizeTextView.visibility = if (it.size > 0) View.VISIBLE else View.GONE
            for (i in 0 until viewGroup.childCount) {
                viewGroup.getChildAt(i).setOnLongClickListener(onLongClick)
                viewGroup.getChildAt(i).setOnClickListener(rowClickListener)
            }
            if (userPhrase.sentState == MessageStatus.FAILED) {
                showErrorLayout(it)
            } else {
                updateFileView(it, buttonClickListener)
            }

            rootLayout.setOnLongClickListener(onLongClick)
            changeHighlighting(isFilterVisible)
        }
        showBubbleByCurrentStatus()
        bindTimeStamp(userPhrase.sentState, userPhrase.timeStamp, onLongClick)
    }

    private fun bindTimeStamp(
        messageStatus: MessageStatus,
        timeStamp: Long,
        onLongClick: OnLongClickListener
    ) {
        timeStampTextView.setOnLongClickListener {
            onLongClick.onLongClick(it)
            true
        }
        timeStampTextView.text = sdf.format(Date(timeStamp))
        val rightDrawable: Drawable? =
            when (messageStatus) {
                MessageStatus.SENDING -> {
                    val previousStatus = statuses[timeStamp]
                    if (previousStatus == null || previousStatus != MessageStatus.FAILED) {
                        showNormalBubble()
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
                MessageStatus.SENT -> {
                    showNormalBubble()
                    getColoredDrawable(
                        style.messageSentIconResId,
                        style.messageSentIconColorResId
                    )
                }
                MessageStatus.DELIVERED -> {
                    showNormalBubble()
                    getColoredDrawable(
                        style.messageDeliveredIconResId,
                        style.messageDeliveredIconColorResId
                    )
                }
                MessageStatus.READ -> {
                    showNormalBubble()
                    getColoredDrawable(
                        style.messageReadIconResId,
                        style.messageReadIconColorResId
                    )
                }
                MessageStatus.FAILED -> {
                    showErrorBubble()
                    scrollToErrorIfAppearsFirstTime()
                    getColoredDrawable(
                        style.messageFailedIconResId,
                        style.messageFailedIconColorResId
                    )
                }
            }
        timeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null)
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

    private fun updateFileView(
        fileDescription: FileDescription,
        buttonClickListener: View.OnClickListener
    ) {
        when (fileDescription.state) {
            AttachmentStateEnum.ERROR -> {
                showErrorLayout(fileDescription)
            }
            AttachmentStateEnum.PENDING -> {
                circularProgressButton.isVisible = false
                loader.isVisible = true
                errorTextView.isVisible = false
                initAnimation(loader, false)
            }
            else -> {
                loader.isVisible = false
                errorTextView.isVisible = false
                circularProgressButton.isVisible = true
                circularProgressButton.setProgress(
                    if (fileDescription.fileUri != null) 100 else fileDescription.downloadProgress
                )
                circularProgressButton.setOnClickListener(buttonClickListener)
            }
        }
    }

    private fun showErrorLayout(fileDescription: FileDescription) {
        loader.isVisible = true
        errorTextView.isVisible = true
        circularProgressButton.isVisible = false
        loader.setImageResource(getErrorImageResByErrorCode(fileDescription.errorCode))
        val errorString = getString(getErrorStringResByErrorCode(fileDescription.errorCode))
        errorTextView.text = errorString
    }

    private fun hideErrorLayout() {
        loader.gone()
        errorTextView.gone()
    }

    private fun scrollToErrorIfAppearsFirstTime() {
        val previousStatus = statuses[timeStamp]
        if (previousStatus == null || previousStatus != MessageStatus.FAILED) {
            timeStamp?.let { messageErrorProcessor.onNext(it) }
        }
    }

    private fun showErrorBubble() {
        bubbleLayout.background.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                getColorInt(style.messageNotSentBubbleBackgroundColor),
                BlendModeCompat.SRC_ATOP
            )
        fileDescription?.let { showErrorLayout(it) }
    }

    private fun showNormalBubble() {
        bubbleLayout.background.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                getColorInt(style.outgoingMessageBubbleColor),
                BlendModeCompat.SRC_ATOP
            )
        hideErrorLayout()
    }

    private fun showBubbleByCurrentStatus() {
        val previousStatus = statuses[timeStamp]
        if (previousStatus == null || previousStatus != MessageStatus.FAILED) {
            showNormalBubble()
        } else {
            showErrorBubble()
        }
    }
}
