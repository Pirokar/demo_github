package im.threads.ui.holders

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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.isVisible
import im.threads.R
import im.threads.business.imageLoading.ImageLoader
import im.threads.business.imageLoading.ImageModifications
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.FileDescription
import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.models.enums.ModificationStateEnum
import im.threads.business.ogParser.OGDataContent
import im.threads.business.ogParser.OpenGraphParser
import im.threads.business.utils.FileUtils.isImage
import im.threads.business.utils.UrlUtils
import im.threads.ui.config.Config
import im.threads.ui.utils.ColorsHelper
import im.threads.ui.utils.gone
import im.threads.ui.utils.visible
import im.threads.ui.views.CircularProgressButton
import im.threads.ui.widget.textView.BubbleMessageTextView
import im.threads.ui.widget.textView.BubbleTimeTextView
import io.reactivex.subjects.PublishSubject
import java.lang.ref.WeakReference
import java.util.Date

/** layout/item_consultant_text_with_file.xml */
class ConsultPhraseHolder(
    private val parent: ViewGroup,
    private val maskedTransformation: ImageModifications.MaskedModification?,
    highlightingStream: PublishSubject<ChatItem>,
    openGraphParser: OpenGraphParser
) : BaseHolder(
    LayoutInflater.from(parent.context)
        .inflate(R.layout.ecc_item_consultant_text_with_file, parent, false),
    highlightingStream,
    openGraphParser
) {
    private val fileRow: View = itemView.findViewById(R.id.rightTextRow)
    private val quoteLayout: LinearLayout = itemView.findViewById(R.id.quoteLayout)
    private val quoteTextHeader: TextView = itemView.findViewById(R.id.quoteTo)
    private val quoteTextDescription: TextView = itemView.findViewById(R.id.quoteFileSpecs)
    private val quoteTextTimeStamp: TextView = itemView.findViewById(R.id.quoteSendAt)
    private val quoteFileImage = itemView.findViewById<ImageView>(R.id.quoteFileImage)
    private val quoteProgressButton: CircularProgressButton = itemView.findViewById(R.id.quoteButtonDownload)
    private val circularProgressButton: CircularProgressButton = itemView.findViewById(R.id.buttonDownload)
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
            .setBackgroundColor(getColorInt(style.incomingDelimitersColor))
        itemView.findViewById<View>(R.id.quoteDelimiter)
            .setBackgroundColor(getColorInt(style.incomingDelimitersColor))
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
        showConsultTimeStamp(consultPhrase, listOf(ogTimestamp, timeStampTextView))
        imageLayout.isVisible = false
        fileRow.isVisible = false
        quoteLayout.isVisible = false
        imageRoot.isVisible = false
        setLayoutMargins(true, bubbleLayout)
        showAvatar(consultAvatar, consultPhrase, onAvatarClickListener)
        changeHighlighting(highlighted)

        if (consultPhrase.modified == ModificationStateEnum.DELETED) {
            phraseTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemView.getContext().resources.getDimension(style.systemMessageTextSize))
            phraseTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), style.systemMessageTextColorResId))
            phraseTextView.bindTimestampView(timeStampTextView)
            phraseTextView.visible()
            phraseTextView.text = this.parent.context.getString(R.string.ecc_message_deleted)
        } else {
            setTextColorToViews(arrayOf(phraseTextView), style.incomingMessageTextColor)
            phraseTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemView.getContext().resources.getDimension(R.dimen.ecc_text_medium))
            phraseTextView.text = ""
            consultPhrase.phraseText?.let {
                showPhrase(consultPhrase, it.trim())
            } ?: run {
                phraseTextView.isVisible = false
            }

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
            if (consultPhrase.fileDescription != null || consultPhrase.quote != null) {
                phraseFrame.layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
            } else {
                phraseFrame.layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT
            }
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

        setImageSize(imageRoot)
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
                quoteLayout.setPadding(0, 0, 0, 0)
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
                quoteLayout.setPadding(
                    borderLeft,
                    resources.getDimensionPixelSize(style.bubbleIncomingPaddingTop),
                    borderRight,
                    0
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
            quoteLayout.setPadding(0, 0, 0, 0)
            setPaddings(true, this)
        }
    }

    private fun loadImage(
        fileDescription: FileDescription,
        imageClickListener: View.OnClickListener,
        isExternalImage: Boolean = false
    ) {
        fileDescription.getPreviewFileDescription()?.let {
            val previewUri = getPreviewUri(it.getPreviewFileDescription())
            val fileUri = if (previewUri?.toString().isNullOrBlank()) {
                it.downloadPath
            } else {
                previewUri?.toString()
            }
            loadImage(fileUri, imageClickListener, isExternalImage)
        }
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
        if (!imagePath.isNullOrEmpty()) {
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
        } else {
            image.setImageResource(style.imagePlaceholder)
        }
    }

    private fun showPhrase(
        consultPhrase: ConsultPhrase,
        phrase: String
    ) {
        phraseTextView.bindTimestampView(timeStampTextView)
        phraseTextView.visible()
        val deeplink = UrlUtils.extractDeepLink(phrase)
        val extractedLink = if (style.linkPreviewEnabled) bindOGData(phrase) else UrlUtils.extractLink(phrase)
        val emails = UrlUtils.extractEmailAddresses(phrase)
        highlightOperatorText(
            phraseTextView,
            consultPhrase.formattedPhrase,
            consultPhrase.phraseText,
            deeplink ?: extractedLink?.link,
            emails
        )
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
            fileNameFromDescription(fileDescription) { fileName ->
                getFileDescriptionText(fileName, fileDescription)
            }
            rightTextFileStamp.text = itemView.context.getString(
                R.string.ecc_sent_at,
                quoteSdf.format(Date(fileDescription.timeStamp))
            )
            circularProgressButton.setProgress(if (fileDescription.fileUri != null) 100 else fileDescription.downloadProgress)
        }
    }
}
