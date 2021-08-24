package im.threads.internal.holders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import im.threads.ChatStyle
import im.threads.R
import im.threads.internal.Config
import im.threads.internal.model.ConsultTyping
import im.threads.internal.utils.CircleTransformation
import im.threads.internal.utils.FileUtils

/**
 * layout/item_consult_typing.xml
 */
class ConsultIsTypingViewHolderNew(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_consult_typing, parent, false)
) {
    private val style: ChatStyle = Config.instance.chatStyle

    private val mConsultAvatar = itemView.findViewById<ImageView>(R.id.image).apply {
        layoutParams.height =
            itemView.context.resources.getDimension(style.operatorSystemAvatarSize)
                .toInt()
        layoutParams.width =
            itemView.context.resources.getDimension(style.operatorSystemAvatarSize)
                .toInt()
    }

    init {
        itemView.findViewById<TextView>(R.id.typing_in_progress).apply {
            setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    style.chatSystemMessageTextColor
                )
            )
        }
    }

    fun onBind(consultTyping: ConsultTyping, consultClickListener: View.OnClickListener) {
        mConsultAvatar.setOnClickListener(consultClickListener)
        val avatarPath = FileUtils.convertRelativeUrlToAbsolute(consultTyping.avatarPath)
        Picasso.get()
            .load(avatarPath)
            .fit()
            .error(style.defaultOperatorAvatar)
            .placeholder(style.defaultOperatorAvatar)
            .centerCrop()
            .transform(CircleTransformation())
            .into(mConsultAvatar)
    }
}