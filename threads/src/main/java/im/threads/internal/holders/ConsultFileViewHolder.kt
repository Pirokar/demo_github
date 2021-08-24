package im.threads.internal.holders

import android.graphics.PorterDuff
import android.text.TextUtils
import android.text.format.Formatter
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import im.threads.ChatStyle
import im.threads.R
import im.threads.internal.Config
import im.threads.internal.model.ConsultPhrase
import im.threads.internal.utils.CircleTransformation
import im.threads.internal.utils.FileUtils
import im.threads.internal.views.CircularProgressButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConsultFileViewHolder(parent: ViewGroup) : BaseHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_consult_chat_file, parent, false)
) {
    private val style: ChatStyle = Config.instance.chatStyle
    private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val mCircularProgressButton =
        itemView.findViewById<CircularProgressButton>(R.id.circ_button).apply {
            setBackgroundColorResId(style.chatBackgroundColor)
        }
    private val mFileHeader: TextView = itemView.findViewById(R.id.header)
    private val mSizeTextView: TextView = itemView.findViewById(R.id.file_size)
    private val mTimeStampTextView = itemView.findViewById<TextView>(R.id.timestamp).apply {
        setTextColor(getColorInt(style.incomingMessageTimeColor))
    }
    private val mConsultAvatar = itemView.findViewById<ImageView>(R.id.consult_avatar).apply {
        layoutParams.height =
            itemView.context.resources.getDimension(style.operatorAvatarSize)
                .toInt()
        layoutParams.width =
            itemView.context.resources.getDimension(style.operatorAvatarSize)
                .toInt()
    }
    private val filterView = itemView.findViewById<View>(R.id.filter).apply {
        setBackgroundColor(
            ContextCompat.getColor(
                itemView.context,
                style.chatHighlightingColor
            )
        )
    }
    private val secondFilterView = itemView.findViewById<View>(R.id.filter_second).apply {
        setBackgroundColor(
            ContextCompat.getColor(
                itemView.context,
                style.chatHighlightingColor
            )
        )
    }

    init {
        itemView.findViewById<View>(R.id.bubble).apply {
            background =
                AppCompatResources.getDrawable(
                    itemView.context,
                    style.incomingMessageBubbleBackground
                )
            background.setColorFilter(
                getColorInt(style.incomingMessageBubbleColor),
                PorterDuff.Mode.SRC_ATOP
            )
            val bubbleLeftMarginDp =
                itemView.context.resources.getDimension(R.dimen.margin_quarter)
            val bubbleLeftMarginPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                bubbleLeftMarginDp,
                itemView.resources.displayMetrics
            ).toInt()
            val lp = layoutParams as RelativeLayout.LayoutParams
            lp.setMargins(bubbleLeftMarginPx, lp.topMargin, lp.rightMargin, lp.bottomMargin)
            layoutParams = lp
        }
        setTextColorToViews(arrayOf(mFileHeader, mSizeTextView), style.incomingMessageTextColor)
        setTintToProgressButtonConsult(mCircularProgressButton, style.chatBodyIconsTint)
    }

    fun onBind(
        consultPhrase: ConsultPhrase,
        buttonClickListener: View.OnClickListener,
        onLongClickListener: OnLongClickListener,
        onAvatarClickListener: View.OnClickListener
    ) {
        val fileDescription = consultPhrase.fileDescription
        if (fileDescription != null) {
            mFileHeader.text = FileUtils.getFileName(fileDescription)
            if (mFileHeader.text.toString().equals("null", ignoreCase = true)) mFileHeader.text = ""
            mCircularProgressButton.setProgress(if (fileDescription.fileUri != null) 100 else fileDescription.downloadProgress)
        }
        val size = fileDescription?.size ?: 0
        mSizeTextView.text = Formatter.formatFileSize(itemView.context, size)
        mSizeTextView.visibility = if (size > 0) View.VISIBLE else View.GONE
        mTimeStampTextView.text = sdf.format(Date(consultPhrase.timeStamp))
        val vg = itemView as ViewGroup
        for (i in 0 until vg.childCount) {
            vg.getChildAt(i).setOnLongClickListener(onLongClickListener)
        }
        mCircularProgressButton.setOnClickListener(buttonClickListener)
        filterView.visibility = if (consultPhrase.isChosen) View.VISIBLE else View.INVISIBLE
        secondFilterView.visibility = if (consultPhrase.isChosen) View.VISIBLE else View.INVISIBLE
        if (consultPhrase.isAvatarVisible) {
            mConsultAvatar.visibility = View.VISIBLE
            mConsultAvatar.setOnClickListener(onAvatarClickListener)
            if (!TextUtils.isEmpty(consultPhrase.avatarPath)) {
                Picasso.get()
                    .load(FileUtils.convertRelativeUrlToAbsolute(consultPhrase.avatarPath))
                    .fit()
                    .noPlaceholder()
                    .transform(CircleTransformation())
                    .into(mConsultAvatar, object : Callback {
                        override fun onSuccess() {}
                        override fun onError(e: Exception) {
                            Picasso.get()
                                .load(style.defaultOperatorAvatar)
                                .fit()
                                .noPlaceholder()
                                .transform(CircleTransformation())
                                .into(mConsultAvatar)
                        }
                    })
            } else {
                Picasso.get()
                    .load(style.defaultOperatorAvatar)
                    .fit()
                    .noPlaceholder()
                    .transform(CircleTransformation())
                    .into(mConsultAvatar)
            }
        } else {
            mConsultAvatar.visibility = View.INVISIBLE
        }
    }
}