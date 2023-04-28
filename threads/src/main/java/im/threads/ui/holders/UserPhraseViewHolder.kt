package im.threads.ui.holders

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.isVisible
import com.google.android.material.slider.Slider
import im.threads.R
import im.threads.business.core.ContextHolder.context
import im.threads.business.formatters.RussianFormatSymbols
import im.threads.business.imageLoading.ImageLoader
import im.threads.business.imageLoading.ImageLoader.Companion.get
import im.threads.business.imageLoading.ImageModifications
import im.threads.business.media.FileDescriptionMediaPlayer
import im.threads.business.models.CampaignMessage
import im.threads.business.models.ChatItem
import im.threads.business.models.FileDescription
import im.threads.business.models.MessageStatus
import im.threads.business.models.Quote
import im.threads.business.models.UserPhrase
import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.ogParser.OGDataContent
import im.threads.business.ogParser.OpenGraphParser
import im.threads.business.utils.FileUtils
import im.threads.business.utils.FileUtils.isImage
import im.threads.business.utils.FileUtils.isVoiceMessage
import im.threads.business.utils.UrlUtils
import im.threads.business.utils.toFileSize
import im.threads.ui.config.Config
import im.threads.ui.utils.ColorsHelper
import im.threads.ui.utils.gone
import im.threads.ui.utils.invisible
import im.threads.ui.utils.isVisible
import im.threads.ui.utils.visible
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
    private val maskedTransformation: ImageModifications.MaskedModification?,
    highlightingStream: PublishSubject<ChatItem>,
    openGraphParser: OpenGraphParser,
    fdMediaPlayer: FileDescriptionMediaPlayer,
    private val messageErrorProcessor: PublishSubject<Long>
) : VoiceMessageBaseHolder(
    LayoutInflater.from(parentView.context)
        .inflate(R.layout.ecc_item_user_text_with_file, parentView, false),
    highlightingStream,
    openGraphParser,
    fdMediaPlayer,
    false
) {
    private val sdf = SimpleDateFormat("HH:mm", Locale.US)

    @SuppressLint("SimpleDateFormat")
    private val fileSdf = if (Locale.getDefault().language.equals("ru", ignoreCase = true)) {
        SimpleDateFormat("dd MMMM yyyy", RussianFormatSymbols())
    } else {
        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    }
    override var fileDescription: FileDescription? = null
    private var timeStamp: Long? = null
    private var formattedDuration = ""

    private val rootLayout: RelativeLayout = itemView.findViewById(R.id.rootLayout)
    private val rightTextRow: TableRow = itemView.findViewById(R.id.rightTextRow)
    private val image: ImageView = itemView.findViewById(R.id.image)
    private val imageRoot: FrameLayout = itemView.findViewById(R.id.imageRoot)
    private val imageLayout: FrameLayout = itemView.findViewById(R.id.imageLayout)
    private val errorImage: ImageView = itemView.findViewById(R.id.errorImage)
    private val rightTextDescription: TextView = itemView.findViewById(R.id.fileSpecs)
    private val quoteTextRow: TableRow = itemView.findViewById(R.id.quoteTextRow)
    private val quoteImage: ImageView = itemView.findViewById(R.id.quoteImage)
    private val quoteTextDescription: TextView = itemView.findViewById(R.id.quoteFileSpecs)
    private val quoteTextHeader: TextView = itemView.findViewById(R.id.quoteTo)
    private val quoteTextTimeStamp: TextView = itemView.findViewById(R.id.quoteSendAt)
    private val phraseFrame: FrameLayout = itemView.findViewById(R.id.phraseFrame)
    private val ogDataLayout: ViewGroup = itemView.findViewById(R.id.ogDataLayout)
    private val slider: Slider = itemView.findViewById(R.id.voiceMessageUserSlider)
    private val fileSizeTextView: TextView = itemView.findViewById(R.id.fileSize)
    private val errorText: TextView = itemView.findViewById(R.id.errorText)
    private val loader: ImageView = itemView.findViewById(R.id.loader)
    private val rightTextHeader: TextView = itemView.findViewById(R.id.to)
    private val rightTextTimeStamp: TextView = itemView.findViewById(R.id.sendAt)

    private val bubbleLayout: ViewGroup = itemView.findViewById<ViewGroup>(R.id.bubble).apply {
        background =
            AppCompatResources.getDrawable(
                itemView.context,
                style.outgoingMessageBubbleBackground
            )
    }

    private val ogTimestamp: BubbleTimeTextView = itemView.findViewById<BubbleTimeTextView>(R.id.ogTimeStamp).apply {
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

    override val voiceMessage: ViewGroup = itemView.findViewById(R.id.voiceMessage)

    private lateinit var timeStampTextView: BubbleTimeTextView

    init {
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
        itemView.findViewById<View>(R.id.delimiter).setBackgroundColor(getColorInt(style.outgoingMessageTextColor))
        ColorsHelper.setTextColor(errorText, style.errorMessageTextColor)
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
        timeStamp = userPhrase.timeStamp
        showBubbleByCurrentStatusOrErrorMock(userPhrase.errorMock)
        initTimeStampView(userPhrase)
        hideAll()
        setupPaddingsAndBorders(userPhrase.fileDescription)
        subscribeForHighlighting(userPhrase, rootLayout)
        subscribeForOpenGraphData(
            OGDataContent(
                WeakReference(ogDataLayout),
                WeakReference(ogTimestamp),
                WeakReference(timeStampTextView),
                userPhrase.phraseText
            )
        )
        rootLayout.setOnLongClickListener(onLongClickListener)
        changeHighlighting(isChosen)
        val phrase = if (userPhrase.phraseText != null) userPhrase.phraseText.trim { it <= ' ' } else null
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

        if (userPhrase.sentState == MessageStatus.FAILED) {
            showErrorText()
        }

        showFiles(userPhrase, imageClickListener, fileClickListener)
        setTimestamp(timeStamp)
        setSendState(sendState)
        phrase?.let {
            showPhrase(it)
        } ?: run {
            phraseTextView.gone()
        }

        quote?.let { showQuote(it, onQuoteClickListener) }
            ?: campaignMessage?.let { showCampaign(it) }
        if ((quote != null || fileDescription != null) && voiceMessage.visibility != VISIBLE) {
            phraseFrame.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            phraseFrame.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        rightTextHeader.isVisible =
            !(rightTextHeader.text == null || rightTextHeader.text.toString() == "null")

        checkOpenGraphVisibility()
    }

    private fun checkOpenGraphVisibility() {
        if (ogDataLayout.isVisible()) {
            timeStampTextView.invisible()
        } else {
            timeStampTextView.visible()
        }
    }

    private fun hideAll() {
        imageLayout.gone()
        quoteImage.gone()
        fileImageButton.gone()
        voiceMessage.gone()
        quoteTextRow.gone()
        rightTextRow.gone()
    }

    private fun setupPaddingsAndBorders(fileDescription: FileDescription?) = with(bubbleLayout) {
        val chatStyle = Config.getInstance().getChatStyle()
        val resources = context.resources
        val borderLeft = resources.getDimensionPixelSize(chatStyle.outgoingImageLeftBorderSize)
        val borderTop = resources.getDimensionPixelSize(chatStyle.outgoingImageTopBorderSize)
        val borderRight = resources.getDimensionPixelSize(chatStyle.outgoingImageRightBorderSize)
        val borderBottom = resources.getDimensionPixelSize(chatStyle.outgoingImageBottomBorderSize)
        val isBordersNotSet = borderLeft == 0 && borderTop == 0 && borderRight == 0 && borderBottom == 0
        val isImage = isImage(fileDescription)

        setLayoutMargins(false, bubbleLayout)
        if (isImage) {
            imageRoot.visible()

            (bubbleLayout.layoutParams as ViewGroup.MarginLayoutParams).let {
                it.marginStart = 0
                bubbleLayout.layoutParams = it
            }
            bubbleLayout.invalidate()
            bubbleLayout.requestLayout()

            if (isBordersNotSet) {
                phraseFrame.setPadding(borderLeft, 0, borderRight, 0)
                setPaddings(false, this)
            } else {
                setPadding(0, 0, 0, 0)
                (image.layoutParams as FrameLayout.LayoutParams).apply {
                    setMargins(borderLeft, borderTop, borderRight, borderBottom)
                    image.layoutParams = this
                }
                phraseFrame.setPadding(
                    borderLeft,
                    0,
                    borderRight,
                    resources.getDimensionPixelSize(style.bubbleIncomingPaddingBottom)
                )
            }
            image.invalidate()
            image.requestLayout()
        } else {
            (bubbleLayout.layoutParams as ViewGroup.MarginLayoutParams).let {
                it.width = ActionBar.LayoutParams.WRAP_CONTENT
                it.height = ActionBar.LayoutParams.WRAP_CONTENT
            }
            bubbleLayout.invalidate()
            bubbleLayout.requestLayout()

            imageRoot.gone()

            phraseFrame.setPadding(0, 0, 0, 0)
            setPaddings(false, this)
        }
    }

    private fun showPhrase(phrase: String) {
        phraseTextView.visible()
        phraseTextView.bindTimestampView(timeStampTextView)
        bindOGData(phrase)
        val extractedLink = UrlUtils.extractLink(phrase)
        val deeplink = UrlUtils.extractDeepLink(phrase)
        val urlLink = if (extractedLink == null || extractedLink.isEmail) {
            null
        } else {
            extractedLink.link
        }
        highlightClientText(phraseTextView, phrase, deeplink ?: urlLink)
    }

    private fun showFiles(
        userPhrase: UserPhrase,
        imageClickListener: View.OnClickListener,
        fileClickListener: View.OnClickListener?
    ) {
        userPhrase.fileDescription?.let {
            fileDescription = it
            subscribeForVoiceMessageDownloaded()
            rightTextDescription.text = getFileDescriptionText(it)
            val statusKey = "${it.incomingName}:${it.size}"
            val lastStatus = fileStatuses[statusKey]
            val status = if (lastStatus != null && it.state < lastStatus) {
                lastStatus
            } else {
                it.state
            }
            val isLoading = status == AttachmentStateEnum.PENDING || userPhrase.sentState == MessageStatus.SENDING
            fileStatuses[statusKey] = status
            if (isVoiceMessage(it) && isLoading) {
                startLoader()
            } else if (isLoading) {
                stopLoader()
                voiceMessage.gone()
                imageRoot.gone()
                rightTextRow.visible()
                showLoaderLayout()
            } else if (it.state === AttachmentStateEnum.ERROR || userPhrase.sentState == MessageStatus.FAILED) {
                stopLoader()
                voiceMessage.gone()
                rightTextRow.visible()
                showErrorText(it)
                initTimeStampView(userPhrase)
                timeStampTextView = itemView.findViewById(R.id.timeStamp)
                itemView.findViewById<BubbleTimeTextView>(R.id.timeStamp).gone()
                timeStampTextView.visible()
            } else {
                stopLoader()
                showCommonLayout()
                if (isVoiceMessage(it)) {
                    phraseTextView.gone()
                    voiceMessage.visible()
                } else {
                    voiceMessage.gone()
                    if (isImage(it)) {
                        imageRoot.visible()
                        hideErrorImage(imageLayout, errorImage)
                        image.setOnClickListener(imageClickListener)
                        val downloadPath: String? = if (it.fileUri == null) {
                            it.downloadPath
                        } else {
                            it.fileUri.toString()
                        }
                        if (!downloadPath.isNullOrEmpty()) {
                            get()
                                .load(downloadPath)
                                .autoRotateWithExif(true)
                                .scales(ImageView.ScaleType.FIT_XY, ImageView.ScaleType.CENTER_CROP)
                                .modifications(maskedTransformation)
                                .callback(object : ImageLoader.ImageLoaderCallback {
                                    override fun onImageLoadError() {
                                        showErrorImage(imageLayout, errorImage)
                                    }
                                })
                                .into(image)
                        } else {
                            image.setImageResource(style.imagePlaceholder)
                        }
                        val chatStyle = Config.getInstance().getChatStyle()
                        val resources = context.resources
                        val paddingLeft = resources.getDimensionPixelSize(chatStyle.bubbleOutgoingPaddingLeft)
                        val paddingTop = resources.getDimensionPixelSize(chatStyle.bubbleOutgoingPaddingTop)
                        val paddingRight = resources.getDimensionPixelSize(chatStyle.bubbleOutgoingPaddingRight)
                        val paddingBottom = resources.getDimensionPixelSize(chatStyle.bubbleOutgoingPaddingBottom)
                        ogDataLayout.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
                        ogDataLayout.layoutParams.width = resources.getDimensionPixelSize(R.dimen.ecc_message_image_size)
                    } else {
                        if (it.fileUri != null) {
                            it.downloadProgress = 100
                        }
                        rightTextRow.visible()
                        viewUtils.setClickListener(rightTextRow, null as View.OnClickListener?)
                        fileImageButton.visible()
                        rightTextHeader.text = it.from
                        val timeStampText = parentView.context.getString(
                            R.string.ecc_sent_at,
                            fileSdf.format(Date(it.timeStamp))
                        )
                        rightTextTimeStamp.text = timeStampText
                        if (fileClickListener != null) {
                            fileImageButton.setOnClickListener(fileClickListener)
                        }
                        val statusKey = "${it.incomingName}:${it.size}"
                        val progress = if (it.fileUri != null) {
                            100
                        } else if (fileProgress[statusKey] != null && it.downloadProgress < fileProgress[statusKey]!!) {
                            fileProgress[statusKey]
                        } else {
                            it.downloadProgress
                        }
                        fileImageButton.setProgress(progress ?: 100)
                        if (progress != null) {
                            fileProgress[statusKey] = progress
                        }
                    }
                }
            }
        }
    }

    private fun showQuote(quote: Quote, onQuoteClickListener: View.OnClickListener?) {
        quoteTextRow.visible()
        viewUtils.setClickListener(quoteTextRow, onQuoteClickListener)
        quoteTextDescription.text = quote.text
        quoteTextHeader.text = quote.phraseOwnerTitle
        val timeStampText = parentView.context.resources.getString(
            R.string.ecc_sent_at,
            fileSdf.format(Date(quote.timeStamp))
        )
        quoteTextTimeStamp.text = timeStampText
        quote.fileDescription?.let {
            if (isImage(it)) {
                quoteImage.visible()
                var downloadPath: String? = ""
                if (it.fileUri != null) {
                    downloadPath = it.fileUri.toString()
                } else if (it.downloadPath != null) {
                    downloadPath = it.downloadPath
                }
                if (!downloadPath.isNullOrEmpty()) {
                    get()
                        .autoRotateWithExif(true)
                        .load(downloadPath)
                        .scales(ImageView.ScaleType.FIT_XY, ImageView.ScaleType.CENTER_CROP)
                        .errorDrawableResourceId(style.imagePlaceholder)
                        .into(quoteImage)
                } else {
                    quoteImage.setImageResource(style.imagePlaceholder)
                }
                if (onQuoteClickListener != null) {
                    quoteImage.setOnClickListener(onQuoteClickListener)
                }
            } else if (isVoiceMessage(it)) {
                quoteTextDescription.setText(R.string.ecc_voice_message)
            } else {
                quoteTextDescription.setText(R.string.ecc_file)
            }
        }
    }

    private fun showCampaign(campaignMessage: CampaignMessage) {
        quoteTextRow.visible()
        quoteTextDescription.text = campaignMessage.text
        quoteTextHeader.text = campaignMessage.senderName
        val text = parentView.context.resources.getString(
            R.string.ecc_sent_at,
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
        if (buttonPlayPause.tag != loadingStateTag) {
            val imageResource =
                if (isPlaying) style.voiceMessagePauseButton else style.voiceMessagePlayButton
            buttonPlayPause.setImageResource(imageResource)
        }
    }

    override fun resetProgress() {
        fileSizeTextView.text = formattedDuration
        slider.isEnabled = false
        slider.value = 0f
        if (buttonPlayPause.tag != loadingStateTag) {
            buttonPlayPause.setImageResource(style.voiceMessagePlayButton)
        }
    }

    private fun setTimestamp(timeStamp: Long) {
        val timeText = sdf.format(Date(timeStamp))
        timeStampTextView.text = timeText
        ogTimestamp.text = timeText
    }

    private fun setSendState(sendStatus: MessageStatus) {
        when (sendStatus) {
            MessageStatus.SENDING -> {
                style.approveRequestToResolveThreadTextResId
                val previousStatus = statuses[timeStamp]
                if (previousStatus == null || previousStatus != MessageStatus.FAILED) {
                    showNormalBubble()
                    updateDrawable(
                        parentView.context,
                        style.messageSendingIconResId,
                        style.messageSendingIconColorResId
                    )
                }
            }
            MessageStatus.SENT -> {
                showNormalBubble()
                updateDrawable(
                    parentView.context,
                    style.messageSentIconResId,
                    style.messageSentIconColorResId
                )
            }
            MessageStatus.DELIVERED -> {
                showNormalBubble()
                updateDrawable(
                    parentView.context,
                    style.messageDeliveredIconResId,
                    style.messageDeliveredIconColorResId
                )
            }
            MessageStatus.READ -> {
                showNormalBubble()
                updateDrawable(
                    parentView.context,
                    style.messageReadIconResId,
                    style.messageReadIconColorResId
                )
            }
            MessageStatus.FAILED -> {
                showErrorBubble()
                scrollToErrorIfAppearsFirstTime()
                updateDrawable(
                    parentView.context,
                    style.messageFailedIconResId,
                    style.messageFailedIconColorResId
                )
            }
        }
        timeStamp?.let { statuses[it] = sendStatus }
    }

    private fun scrollToErrorIfAppearsFirstTime() {
        val previousStatus = statuses[timeStamp]
        if (previousStatus == null || previousStatus != MessageStatus.FAILED) {
            timeStamp?.let { messageErrorProcessor.onNext(it) }
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
        errorText.gone()
        loader.visible()
        fileImageButton.gone()
        initAnimation(loader, false)
    }

    private fun showErrorBubble() {
        bubbleLayout.background.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                getColorInt(style.messageNotSentBubbleBackgroundColor),
                BlendModeCompat.SRC_ATOP
            )
        showErrorText()
    }

    private fun showNormalBubble() {
        bubbleLayout.background.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                getColorInt(style.outgoingMessageBubbleColor),
                BlendModeCompat.SRC_ATOP
            )
        hideErrorText()
    }

    private fun showBubbleByCurrentStatusOrErrorMock(errorMock: Boolean?) {
        val previousStatus = statuses[timeStamp]
        if ((previousStatus == null || previousStatus != MessageStatus.FAILED) && errorMock == true) {
            showNormalBubble()
        } else {
            showErrorBubble()
        }
    }

    private fun showErrorText() {
        errorText.visible()
        loader.visible()
        fileImageButton.gone()
        errorText.text = getString(R.string.ecc_message_not_sent)
        rotateAnim.cancel()
        rotateAnim.reset()
    }

    private fun showErrorText(fileDescription: FileDescription) {
        errorText.visible()
        loader.visible()
        imageLayout.gone()
        imageRoot.gone()
        image.gone()
        loader.setImageResource(getErrorImageResByErrorCode(fileDescription.errorCode))
        fileImageButton.gone()
        val errorString = getString(getErrorStringResByErrorCode(fileDescription.errorCode))
        errorText.text = errorString
        rotateAnim.cancel()
        rotateAnim.reset()
    }

    private fun hideErrorText() {
        errorText.gone()
    }

    private fun showCommonLayout() {
        errorText.gone()
        loader.gone()
        rotateAnim.cancel()
        rotateAnim.reset()
    }

    private fun getFileDescriptionText(fileDescription: FileDescription): String {
        return "${FileUtils.getFileName(fileDescription)} " +
            if (fileDescription.size > 0) {
                fileDescription.size.toFileSize().trimIndent()
            } else {
                ""
            }
    }

    private fun initTimeStampView(userPhrase: UserPhrase) {
        timeStampTextView = if (isVoiceMessage(userPhrase.fileDescription)) {
            itemView.findViewById<BubbleTimeTextView>(R.id.timeStamp).gone()
            itemView.findViewById(R.id.voiceTimeStamp)
        } else {
            itemView.findViewById<BubbleTimeTextView>(R.id.voiceTimeStamp).gone()
            itemView.findViewById(R.id.timeStamp)
        }
        timeStampTextView.visible()
        timeStampTextView.setTextColor(getColorInt(style.outgoingMessageTimeColor))
        if (style.outgoingMessageTimeTextSize > 0) {
            val textSize =
                parentView.context.resources.getDimension(style.outgoingMessageTimeTextSize)
            timeStampTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        }
    }

    companion object {
        private val fileProgress: HashMap<String, Int> = HashMap()
        private val fileStatuses: HashMap<String, AttachmentStateEnum> = HashMap()
    }
}
