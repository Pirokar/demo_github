package im.threads.ui.adapters.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import im.threads.R
import im.threads.business.imageLoading.ImageLoader
import im.threads.business.imageLoading.ImageModifications
import im.threads.business.models.MessageFromHistory
import im.threads.business.utils.DateHelper
import im.threads.business.utils.FileUtils.toAbsoluteUrl
import im.threads.business.utils.UrlUtils
import im.threads.databinding.EccItemSearchResultBinding
import im.threads.ui.holders.BaseHolder
import im.threads.ui.utils.invisible
import im.threads.ui.utils.visible
import im.threads.ui.widget.CustomFontTextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal class SearchListViewAdapter(private val onClickCallback: (String?) -> Unit) :
    RecyclerView.Adapter<SearchListViewAdapter.SearchListViewHolder>() {

    private var data: List<MessageFromHistory> = listOf()

    fun updateData(newData: List<MessageFromHistory>?) {
        if (!newData.isNullOrEmpty()) {
            val diffResult = DiffUtil.calculateDiff(SearchListDiffCallback(data, newData))
            data = newData
            diffResult.dispatchUpdatesTo(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchListViewHolder {
        val binding = EccItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchListViewHolder(binding)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: SearchListViewHolder, position: Int) {
        holder.bind(data[position], itemCount - position == 1)
    }

    inner class SearchListViewHolder(val binding: EccItemSearchResultBinding) : BaseHolder(binding.root) {
        fun bind(message: MessageFromHistory, isLastItem: Boolean) = with(binding) {
            loadAvatar(message)
            setNameTextView(nameTextView, message)
            setMessageText(messageTextView, message)
            setDate(dateTextView, message)
            setDividerVisibility(dividerView, isLastItem)
            setOnItemClick(root, message.uuid)
        }

        private fun loadAvatar(message: MessageFromHistory) {
            val avatarUrl = message.operator?.photoUrl?.toAbsoluteUrl()
            if (avatarUrl != null) {
                ImageLoader
                    .get()
                    .load(avatarUrl)
                    .modifications(ImageModifications.CircleCropModification)
                    .into(binding.avatarImage)
            } else {
                binding.avatarImage.setImageDrawable(null)
            }
        }

        private fun setNameTextView(
            nameTextView: CustomFontTextView,
            message: MessageFromHistory
        ) {
            nameTextView.text = message.operator?.name
                ?: this@SearchListViewHolder.itemView.context.getString(R.string.ecc_I)
        }

        private fun setMessageText(
            messageTextView: CustomFontTextView,
            message: MessageFromHistory
        ) {
            if (message.operator != null) {
                var deeplink: String? = null
                var extractedLink: String? = null
                var emails: List<String> = listOf()

                message.text?.let {
                    deeplink = UrlUtils.extractDeepLink(it)
                    extractedLink = UrlUtils.extractLink(it)?.link
                    emails = UrlUtils.extractEmailAddresses(it)
                }

                highlightOperatorText(
                    messageTextView,
                    message.formattedText,
                    message.text,
                    deeplink ?: extractedLink,
                    emails
                )
            } else {
                messageTextView.text = message.text
            }
        }

        private fun setDate(
            dateTextView: CustomFontTextView,
            message: MessageFromHistory
        ) {
            val dateFormatTo = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val date = Date(DateHelper.getMessageTimestampFromDateString(message.receivedDate))
            dateTextView.text = dateFormatTo.format(date)
        }

        private fun setDividerVisibility(dividerView: View, isLastItem: Boolean) {
            if (isLastItem) {
                dividerView.invisible()
            } else {
                dividerView.visible()
            }
        }

        private fun setOnItemClick(rootView: View, messageUuid: String?) {
            rootView.setOnClickListener { onClickCallback(messageUuid) }
        }
    }
}
