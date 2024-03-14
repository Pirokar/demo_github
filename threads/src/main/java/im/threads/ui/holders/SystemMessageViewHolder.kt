package im.threads.ui.holders

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.util.LinkifyCompat
import androidx.recyclerview.widget.RecyclerView
import im.threads.R
import im.threads.business.models.SystemMessage
import im.threads.business.utils.UrlUtils
import im.threads.business.utils.UrlUtils.extractDeepLink
import im.threads.business.utils.UrlUtils.extractLink
import im.threads.business.utils.UrlUtils.openUrl
import im.threads.ui.ChatStyle
import im.threads.ui.config.Config.Companion.getInstance

class SystemMessageViewHolder(parent: ViewGroup) :
    RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.ecc_item_system_message, parent, false)
    ) {

    private val systemMessageTextView: TextView = itemView.findViewById(R.id.tv_system_message)
    private var style: ChatStyle = getInstance().chatStyle
    private val context: Context = parent.context

    init {
        applyStyle()
    }

    fun onBind(systemMessage: SystemMessage) {
        val text = SpannableString(systemMessage.getText())
        LinkifyCompat.addLinks(text, UrlUtils.WEB_URL, "")
        systemMessageTextView.text = text
        systemMessageTextView.setOnClickListener { _: View? ->
            val deepLink = extractDeepLink(systemMessage.getText())
            val url = extractLink(systemMessage.getText())
            if (deepLink != null) {
                openUrl(context, deepLink)
            } else if (url != null && !url.link.isNullOrEmpty()) {
                openUrl(context, url.link)
            }
        }
    }

    private fun applyStyle() {
        if (!TextUtils.isEmpty(style.systemMessageFont)) {
            val assets = itemView.context.assets
            systemMessageTextView.typeface = Typeface.createFromAsset(assets, style.systemMessageFont)
        }
        systemMessageTextView.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            itemView.context.resources.getDimension(style.systemMessageTextSize)
        )
        systemMessageTextView.setTextColor(ContextCompat.getColor(context, style.systemMessageTextColorResId))
        systemMessageTextView.setLinkTextColor(ContextCompat.getColor(context, style.systemMessageLinkColor))
        systemMessageTextView.gravity = style.systemMessageTextGravity

        val leftRightPadding = itemView.context.resources.getDimension(style.systemMessageLeftRightPadding).toInt()
        systemMessageTextView.setPadding(
            leftRightPadding,
            systemMessageTextView.paddingTop,
            leftRightPadding,
            systemMessageTextView.paddingBottom
        )
    }
}
