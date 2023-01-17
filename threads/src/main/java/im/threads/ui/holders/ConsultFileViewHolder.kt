package im.threads.ui.holders

import android.graphics.PorterDuff
import android.text.TextUtils
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
import im.threads.R
import im.threads.business.imageLoading.ImageModifications
import im.threads.business.imageLoading.loadImage
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.ogParser.OpenGraphParser
import im.threads.business.utils.FileUtils
import im.threads.business.utils.toFileSize
import im.threads.ui.utils.gone
import im.threads.ui.utils.visible
import im.threads.ui.views.CircularProgressButton
import io.reactivex.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConsultFileViewHolder(
    parent: ViewGroup,
    highlightingStream: PublishSubject<ChatItem>,
    openGraphParser: OpenGraphParser,
) : BaseHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_consult_chat_file, parent, false),
    highlightingStream,
    openGraphParser
) {
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
        itemView.findViewById<ViewGroup>(R.id.bubble).apply {
            background =
                AppCompatResources.getDrawable(
                    itemView.context,
                    style.incomingMessageBubbleBackground
                )
            setPaddings(true, this)
            background.setColorFilter(
                getColorInt(style.incomingMessageBubbleColor),
                PorterDuff.Mode.SRC_ATOP
            )
            setLayoutMargins(true, this)
        }
        setTextColorToViews(arrayOf(mFileHeader, mSizeTextView), style.incomingMessageTextColor)
        setUpProgressButton(mCircularProgressButton)
    }

    fun onBind(
        consultPhrase: ConsultPhrase,
        highlighted: Boolean,
        buttonClickListener: View.OnClickListener,
        onLongClickListener: OnLongClickListener,
        onAvatarClickListener: View.OnClickListener,
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
                        config.context.getString(R.string.threads_some_error_during_load_file)
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
        mSizeTextView.text = size.toFileSize()
        if (size > 0) {
            mSizeTextView.visible()
            mSizeTextView.text = size.toFileSize()
        } else {
            mSizeTextView.gone()
        }
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
                    errorDrawableResId = R.drawable.threads_operator_avatar_placeholder,
                    autoRotateWithExif = true,
                    modifications = listOf(ImageModifications.CircleCropModification)
                )
            }
        } else {
            mConsultAvatar.visibility = View.INVISIBLE
        }
    }
}
