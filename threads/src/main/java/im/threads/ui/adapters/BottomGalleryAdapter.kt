package im.threads.ui.adapters

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import im.threads.R
import im.threads.business.config.BaseConfig
import im.threads.business.utils.Balloon.show
import im.threads.business.utils.FileUtils
import im.threads.ui.config.Config
import im.threads.ui.holders.BottomGalleryImageHolder
import im.threads.ui.models.BottomGalleryItem
import im.threads.ui.utils.FileHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

internal class BottomGalleryAdapter(
    private val list: List<BottomGalleryItem>,
    private val onChooseItemsListener: OnChooseItemsListener?
) : RecyclerView.Adapter<BottomGalleryImageHolder>() {
    private val chosenItems: MutableList<Uri> = ArrayList()
    private val config = Config.getInstance()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomGalleryImageHolder {
        context = parent.context
        return BottomGalleryImageHolder(parent)
    }

    override fun onBindViewHolder(holder: BottomGalleryImageHolder, position: Int) {
        val item = list[position]
        coroutineScope.launch {
            item.isSendAllowed = async(Dispatchers.IO) { isSendingAllowed(item) }.await()
            holder.onBind(list[position]) {
                if (!item.isSendAllowed) return@onBind
                if (!item.isChosen && chosenItems.size >= config.chatStyle.getMaxGalleryImagesCount(BaseConfig.getInstance().context)) {
                    show(
                        holder.itemView.context,
                        holder.itemView.context.getString(R.string.ecc_achieve_images_count_limit_message)
                    )
                    return@onBind
                }
                item.isChosen = !item.isChosen
                notifyItemChanged(holder.adapterPosition)
                chosenItems.clear()
                for (listItem in list) {
                    if (listItem.isChosen && listItem.imagePath != null) {
                        chosenItems.add(listItem.imagePath!!)
                    }
                }
                onChooseItemsListener?.onChosenItems(chosenItems)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun isSendingAllowed(item: BottomGalleryItem): Boolean {
        val uri = item.imagePath
        return if (uri != null) {
            if (FileHelper.isAllowedFileExtension(
                    FileUtils.getExtensionFromMediaStore(BaseConfig.getInstance().context, uri)
                )
            ) {
                if (FileHelper.isAllowedFileSize(
                        FileUtils.getFileSizeFromMediaStore(BaseConfig.getInstance().context, uri)
                    )
                ) {
                    true
                } else {
                    // Недопустимый размер файла
                    show(context, context.getString(R.string.ecc_not_allowed_file_size, FileHelper.maxAllowedFileSize))
                    false
                }
            } else {
                // Недопустимое расширение файла
                show(context, context.getString(R.string.ecc_not_allowed_file_extension))
                false
            }
        } else {
            false
        }
    }

    interface OnChooseItemsListener {
        fun onChosenItems(items: List<Uri>?)
    }
}
