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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import im.threads.R
import im.threads.internal.Config
import im.threads.internal.formatters.RussianFormatSymbols
import im.threads.internal.imageLoading.ImageLoader
import im.threads.internal.imageLoading.ImageModifications
import im.threads.internal.imageLoading.loadImage
import im.threads.internal.model.AttachmentStateEnum
import im.threads.internal.model.ConsultPhrase
import im.threads.internal.model.FileDescription
import im.threads.internal.utils.FileUtils
import im.threads.internal.utils.FileUtils.isImage
import im.threads.internal.utils.UrlUtils
import im.threads.internal.utils.ViewUtils
import im.threads.internal.views.CircularProgressButton
import im.threads.internal.widget.textView.BubbleMessageTextView
import im.threads.internal.widget.textView.BubbleTimeTextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * layout/item_consultant_text_with_file.xml
 */
class ConsultPhraseHolder(parent: ViewGroup) : BaseHolder(
    LayoutInflater.from(parent.context)
        .inflate(R.layout.item_consultant_text_with_file, parent, false)
) {
    private val style = Config.instance.chatStyle
    private val rotateAnim = RotateAnimation(
        0f,
        360f,
        Animation.RELATIVE_TO_SELF,
        0.5f,
        Animation.RELATIVE_TO_SELF,
        0.5f
    )
    private val timeStampSdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var quoteSdf = if (Locale.getDefault().language.equals("ru", ignoreCase = true)) {
        SimpleDateFormat("dd MMMM yyyy", RussianFormatSymbols())
    } else {
        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    }

    private val fileRow: View = itemView.findViewById(R.id.right_text_row)
    private val mCircularProgressButton =
        itemView.findViewById<CircularProgressButton>(R.id.button_download).apply {
            setBackgroundColorResId(style.chatBackgroundColor)
        }
    private val mFileImage = itemView.findViewById<ImageView>(R.id.file_image)
    private val rightTextHeader: TextView = itemView.findViewById(R.id.to)
    private val mImageLayout: FrameLayout = itemView.findViewById(R.id.imageLayout)
    private val mImage: ImageView = itemView.findViewById(R.id.image)
    private val mLoaderImage: ImageView = itemView.findViewById<ImageView>(R.id.loaderImage)
    private val mRightTextDescr: TextView = itemView.findViewById(R.id.file_specs)
    private val rightTextFileStamp: TextView = itemView.findViewById(R.id.send_at)
    private val mTimeStampTextView =
        itemView.findViewById<BubbleTimeTextView>(R.id.timestamp).apply {
            setTextColor(getColorInt(style.incomingMessageTimeColor))
            if (style.incomingMessageTimeTextSize > 0) {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, itemView.context.resources.getDimension(style.incomingMessageTimeTextSize))
            }
        }
    private val mPhraseTextView = itemView.findViewById<BubbleMessageTextView>(R.id.text).apply {
        setLinkTextColor(getColorInt(style.incomingMessageLinkColor))
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
    private val secondFilterView = itemView.findViewById<View>(R.id.filter_bottom).apply {
        setBackgroundColor(
            ContextCompat.getColor(
                itemView.context,
                style.chatHighlightingColor
            )
        )
    }
    private val mPhraseFrame: View = itemView.findViewById(R.id.phrase_frame)
    private val ogDataLayout: ViewGroup = itemView.findViewById(R.id.og_data_layout)
    private val ogTimestamp = itemView.findViewById<TextView>(R.id.og_timestamp).apply {
        setTextColor(getColorInt(style.incomingMessageTimeColor))
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
            val bubbleLeftMarginDp = itemView.context.resources.getDimension(R.dimen.margin_quarter)
            val bubbleLeftMarginPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                bubbleLeftMarginDp,
                itemView.resources.displayMetrics
            ).toInt()
            val lp = layoutParams as RelativeLayout.LayoutParams
            lp.setMargins(bubbleLeftMarginPx, lp.topMargin, lp.rightMargin, lp.bottomMargin)
            layoutParams = lp
        }
        itemView.findViewById<View>(R.id.delimeter)
            .setBackgroundColor(getColorInt(style.chatToolbarColorResId))
        setTextColorToViews(
            arrayOf(
                mPhraseTextView,
                rightTextHeader,
                mRightTextDescr,
                rightTextFileStamp
            ),
            style.incomingMessageTextColor
        )
        setUpProgressButton(mCircularProgressButton)
    }

    fun onBind(
        consultPhrase: ConsultPhrase,
        highlighted: Boolean,
        imageClickListener: View.OnClickListener,
        fileClickListener: View.OnClickListener,
        onQuoteClickListener: View.OnClickListener,
        onRowLongClickListener: OnLongClickListener,
        onAvatarClickListener: View.OnClickListener
    ) {
        val phrase = consultPhrase.phraseText?.trim()
        val quote = consultPhrase.quote
        val fileDescription = consultPhrase.fileDescription
        ViewUtils.setClickListener(itemView as ViewGroup, onRowLongClickListener)
        mConsultAvatar.setImageBitmap(null)
        val timeText = timeStampSdf.format(Date(consultPhrase.timeStamp))
        mTimeStampTextView.text = timeText
        ogTimestamp.text = timeText
        if (phrase == null) {
            mPhraseTextView.visibility = View.GONE
        } else {
            mPhraseTextView.bindTimestampView(mTimeStampTextView)
            mPhraseTextView.visibility = View.VISIBLE
            val url = UrlUtils.extractLink(phrase)
            highlightOperatorText(mPhraseTextView, consultPhrase)
            if (url != null) {
                bindOGData(ogDataLayout, mTimeStampTextView, url)
            } else {
                hideOGView(ogDataLayout, mTimeStampTextView)
            }
        }
        mImageLayout.visibility = View.GONE
        if (quote == null) {
            fileRow.visibility = View.GONE
        } else {
            fileRow.visibility = View.VISIBLE
            ViewUtils.setClickListener(fileRow as ViewGroup, onQuoteClickListener)
            mFileImage.visibility = View.GONE
            mCircularProgressButton.visibility = View.GONE
            rightTextHeader.text = if (quote.phraseOwnerTitle == null) itemView.getContext()
                .getString(R.string.threads_I) else quote.phraseOwnerTitle
            mRightTextDescr.text = quote.text
            rightTextFileStamp.text = itemView.getContext()
                .getString(R.string.threads_sent_at, quoteSdf.format(Date(quote.timeStamp)))
            val quoteFileDescription = quote.fileDescription
            if (quoteFileDescription != null) {
                if (FileUtils.isVoiceMessage(quoteFileDescription)) {
                    mRightTextDescr.setText(R.string.threads_voice_message)
                } else {
                    if (isImage(quote.fileDescription)) {
                        mFileImage.visibility = View.VISIBLE
                        mFileImage.loadImage(
                            quoteFileDescription.downloadPath,
                            listOf(ImageView.ScaleType.FIT_CENTER, ImageView.ScaleType.CENTER_CROP),
                            style.imagePlaceholder,
                            autoRotateWithExif = true
                        )
                        mFileImage.setOnClickListener(onQuoteClickListener)
                    } else {
                        mCircularProgressButton.visibility = View.VISIBLE
                        mRightTextDescr.text = getFileDescriptionText(quoteFileDescription)
                        mCircularProgressButton.setOnClickListener(onQuoteClickListener)
                        mCircularProgressButton.setProgress(if (quoteFileDescription.fileUri != null) 100 else quoteFileDescription.downloadProgress)
                    }
                }
            }
        }
        if (fileDescription != null) {
            val isStateReady = fileDescription.state == AttachmentStateEnum.READY

            if (isStateReady && isImage(fileDescription)) {
                loadImage(fileDescription.downloadPath, imageClickListener)
            } else if (!isStateReady && isImage(fileDescription)) {
                startLoaderAnimation()
            } else {
                fileRow.visibility = View.VISIBLE
                ViewUtils.setClickListener(fileRow as ViewGroup, null as View.OnClickListener?)
                mCircularProgressButton.visibility = View.VISIBLE
                mCircularProgressButton.setOnClickListener(fileClickListener)
                rightTextHeader.text =
                    if (fileDescription.from == null) "" else fileDescription.from
                if (!TextUtils.isEmpty(rightTextHeader.text)) {
                    rightTextHeader.visibility = View.VISIBLE
                } else {
                    rightTextHeader.visibility = View.GONE
                }
                mRightTextDescr.text = getFileDescriptionText(fileDescription)
                rightTextFileStamp.text = itemView.getContext().getString(
                    R.string.threads_sent_at,
                    quoteSdf.format(Date(fileDescription.timeStamp))
                )
                mCircularProgressButton.setProgress(if (fileDescription.fileUri != null) 100 else fileDescription.downloadProgress)
            }
        } else {
            consultPhrase.formattedPhrase?.let {
                UrlUtils.extractImageMarkdownLink(it)?.let { imageUrl ->
                    loadImage(imageUrl, imageClickListener, true)
                }
            }
        }
        if (fileDescription == null && quote == null) {
            fileRow.visibility = View.GONE
        }
        if (fileDescription != null || quote != null) {
            mPhraseFrame.layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        } else {
            mPhraseFrame.layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT
        }
        if (consultPhrase.isAvatarVisible) {
            mConsultAvatar.visibility = View.VISIBLE
            mConsultAvatar.setOnClickListener(onAvatarClickListener)
            showDefIcon()
            if (!TextUtils.isEmpty(consultPhrase.avatarPath)) {
                mConsultAvatar.loadImage(
                    FileUtils.convertRelativeUrlToAbsolute(consultPhrase.avatarPath),
                    listOf(ImageView.ScaleType.FIT_XY, ImageView.ScaleType.CENTER_CROP),
                    modifications = listOf(ImageModifications.CircleCropModification)
                )
            }
        } else {
            mConsultAvatar.visibility = View.INVISIBLE
        }
        filterView.visibility = if (highlighted) View.VISIBLE else View.INVISIBLE
        secondFilterView.visibility = if (highlighted) View.VISIBLE else View.INVISIBLE
    }

    private fun getFileDescriptionText(fileDescription: FileDescription): String {
        return "${FileUtils.getFileName(fileDescription)} " +
            if (fileDescription.size > 0) {
                Formatter.formatFileSize(itemView.getContext(), fileDescription.size).trimIndent()
            } else {
                ""
            }
    }

    private fun loadImage(
        imagePath: String?,
        imageClickListener: View.OnClickListener,
        isExternalImage: Boolean = false
    ) {
        fileRow.visibility = View.GONE
        mCircularProgressButton.visibility = View.GONE
        mImageLayout.visibility = View.VISIBLE
        mImage.visibility = View.VISIBLE
        mImage.setOnClickListener(imageClickListener)

        startLoaderAnimation()
        mImage.loadImage(
            imagePath,
            scales = listOf(ImageView.ScaleType.FIT_CENTER, ImageView.ScaleType.CENTER_CROP),
            errorDrawableResId = style.imagePlaceholder,
            autoRotateWithExif = true,
            isExternalImage = isExternalImage,
            callback = object : ImageLoader.ImageLoaderCallback {
                override fun onImageLoaded() {
                    stopLoaderAnimation()
                }

                override fun onImageLoadError() {
                    stopLoaderAnimation()
                }
            }
        )
    }

    private fun showDefIcon() {
        mConsultAvatar.setImageResource(style.defaultOperatorAvatar)
    }

    private fun startLoaderAnimation() {
        mImageLayout.visibility = View.VISIBLE
        mLoaderImage.visibility = View.VISIBLE
        mImage.visibility = View.INVISIBLE
        rotateAnim.duration = 3000
        rotateAnim.repeatCount = Animation.INFINITE
        mLoaderImage.animation = rotateAnim
        rotateAnim.start()
    }

    private fun stopLoaderAnimation() {
        mLoaderImage.visibility = View.INVISIBLE
        mImage.visibility = View.VISIBLE
        rotateAnim.cancel()
        rotateAnim.reset()
    }
}
