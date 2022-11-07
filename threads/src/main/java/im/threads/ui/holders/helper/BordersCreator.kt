package im.threads.ui.holders.helper

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import im.threads.R
import im.threads.ui.config.Config
import im.threads.ui.widget.textView.BubbleTimeTextView

class BordersCreator(
    private val context: Context,
    private val isIncomingMessage: Boolean
) {
    private val sideSize: Int by lazy {
        val percentage = ResourcesCompat.getFloat(context.resources, R.dimen.imageBubbleSize)
        (Config.getInstance().screenSize.width * percentage).toInt()
    }

    private val bordersSize: BordersSize
        get() {
            val res = context.resources
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

    /**
     * Перемещает временную ветку поверх изображения с учетом крувого padding
     * @param timeLabel временная метка
     */
    fun moveTimeToImageLayout(timeLabel: BubbleTimeTextView) {
        val tag = "moved_to_picture"
        if (timeLabel.tag != tag) {
            val layoutParams = timeLabel.layoutParams as ViewGroup.MarginLayoutParams

            layoutParams.marginEnd += bordersSize.right
            layoutParams.bottomMargin += bordersSize.bottom

            timeLabel.layoutParams = layoutParams
            timeLabel.tag = "moved_to_picture"
        }
    }

    /**
     * Устанавливает отступы в соответствии с шириной бордера для заданной вью
     * @param view вью, для которой нужно установить отступы
     */
    fun addMargins(view: View, parentView: View) {
        val viewLayoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
        viewLayoutParams.setMargins(bordersSize.left, bordersSize.top, bordersSize.right, bordersSize.bottom)
        view.layoutParams = viewLayoutParams

        val parentViewLayoutParams = parentView.layoutParams as ViewGroup.MarginLayoutParams
        parentViewLayoutParams.setMargins(0, bordersSize.top, 0, 0)
        parentView.layoutParams = parentViewLayoutParams
    }

    /**
     * Устанавливает сторону квадрата баббла как 3/4 от ширины экрана
     * @param layout viewGroup, для которого будут установлены новые параметры
     */
    fun <T : ViewGroup> applyViewSize(layout: T, keepHeight: Boolean = false) {
        val lp = layout.layoutParams
        lp.width = sideSize
        lp.height = if (keepHeight) lp.height else sideSize

        layout.layoutParams = lp
    }

    private data class BordersSize(val left: Int, val top: Int, val right: Int, val bottom: Int)
}
