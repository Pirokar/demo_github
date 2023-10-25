package im.threads.ui.holders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.flexbox.FlexboxLayout
import im.threads.R
import im.threads.business.models.QuickReplyItem
import im.threads.ui.adapters.ChatAdapter
import im.threads.ui.widget.CustomFontTextView

/**
 * ViewHolder для отображения быстрых ответов
 */
class QuickRepliesViewHolder(val parent: ViewGroup) : BaseHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.ecc_item_quick_replies, parent, false)
) {
    private val chipGroup: FlexboxLayout = itemView.findViewById(R.id.chipGroup)

    fun bind(quickReplies: QuickReplyItem, callback: ChatAdapter.Callback) {
        chipGroup.removeAllViews()
        for (repl in quickReplies.items) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.ecc_layout_chip, null, false)
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
            chipGroup.addView(view)
        }
    }
}