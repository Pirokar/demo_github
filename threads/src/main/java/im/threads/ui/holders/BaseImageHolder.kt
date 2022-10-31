package im.threads.ui.holders

import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import im.threads.R
import im.threads.business.models.ChatItem
import im.threads.business.ogParser.OpenGraphParser
import im.threads.ui.config.Config
import io.reactivex.subjects.PublishSubject

open class BaseImageHolder(
    itemView: View,
    private val highlightingStream: PublishSubject<ChatItem>?,
    private val openGraphParser: OpenGraphParser?,
    private val isIncomingMessage: Boolean
) : BaseHolder(
    itemView,
    highlightingStream,
    openGraphParser
) {
    protected val image: ImageView = itemView.findViewById<ImageView>(R.id.image)
    protected val imageBackground: FrameLayout = itemView.findViewById(R.id.imageBackground)
    protected val commonLayout: FrameLayout = itemView.findViewById<FrameLayout>(R.id.commonLayout).also {
        applyCommonLayoutParams(it)
    }
    protected val timeStampTextView: TextView = itemView.findViewById<TextView>(R.id.timeStamp).apply {
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

    private val bordersSize: Int
        get() {
            val bordersSizeDimen = if (isIncomingMessage) {
                Config.getInstance().getChatStyle().incomingImageBorderSize
            } else {
                Config.getInstance().getChatStyle().outgoingImageBorderSize
            }
            return itemView.context.resources.getDimensionPixelSize(bordersSizeDimen)
        }

    init {
        addPadding(image)
    }

    /**
     * Добавляет padding со всех сторон для входящей вью
     * @param view вью, для которой необходимо добавить padding
     */
    protected fun addPadding(view: View) {
        view.setPadding(bordersSize, bordersSize, bordersSize, bordersSize)
    }

    /**
     * Перемещает временную ветку поверх изображения с учетом крувого padding
     */
    protected fun moveTimeToCommonLayout() {
        val layoutParams = timeStampTextView.layoutParams as FrameLayout.LayoutParams

        layoutParams.marginEnd += bordersSize
        layoutParams.bottomMargin += bordersSize

        timeStampTextView.layoutParams = layoutParams
    }

    private fun applyCommonLayoutParams(layout: FrameLayout) {
        val bubbleLeftMarginDp = itemView.context.resources.getDimension(R.dimen.margin_quarter)
        val bubbleLeftMarginPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            bubbleLeftMarginDp,
            itemView.resources.displayMetrics
        ).toInt()
        val lp = layout.layoutParams as FrameLayout.LayoutParams
        lp.setMargins(bubbleLeftMarginPx, lp.topMargin, lp.rightMargin, lp.bottomMargin)
        layout.layoutParams = lp
    }
}
