package im.threads.internal.holders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.flexbox.FlexboxLayout
import im.threads.R
import im.threads.internal.adapters.ChatAdapter
import im.threads.internal.model.QuickReplyItem
import im.threads.internal.widget.CustomFontTextView

/**
 * ViewHolder для отображения быстрых ответов
 */
class QuickRepliesViewHolder(val parent: ViewGroup) : BaseHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_quick_replies, parent, false)
) {
    private val chipGroup: FlexboxLayout = itemView.findViewById(R.id.chipGroup)

    fun bind(quickReplies: QuickReplyItem, callback: ChatAdapter.Callback) {
        chipGroup.removeAllViews()
        for (repl in quickReplies.items) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.layout_chip, null, false)
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
                callback.onQiuckReplyClick(repl)
            }
            chipGroup.addView(view)
        }
    }
}
