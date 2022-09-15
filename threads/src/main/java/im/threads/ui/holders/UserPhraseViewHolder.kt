package im.threads.ui.holders

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.Formatter
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.isVisible
import com.google.android.material.slider.Slider
import im.threads.R
import im.threads.business.formatters.RussianFormatSymbols
import im.threads.business.imageLoading.ImageLoader.Companion.get
import im.threads.business.media.FileDescriptionMediaPlayer
import im.threads.business.models.CampaignMessage
import im.threads.business.models.ChatItem
import im.threads.business.models.FileDescription
import im.threads.business.models.MessageState
import im.threads.business.models.Quote
import im.threads.business.models.UserPhrase
import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.ogParser.OGDataContent
import im.threads.business.utils.FileUtils
import im.threads.business.utils.FileUtils.isImage
import im.threads.business.utils.FileUtils.isVoiceMessage
import im.threads.ui.views.CircularProgressButton
import im.threads.ui.views.VoiceTimeLabelFormatter
import im.threads.ui.views.formatAsDuration
import im.threads.ui.widget.textView.BubbleMessageTextView
import im.threads.ui.widget.textView.BubbleTimeTextView
import io.reactivex.subjects.PublishSubject
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

/** layout/item_user_text_with_file.xml */
class UserPhraseViewHolder(
    private val parentView: ViewGroup,
    highlightingStream: PublishSubject<ChatItem>,
    fdMediaPlayer: FileDescriptionMediaPlayer
) : VoiceMessageBaseHolder(
    LayoutInflater.from(parentView.context)
        .inflate(R.layout.item_user_text_with_file, parentView, false),
    highlightingStream,
    fdMediaPlayer
) {
    private val sdf = SimpleDateFormat("HH:mm", Locale.US)

    @SuppressLint("SimpleDateFormat")
    private val fileSdf = if (Locale.getDefault().language.equals("ru", ignoreCase = true)) {
        SimpleDateFormat("dd MMMM yyyy", RussianFormatSymbols())
    } else {
        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    }
    override var fileDescription: FileDescription? = null
    private var formattedDuration = ""

    private val rootLayout: RelativeLayout = itemView.findViewById(R.id.rootLayout)
    private val rightTextRow: TableRow = itemView.findViewById(R.id.rightTextRow)
    private val image: ImageView = itemView.findViewById(R.id.image)
    private val rightTextDescription: TextView = itemView.findViewById(R.id.fileSpecs)
    private val quoteTextRow: TableRow = itemView.findViewById(R.id.quoteTextRow)
    private val quoteImage: ImageView = itemView.findViewById(R.id.quoteImage)
    private val quoteTextDescription: TextView = itemView.findViewById(R.id.quoteFileSpecs)
    private val quoteTextHeader: TextView = itemView.findViewById(R.id.quoteTo)
    private val quoteTextTimeStamp: TextView = itemView.findViewById(R.id.quoteSendAt)
    private val phraseFrame: FrameLayout = itemView.findViewById(R.id.phraseFrame)
    private val ogDataLayout: ViewGroup = itemView.findViewById(R.id.ogDataLayout)
    private val voiceMessage: ViewGroup = itemView.findViewById(R.id.voiceMessage)
    private val slider: Slider = itemView.findViewById(R.id.voiceMessageUserSlider)
    private val fileSizeTextView: TextView = itemView.findViewById(R.id.fileSize)
    private val errorText: TextView = itemView.findViewById(R.id.errorText)
    private val loader: ImageView = itemView.findViewById(R.id.loader)
    private val rightTextHeader: TextView = itemView.findViewById(R.id.to)
    private val rightTextTimeStamp: TextView = itemView.findViewById(R.id.sendAt)

    private val ogTimestamp: TextView = itemView.findViewById<TextView>(R.id.ogTimestamp).apply {
        setTextColor(getColorInt(style.outgoingMessageTimeColor))
    }

    private val phraseTextView: BubbleMessageTextView =
        itemView.findViewById<BubbleMessageTextView>(R.id.text).apply {
            setLinkTextColor(getColorInt(style.outgoingMessageLinkColor))
        }

    private val fileImageButton: CircularProgressButton =
        itemView.findViewById<CircularProgressButton>(R.id.buttonDownload).apply {
            setBackgroundColorResId(style.outgoingMessageTextColor)
        }

    override val buttonPlayPause: ImageView =
        itemView.findViewById<ImageView>(R.id.voiceMessageUserButtonPlayPause).apply {
            drawable.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    getColorInt(style.outgoingPlayPauseButtonColor),
                    BlendModeCompat.SRC_ATOP
                )
        }

    private val timeStampTextView =
        itemView.findViewById<BubbleTimeTextView>(R.id.timeStamp).apply {
            setTextColor(getColorInt(style.outgoingMessageTimeColor))
            if (style.outgoingMessageTimeTextSize > 0) {
                val textSize =
                    parentView.context.resources.getDimension(style.outgoingMessageTimeTextSize)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            }
        }

    init {
        itemView.findViewById<View>(R.id.bubble).apply {
            background =
                AppCompatResources.getDrawable(
                    parentView.context,
                    style.outgoingMessageBubbleBackground
                )
            setPadding(
                parentView.context.resources.getDimensionPixelSize(style.bubbleOutgoingPaddingLeft),
                parentView.context.resources.getDimensionPixelSize(style.bubbleOutgoingPaddingTop),
                parentView.context.resources.getDimensionPixelSize(style.bubbleOutgoingPaddingRight),
                parentView.context.resources.getDimensionPixelSize(style.bubbleOutgoingPaddingBottom)
            )
            background.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    getColorInt(style.outgoingMessageBubbleColor),
                    BlendModeCompat.SRC_ATOP
                )
        }
        setTextColorToViews(
            arrayOf(
                rightTextDescription,
                phraseTextView,
                rightTextHeader,
                rightTextTimeStamp,
                fileSizeTextView,
                quoteTextDescription,
                quoteTextHeader,
                quoteTextTimeStamp
            ),
            style.outgoingMessageTextColor
        )
        itemView.findViewById<View>(R.id.delimiter)
            .setBackgroundColor(getColorInt(style.outgoingMessageTextColor))
        setUpProgressButton(fileImageButton)
    }

    fun onBind(
        userPhrase: UserPhrase,
        formattedDuration: String,
        imageClickListener: View.OnClickListener,
        fileClickListener: View.OnClickListener?,
        buttonClickListener: View.OnClickListener?,
        onRowClickListener: View.OnClickListener?,
        onChangeListener: Slider.OnChangeListener,
        onSliderTouchListener: Slider.OnSliderTouchListener,
        onQuoteClickListener: View.OnClickListener?,
        onLongClickListener: OnLongClickListener?,
        isChosen: Boolean
    ) {
        subscribeForHighlighting(userPhrase, rootLayout)
        subscribeForOpenGraphData(
            OGDataContent(
                WeakReference(ogDataLayout),
                WeakReference(timeStampTextView),
                userPhrase.phraseText
            )
        )
        rootLayout.setOnLongClickListener(onLongClickListener)
        changeHighlighting(isChosen)
        val phrase =
            if (userPhrase.phraseText != null) userPhrase.phraseText.trim { it <= ' ' } else null
        val timeStamp = userPhrase.timeStamp
        val sendState = userPhrase.sentState
        val quote = userPhrase.quote
        val campaignMessage = userPhrase.campaignMessage
        this.formattedDuration = formattedDuration
        viewUtils.setClickListener(rootLayout, onLongClickListener)
        viewUtils.setClickListener(rootLayout, onRowClickListener)
        buttonPlayPause.setOnClickListener(buttonClickListener)
        slider.addOnChangeListener(onChangeListener)
        slider.addOnSliderTouchListener(onSliderTouchListener)
        slider.setLabelFormatter(VoiceTimeLabelFormatter())
        fileSizeTextView.text = formattedDuration
        setTimestamp(timeStamp)
        setSendState(sendState)

        phrase?.let {
            showPhrase(it)
        } ?: run {
            phraseTextView.isVisible = false
        }

        image.isVisible = false
        quoteImage.isVisible = false
        fileImageButton.isVisible = false
        voiceMessage.isVisible = false
        quoteTextRow.isVisible = false
        rightTextRow.isVisible = false
        showFiles(userPhrase, imageClickListener, fileClickListener)
        quote?.let { showQuote(it, onQuoteClickListener) }
            ?: campaignMessage?.let { showCampaign(it) }
        if (quote != null || fileDescription != null) {
            phraseFrame.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            phraseFrame.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        rightTextHeader.isVisible =
            !(rightTextHeader.text == null || rightTextHeader.text.toString() == "null")
    }

    private fun showPhrase(phrase: String) {
        phraseTextView.isVisible = true
        phraseTextView.bindTimestampView(timeStampTextView)
        bindOGData(phrase)
        highlightClientText(phraseTextView, phrase)
    }

    private fun showFiles(
        userPhrase: UserPhrase,
        imageClickListener: View.OnClickListener,
        fileClickListener: View.OnClickListener?
    ) {
        userPhrase.fileDescription?.let {
            fileDescription = it
            subscribeForVoiceMessageDownloaded(false)

            rightTextDescription.text = getFileDescriptionText(it)
            if (it.state == AttachmentStateEnum.PENDING || userPhrase.sentState == MessageState.STATE_SENDING) {
                rightTextRow.isVisible = true
                showLoaderLayout()
            } else if (it.state === AttachmentStateEnum.ERROR) {
                rightTextRow.isVisible = true
                showErrorLayout(it)
            } else {
                showCommonLayout()
                if (isVoiceMessage(it)) {
                    phraseTextView.isVisible = false
                    voiceMessage.isVisible = true
                } else {
                    if (isImage(it)) {
                        image.isVisible = true
                        image.setOnClickListener(imageClickListener)
                        val downloadPath: String? = if (it.fileUri == null) {
                            it.downloadPath
                        } else {
                            it.fileUri.toString()
                        }
                        get()
                            .load(downloadPath)
                            .autoRotateWithExif(true)
                            .scales(ImageView.ScaleType.FIT_XY, ImageView.ScaleType.CENTER_CROP)
                            .errorDrawableResourceId(style.imagePlaceholder)
                            .into(image)
                    } else {
                        if (it.fileUri != null) {
                            it.downloadProgress = 100
                        }
                        rightTextRow.isVisible = true
                        viewUtils.setClickListener(rightTextRow, null as View.OnClickListener?)
                        fileImageButton.isVisible = true
                        rightTextHeader.text = it.from
                        val timeStampText = parentView.context.getString(
                            R.string.threads_sent_at,
                            fileSdf.format(Date(it.timeStamp))
                        )
                        rightTextTimeStamp.text = timeStampText
                        if (fileClickListener != null) {
                            fileImageButton.setOnClickListener(fileClickListener)
                        }
                        fileImageButton.setProgress(if (it.fileUri != null) 100 else it.downloadProgress)
                    }
                }
            }
        }
    }

    private fun showQuote(quote: Quote, onQuoteClickListener: View.OnClickListener?) {
        quoteTextRow.isVisible = true
        viewUtils.setClickListener(quoteTextRow, onQuoteClickListener)
        quoteTextDescription.text = quote.text
        quoteTextHeader.text = quote.phraseOwnerTitle
        val timeStampText = parentView.context.resources.getString(
            R.string.threads_sent_at,
            fileSdf.format(Date(quote.timeStamp))
        )
        quoteTextTimeStamp.text = timeStampText
        quote.fileDescription?.let {
            if (isImage(it)) {
                quoteImage.isVisible = true
                var downloadPath: String? = ""
                if (it.fileUri != null) {
                    downloadPath = it.fileUri.toString()
                } else if (it.downloadPath != null) {
                    downloadPath = it.downloadPath
                }
                get()
                    .autoRotateWithExif(true)
                    .load(downloadPath)
                    .scales(ImageView.ScaleType.FIT_XY, ImageView.ScaleType.CENTER_CROP)
                    .errorDrawableResourceId(style.imagePlaceholder)
                    .into(quoteImage)
                if (onQuoteClickListener != null) {
                    quoteImage.setOnClickListener(onQuoteClickListener)
                }
            } else if (isVoiceMessage(it)) {
                quoteTextDescription.setText(R.string.threads_voice_message)
            } else {
                quoteTextDescription.setText(R.string.threads_file)
            }
        }
    }

    private fun showCampaign(campaignMessage: CampaignMessage) {
        quoteTextRow.isVisible = true
        quoteTextDescription.text = campaignMessage.text
        quoteTextHeader.text = campaignMessage.senderName
        val text = parentView.context.resources.getString(
            R.string.threads_sent_at,
            fileSdf.format(campaignMessage.receivedDate)
        )
        quoteTextTimeStamp.text = text
    }

    override fun init(maxValue: Int, progress: Int, isPlaying: Boolean) {
        val effectiveProgress = min(progress, maxValue)
        fileSizeTextView.text = effectiveProgress.formatAsDuration()
        slider.isEnabled = true
        slider.valueTo = maxValue.toFloat()
        slider.value = effectiveProgress.toFloat()
        buttonPlayPause.setImageResource(if (isPlaying) style.voiceMessagePauseButton else style.voiceMessagePlayButton)
    }

    override fun updateProgress(progress: Int) {
        fileSizeTextView.text = progress.formatAsDuration()
        slider.value = min(progress.toFloat(), slider.valueTo)
    }

    override fun updateIsPlaying(isPlaying: Boolean) {
        buttonPlayPause.setImageResource(if (isPlaying) style.voiceMessagePauseButton else style.voiceMessagePlayButton)
    }

    override fun resetProgress() {
        fileSizeTextView.text = formattedDuration
        slider.isEnabled = false
        slider.value = 0f
        buttonPlayPause.setImageResource(style.voiceMessagePlayButton)
    }

    private fun setTimestamp(timeStamp: Long) {
        val timeText = sdf.format(Date(timeStamp))
        timeStampTextView.text = timeText
        ogTimestamp.text = timeText
    }

    private fun setSendState(sendState: MessageState) {
        when (sendState) {
            MessageState.STATE_WAS_READ -> updateDrawable(
                parentView.context,
                R.drawable.threads_message_received,
                R.color.threads_outgoing_message_received_icon
            )
            MessageState.STATE_SENT -> updateDrawable(
                parentView.context,
                R.drawable.threads_message_sent,
                R.color.threads_outgoing_message_sent_icon
            )
            MessageState.STATE_NOT_SENT -> updateDrawable(
                parentView.context,
                R.drawable.threads_message_waiting,
                R.color.threads_outgoing_message_not_send_icon
            )
            MessageState.STATE_SENDING -> updateDrawable(
                parentView.context,
                R.drawable.empty_space_24dp,
                -1
            )
        }
    }

    private fun updateDrawable(context: Context, srcDrawableResId: Int, colorResId: Int) {
        val drawable = AppCompatResources.getDrawable(context, srcDrawableResId)
        if (drawable != null) {
            if (colorResId >= 0) {
                drawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    ContextCompat.getColor(context, colorResId),
                    BlendModeCompat.SRC_ATOP
                )
            }
            timeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
            ogTimestamp.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        }
    }

    private fun showLoaderLayout() {
        errorText.isVisible = false
        loader.isVisible = true
        fileImageButton.isVisible = false
        initAnimation(loader, false)
    }

    private fun showErrorLayout(fileDescription: FileDescription) {
        errorText.isVisible = true
        loader.isVisible = true
        loader.setImageResource(getErrorImageResByErrorCode(fileDescription.errorCode))
        fileImageButton.isVisible = false
        val errorString = getString(getErrorStringResByErrorCode(fileDescription.errorCode))
        errorText.text = errorString
        rotateAnim.cancel()
        rotateAnim.reset()
    }

    private fun showCommonLayout() {
        errorText.isVisible = false
        loader.isVisible = false
        rotateAnim.cancel()
        rotateAnim.reset()
    }

    private fun getFileDescriptionText(fileDescription: FileDescription): String {
        return "${FileUtils.getFileName(fileDescription)} " +
            if (fileDescription.size > 0) {
                Formatter.formatFileSize(parentView.context, fileDescription.size).trimIndent()
            } else {
                ""
            }
    }
}
