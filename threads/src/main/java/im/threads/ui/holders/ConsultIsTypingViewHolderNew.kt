package im.threads.ui.holders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import im.threads.R
import im.threads.business.imageLoading.ImageModifications
import im.threads.business.imageLoading.loadImage
import im.threads.business.models.ConsultTyping
import im.threads.business.utils.FileUtils
import im.threads.ui.ChatStyle
import im.threads.ui.config.Config

/**
 * layout/item_consult_typing.xml
 */
class ConsultIsTypingViewHolderNew(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_consult_typing, parent, false)
) {
    private val config: Config by lazy { Config.getInstance() }

    private val style: ChatStyle = config.getChatStyle()

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
        mConsultAvatar.setImageResource(style.defaultOperatorAvatar)
        mConsultAvatar.loadImage(
            FileUtils.convertRelativeUrlToAbsolute(consultTyping.avatarPath),
            listOf(ImageView.ScaleType.FIT_XY, ImageView.ScaleType.CENTER_CROP),
            modifications = listOf(ImageModifications.CircleCropModification)
        )
    }
}
