package im.threads.internal.holders

import android.graphics.PorterDuff
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.google.android.material.slider.Slider
import im.threads.R
import im.threads.business.formatters.SpeechStatus
import im.threads.business.imageLoading.ImageModifications
import im.threads.business.imageLoading.loadImage
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.FileDescription
import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.utils.FileUtils
import im.threads.internal.utils.gone
import im.threads.internal.utils.visible
import im.threads.ui.views.VoiceTimeLabelFormatter
import im.threads.ui.views.formatAsDuration
import im.threads.ui.widget.textView.QuoteMessageTextView
import io.reactivex.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConsultVoiceMessageViewHolder(
    parent: ViewGroup,
    highlightingStream: PublishSubject<ChatItem>
) : VoiceMessageBaseHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_consult_voice_message, parent, false),
    highlightingStream
) {
    private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

    private var fileDescription: FileDescription? = null
    private var formattedDuration = ""

    private val errorTextView: TextView = itemView.findViewById(R.id.errorText)
    private val loader: ImageView = itemView.findViewById(R.id.loader)
    private val rootLayout: LinearLayout = itemView.findViewById(R.id.rootLayout)

    private val phraseTextView =
        itemView.findViewById<QuoteMessageTextView>(R.id.voiceMessageConsultText).apply {
            setLinkTextColor(getColorInt(style.incomingMessageLinkColor))
        }
    private val slider: Slider = itemView.findViewById(R.id.voiceMessageConsultSlider)
    private val buttonPlayPause =
        itemView.findViewById<ImageView>(R.id.voiceMessageConsultButtonPlayPause).apply {
            setColorFilter(
                getColorInt(style.incomingPlayPauseButtonColor),
                PorterDuff.Mode.SRC_ATOP
            )
        }
    private val fileSizeTextView: TextView = itemView.findViewById(R.id.file_size)
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
        itemView.findViewById<View>(R.id.bubble).apply {
            background =
                AppCompatResources.getDrawable(
                    itemView.context,
                    style.incomingMessageBubbleBackground
                )
            setPadding(
                context.resources.getDimensionPixelSize(style.bubbleIncomingPaddingLeft),
                context.resources.getDimensionPixelSize(style.bubbleIncomingPaddingTop),
                context.resources.getDimensionPixelSize(style.bubbleIncomingPaddingRight),
                context.resources.getDimensionPixelSize(style.bubbleIncomingPaddingBottom)
            )
            background.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    getColorInt(style.incomingMessageBubbleColor),
                    BlendModeCompat.SRC_ATOP
                )
            val bubbleLeftMarginDp = itemView.context.resources.getDimension(R.dimen.margin_quarter)
            val bubbleLeftMarginPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                bubbleLeftMarginDp,
                itemView.resources.displayMetrics
            ).toInt()
            val lp = layoutParams as RelativeLayout.LayoutParams
            lp.setMargins(bubbleLeftMarginPx, lp.topMargin, lp.rightMargin, lp.bottomMargin)
            layoutParams = lp
        }
        setTextColorToViews(
            arrayOf(phraseTextView, fileSizeTextView, audioStatusTextView),
            style.incomingMessageTextColor
        )
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
            buttonPlayPause.setOnClickListener(pausePlayClickListener)
            slider.addOnChangeListener(onChangeListener)
            slider.addOnSliderTouchListener(onSliderTouchListener)

            this.formattedDuration = formattedDuration
            val viewGroup = itemView as ViewGroup
            for (i in 0 until viewGroup.childCount) {
                viewGroup.getChildAt(i).setOnLongClickListener(onLongClick)
            }
            when (it.state) {
                AttachmentStateEnum.PENDING -> {
                    showLoaderLayout(it)
                }
                AttachmentStateEnum.ERROR -> {
                    showErrorLayout(it)
                }
                else -> {
                    showCommonLayout(consultPhrase)
                }
            }

            fileSizeTextView.text = formattedDuration
            timeStampTextView.text = sdf.format(Date(consultPhrase.timeStamp))
            showAvatar(consultPhrase)

            changeHighlighting(highlighted)
        }
    }

    private fun checkText(consultPhrase: ConsultPhrase) {
        if (!consultPhrase.phraseText.isNullOrBlank()) {
            phraseTextView.visible()
            highlightOperatorText(phraseTextView, consultPhrase)
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
                    listOf(ImageView.ScaleType.FIT_XY, ImageView.ScaleType.CENTER_INSIDE),
                    modifications = listOf(ImageModifications.CircleCropModification)
                )
            } ?: run {
                consultAvatar.setImageResource(style.defaultOperatorAvatar)
            }
        } else {
            consultAvatar.gone()
        }
    }

    override fun getFileDescription(): FileDescription? {
        return fileDescription
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
        buttonPlayPause.gone()
        errorTextView.gone()
        audioStatusTextView.text = fileDescription.incomingName
        initAnimation(loader, true)
    }

    private fun showErrorLayout(fileDescription: FileDescription) {
        loader.visible()
        errorTextView.visible()
        buttonPlayPause.gone()
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
            SpeechStatus.SUCCESS -> {
                buttonPlayPause.isClickable = true
                buttonPlayPause.alpha = 1f
                audioStatusTextView.gone()
                fileSizeTextView.visible()
                slider.gone()
                slider.setLabelFormatter(VoiceTimeLabelFormatter())
            }
            SpeechStatus.PROCESSING -> {
                buttonPlayPause.isClickable = false
                buttonPlayPause.alpha = 0.3f
                audioStatusTextView.visible()
                fileSizeTextView.gone()
                slider.gone()
                audioStatusTextView.setText(R.string.threads_voice_message_is_processing)
            }
            else -> {
                buttonPlayPause.isClickable = false
                buttonPlayPause.alpha = 0.3f
                audioStatusTextView.visible()
                fileSizeTextView.gone()
                slider.gone()
                audioStatusTextView.setText(R.string.threads_voice_message_error)
            }
        }
    }
}
