package im.threads.ui.holders

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.isVisible
import im.threads.R
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.FileDescription
import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.ogParser.OpenGraphParser
import im.threads.business.utils.toFileSize
import im.threads.ui.utils.ColorsHelper
import im.threads.ui.utils.gone
import im.threads.ui.utils.visible
import im.threads.ui.views.CircularProgressButton
import io.reactivex.subjects.PublishSubject

class ConsultFileViewHolder(
    parent: ViewGroup,
    highlightingStream: PublishSubject<ChatItem>,
    openGraphParser: OpenGraphParser
) : BaseHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.ecc_item_consult_chat_file, parent, false),
    highlightingStream,
    openGraphParser
) {
    private val quoteLayout: LinearLayout = itemView.findViewById(R.id.quoteLayout)
    private val quoteTextHeader: TextView = itemView.findViewById(R.id.quoteTo)
    private val quoteTextDescription: TextView = itemView.findViewById(R.id.quoteFileSpecs)
    private val quoteTextTimeStamp: TextView = itemView.findViewById(R.id.quoteSendAt)
    private val quoteFileImage = itemView.findViewById<ImageView>(R.id.quoteFileImage)
    private val quoteProgressButton: CircularProgressButton = itemView.findViewById(R.id.quoteButtonDownload)
    private val mCircularProgressButton: CircularProgressButton = itemView.findViewById(R.id.circ_button)
    private val errorText: TextView = itemView.findViewById(R.id.errorText)
    private val loader: ImageView = itemView.findViewById(R.id.loader)
    private val mFileHeader: TextView = itemView.findViewById(R.id.header)
    private val mSizeTextView: TextView = itemView.findViewById(R.id.file_size)
    private val rootLayout: RelativeLayout = itemView.findViewById(R.id.rootLayout)
    private val bubbleLayout: ConstraintLayout = itemView.findViewById(R.id.bubble)
    private val timeStampTextView = itemView.findViewById<TextView>(R.id.timestamp).apply {
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
        itemView.findViewById<View>(R.id.quoteDelimiter)
            .setBackgroundColor(getColorInt(style.incomingDelimitersColor))
        itemView.findViewById<View>(R.id.delimiter)
            .setBackgroundColor(getColorInt(style.incomingDelimitersColor))
        setTextColorToViews(arrayOf(mFileHeader, mSizeTextView), style.incomingMessageTextColor)
        ColorsHelper.setTextColor(errorText, style.errorMessageTextColor)
        setUpProgressButton(mCircularProgressButton)
    }

    fun onBind(
        consultPhrase: ConsultPhrase,
        highlighted: Boolean,
        buttonClickListener: View.OnClickListener,
        onLongClickListener: OnLongClickListener,
        onQuoteClickListener: View.OnClickListener,
        onAvatarClickListener: View.OnClickListener
    ) {
        subscribeForHighlighting(consultPhrase, rootLayout)
        applyBubbleLayoutStyle()
        consultPhrase.quote?.let {
            showQuote(
                it,
                onQuoteClickListener,
                quoteLayout,
                quoteTextHeader,
                quoteTextDescription,
                quoteTextTimeStamp,
                quoteFileImage,
                quoteProgressButton
            )
        } ?: run {
            quoteLayout.isVisible = false
        }
        showFile(consultPhrase.fileDescription)
        showConsultTimeStamp(consultPhrase, listOf(timeStampTextView))
        val vg = itemView as ViewGroup
        for (i in 0 until vg.childCount) {
            vg.getChildAt(i).setOnLongClickListener(onLongClickListener)
        }
        mCircularProgressButton.setOnClickListener(buttonClickListener)
        changeHighlighting(highlighted)
        showAvatar(mConsultAvatar, consultPhrase, onAvatarClickListener)
    }

    private fun applyBubbleLayoutStyle() {
        bubbleLayout.background = AppCompatResources.getDrawable(
            itemView.context,
            style.incomingMessageBubbleBackground
        )
        setPaddings(true, bubbleLayout)
        setLayoutMargins(true, bubbleLayout)
        bubbleLayout.background.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                getColorInt(style.incomingMessageBubbleColor),
                BlendModeCompat.SRC_ATOP
            )
    }

    private fun showFile(fileDescription: FileDescription?) {
        if (fileDescription != null) {
            when (fileDescription.state) {
                AttachmentStateEnum.ERROR -> showErrorLayout(fileDescription)
                AttachmentStateEnum.PENDING -> showPendingState()
                else -> showImageLayout(fileDescription)
            }
            fileNameFromDescription(fileDescription) { mFileHeader.text = it }
            if (mFileHeader.text.toString().equals("null", ignoreCase = true)) mFileHeader.text = ""
            val size = fileDescription.size
            mSizeTextView.text = size.toFileSize()
            if (size > 0) {
                mSizeTextView.visible()
                mSizeTextView.text = size.toFileSize()
            } else {
                mSizeTextView.gone()
            }
        }
    }

    private fun showImageLayout(fileDescription: FileDescription) {
        loader.visibility = View.INVISIBLE
        errorText.isVisible = false
        mCircularProgressButton.visibility = View.VISIBLE
        mCircularProgressButton.setProgress(if (fileDescription.fileUri != null) 100 else fileDescription.downloadProgress)
    }

    private fun showPendingState() {
        mCircularProgressButton.visibility = View.INVISIBLE
        loader.isVisible = true
        errorText.isVisible = false
        initAnimation(loader, true)
    }

    private fun showErrorLayout(fileDescription: FileDescription) {
        mCircularProgressButton.visibility = View.INVISIBLE
        loader.isVisible = true
        errorText.isVisible = true
        loader.setImageResource(getErrorImageResByErrorCode(fileDescription.errorCode))
        if (fileDescription.errorMessage.isNullOrEmpty()) {
            errorText.text = config.context.getString(R.string.ecc_some_error_during_load_file)
        } else {
            errorText.text = fileDescription.errorMessage
        }
    }
}