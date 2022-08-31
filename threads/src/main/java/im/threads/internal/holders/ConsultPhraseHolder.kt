package im.threads.internal.holders

import android.annotation.SuppressLint
import android.text.TextUtils
import android.text.format.Formatter
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.isVisible
import im.threads.R
import im.threads.business.imageLoading.ImageLoader
import im.threads.business.imageLoading.ImageModifications
import im.threads.business.imageLoading.loadImage
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.FileDescription
import im.threads.business.models.Quote
import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.utils.FileUtils
import im.threads.business.utils.FileUtils.isImage
import im.threads.internal.Config
import im.threads.internal.formatters.RussianFormatSymbols
import im.threads.internal.utils.UrlUtils
import im.threads.internal.utils.ViewUtils
import im.threads.internal.views.CircularProgressButton
import im.threads.internal.widget.textView.BubbleMessageTextView
import im.threads.internal.widget.textView.BubbleTimeTextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** layout/item_consultant_text_with_file.xml */
class ConsultPhraseHolder(parent: ViewGroup) : BaseHolder(
    LayoutInflater.from(parent.context)
        .inflate(R.layout.item_consultant_text_with_file, parent, false)
) {
    private val style = Config.instance.chatStyle
    private val timeStampSdf = SimpleDateFormat("HH:mm", Locale.getDefault())

    @SuppressLint("SimpleDateFormat")
    private var quoteSdf = if (Locale.getDefault().language.equals("ru", ignoreCase = true)) {
        SimpleDateFormat("dd MMMM yyyy", RussianFormatSymbols())
    } else {
        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    }

    private val fileRow: View = itemView.findViewById(R.id.rightTextRow)
    private val circularProgressButton =
        itemView.findViewById<CircularProgressButton>(R.id.buttonDownload).apply {
            setBackgroundColorResId(style.chatBackgroundColor)
        }
    private val errorTextView: TextView = itemView.findViewById(R.id.errorText)
    private val fileImage = itemView.findViewById<ImageView>(R.id.fileImage)
    private val rightTextHeader: TextView = itemView.findViewById(R.id.to)
    private val imageLayout: FrameLayout = itemView.findViewById(R.id.imageLayout)
    private val image: ImageView = itemView.findViewById(R.id.image)
    private val loaderImage: ImageView = itemView.findViewById(R.id.loaderImage)
    private val rightTextDescription: TextView = itemView.findViewById(R.id.fileSpecs)
    private val rightTextFileStamp: TextView = itemView.findViewById(R.id.sendAt)
    private val timeStampTextView =
        itemView.findViewById<BubbleTimeTextView>(R.id.timeStamp).apply {
            setTextColor(getColorInt(style.incomingMessageTimeColor))
            if (style.incomingMessageTimeTextSize > 0) {
                setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    itemView.context.resources.getDimension(style.incomingMessageTimeTextSize)
                )
            }
        }
    private val phraseTextView = itemView.findViewById<BubbleMessageTextView>(R.id.text).apply {
        setLinkTextColor(getColorInt(style.incomingMessageTextColor))
    }
    private val consultAvatar = itemView.findViewById<ImageView>(R.id.consultAvatar).apply {
        layoutParams.height =
            itemView.context.resources.getDimension(style.operatorAvatarSize)
                .toInt()
        layoutParams.width =
            itemView.context.resources.getDimension(style.operatorAvatarSize)
                .toInt()
    }

    private val rootLayout: RelativeLayout = itemView.findViewById(R.id.rootLayout)
    private val phraseFrame: View = itemView.findViewById(R.id.phraseFrame)
    private val ogDataLayout: ViewGroup = itemView.findViewById(R.id.ogDataLayout)
    private val ogTimestamp = itemView.findViewById<TextView>(R.id.ogTimeStamp).apply {
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
            background.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    getColorInt(style.incomingMessageBubbleColor),
                    BlendModeCompat.SRC_ATOP
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
        itemView.findViewById<View>(R.id.delimiter)
            .setBackgroundColor(getColorInt(style.chatToolbarColorResId))
        setTextColorToViews(
            arrayOf(
                phraseTextView,
                rightTextHeader,
                rightTextDescription,
                rightTextFileStamp
            ),
            style.incomingMessageTextColor
        )
        setUpProgressButton(circularProgressButton)
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
        ViewUtils.setClickListener(itemView as ViewGroup, onRowLongClickListener)
        val timeText = timeStampSdf.format(Date(consultPhrase.timeStamp))
        timeStampTextView.text = timeText
        ogTimestamp.text = timeText
        imageLayout.isVisible = false

        consultPhrase.phraseText?.let {
            showPhrase(consultPhrase, it.trim())
        } ?: run {
            phraseTextView.isVisible = false
        }

        consultPhrase.quote?.let {
            showQuote(it, onQuoteClickListener)
        } ?: run {
            fileRow.isVisible = false
        }

        consultPhrase.fileDescription?.let {
            when (it.state) {
                AttachmentStateEnum.PENDING -> {
                    showLoaderLayout(it)
                }
                AttachmentStateEnum.ERROR -> {
                    showErrorLayout(it)
                }
                else -> {
                    showCommonLayout(it, fileClickListener, imageClickListener)
                }
            }
        } ?: run {
            consultPhrase.formattedPhrase?.let {
                UrlUtils.extractImageMarkdownLink(it)?.let { imageUrl ->
                    loadImage(imageUrl, imageClickListener, true)
                }
            }
        }

        consultAvatar.setOnClickListener(onAvatarClickListener)
        showAvatar(consultPhrase)
        rootLayout.apply {
            setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    if (highlighted) style.chatHighlightingColor else R.color.threads_transparent
                )
            )
        }
        if (consultPhrase.fileDescription != null || consultPhrase.quote != null) {
            phraseFrame.layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        } else {
            phraseFrame.layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT
        }
        if (consultPhrase.fileDescription == null && consultPhrase.quote == null) {
            fileRow.isVisible = false
        }
    }

    private fun getFileDescriptionText(fileDescription: FileDescription): String {
        return "${FileUtils.getFileName(fileDescription)} " +
            if (fileDescription.size > 0) {
                Formatter.formatFileSize(itemView.context, fileDescription.size).trimIndent()
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
        circularProgressButton.visibility = View.GONE
        imageLayout.visibility = View.VISIBLE
        image.visibility = View.VISIBLE
        image.setOnClickListener(imageClickListener)

        startLoaderAnimation()
        image.loadImage(
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

    private fun showPhrase(
        consultPhrase: ConsultPhrase,
        phrase: String
    ) {
        phraseTextView.bindTimestampView(timeStampTextView)
        phraseTextView.visibility = View.VISIBLE
        highlightOperatorText(phraseTextView, consultPhrase)
        UrlUtils.extractLink(phrase)?.let {
            it.link?.let { link ->
                bindOGData(ogDataLayout, timeStampTextView, link)
            }
        } ?: run {
            hideOGView(ogDataLayout, timeStampTextView)
        }
    }

    private fun showQuote(
        quote: Quote,
        onQuoteClickListener: View.OnClickListener
    ) {
        fileRow.visibility = View.VISIBLE
        fileImage.visibility = View.GONE
        circularProgressButton.visibility = View.GONE
        rightTextHeader.text = if (quote.phraseOwnerTitle == null) itemView.context
            .getString(R.string.threads_I) else quote.phraseOwnerTitle
        rightTextDescription.text = quote.text
        rightTextFileStamp.text = itemView.context
            .getString(R.string.threads_sent_at, quoteSdf.format(Date(quote.timeStamp)))
        ViewUtils.setClickListener(fileRow as ViewGroup, onQuoteClickListener)
        val quoteFileDescription = quote.fileDescription
        if (quoteFileDescription != null) {
            if (FileUtils.isVoiceMessage(quoteFileDescription)) {
                rightTextDescription.setText(R.string.threads_voice_message)
            } else {
                if (isImage(quote.fileDescription)) {
                    fileImage.visibility = View.VISIBLE
                    fileImage.loadImage(
                        quoteFileDescription.downloadPath,
                        listOf(ImageView.ScaleType.FIT_CENTER, ImageView.ScaleType.CENTER_CROP),
                        style.imagePlaceholder,
                        autoRotateWithExif = true
                    )
                    fileImage.setOnClickListener(onQuoteClickListener)
                } else {
                    circularProgressButton.visibility = View.VISIBLE
                    rightTextDescription.text = getFileDescriptionText(quoteFileDescription)
                    circularProgressButton.setOnClickListener(onQuoteClickListener)
                    circularProgressButton.setProgress(if (quoteFileDescription.fileUri != null) 100 else quoteFileDescription.downloadProgress)
                }
            }
        }
    }

    private fun startLoaderAnimation() {
        imageLayout.visibility = View.VISIBLE
        loaderImage.visibility = View.VISIBLE
        image.visibility = View.INVISIBLE
        rotateAnim.duration = 3000
        rotateAnim.repeatCount = Animation.INFINITE
        loaderImage.animation = rotateAnim
        rotateAnim.start()
    }

    private fun stopLoaderAnimation() {
        loaderImage.visibility = View.INVISIBLE
        image.visibility = View.VISIBLE
        rotateAnim.cancel()
        rotateAnim.reset()
    }

    private fun showLoaderLayout(fileDescription: FileDescription) {
        fileRow.isVisible = true
        fileImage.isVisible = true
        errorTextView.isVisible = false
        rightTextDescription.text = fileDescription.incomingName
        circularProgressButton.isVisible = false
        imageLayout.isVisible = false
        fileImage.background = null
        initAnimation(fileImage, true)
    }

    private fun showErrorLayout(fileDescription: FileDescription) {
        fileRow.isVisible = true
        fileImage.isVisible = true
        errorTextView.isVisible = true
        imageLayout.isVisible = false
        circularProgressButton.isVisible = false
        fileImage.background = null
        imageLayout.isVisible = false
        fileImage.setImageResource(getErrorImageResByErrorCode(fileDescription.errorCode))
        rightTextDescription.text = fileDescription.incomingName
        val errorString = getString(getErrorStringResByErrorCode(fileDescription.errorCode))
        errorTextView.text = errorString
        rotateAnim.cancel()
    }

    private fun showCommonLayout(
        fileDescription: FileDescription,
        fileClickListener: View.OnClickListener,
        imageClickListener: View.OnClickListener
    ) {
        imageLayout.isVisible = false
        fileRow.isVisible = true
        errorTextView.isVisible = false
        circularProgressButton.isVisible = true
        rotateAnim.cancel()
        val isStateReady = fileDescription.state == AttachmentStateEnum.READY
        if (isStateReady && isImage(fileDescription)) {
            loadImage(fileDescription.downloadPath, imageClickListener)
        } else if (!isStateReady && isImage(fileDescription)) {
            startLoaderAnimation()
        } else {
            fileRow.visibility = View.VISIBLE
            circularProgressButton.isVisible = true
            circularProgressButton.isClickable = true
            circularProgressButton.setOnClickListener(fileClickListener)
            rightTextHeader.text =
                if (fileDescription.from == null) "" else fileDescription.from
            if (!TextUtils.isEmpty(rightTextHeader.text)) {
                rightTextHeader.visibility = View.VISIBLE
            } else {
                rightTextHeader.visibility = View.GONE
            }
            rightTextDescription.text = getFileDescriptionText(fileDescription)
            rightTextFileStamp.text = itemView.context.getString(
                R.string.threads_sent_at,
                quoteSdf.format(Date(fileDescription.timeStamp))
            )
            circularProgressButton.setProgress(if (fileDescription.fileUri != null) 100 else fileDescription.downloadProgress)
        }
    }

    private fun showAvatar(consultPhrase: ConsultPhrase) {
        if (consultPhrase.isAvatarVisible) {
            consultAvatar.isVisible = true
            consultPhrase.avatarPath?.let {
                consultAvatar.loadImage(
                    FileUtils.convertRelativeUrlToAbsolute(it),
                    listOf(ImageView.ScaleType.FIT_XY, ImageView.ScaleType.CENTER_INSIDE),
                    modifications = listOf(ImageModifications.CircleCropModification),
                    callback = object : ImageLoader.ImageLoaderCallback {
                        override fun onImageLoaded() {
                            consultAvatar.setImageResource(style.defaultOperatorAvatar)
                        }
                    }
                )
            } ?: run {
                consultAvatar.setImageResource(style.defaultOperatorAvatar)
            }
        } else {
            consultAvatar.isVisible = false
        }
    }
}
