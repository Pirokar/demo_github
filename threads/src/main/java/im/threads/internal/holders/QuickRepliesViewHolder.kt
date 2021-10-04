package im.threads.internal.holders

import android.view.Gravity
import android.view.ViewGroup
import android.view.LayoutInflater
import com.google.android.flexbox.FlexboxLayout
import im.threads.R
import com.google.android.material.chip.Chip
import im.threads.internal.adapters.ChatAdapter
import im.threads.internal.model.QuickReplyItem

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
            val newChip = view.findViewById<Chip>(R.id.chip)
            newChip.gravity = Gravity.END
            newChip.text = repl.text
            newChip.setOnClickListener {
                callback.onQiuckReplyClick(repl)
            }
            chipGroup.addView(view)
        }
    }
}