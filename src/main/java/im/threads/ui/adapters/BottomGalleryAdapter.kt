package im.threads.ui.adapters

import android.net.Uri
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import im.threads.R
import im.threads.business.config.BaseConfig
import im.threads.business.utils.Balloon.show
import im.threads.ui.config.Config
import im.threads.ui.holders.BottomGalleryImageHolder
import im.threads.ui.models.BottomGalleryItem

internal class BottomGalleryAdapter(
    private val list: List<BottomGalleryItem>,
    private val onChooseItemsListener: OnChooseItemsListener?
) : RecyclerView.Adapter<BottomGalleryImageHolder>() {
    private val chosenItems: MutableList<Uri> = ArrayList()
    private val config = Config.getInstance()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomGalleryImageHolder {
        return BottomGalleryImageHolder(parent)
    }

    override fun onBindViewHolder(holder: BottomGalleryImageHolder, position: Int) {
        val item = list[position]
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

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnChooseItemsListener {
        fun onChosenItems(items: List<Uri>?)
    }
}
