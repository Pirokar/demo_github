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
 * ViewHolder для результатов опроса с рейтингом
 */
class RatingStarsSentViewHolder(parent: ViewGroup) : BaseHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.ecc_item_rate_stars_sent, parent, false),
    null,
    null
) {
    private val star: ImageView = itemView.findViewById(R.id.star)
    private val mHeader: TextView = itemView.findViewById(R.id.header)
    private val rateStarsCount: TextView = itemView.findViewById(R.id.rate_stars_count)
    private val from: TextView = itemView.findViewById(R.id.from)
    private val totalStarsCount: TextView = itemView.findViewById(R.id.total_stars_count)
    private val timeStampTextView: TextView = itemView.findViewById(R.id.timestamp)
    private val sdf: SimpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val mBubble: ViewGroup = itemView.findViewById(R.id.bubble)

    init {
        rateStarsCount.setTextColor(getColorInt(style.outgoingMessageBubbleColor))
        mBubble.background =
            AppCompatResources.getDrawable(itemView.context, style.outgoingMessageBubbleBackground)
        setPaddings(false, mBubble)
        setLayoutMargins(false, mBubble)
        mBubble.background.setColorFilter(ContextCompat.getColor(itemView.context, style.outgoingMessageBubbleColor))
        setTextColorToViews(arrayOf(mHeader, from, totalStarsCount), style.outgoingMessageTextColor)
        timeStampTextView.setTextColor(getColorInt(style.outgoingMessageTimeColor))
        star.setColorFilter(ContextCompat.getColor(itemView.context, style.outgoingMessageTextColor), PorterDuff.Mode.SRC_ATOP)
        star.setImageResource(style.optionsSurveySelectedIconResId)
    }

    fun bind(survey: Survey) {
        val rate = survey.questions[0].rate
        val scale = survey.questions[0].scale
        rateStarsCount.text = rate.toString()
        totalStarsCount.text = scale.toString()
        timeStampTextView.text = sdf.format(Date(survey.timeStamp))
        mHeader.text = survey.questions[0].text
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
