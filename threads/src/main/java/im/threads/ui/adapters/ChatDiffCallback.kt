package im.threads.ui.adapters

import androidx.core.util.ObjectsCompat
import androidx.recyclerview.widget.DiffUtil
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultPhrase

class ChatDiffCallback(private val oldList: List<ChatItem>, private val newList: List<ChatItem>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].isTheSameItem(newList[newItemPosition])
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        val isAvatarsVisibilityTheSame = if (oldItem is ConsultPhrase && newItem is ConsultPhrase) {
            oldItem.isAvatarVisible == newItem.isAvatarVisible
        } else {
            true
        }
        return ObjectsCompat.equals(oldList[oldItemPosition], newList[newItemPosition]) && isAvatarsVisibilityTheSame
    }
}
