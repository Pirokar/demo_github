package im.threads.internal.holders

import android.graphics.PorterDuff
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.text.util.LinkifyCompat
import com.google.android.material.slider.Slider
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import im.threads.ChatStyle
import im.threads.R
import im.threads.internal.Config
import im.threads.internal.formatters.SpeechStatus
import im.threads.internal.markdown.MarkdownProcessorHolder
import im.threads.internal.model.ConsultPhrase
import im.threads.internal.model.FileDescription
import im.threads.internal.utils.CircleTransformation
import im.threads.internal.utils.FileUtils.convertRelativeUrlToAbsolute
import im.threads.internal.utils.UrlUtils
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

    private val phraseTextView =
        itemView.findViewById<QuoteMessageTextView>(R.id.voice_message_consult_text).apply {
            setLinkTextColor(getColorInt(style.incomingMessageLinkColor))
            movementMethod = LinkMovementMethod.getInstance()
        }
    private val slider: Slider = itemView.findViewById(R.id.voice_message_consult_slider)
    private val buttonPlayPause =
        itemView.findViewById<ImageView>(R.id.voice_message_consult_button_play_pause).apply {
            setColorFilter(
                getColorInt(style.incomingPlayPauseButtonColor),
                PorterDuff.Mode.SRC_ATOP
            )
        }
    private val fileSizeTextView: TextView = itemView.findViewById(R.id.file_size)
    private val audioStatusTextView: TextView =
        itemView.findViewById(R.id.voice_message_consult_audio_status)
    private val mTimeStampTextView =
        itemView.findViewById<TextView>(R.id.timestamp).apply {
            setTextColor(getColorInt(style.incomingMessageTimeColor))
        }
    private val filterView = itemView.findViewById<View>(R.id.filter).apply {
        setBackgroundColor(
            ContextCompat.getColor(
                itemView.context,
                style.chatHighlightingColor
            )
        )
    }
    private val secondFilterView = itemView.findViewById<View>(R.id.filter_second).apply {
        setBackgroundColor(
            ContextCompat.getColor(
                itemView.context,
                style.chatHighlightingColor
            )
        )
    }
    private val mConsultAvatar = itemView.findViewById<ImageView>(R.id.consult_avatar).apply {
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
            background.setColorFilter(
                getColorInt(style.incomingMessageBubbleColor),
                PorterDuff.Mode.SRC_ATOP
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
            arrayOf(phraseTextView, fileSizeTextView),
            style.incomingMessageTextColor
        )
        itemView.findViewById<View>(R.id.voice_message_consult_delimiter)
            .setBackgroundColor(getColorInt(style.chatToolbarColorResId))
    }

    fun onBind(
        consultPhrase: ConsultPhrase,
        formattedDuration: String,
        onLongClick: OnLongClickListener,
        onAvatarClickListener: View.OnClickListener,
        pausePlayClickListener: View.OnClickListener,
        onChangeListener: Slider.OnChangeListener,
        onSliderTouchListener: Slider.OnSliderTouchListener
    ) {
        this.fileDescription = consultPhrase.fileDescription
        if (this.fileDescription == null) return
        mConsultAvatar.setOnClickListener(onAvatarClickListener)
        this.formattedDuration = formattedDuration
        fileSizeTextView.text = formattedDuration
        mTimeStampTextView.text = sdf.format(Date(consultPhrase.timeStamp))
        val vg = itemView as ViewGroup
        for (i in 0 until vg.childCount) {
            vg.getChildAt(i).setOnLongClickListener(onLongClick)
        }
        when (consultPhrase.speechStatus) {
            SpeechStatus.SUCCESS -> {
                buttonPlayPause.setOnClickListener(pausePlayClickListener)
                buttonPlayPause.isClickable = true
                buttonPlayPause.alpha = 1f
                audioStatusTextView.visibility = View.GONE
                fileSizeTextView.visibility = View.VISIBLE
                slider.addOnChangeListener(onChangeListener)
                slider.addOnSliderTouchListener(onSliderTouchListener)
                slider.setLabelFormatter(VoiceTimeLabelFormatter())
                slider.visibility = View.VISIBLE
            }
            SpeechStatus.PROCESSING -> {
                buttonPlayPause.isClickable = false
                buttonPlayPause.alpha = 0.3f
                audioStatusTextView.visibility = View.VISIBLE
                audioStatusTextView.setText(R.string.threads_voice_message_is_processing)
                fileSizeTextView.visibility = View.GONE
                slider.visibility = View.GONE
            }
            else -> {
                buttonPlayPause.isClickable = false
                buttonPlayPause.alpha = 0.3f
                audioStatusTextView.visibility = View.VISIBLE
                audioStatusTextView.setText(R.string.threads_voice_message_error)
                fileSizeTextView.visibility = View.GONE
                slider.visibility = View.GONE
            }
        }
        if (consultPhrase.isAvatarVisible) {
            mConsultAvatar.visibility = View.VISIBLE
            @DrawableRes val resID = style.defaultOperatorAvatar
            if (consultPhrase.avatarPath != null) {
                Picasso.get()
                    .load(convertRelativeUrlToAbsolute(consultPhrase.avatarPath))
                    .fit()
                    .noPlaceholder()
                    .transform(CircleTransformation())
                    .into(mConsultAvatar, object : Callback {
                        override fun onSuccess() {}
                        override fun onError(e: Exception) {
                            Picasso.get()
                                .load(resID)
                                .fit()
                                .noPlaceholder()
                                .transform(CircleTransformation())
                                .into(mConsultAvatar)
                        }
                    })
            } else {
                Picasso.get()
                    .load(resID)
                    .fit()
                    .noPlaceholder()
                    .transform(CircleTransformation())
                    .into(mConsultAvatar)
            }
        } else {
            mConsultAvatar.visibility = View.INVISIBLE
        }
        filterView.visibility = if (consultPhrase.isChosen) View.VISIBLE else View.INVISIBLE
        secondFilterView.visibility = if (consultPhrase.isChosen) View.VISIBLE else View.INVISIBLE
        val phrase = consultPhrase.phrase?.trim()
        if (phrase == null) {
            phraseTextView.visibility = View.GONE
        } else {
            phraseTextView.visibility = View.VISIBLE
            val url = UrlUtils.extractLink(phrase)
            when {
                consultPhrase.formattedPhrase != null -> {
                    phraseTextView.autoLinkMask = 0
                    phraseTextView.text = MarkdownProcessorHolder.getMarkdownProcessor()
                        .parse(consultPhrase.formattedPhrase.trim { it <= ' ' })
                }
                url != null -> {
                    val text = SpannableString(phrase)
                    LinkifyCompat.addLinks(text, UrlUtils.WEB_URL, "")
                    phraseTextView.text = text
                    phraseTextView.movementMethod = LinkMovementMethod.getInstance()
                }
                else -> {
                    phraseTextView.text = phrase
                }
            }
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
}
