package im.threads.internal.holders

import android.graphics.PorterDuff
import android.text.TextUtils
import android.text.format.Formatter
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import im.threads.ChatStyle
import im.threads.R
import im.threads.business.imageLoading.ImageModifications
import im.threads.business.imageLoading.loadImage
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.utils.FileUtils
import im.threads.internal.Config
import im.threads.internal.views.CircularProgressButton
import io.reactivex.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConsultFileViewHolder(parent: ViewGroup, highlightingStream: PublishSubject<ChatItem>) : BaseHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_consult_chat_file, parent, false),
    highlightingStream
) {
    private val style: ChatStyle = Config.instance.chatStyle
    private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val mCircularProgressButton =
        itemView.findViewById<CircularProgressButton>(R.id.circ_button).apply {
            setBackgroundColorResId(style.chatBackgroundColor)
        }
    private val errortext: TextView = itemView.findViewById(R.id.errortext)
    private val loader: ImageView = itemView.findViewById(R.id.loader)
    private val mFileHeader: TextView = itemView.findViewById(R.id.header)
    private val mSizeTextView: TextView = itemView.findViewById(R.id.file_size)
    private val rootLayout: RelativeLayout = itemView.findViewById(R.id.rootLayout)
    private val mTimeStampTextView = itemView.findViewById<TextView>(R.id.timestamp).apply {
        setTextColor(getColorInt(style.incomingMessageTimeColor))
        if (style.incomingMessageTimeTextSize > 0) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, itemView.context.resources.getDimension(style.incomingMessageTimeTextSize))
        }
    }
    private val mConsultAvatar = itemView.findViewById<ImageView>(R.id.consult_avatar).apply {
        layoutParams.height =
            itemView.context.resources.getDimension(style.operatorAvatarSize)
                .toInt()
        layoutParams.width =
            itemView.context.resources.getDimension(style.operatorAvatarSize)
                .toInt()
    }

    init {
        itemView.findViewById<View>(R.id.bubble).apply {
            background =
                AppCompatResources.getDrawable(
                    itemView.context,
                    style.incomingMessageBubbleBackground
                )
            setPadding(
                context.resources.getDimensionPixelSize(style.bubbleIncomingPaddingLeft),
                context.resources.getDimensionPixelSize(style.bubbleIncomingPaddingTop),
                context.resources.getDimensionPixelSize(style.bubbleIncomingPaddingRight),
                context.resources.getDimensionPixelSize(style.bubbleIncomingPaddingBottom)
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
        setUpProgressButton(mCircularProgressButton)
    }

    fun onBind(
        consultPhrase: ConsultPhrase,
        highlighted: Boolean,
        buttonClickListener: View.OnClickListener,
        onLongClickListener: OnLongClickListener,
        onAvatarClickListener: View.OnClickListener
    ) {
        subscribeForHighlighting(consultPhrase, rootLayout)
        val fileDescription = consultPhrase.fileDescription
        if (fileDescription != null) {
            if (fileDescription.state == AttachmentStateEnum.ERROR) {
                mCircularProgressButton.visibility = View.INVISIBLE
                loader.isVisible = true
                errortext.isVisible = true
                loader.setImageResource(getErrorImageResByErrorCode(fileDescription.errorCode))
                if (fileDescription.errorMessage.isNullOrEmpty()) {
                    errortext.text =
                        Config.instance.context.getString(R.string.threads_some_error_during_load_file)
                } else {
                    errortext.text = fileDescription.errorMessage
                }
            } else if (fileDescription.state == AttachmentStateEnum.PENDING) {
                mCircularProgressButton.visibility = View.INVISIBLE
                loader.setImageResource(R.drawable.im_loading_consult)
                loader.isVisible = true
                errortext.isVisible = false
                val rotate = RotateAnimation(
                    0f,
                    360f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
                )
                rotate.duration = 3000
                rotate.repeatCount = Animation.INFINITE
                loader.setAnimation(rotate)
            } else {
                loader.visibility = View.INVISIBLE
                errortext.isVisible = false
                mCircularProgressButton.visibility = View.VISIBLE
                mCircularProgressButton.setProgress(if (fileDescription.fileUri != null) 100 else fileDescription.downloadProgress)
            }
            mFileHeader.text = FileUtils.getFileName(fileDescription)
            if (mFileHeader.text.toString().equals("null", ignoreCase = true)) mFileHeader.text = ""
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
        changeHighlighting(highlighted)
        if (consultPhrase.isAvatarVisible) {
            mConsultAvatar.visibility = View.VISIBLE
            mConsultAvatar.setOnClickListener(onAvatarClickListener)
            mConsultAvatar.setImageResource(style.defaultOperatorAvatar)
            if (!TextUtils.isEmpty(consultPhrase.avatarPath)) {
                mConsultAvatar.loadImage(
                    FileUtils.convertRelativeUrlToAbsolute(consultPhrase.avatarPath),
                    listOf(ImageView.ScaleType.FIT_XY),
                    autoRotateWithExif = true,
                    modifications = listOf(ImageModifications.CircleCropModification)
                )
            }
        } else {
            mConsultAvatar.visibility = View.INVISIBLE
        }
    }
}
