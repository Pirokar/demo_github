package im.threads.ui.holders

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import im.threads.R
import im.threads.business.models.MessageStatus
import im.threads.business.models.Survey
import im.threads.ui.utils.setColorFilter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewHolder для результатов бинарного опроса
 */
class RatingThumbsSentViewHolder(parent: ViewGroup) : BaseHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.ecc_item_rate_thumbs_sent, parent, false),
    null,
    null
) {
    private val thumb: ImageView = itemView.findViewById(R.id.thumb)
    private val header: TextView = itemView.findViewById(R.id.header)
    private val timeStampTextView: TextView = itemView.findViewById(R.id.timestamp)
    private val sdf: SimpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val bubble: ViewGroup = itemView.findViewById(R.id.bubble)

    init {
        bubble.background =
            AppCompatResources.getDrawable(itemView.context, style.outgoingMessageBubbleBackground)
        setPaddings(false, bubble)
        setLayoutMargins(false, bubble)
        bubble.background.setColorFilter(getColorInt(style.outgoingMessageBubbleColor))
        setTextColorToViews(arrayOf(header), style.outgoingMessageTextColor)
        timeStampTextView.setTextColor(getColorInt(style.outgoingMessageTimeColor))
    }

    fun bind(survey: Survey) {
        if (survey.isRead) {
            survey.sentState = MessageStatus.READ
        }
        val rate = survey.questions[0].rate
        if (rate == 1) {
            thumb.setImageResource(style.binarySurveyLikeSelectedIconResId)
        } else {
            thumb.setImageResource(style.binarySurveyDislikeSelectedIconResId)
        }
        thumb.setColorFilter(ContextCompat.getColor(itemView.context, style.outgoingMessageTextColor), PorterDuff.Mode.SRC_ATOP)
        header.text = survey.questions[0].text
        timeStampTextView.text = sdf.format(Date(survey.timeStamp))
        val drawable: Drawable?
        when (survey.sentState ?: MessageStatus.FAILED) {
            MessageStatus.SENDING -> {
                drawable = AppCompatResources.getDrawable(itemView.context, style.messageSendingIconResId)
                drawable?.setColorFilter(ContextCompat.getColor(itemView.context, style.messageSendingIconColorResId))
                timeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
            }
            MessageStatus.SENT -> {
                drawable = AppCompatResources.getDrawable(itemView.context, style.messageSentIconResId)
                drawable?.setColorFilter(ContextCompat.getColor(itemView.context, style.messageSentIconColorResId))
                timeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
            }
            MessageStatus.DELIVERED -> {
                drawable = AppCompatResources.getDrawable(itemView.context, style.messageDeliveredIconResId)
                drawable?.setColorFilter(ContextCompat.getColor(itemView.context, style.messageDeliveredIconColorResId))
                timeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
            }
            MessageStatus.READ -> {
                drawable = AppCompatResources.getDrawable(itemView.context, style.messageReadIconResId)
                drawable?.setColorFilter(ContextCompat.getColor(itemView.context, style.messageReadIconColorResId))
                timeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
            }
            MessageStatus.FAILED -> {
                drawable = AppCompatResources.getDrawable(itemView.context, style.messageFailedIconResId)
                drawable?.setColorFilter(ContextCompat.getColor(itemView.context, style.messageFailedIconColorResId))
                timeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
            }
        }
    }
}