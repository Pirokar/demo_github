package im.threads.ui.holders

import android.annotation.SuppressLint
import android.app.ActionBar
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import im.threads.R
import im.threads.business.formatters.RussianFormatSymbols
import im.threads.business.imageLoading.ImageLoader
import im.threads.business.imageLoading.ImageModifications
import im.threads.business.imageLoading.loadImage
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.FileDescription
import im.threads.business.models.Quote
import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.ogParser.OGDataContent
import im.threads.business.ogParser.OpenGraphParser
import im.threads.business.utils.FileUtils
import im.threads.business.utils.FileUtils.isImage
import im.threads.business.utils.UrlUtils
import im.threads.business.utils.toFileSize
import im.threads.ui.config.Config
import im.threads.ui.utils.ColorsHelper
import im.threads.ui.utils.gone
import im.threads.ui.utils.invisible
import im.threads.ui.utils.visible
import im.threads.ui.views.CircularProgressButton
import im.threads.ui.widget.textView.BubbleMessageTextView
import im.threads.ui.widget.textView.BubbleTimeTextView
import io.reactivex.subjects.PublishSubject
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** layout/item_consultant_text_with_file.xml */
class ConsultPhraseHolder(
    parent: ViewGroup,
    private val maskedTransformation: ImageModifications.MaskedModification?,
    highlightingStream: PublishSubject<ChatItem>,
    openGraphParser: OpenGraphParser
) : BaseHolder(
    LayoutInflater.from(parent.context)
        .inflate(R.layout.ecc_item_consultant_text_with_file, parent, false),
    highlightingStream,
    openGraphParser
) {
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
    private val image: ImageView = itemView.findViewById(R.id.image)
    private val imageRoot: FrameLayout = itemView.findViewById(R.id.imageRoot)
    private val imageLayout: FrameLayout = itemView.findViewById(R.id.imageLayout)
    private val errorImage: ImageView = itemView.findViewById(R.id.errorImage)
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
        setLinkTextColor(getColorInt(style.incomingMessageLinkColor))
    }
    private val consultAvatar = itemView.findViewById<ImageView>(R.id.consultAvatar).apply {
        layoutParams.height =
            itemView.context.resources.getDimension(style.operatorAvatarSize)
                .toInt()
        layoutParams.width =
            itemView.context.resources.getDimension(style.operatorAvatarSize)
                .toInt()
    }

    private val phraseFrame: View = itemView.findViewById(R.id.phraseFrame)
    private val ogDataLayout: ViewGroup = itemView.findViewById(R.id.ogDataLayout)
    private val ogTimestamp = itemView.findViewById<BubbleTimeTextView>(R.id.ogTimeStamp).apply {
        setTextColor(getColorInt(style.incomingMessageTimeColor))
    }
    private val bubbleLayout = itemView.findViewById<ViewGroup>(R.id.bubble).apply {
        background =
            AppCompatResources.getDrawable(
                itemView.context,
                style.incomingMessageBubbleBackground
            )

        background.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                getColorInt(style.incomingMessageBubbleColor),
                BlendModeCompat.SRC_ATOP
            )
    }

    init {
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
        ColorsHelper.setTextColor(errorTextView, style.errorMessageTextColor)
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
        setupPaddingsAndBorders(consultPhrase.fileDescription)
        subscribeForHighlighting(consultPhrase, itemView)
        subscribeForOpenGraphData(
            OGDataContent(
                WeakReference(ogDataLayout),
                WeakReference(ogTimestamp),
                WeakReference(timeStampTextView),
                consultPhrase.phraseText
            )
        )
        viewUtils.setClickListener(itemView as ViewGroup, onRowLongClickListener)
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

        setLayoutMargins(true, bubbleLayout)
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
        changeHighlighting(highlighted)
        if (consultPhrase.fileDescription != null || consultPhrase.quote != null) {
            phraseFrame.layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        } else {
            phraseFrame.layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT
        }
        if (consultPhrase.fileDescription == null && consultPhrase.quote == null) {
            fileRow.isVisible = false
        }
    }

    private fun setupPaddingsAndBorders(fileDescription: FileDescription?) = with(bubbleLayout) {
        val chatStyle = Config.getInstance().chatStyle
        val resources = context.resources
        val borderLeft = resources.getDimensionPixelSize(chatStyle.incomingImageLeftBorderSize)
        val borderTop = resources.getDimensionPixelSize(chatStyle.incomingImageTopBorderSize)
        val borderRight = resources.getDimensionPixelSize(chatStyle.incomingImageRightBorderSize)
        val borderBottom = resources.getDimensionPixelSize(chatStyle.incomingImageBottomBorderSize)
        val isBordersNotSet = borderLeft == 0 && borderTop == 0 && borderRight == 0 && borderBottom == 0
        val isImage = isImage(fileDescription)

        setLayoutMargins(true, bubbleLayout)
        if (isImage && fileDescription?.state == AttachmentStateEnum.READY) {
            imageRoot.visible()

            (bubbleLayout.layoutParams as MarginLayoutParams).let {
                it.marginEnd = 0
                bubbleLayout.layoutParams = it
            }
            bubbleLayout.invalidate()
            bubbleLayout.requestLayout()

            if (isBordersNotSet) {
                phraseFrame.setPadding(0, 0, 0, 0)
                setPaddings(true, this)
            } else {
                setPadding(0, 0, 0, 0)
                (image.layoutParams as FrameLayout.LayoutParams).apply {
                    setMargins(borderLeft, borderTop, borderRight, borderBottom)
                    image.layoutParams = this
                }
                phraseFrame.setPadding(
                    borderLeft,
                    0,
                    borderRight,
                    resources.getDimensionPixelSize(style.bubbleIncomingPaddingBottom)
                )
            }
            image.invalidate()
            image.requestLayout()
        } else {
            (bubbleLayout.layoutParams as MarginLayoutParams).let {
                it.width = ActionBar.LayoutParams.WRAP_CONTENT
                it.height = ActionBar.LayoutParams.WRAP_CONTENT
                it.marginEnd = resources.getDimensionPixelSize(R.dimen.ecc_user_margin_right)
            }
            bubbleLayout.invalidate()
            bubbleLayout.requestLayout()

            imageRoot.gone()

            phraseFrame.setPadding(0, 0, 0, 0)
            setPaddings(true, this)
        }
    }

    private fun getFileDescriptionText(fileDescription: FileDescription): String {
        return "${FileUtils.getFileName(fileDescription)} " +
            if (fileDescription.size > 0) {
                fileDescription.size.toFileSize().trimIndent()
            } else {
                ""
            }
    }

    private fun loadImage(
        fileDescription: FileDescription,
        imageClickListener: View.OnClickListener,
        isExternalImage: Boolean = false
    ) {
        val fileUri = if (fileDescription.fileUri?.toString()?.isNotBlank() == true) {
            fileDescription.fileUri.toString()
        } else {
            fileDescription.downloadPath
        }
        loadImage(fileUri, imageClickListener, isExternalImage)
    }

    private fun loadImage(
        imagePath: String?,
        imageClickListener: View.OnClickListener,
        isExternalImage: Boolean = false
    ) {
        hideErrorImage(imageLayout, errorImage)
        fileRow.gone()
        circularProgressButton.gone()
        image.visible()
        imageRoot.visible()
        image.setOnClickListener(imageClickListener)
        startLoaderAnimation()
        val loadConfig = ImageLoader
            .get()
            .load(imagePath)
            .autoRotateWithExif(true)
            .scales(ImageView.ScaleType.FIT_XY, ImageView.ScaleType.CENTER_CROP)
            .modifications(maskedTransformation)
            .callback(object : ImageLoader.ImageLoaderCallback {
                override fun onImageLoaded() {
                    stopLoaderAnimation()
                }

                override fun onImageLoadError() {
                    showErrorImage(imageLayout, errorImage)
                    stopLoaderAnimation()
                }
            })
        if (isExternalImage) {
            loadConfig.disableEdnaSsl()
        }
        loadConfig.into(image)
    }

    private fun showPhrase(
        consultPhrase: ConsultPhrase,
        phrase: String
    ) {
        phraseTextView.bindTimestampView(timeStampTextView)
        phraseTextView.visible()
        val deeplink = UrlUtils.extractDeepLink(phrase)
        val extractedLink = bindOGData(phrase)
        highlightOperatorText(phraseTextView, consultPhrase, deeplink ?: extractedLink?.link)
    }

    private fun showQuote(
        quote: Quote,
        onQuoteClickListener: View.OnClickListener
    ) {
        fileRow.visibility = View.VISIBLE
        fileImage.visibility = View.GONE
        circularProgressButton.visibility = View.GONE
        rightTextHeader.text = if (quote.phraseOwnerTitle == null) {
            itemView.context
                .getString(R.string.ecc_I)
        } else {
            quote.phraseOwnerTitle
        }
        rightTextDescription.text = quote.text
        rightTextFileStamp.text = itemView.context
            .getString(R.string.ecc_sent_at, quoteSdf.format(Date(quote.timeStamp)))
        viewUtils.setClickListener(fileRow as ViewGroup, onQuoteClickListener)
        val quoteFileDescription = quote.fileDescription
        if (quoteFileDescription != null) {
            if (FileUtils.isVoiceMessage(quoteFileDescription)) {
                rightTextDescription.setText(R.string.ecc_voice_message)
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
        initAnimation(loaderImage, true)
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
        imageRoot.isVisible = false
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
        imageRoot.isVisible = false
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
        imageRoot.isVisible = false
        fileRow.isVisible = true
        errorTextView.isVisible = false
        circularProgressButton.isVisible = true
        rotateAnim.cancel()
        val isStateReady = fileDescription.state == AttachmentStateEnum.READY
        if (isStateReady && isImage(fileDescription)) {
            loadImage(fileDescription, imageClickListener)
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
                R.string.ecc_sent_at,
                quoteSdf.format(Date(fileDescription.timeStamp))
            )
            circularProgressButton.setProgress(if (fileDescription.fileUri != null) 100 else fileDescription.downloadProgress)
        }
    }

    private fun showAvatar(consultPhrase: ConsultPhrase) {
        if (consultPhrase.isAvatarVisible) {
            if (consultAvatar.isInvisible) consultAvatar.visible()
            consultPhrase.avatarPath?.let {
                consultAvatar.loadImage(
                    FileUtils.convertRelativeUrlToAbsolute(consultPhrase.avatarPath),
                    listOf(ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.FIT_XY),
                    errorDrawableResId = R.drawable.ecc_operator_avatar_placeholder,
                    autoRotateWithExif = true,
                    modifications = listOf(ImageModifications.CircleCropModification),
                    noPlaceholder = true
                )
            } ?: run {
                consultAvatar.setImageResource(style.defaultOperatorAvatar)
            }
        } else {
            consultAvatar.invisible()
        }
    }
}
