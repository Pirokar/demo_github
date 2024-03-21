package im.threads.ui.adapters.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import im.threads.R
import im.threads.business.imageLoading.ImageLoader
import im.threads.business.imageLoading.ImageModifications
import im.threads.business.models.MessageFromHistory
import im.threads.business.serviceLocator.core.inject
import im.threads.business.utils.ClientUseCase
import im.threads.business.utils.DateHelper
import im.threads.business.utils.FileUtils.toAbsoluteUrl
import im.threads.business.utils.UrlUtils
import im.threads.databinding.EccItemSearchResultBinding
import im.threads.ui.config.Config
import im.threads.ui.holders.BaseHolder
import im.threads.ui.utils.ColorsHelper
import im.threads.ui.utils.invisible
import im.threads.ui.utils.visible
import im.threads.ui.widget.CustomFontTextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal class SearchListViewAdapter(private val onClickCallback: (String?, date: String?) -> Unit) :
    RecyclerView.Adapter<SearchListViewAdapter.SearchListViewHolder>() {

    private val clientUseCase: ClientUseCase by inject()
    private var data: List<MessageFromHistory> = listOf()

    fun updateData(newData: List<MessageFromHistory>?) {
        val dataForUpdate = newData ?: listOf()
        val diffResult = DiffUtil.calculateDiff(SearchListDiffCallback(data, dataForUpdate))
        data = dataForUpdate
        diffResult.dispatchUpdatesTo(this)
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
        private val context = binding.root.context
        private val chatStyle = Config.getInstance().chatStyle

        fun bind(message: MessageFromHistory, isLastItem: Boolean) = with(binding) {
            loadAvatar(message)
            setNameTextView(nameTextView, message)
            setMessageText(messageTextView, message)
            setDate(dateTextView, message)
            setDivider(dividerView, isLastItem)
            setRightArrow(rightArrowImageView)
            setOnItemClick(binding.clickableView, message.uuid, message.receivedDate)
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
            nameTextView.setTextColor(ContextCompat.getColor(context, chatStyle.searchResultsItemNameTextColor))
            nameTextView.text = message.operator?.name
                ?: clientUseCase.getUserInfo()?.userName
                ?: this@SearchListViewHolder.itemView.context.getString(R.string.ecc_you)
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
            messageTextView.setTextColor(ContextCompat.getColor(context, chatStyle.searchResultsItemMessageTextColor))
        }

        private fun setDate(
            dateTextView: CustomFontTextView,
            message: MessageFromHistory
        ) {
            val dateFormatTo = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val date = Date(DateHelper.getMessageTimestampFromDateString(message.receivedDate))
            dateTextView.text = dateFormatTo.format(date)
            dateTextView.setTextColor(ContextCompat.getColor(context, chatStyle.searchResultsItemDateTextColor))
        }

        private fun setDivider(dividerView: View, isLastItem: Boolean) {
            if (isLastItem) {
                dividerView.invisible()
            } else {
                dividerView.setBackgroundColor(ContextCompat.getColor(context, chatStyle.searchResultsDividerColor))
                dividerView.visible()
            }
        }

        private fun setRightArrow(rightArrowImageView: ImageView) {
            rightArrowImageView.setImageDrawable(ContextCompat.getDrawable(context, chatStyle.searchResultsItemRightArrowDrawable))
            ColorsHelper.setTint(context, rightArrowImageView, chatStyle.searchResultsItemRightArrowTintColor)
        }

        private fun setOnItemClick(clickableView: View, messageUuid: String?, date: String?) {
            clickableView.setOnClickListener { onClickCallback(messageUuid, date) }
        }
    }
}
