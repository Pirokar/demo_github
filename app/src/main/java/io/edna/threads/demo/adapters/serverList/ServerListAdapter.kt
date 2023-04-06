package io.edna.threads.demo.adapters.serverList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import im.threads.ui.utils.gone
import im.threads.ui.utils.visible
import io.edna.threads.demo.R
import io.edna.threads.demo.databinding.ServerListItemBinding
import io.edna.threads.demo.models.ServerConfig
import io.edna.threads.demo.ui.extenstions.inflateWithBinding

class ServerListAdapter(private val onItemClickListener: ServerListItemOnClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list: MutableList<ServerConfig> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ServerItemHolder(inflater.inflateWithBinding(parent, R.layout.server_list_item))
    }

    fun showMenu(position: Int) {
        for (i in list.indices) {
            if (list[i].isShowMenu) {
                list[i].isShowMenu = false
                notifyItemChanged(i)
            }
        }
        list[position].isShowMenu = true
        notifyItemChanged(position)
    }

    fun closeMenu() {
        for (i in list.indices) {
            if (list[i].isShowMenu) {
                list[i].isShowMenu = false
                notifyItemChanged(i)
            }
        }
    }

    fun isMenuShown(): Boolean {
        for (i in list.indices) {
            if (list[i].isShowMenu) {
                return true
            }
        }
        return false
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? ServerItemHolder)?.onBind(position)
    }

    override fun getItemCount() = list.count()

    fun addItems(newItems: List<ServerConfig>) {
        notifyDatasetChangedWithDiffUtil(newItems)
    }

    private fun notifyDatasetChangedWithDiffUtil(newList: List<ServerConfig>) {
        val diffResult = DiffUtil.calculateDiff(ServerListDiffCallback(list, newList))
        list.clear()
        list.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    private inner class ServerItemHolder(val binding: ServerListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(position: Int) {
            (list[position] as? ServerConfig)?.let { item ->
                binding.name.text = item.name
                binding.description.text = item.serverBaseUrl
                if (item.isShowMenu) {
                    binding.menuLayout.visible()
                    binding.itemLayout.gone()
                    binding.editButton.setOnClickListener { onItemClickListener.onEditItem(item) }
                    binding.deleteButton.setOnClickListener { onItemClickListener.onRemoveItem(item) }
                } else {
                    binding.menuLayout.gone()
                    binding.itemLayout.visible()
                    binding.rootLayout.setOnClickListener { onItemClickListener.onClick(item) }
                }
            }
        }
    }
}
