package im.threads.ui.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import im.threads.R
import im.threads.business.extensions.withMainContext
import im.threads.business.imageLoading.ImageModifications
import im.threads.business.imageLoading.loadImage
import im.threads.business.models.CampaignMessage
import im.threads.business.models.FileDescription
import im.threads.business.models.Quote
import im.threads.business.models.enums.ModificationStateEnum
import im.threads.business.serviceLocator.core.inject
import im.threads.business.utils.ClientUseCase
import im.threads.business.utils.FileUtils
import im.threads.business.utils.toFileSize
import im.threads.ui.config.Config
import im.threads.ui.utils.ColorsHelper
import im.threads.ui.utils.ViewUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuoteHolderView : FrameLayout {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val viewUtils = ViewUtils()
    private val clientUseCase: ClientUseCase by inject()
    private val config: Config by lazy { Config.getInstance() }
    private val style = config.chatStyle

    private var rootLayout: ViewGroup
    private var bgImage: ImageView
    private var delimiterImage: ImageView
    private var quoteAuthor: AppCompatTextView
    private var quoteMessage: AppCompatTextView
    private var quoteImage: ImageView

    private var imageMask: Drawable? = ContextCompat.getDrawable(context, R.drawable.ecc_quote_image_mask)
    private val fileIconBackground = AppCompatResources.getDrawable(context, R.drawable.ecc_circle_gray_48dp)?.mutate()
    private val fileIconImage = AppCompatResources.getDrawable(context, style.completedIconResId)?.mutate()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)
    constructor(context: Context, attr: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attr,
        defStyleAttr
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.ecc_quote_holder_view, this, true)
        quoteAuthor = findViewById(R.id.quoteAuthor)
        quoteMessage = findViewById(R.id.quoteMessage)
        quoteImage = findViewById(R.id.quoteImage)
        rootLayout = findViewById(R.id.rootLayout)
        bgImage = findViewById(R.id.bgImage)
        delimiterImage = findViewById(R.id.delimiterImage)
    }

    private fun applyStyle(isIncoming: Boolean) {
        delimiterImage.setImageResource(style.quoteBackgroundResId)
        bgImage.setImageResource(style.quoteBackgroundResId)
        if (isIncoming) {
            ColorsHelper.setTint(context, delimiterImage, style.quoteIncomingDelimiterColorRes)
            ColorsHelper.setTint(context, bgImage, style.quoteIncomingBackgroundColorRes)
            quoteAuthor.setTextColor(ContextCompat.getColor(context, style.quoteIncomingAuthorTextColorRes))
            quoteMessage.setTextColor(ContextCompat.getColor(context, style.quoteIncomingTextColorRes))
            ColorsHelper.setDrawableColor(context, fileIconBackground, style.quoteIncomingDelimiterColorRes)
        } else {
            ColorsHelper.setTint(context, delimiterImage, style.quoteOutgoingDelimiterColorRes)
            ColorsHelper.setTint(context, bgImage, style.quoteOutgoingBackgroundColorRes)
            quoteAuthor.setTextColor(ContextCompat.getColor(context, style.quoteOutgoingAuthorTextColorRes))
            quoteMessage.setTextColor(ContextCompat.getColor(context, style.quoteOutgoingTextColorRes))
            ColorsHelper.setDrawableColor(context, fileIconBackground, style.quoteOutgoingDelimiterColorRes)
        }
        ColorsHelper.setDrawableColor(context, fileIconImage, style.quoteIconColorRes)
    }

    fun showCampaign(campaignMessage: CampaignMessage) {
        applyStyle(false)
        quoteMessage.text = campaignMessage.text
        quoteAuthor.text = campaignMessage.senderName
    }

    fun showQuote(
        quote: Quote,
        onQuoteClickListener: OnClickListener?,
        isIncoming: Boolean
    ) {
        applyStyle(isIncoming)
        quoteImage.isVisible = false
        if (quote.modified == ModificationStateEnum.DELETED) {
            quoteMessage.setTextColor(ContextCompat.getColor(context, style.systemMessageTextColorResId))
            quoteMessage.text = context.getString(R.string.ecc_message_deleted)
        } else {
            val quoteFileDescription = quote.fileDescription
            if (quoteFileDescription != null) {
                showFile(quoteFileDescription, onQuoteClickListener)
            }
            quoteAuthor.text =
                if (quote.phraseOwnerTitle == null) {
                    clientUseCase.getUserInfo()?.userName ?: context.getString(R.string.ecc_you)
                } else {
                    quote.phraseOwnerTitle
                }
            quoteMessage.setTextColor(ContextCompat.getColor(context, style.incomingMessageTextColor))
            if (!quote.text.isNullOrEmpty()) {
                quoteMessage.text = quote.text
            }
            viewUtils.setClickListener(rootLayout, onQuoteClickListener)
        }
    }

    private fun showFile(
        fileDescription: FileDescription,
        onQuoteClickListener: OnClickListener?
    ) {
        if (FileUtils.isVoiceMessage(fileDescription)) {
            quoteMessage.setText(R.string.ecc_voice_message)
        } else {
            if (FileUtils.isImage(fileDescription)) {
                quoteImage.isVisible = true
                quoteImage.background = null
                quoteImage.setImageDrawable(null)
                showImage(fileDescription)
                quoteImage.setPadding(0, 0, 0, 0)
                quoteImage.setOnClickListener(onQuoteClickListener)
                quoteMessage.setText(R.string.ecc_photo_message)
            } else {
                fileNameFromDescription(fileDescription) { fileName ->
                    quoteMessage.text = getFileDescriptionText(fileName, fileDescription)
                }
                quoteImage.isVisible = true
                quoteImage.background = fileIconBackground
                quoteImage.setImageDrawable(fileIconImage)
                val padding = resources.getDimensionPixelSize(R.dimen.ecc_margin_half)
                quoteImage.setPadding(padding, padding, padding, padding)
            }
        }
    }

    private fun showImage(fileDescription: FileDescription) {
        val fileUri = fileDescription.fileUri?.toString() ?: fileDescription.downloadPath
        val transformations = ArrayList<ImageModifications>()
        imageMask?.let {
            transformations.add(ImageModifications.MaskedModification(it))
        }
        if (!fileUri.isNullOrEmpty()) {
            quoteImage.loadImage(
                fileUri,
                listOf(
                    ImageView.ScaleType.FIT_CENTER,
                    ImageView.ScaleType.CENTER_CROP
                ),
                style.imagePlaceholder,
                transformations,
                autoRotateWithExif = true
            )
        } else {
            quoteImage.setImageResource(style.imagePlaceholder)
        }
    }

    private fun getFileDescriptionText(
        fileName: String?,
        fileDescription: FileDescription
    ): String {
        return (fileName ?: "file") +
            if (fileDescription.size > 0) {
                " ${fileDescription.size.toFileSize().trimIndent()}"
            } else {
                ""
            }
    }

    private fun fileNameFromDescription(
        fileDescription: FileDescription,
        callback: (fileName: String?) -> Unit
    ) {
        if (fileDescription.incomingName != null) {
            callback(fileDescription.incomingName)
        } else {
            coroutineScope.launch {
                val fileName = FileUtils.getFileName(fileDescription.fileUri)
                withMainContext { callback(fileName) }
            }
        }
    }
}
