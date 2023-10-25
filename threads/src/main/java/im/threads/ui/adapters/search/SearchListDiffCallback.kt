package im.threads.ui.adapters.search

import androidx.recyclerview.widget.DiffUtil
import im.threads.business.models.MessageFromHistory

class SearchListDiffCallback(
    private val oldList: List<MessageFromHistory>,
    private val newList: List<MessageFromHistory>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].isTheSameItem(newList[newItemPosition])
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
