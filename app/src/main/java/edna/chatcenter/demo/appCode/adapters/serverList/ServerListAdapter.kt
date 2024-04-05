package edna.chatcenter.demo.appCode.adapters.serverList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import edna.chatcenter.demo.R
import edna.chatcenter.demo.appCode.adapters.ListItemClickListener
import edna.chatcenter.demo.appCode.business.UiThemeProvider
import edna.chatcenter.demo.appCode.models.ServerConfig
import edna.chatcenter.demo.databinding.ServerListItemBinding
import edna.chatcenter.ui.visual.utils.EdnaColors
import org.koin.java.KoinJavaComponent.inject

class ServerListAdapter(private val onItemClickListener: ListItemClickListener) :
    RecyclerView.Adapter<ServerListAdapter.ServerItemHolder>() {

    private val uiThemeProvider: UiThemeProvider by inject(UiThemeProvider::class.java)
    private val list: MutableList<ServerConfig> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ServerItemHolder(ServerListItemBinding.inflate(inflater))
    }

    override fun onBindViewHolder(holder: ServerItemHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount() = list.count()

    fun addItems(newItems: List<ServerConfig>) {
        notifyDatasetChangedWithDiffUtil(newItems)
    }

    fun getItem(position: Int): ServerConfig {
        return list[position]
    }

    private fun notifyDatasetChangedWithDiffUtil(newList: List<ServerConfig>) {
        val diffResult = DiffUtil.calculateDiff(ServerListDiffCallback(list, newList))
        list.clear()
        list.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ServerItemHolder(val binding: ServerListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(position: Int) {
            (list[position] as? ServerConfig)?.let { item ->
                binding.name.text = item.name
                binding.description.text = item.serverBaseUrl
                if (uiThemeProvider.isDarkThemeOn()) {
                    binding.name.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.white_color_fa
                        )
                    )
                    binding.description.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.white_color_fa
                        )
                    )
                    EdnaColors.setTint(
                        binding.root.context,
                        binding.image,
                        R.color.white_color_fa
                    )
                } else {
                    binding.name.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.black_color
                        )
                    )
                    binding.description.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.black_color
                        )
                    )
                    EdnaColors.setTint(binding.root.context, binding.image, R.color.black_color)
                }
                binding.rootLayout.setOnClickListener { onItemClickListener.onClick(position) }
            }
        }
    }
}
