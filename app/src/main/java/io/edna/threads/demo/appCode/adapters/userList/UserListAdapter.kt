package io.edna.threads.demo.appCode.adapters.userList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import im.threads.ui.utils.gone
import im.threads.ui.utils.visible
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.extensions.inflateWithBinding
import io.edna.threads.demo.databinding.UserListItemBinding
import io.edna.threads.demo.models.UserInfo

class UserListAdapter(private val onItemClickListener: UserListItemOnClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list: MutableList<UserInfo> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return UserItemHolder(inflater.inflateWithBinding(parent, R.layout.user_list_item))
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
        (holder as? UserItemHolder)?.onBind(position)
    }

    override fun getItemCount() = list.count()

    fun addItems(newItems: List<UserInfo>) {
        notifyDatasetChangedWithDiffUtil(newItems)
    }

    private fun notifyDatasetChangedWithDiffUtil(newList: List<UserInfo>) {
        val diffResult = DiffUtil.calculateDiff(UserListDiffCallback(list, newList))
        list.clear()
        list.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    private inner class UserItemHolder(val binding: UserListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(position: Int) {
            (list[position] as? UserInfo)?.let { item ->
                binding.name.text = item.nickName
                binding.userId.text = item.userId
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
