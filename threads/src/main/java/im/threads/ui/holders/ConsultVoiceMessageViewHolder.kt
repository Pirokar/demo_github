package im.threads.ui.holders

import android.graphics.PorterDuff
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.google.android.material.slider.Slider
import im.threads.R
import im.threads.business.formatters.SpeechStatus
import im.threads.business.imageLoading.ImageModifications
import im.threads.business.imageLoading.loadImage
import im.threads.business.media.FileDescriptionMediaPlayer
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.FileDescription
import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.ogParser.OpenGraphParser
import im.threads.business.utils.FileUtils
import im.threads.business.utils.UrlUtils
import im.threads.ui.utils.ColorsHelper
import im.threads.ui.utils.gone
import im.threads.ui.utils.invisible
import im.threads.ui.utils.visible
import im.threads.ui.views.VoiceTimeLabelFormatter
import im.threads.ui.views.formatAsDuration
import im.threads.ui.widget.textView.QuoteMessageTextView
import io.reactivex.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConsultVoiceMessageViewHolder(
    parent: ViewGroup,
    highlightingStream: PublishSubject<ChatItem>,
    openGraphParser: OpenGraphParser,
    fdMediaPlayer: FileDescriptionMediaPlayer
) : VoiceMessageBaseHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.ecc_item_consult_voice_message, parent, false),
    highlightingStream,
    openGraphParser,
    fdMediaPlayer,
    true
) {
    private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

    override var fileDescription: FileDescription? = null
    private var formattedDuration = ""

    private val errorTextView: TextView = itemView.findViewById(R.id.errorText)
    private val loader: ImageView = itemView.findViewById(R.id.loader)
    private val rootLayout: ConstraintLayout = itemView.findViewById(R.id.rootLayout)

    private val phraseTextView =
        itemView.findViewById<QuoteMessageTextView>(R.id.voiceMessageConsultText).apply {
            setLinkTextColor(getColorInt(style.incomingMessageLinkColor))
        }
    private val slider: Slider = itemView.findViewById(R.id.voiceMessageConsultSlider)
    override val buttonPlayPause: ImageButton =
        itemView.findViewById<ImageButton>(R.id.voiceMessageConsultButtonPlayPause).apply {
            setColorFilter(
                getColorInt(style.incomingPlayPauseButtonColor),
                PorterDuff.Mode.SRC_ATOP
            )
        }
    override val voiceMessage: ViewGroup = itemView.findViewById(R.id.bubble)
    private val fileSizeTextView: TextView = itemView.findViewById(R.id.fileSize)
    private val audioStatusTextView: TextView =
        itemView.findViewById(R.id.voiceMessageConsultAudioStatus)

    private val timeStampTextView =
        itemView.findViewById<TextView>(R.id.timeStamp).apply {
            setTextColor(getColorInt(style.incomingMessageTimeColor))
            if (style.incomingMessageTimeTextSize > 0) {
                setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    itemView.context.resources.getDimension(style.incomingMessageTimeTextSize)
                )
            }
        }
    private val consultAvatar = itemView.findViewById<ImageView>(R.id.consultAvatar).apply {
        layoutParams.height =
            itemView.context.resources.getDimension(style.operatorAvatarSize)
                .toInt()
        layoutParams.width =
            itemView.context.resources.getDimension(style.operatorAvatarSize)
                .toInt()
    }

    init {
        voiceMessage.apply {
            background =
                AppCompatResources.getDrawable(
                    itemView.context,
                    style.incomingMessageBubbleBackground
                )
            setPaddings(true, this)
            setLayoutMargins(true, this)
            background.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    getColorInt(style.incomingMessageBubbleColor),
                    BlendModeCompat.SRC_ATOP
                )
        }
        setTextColorToViews(
            arrayOf(phraseTextView, fileSizeTextView, audioStatusTextView),
            style.incomingMessageTextColor
        )
        ColorsHelper.setTextColor(errorTextView, style.errorMessageTextColor)
    }

    fun onBind(
        consultPhrase: ConsultPhrase,
        highlighted: Boolean,
        formattedDuration: String,
        onLongClick: OnLongClickListener,
        onAvatarClickListener: View.OnClickListener,
        pausePlayClickListener: View.OnClickListener,
        onChangeListener: Slider.OnChangeListener,
        onSliderTouchListener: Slider.OnSliderTouchListener
    ) {
        subscribeForHighlighting(consultPhrase, rootLayout)
        consultAvatar.setOnClickListener(onAvatarClickListener)
        checkText(consultPhrase)

        consultPhrase.fileDescription?.let {
            fileDescription = it
            subscribeForVoiceMessageDownloaded()

            buttonPlayPause.setOnClickListener(pausePlayClickListener)
            slider.addOnChangeListener(onChangeListener)
            slider.addOnSliderTouchListener(onSliderTouchListener)

            this.formattedDuration = formattedDuration
            val viewGroup = itemView as ViewGroup
            for (i in 0 until viewGroup.childCount) {
                viewGroup.getChildAt(i).setOnLongClickListener(onLongClick)
            }
            it.state = AttachmentStateEnum.PENDING
            when (it.state) {
                AttachmentStateEnum.PENDING -> showLoaderLayout(it)
                AttachmentStateEnum.ERROR -> showErrorLayout(it)
                else -> showCommonLayout(consultPhrase)
            }
            fileSizeTextView.text = formattedDuration
            timeStampTextView.text = sdf.format(Date(consultPhrase.timeStamp))
            showAvatar(consultPhrase)
            changeHighlighting(highlighted)
        }
    }

    private fun checkText(consultPhrase: ConsultPhrase) {
        if (!consultPhrase.phraseText.isNullOrBlank()) {
            val extractedLink = UrlUtils.extractLink(consultPhrase.phraseText)
            phraseTextView.visible()
            highlightOperatorText(phraseTextView, consultPhrase, extractedLink?.link)
        } else {
            phraseTextView.gone()
        }
    }

    private fun showAvatar(consultPhrase: ConsultPhrase) {
        if (consultPhrase.isAvatarVisible) {
            consultAvatar.visible()
            consultPhrase.avatarPath?.let {
                consultAvatar.loadImage(
                    FileUtils.convertRelativeUrlToAbsolute(it),
                    listOf(ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.FIT_XY),
                    errorDrawableResId = R.drawable.ecc_operator_avatar_placeholder,
                    modifications = listOf(ImageModifications.CircleCropModification)
                )
            } ?: run {
                consultAvatar.setImageResource(style.defaultOperatorAvatar)
            }
        } else {
            consultAvatar.invisible()
        }
    }

    override fun init(maxValue: Int, progress: Int, isPlaying: Boolean) {
        val effectiveProgress = progress.coerceAtMost(maxValue)
        fileSizeTextView.text = effectiveProgress.formatAsDuration()
        slider.isEnabled = true
        if (maxValue > progress) {
            slider.valueTo = maxValue.toFloat()
            slider.value = effectiveProgress.toFloat()
        }
        buttonPlayPause.setImageResource(if (isPlaying) style.voiceMessagePauseButton else style.voiceMessagePlayButton)
    }

    override fun updateProgress(progress: Int) {
        fileSizeTextView.text = progress.formatAsDuration()
        slider.value = progress.toFloat().coerceAtMost(slider.valueTo)
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

    private fun showLoaderLayout(fileDescription: FileDescription) {
        loader.visible()
        buttonPlayPause.invisible()
        errorTextView.gone()
        audioStatusTextView.text = fileDescription.incomingName
        initAnimation(loader, true)
    }

    private fun showErrorLayout(fileDescription: FileDescription) {
        loader.visible()
        errorTextView.visible()
        buttonPlayPause.invisible()
        loader.setImageResource(getErrorImageResByErrorCode(fileDescription.errorCode))
        audioStatusTextView.text = fileDescription.incomingName
        val errorString = getString(getErrorStringResByErrorCode(fileDescription.errorCode))
        errorTextView.text = errorString
        rotateAnim.cancel()
    }

    private fun showCommonLayout(consultPhrase: ConsultPhrase) {
        buttonPlayPause.visible()
        loader.gone()
        errorTextView.gone()
        rotateAnim.cancel()
        when (consultPhrase.speechStatus) {
            SpeechStatus.NO_SPEECH_STATUS,
            SpeechStatus.SUCCESS
            -> {
                buttonPlayPause.isClickable = true
                buttonPlayPause.alpha = 1f
                audioStatusTextView.invisible()
                fileSizeTextView.visible()
                timeStampTextView.visible()
                slider.isEnabled = true
                slider.setLabelFormatter(VoiceTimeLabelFormatter())
            }
            SpeechStatus.PROCESSING -> {
                buttonPlayPause.isClickable = false
                buttonPlayPause.alpha = 0.3f
                audioStatusTextView.visible()
                fileSizeTextView.invisible()
                timeStampTextView.invisible()
                slider.isEnabled = false
                audioStatusTextView.setText(R.string.ecc_voice_message_is_processing)
            }
            else -> {
                buttonPlayPause.isClickable = false
                buttonPlayPause.alpha = 0.3f
                audioStatusTextView.visible()
                fileSizeTextView.invisible()
                timeStampTextView.invisible()
                slider.isEnabled = false
                audioStatusTextView.setText(R.string.ecc_voice_message_error)
            }
        }
    }
}
