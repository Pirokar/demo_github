package edna.chatcenter.demo.appCode.adapters.userList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import edna.chatcenter.demo.R
import edna.chatcenter.demo.appCode.adapters.ListItemClickListener
import edna.chatcenter.demo.appCode.business.UiThemeProvider
import edna.chatcenter.demo.appCode.models.UserInfo
import edna.chatcenter.demo.databinding.UserListItemBinding
import edna.chatcenter.ui.visual.utils.EdnaColors
import org.koin.java.KoinJavaComponent.inject
import java.lang.ref.WeakReference

class UserListAdapter(private val onItemClickListener: WeakReference<ListItemClickListener>) :
    RecyclerView.Adapter<UserListAdapter.UserItemHolder>() {

    private val uiThemeProvider: UiThemeProvider by inject(UiThemeProvider::class.java)
    private val list: MutableList<UserInfo> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        return UserItemHolder(UserListItemBinding.inflate(inflater))
    }

    override fun onBindViewHolder(holder: UserItemHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount() = list.count()

    fun addItems(newItems: List<UserInfo>) {
        notifyDatasetChangedWithDiffUtil(newItems)
    }

    fun getItem(position: Int): UserInfo {
        return list[position]
    }

    private fun notifyDatasetChangedWithDiffUtil(newList: List<UserInfo>) {
        val diffResult = DiffUtil.calculateDiff(UserListDiffCallback(list, newList))
        list.clear()
        list.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class UserItemHolder(private val binding: UserListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(position: Int) {
            (list[position] as? UserInfo)?.let { item ->
                binding.userId.text = item.userId
                if (uiThemeProvider.isDarkThemeOn()) {
                    binding.userId.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white_color_fa))
                    EdnaColors.setTint(binding.root.context, binding.image, R.color.white_color_fa)
                } else {
                    binding.userId.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black_color))
                    EdnaColors.setTint(binding.root.context, binding.image, R.color.black_color)
                }
                binding.rootLayout.setOnClickListener { onItemClickListener.get()?.onClick(position) }
            }
        }
    }
}
