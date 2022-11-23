package im.threads.ui.holders

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import im.threads.R
import im.threads.business.models.ChatItem
import im.threads.business.ogParser.OpenGraphParser
import im.threads.ui.holders.helper.BordersCreator
import im.threads.ui.widget.textView.BubbleTimeTextView
import io.reactivex.subjects.PublishSubject

open class BaseImageHolder(
    itemView: View,
    highlightingStream: PublishSubject<ChatItem>?,
    openGraphParser: OpenGraphParser?,
    private val isIncomingMessage: Boolean
) : BaseHolder(
    itemView,
    highlightingStream,
    openGraphParser
) {
    protected val rootLayout: LinearLayout = itemView.findViewById(R.id.rootLayout)
    protected val image: ImageView = itemView.findViewById(R.id.image)
    protected val imageLayout: FrameLayout = itemView.findViewById(R.id.imageLayout)
    protected val timeStampTextView: BubbleTimeTextView = itemView.findViewById<BubbleTimeTextView>(R.id.timeStamp).apply {
        val color = if (isIncomingMessage) style.incomingImageTimeColor else style.outgoingImageTimeColor
        setTextColor(getColorInt(color))

        val textSize = if (isIncomingMessage) style.incomingMessageTimeTextSize else style.outgoingMessageTimeTextSize
        if (textSize > 0) {
            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                itemView.context.resources.getDimension(textSize)
            )
        }

        val timeColor = if (isIncomingMessage) {
            style.incomingImageTimeBackgroundColor
        } else {
            style.outgoingImageTimeBackgroundColor
        }

        background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            getColorInt(timeColor),
            BlendModeCompat.SRC_ATOP
        )
    }

    private val bordersCreator = BordersCreator(itemView.context, isIncomingMessage)

    init {
        bordersCreator.applyViewSize(imageLayout)
        bordersCreator.addMargins(image, imageLayout)

        if (!isIncomingMessage) {
            val resources = itemView.context.resources
            (rootLayout.layoutParams as MarginLayoutParams).let {
                it.marginEnd = resources.getDimensionPixelSize(R.dimen.user_margin_right)
                rootLayout.layoutParams = it
            }
            rootLayout.invalidate()
            rootLayout.requestLayout()
        }
    }

    fun moveTimeToImageLayout() {
        bordersCreator.moveTimeToImageLayout(timeStampTextView)
    }
}
