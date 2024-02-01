package im.threads.ui.holders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import im.threads.R
import im.threads.business.models.QuickReplyItem
import im.threads.ui.adapters.ChatAdapter
import im.threads.ui.utils.ScreenSizeGetter
import im.threads.ui.utils.dpToPx
import im.threads.ui.widget.CustomFontTextView
import java.lang.Exception

/**
 * ViewHolder для отображения быстрых ответов
 */
class QuickRepliesViewHolder(val parent: ViewGroup) : BaseHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.ecc_item_quick_replies, parent, false)
) {
    private val chipGroup: Flow = itemView.findViewById(R.id.chipGroup)
    private val rootLayout: ConstraintLayout = itemView.findViewById(R.id.quickRepliesRootLayout)
    private val screenSize = ScreenSizeGetter().getScreenSize(parent.context)
    private val attachedViews = mutableListOf<View>()

    fun bind(quickReplies: QuickReplyItem, callback: ChatAdapter.Callback) {
        removeAllViews()
        for (repl in quickReplies.items) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.ecc_layout_chip, null, false)
            val newChip = view.findViewById<CustomFontTextView>(R.id.chip)
            newChip.text = repl.text
            newChip.setBackgroundResource(style.quickReplyButtonBackground)
            newChip.setTextColor(
                ContextCompat.getColor(
                    parent.context,
                    style.quickReplyTextColor
                )
            )
            newChip.setOnClickListener {
                callback.onQuickReplyClick(repl)
            }
            view.id = View.generateViewId()
            view.rotationY = 180f
            rootLayout.addView(view, attachedViews.size)
            chipGroup.addView(view)
            attachedViews.add(view)

            view.post {
                val margin = parent.context.dpToPx(16)
                val maxViewWidth = screenSize.width - margin * 2
                if (view.width > maxViewWidth) {
                    view.layoutParams.width = maxViewWidth.toInt()
                    view.requestLayout()
                }
            }
        }
    }

    private fun removeAllViews() {
        attachedViews.forEach {
            try { rootLayout.removeView(it) } catch (ignored: Exception) {}
        }
        chipGroup.referencedIds = intArrayOf()
        attachedViews.clear()
    }
}
