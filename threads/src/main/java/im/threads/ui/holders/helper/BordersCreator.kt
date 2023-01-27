package im.threads.ui.holders.helper

import android.content.Context
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import im.threads.R
import im.threads.ui.config.Config
import im.threads.ui.widget.textView.BubbleTimeTextView

class BordersCreator(
    private val context: Context,
    private val isIncomingMessage: Boolean
) {
    val sideSize: Int by lazy {
        val percentage = Config.getInstance().getChatStyle().imageBubbleSize
        (Config.getInstance().screenSize.width * percentage).toInt()
    }

    private val borders: BordersSize
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
            val layoutParams = timeLabel.layoutParams as MarginLayoutParams

            layoutParams.marginEnd += borders.right
            layoutParams.bottomMargin += borders.bottom

            timeLabel.layoutParams = layoutParams
            timeLabel.tag = "moved_to_picture"
        }
    }

    /**
     * Устанавливает отступы в соответствии с шириной бордера для заданной вью
     * @param view вью, для которой нужно установить отступы
     * @param parentView родительское вью для равномерного установления отступов
     * @param rootView используется для проверки минимального отступа между сообщениями
     */
    fun addMargins(view: View, parentView: View, rootView: View) {
        val resources = context.resources
        val chatStyle = Config.getInstance().getChatStyle()

        val viewLayoutParams = view.layoutParams as MarginLayoutParams
        viewLayoutParams.setMargins(borders.left, borders.top, borders.right, borders.bottom)
        view.layoutParams = viewLayoutParams
        view.invalidate()
        view.requestLayout()

        val parentViewLayoutParams = parentView.layoutParams as MarginLayoutParams
        val defaultLeft = if (isIncomingMessage) {
            resources.getDimensionPixelSize(R.dimen.ecc_margin_eight)
        } else {
            0
        }
        parentViewLayoutParams.setMargins(
            if (borders.left > 0) 0 else defaultLeft,
            borders.top,
            0,
            0
        )
        parentView.layoutParams = parentViewLayoutParams
        parentView.invalidate()
        parentView.requestLayout()

        val minimumVerticalMargin = resources.getDimensionPixelSize(R.dimen.ecc_margin_quarter)
        val rootViewLayoutParams = rootView.layoutParams as MarginLayoutParams
        if (borders.top < minimumVerticalMargin) {
            rootViewLayoutParams.topMargin = minimumVerticalMargin - borders.top
        }
        if (borders.bottom < minimumVerticalMargin) {
            rootViewLayoutParams.bottomMargin = minimumVerticalMargin - borders.bottom
        }
    }

    data class BordersSize(val left: Int, val top: Int, val right: Int, val bottom: Int)
}
