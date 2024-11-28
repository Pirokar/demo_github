package edna.chatcenter.demo.appCode.adapters.logList

import androidx.recyclerview.widget.DiffUtil
import edna.chatcenter.demo.appCode.models.LogModel

class LogListDiffCallback(
    private val oldList: List<LogModel>,
    private val newList: List<LogModel>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem == newItem
    }
}
