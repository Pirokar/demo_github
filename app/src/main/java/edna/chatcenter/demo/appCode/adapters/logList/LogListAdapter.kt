package edna.chatcenter.demo.appCode.adapters.logList

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import edna.chatcenter.demo.R
import edna.chatcenter.demo.appCode.business.UiThemeProvider
import edna.chatcenter.demo.appCode.models.LogModel
import edna.chatcenter.demo.databinding.LogListItemBinding
import edna.chatcenter.ui.core.logger.ChatLogLevel
import org.koin.java.KoinJavaComponent

class LogListAdapter : RecyclerView.Adapter<LogListAdapter.LogItemHolder>() {

    private val uiThemeProvider: UiThemeProvider by KoinJavaComponent.inject(UiThemeProvider::class.java)
    internal val list: MutableList<LogModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        return LogItemHolder(LogListItemBinding.inflate(inflater))
    }

    override fun onBindViewHolder(holder: LogItemHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount() = list.count()

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(newItems: List<LogModel>) {
        list.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addItems(newItems: List<LogModel>) {
        list.addAll(newItems)
        notifyDatasetChangedWithDiffUtil(newItems)
    }

    private fun notifyDatasetChangedWithDiffUtil(newList: List<LogModel>) {
        val diffResult = DiffUtil.calculateDiff(LogListDiffCallback(list, newList))
        list.clear()
        list.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun getCount(): Int {
        return list.size
    }

    inner class LogItemHolder(private val binding: LogListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private fun setTextColor(view: TextView, @ColorRes color: Int) {
            view.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    color
                )
            )
        }

        fun onBind(position: Int) {
            val logEvent = list[position]
            try {
                binding.text.text = logEvent.logText
                when (logEvent.logLevel) {
                    ChatLogLevel.VERBOSE,
                    ChatLogLevel.DEBUG -> if (uiThemeProvider.isDarkThemeOn()) {
                        setTextColor(binding.text, R.color.dark_outgoing_time_text)
                    } else {
                        setTextColor(binding.text, R.color.black_color)
                    }

                    ChatLogLevel.INFO -> setTextColor(binding.text, R.color.log_info_color)
                    ChatLogLevel.WARNING -> setTextColor(binding.text, R.color.log_warning_color)
                    ChatLogLevel.ERROR -> setTextColor(binding.text, R.color.log_error_color)
                    ChatLogLevel.FLUSH -> setTextColor(binding.text, R.color.log_flush_color)
                    else -> {}
                }
            } catch (_: NullPointerException) {}
        }
    }
}
