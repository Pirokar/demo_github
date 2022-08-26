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
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.isVisible
import com.google.android.material.slider.Slider
import im.threads.ChatStyle
import im.threads.R
import im.threads.internal.Config
import im.threads.internal.formatters.SpeechStatus
import im.threads.internal.imageLoading.ImageLoader
import im.threads.internal.imageLoading.ImageModifications
import im.threads.internal.imageLoading.loadImage
import im.threads.internal.model.AttachmentStateEnum
import im.threads.internal.model.ConsultPhrase
import im.threads.internal.model.FileDescription
import im.threads.internal.utils.FileUtils
import im.threads.internal.views.VoiceTimeLabelFormatter
import im.threads.internal.views.formatAsDuration
import im.threads.internal.widget.text_view.QuoteMessageTextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConsultVoiceMessageViewHolder(parent: ViewGroup) : VoiceMessageBaseHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_consult_voice_message, parent, false)
) {
    private val style: ChatStyle = Config.instance.chatStyle
    private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

    private var fileDescription: FileDescription? = null
    private var formattedDuration = ""

    private val errorTextView: TextView = itemView.findViewById(R.id.errorText)
    private val loader: ImageView = itemView.findViewById(R.id.loader)
    private val rootLayout: LinearLayout = itemView.findViewById(R.id.rootLayout)

    private val phraseTextView =
        itemView.findViewById<QuoteMessageTextView>(R.id.voiceMessageConsultText).apply {
            setLinkTextColor(getColorInt(style.incomingMessageTextColor))
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
        consultAvatar.setOnClickListener(onAvatarClickListener)
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

            phraseTextView.isVisible = false
            if (consultPhrase.phraseText?.trim()?.isNotEmpty() == true) {
                phraseTextView.isVisible = true
                highlightOperatorText(phraseTextView, consultPhrase)
            }

            fileSizeTextView.text = formattedDuration
            timeStampTextView.text = sdf.format(Date(consultPhrase.timeStamp))
            showAvatar(consultPhrase)

            rootLayout.apply {
                setBackgroundColor(
                    ContextCompat.getColor(
                        viewGroup.context,
                        if (highlighted) style.chatHighlightingColor else R.color.threads_transparent
                    )
                )
            }
        }
    }

    private fun showAvatar(consultPhrase: ConsultPhrase) {
        if (consultPhrase.isAvatarVisible) {
            consultAvatar.isVisible = true
            consultPhrase.avatarPath?.let {
                consultAvatar.loadImage(
                    FileUtils.convertRelativeUrlToAbsolute(it),
                    listOf(ImageView.ScaleType.FIT_XY, ImageView.ScaleType.CENTER_INSIDE),
                    modifications = listOf(ImageModifications.CircleCropModification),
                    callback = object : ImageLoader.ImageLoaderCallback {
                        override fun onImageLoaded() {
                            consultAvatar.setImageResource(style.defaultOperatorAvatar)
                        }
                    }
                )
            } ?: run {
                consultAvatar.setImageResource(style.defaultOperatorAvatar)
            }
        } else {
            consultAvatar.isVisible = false
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
        loader.isVisible = true
        buttonPlayPause.isVisible = false
        errorTextView.isVisible = false
        audioStatusTextView.text = fileDescription.incomingName
        initAnimation(loader, true)
    }

    private fun showErrorLayout(fileDescription: FileDescription) {
        loader.isVisible = true
        errorTextView.isVisible = true
        buttonPlayPause.isVisible = false
        loader.setImageResource(getErrorImageResByErrorCode(fileDescription.errorCode))
        audioStatusTextView.text = fileDescription.incomingName
        val errorString = getString(getErrorStringResByErrorCode(fileDescription.errorCode))
        errorTextView.text = errorString
        rotateAnim.cancel()
    }

    private fun showCommonLayout(consultPhrase: ConsultPhrase) {
        buttonPlayPause.isVisible = true
        loader.isVisible = false
        errorTextView.isVisible = false
        rotateAnim.cancel()
        when (consultPhrase.speechStatus) {
            SpeechStatus.SUCCESS -> {
                buttonPlayPause.isClickable = true
                buttonPlayPause.alpha = 1f
                audioStatusTextView.isVisible = false
                fileSizeTextView.isVisible = true
                slider.isVisible = false
                slider.setLabelFormatter(VoiceTimeLabelFormatter())
            }
            SpeechStatus.PROCESSING -> {
                buttonPlayPause.isClickable = false
                buttonPlayPause.alpha = 0.3f
                audioStatusTextView.isVisible = true
                fileSizeTextView.isVisible = false
                slider.isVisible = false
                audioStatusTextView.setText(R.string.threads_voice_message_is_processing)
            }
            else -> {
                buttonPlayPause.isClickable = false
                buttonPlayPause.alpha = 0.3f
                audioStatusTextView.isVisible = true
                fileSizeTextView.isVisible = false
                slider.isVisible = false
                audioStatusTextView.setText(R.string.threads_voice_message_error)
            }
        }
    }
}
