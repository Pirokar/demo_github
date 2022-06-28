package im.threads.internal.adapters.files_and_media

import androidx.recyclerview.widget.DiffUtil
import im.threads.internal.model.DateRow
import im.threads.internal.model.FileAndMediaItem
import im.threads.internal.model.MediaAndFileItem

class FilesAndMediaDiffUtil(
    private val oldList: List<MediaAndFileItem>,
    private val newList: List<MediaAndFileItem>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].timeStamp == newList[newItemPosition].timeStamp
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[oldItemPosition]

        return when (oldItem) {
            is DateRow -> {
                newItem is DateRow && oldItem.isTheSameItem(newItem)
            }
            is FileAndMediaItem -> {
                newItem is FileAndMediaItem && oldItem.fileName == newItem.fileName
            }
            else -> false
        }
    }
}
