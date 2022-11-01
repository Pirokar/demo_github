package im.threads.ui.holders

import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
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
    highlightingStream: PublishSubject<ChatItem>?,
    openGraphParser: OpenGraphParser?,
    private val isIncomingMessage: Boolean
) : BaseHolder(
    itemView,
    highlightingStream,
    openGraphParser
) {
    private val sideSize: Int by lazy {
        (Config.getInstance().screenSize.width / 3) * 2
    }
    protected val image: ImageView = itemView.findViewById<ImageView>(R.id.image)
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

    private val bordersSize: BordersSize
        get() {
            val res = itemView.context.resources
            val bordersSize = if (isIncomingMessage) {
                BordersSize(
                    res.getDimensionPixelSize(Config.getInstance().getChatStyle().incomingImageLeftBorderSize),
                    res.getDimensionPixelSize(Config.getInstance().getChatStyle().incomingImageTopBorderSize),
                    res.getDimensionPixelSize(Config.getInstance().getChatStyle().incomingImageRightBorderSize),
                    res.getDimensionPixelSize(Config.getInstance().getChatStyle().incomingImageBottomBorderSize)
                )
            } else {
                BordersSize(
                    res.getDimensionPixelSize(Config.getInstance().getChatStyle().outgoingImageLeftBorderSize),
                    res.getDimensionPixelSize(Config.getInstance().getChatStyle().outgoingImageTopBorderSize),
                    res.getDimensionPixelSize(Config.getInstance().getChatStyle().outgoingImageRightBorderSize),
                    res.getDimensionPixelSize(Config.getInstance().getChatStyle().outgoingImageBottomBorderSize)
                )
            }
            return bordersSize
        }

    init {
        addPadding(image)
    }

    /**
     * Перемещает временную ветку поверх изображения с учетом крувого padding
     */
    protected fun moveTimeToCommonLayout() {
        val tag = "moved_to_picture"
        if (timeStampTextView.tag != tag) {
            val layoutParams = timeStampTextView.layoutParams as RelativeLayout.LayoutParams
            val additionalMarginRight = if (isIncomingMessage) {
                0
            } else {
                itemView.context.resources.getDimensionPixelSize(R.dimen.timeLabelOutgoingExtraMarginRight)
            }

            layoutParams.marginEnd += bordersSize.right
            layoutParams.bottomMargin += bordersSize.bottom

            timeStampTextView.layoutParams = layoutParams
            timeStampTextView.tag = "moved_to_picture"
        }
    }

    private fun addPadding(view: View) {
        val layoutParams = view.layoutParams as FrameLayout.LayoutParams
        layoutParams.setMargins(bordersSize.left, bordersSize.top, bordersSize.right, bordersSize.bottom)
        view.layoutParams = layoutParams
    }

    /**
     * Устанавливает сторону квадрата баббла как 2/3 от ширины экрана
     */
    private fun applyCommonLayoutParams(layout: FrameLayout) {
        val lp = layout.layoutParams as RelativeLayout.LayoutParams
        lp.width = sideSize
        lp.height = sideSize

        layout.layoutParams = lp
    }

    private data class BordersSize(val left: Int, val top: Int, val right: Int, val bottom: Int)
}
