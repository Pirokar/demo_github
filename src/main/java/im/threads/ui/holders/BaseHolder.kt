package im.threads.ui.holders

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import im.threads.R
import im.threads.business.extensions.withMainContext
import im.threads.business.formatters.RussianFormatSymbols
import im.threads.business.imageLoading.ImageLoader
import im.threads.business.imageLoading.ImageModifications
import im.threads.business.imageLoading.loadImage
import im.threads.business.logger.LoggerEdna
import im.threads.business.markdown.LinkifyLinksHighlighter
import im.threads.business.markdown.LinksHighlighter
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.ExtractedLink
import im.threads.business.models.FileDescription
import im.threads.business.models.MessageStatus
import im.threads.business.models.Quote
import im.threads.business.models.enums.ErrorStateEnum
import im.threads.business.ogParser.OGData
import im.threads.business.ogParser.OGDataContent
import im.threads.business.ogParser.OpenGraphParser
import im.threads.business.serviceLocator.core.inject
import im.threads.business.utils.FileDownloader
import im.threads.business.utils.FileProvider
import im.threads.business.utils.FileUtils
import im.threads.business.utils.FileUtils.generateFileName
import im.threads.business.utils.UrlUtils
import im.threads.business.utils.toFileSize
import im.threads.ui.config.Config
import im.threads.ui.utils.ColorsHelper
import im.threads.ui.utils.NoLongClickMovementMethod
import im.threads.ui.utils.ScreenSizeGetter
import im.threads.ui.utils.ViewUtils
import im.threads.ui.utils.gone
import im.threads.ui.utils.invisible
import im.threads.ui.utils.visible
import im.threads.ui.views.CircularProgressButton
import im.threads.ui.widget.textView.BubbleMessageTextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

abstract class BaseHolder internal constructor(
    itemView: View,
    private val highlightingStream: PublishSubject<ChatItem>? = null,
    private val openGraphParser: OpenGraphParser? = null
) : RecyclerView.ViewHolder(itemView) {
    private val fileProvider: FileProvider by inject()
    private var currentChatItem: ChatItem? = null
    private var viewsToHighlight: Array<out View>? = null
    private var isThisItemHighlighted = false
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val linksHighlighter: LinksHighlighter = LinkifyLinksHighlighter()
    protected val coroutineScope = CoroutineScope(Dispatchers.IO)
    protected val rotateAnim = RotateAnimation(
        0f,
        360f,
        Animation.RELATIVE_TO_SELF,
        0.5f,
        Animation.RELATIVE_TO_SELF,
        0.5f
    )
    private var ogDataContent: OGDataContent? = null
    val viewUtils = ViewUtils()

    @SuppressLint("SimpleDateFormat")
    protected var quoteSdf = if (Locale.getDefault().language.equals("ru", ignoreCase = true)) {
        SimpleDateFormat("dd MMMM yyyy", RussianFormatSymbols())
    } else {
        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    }

    protected fun subscribeForOpenGraphData(ogDataContent: OGDataContent) {
        this.ogDataContent = ogDataContent

        if (openGraphParser?.openGraphParsingStream != null) {
            compositeDisposable.add(
                openGraphParser.openGraphParsingStream
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        onOgDataReceived(it)
                    }, {
                        LoggerEdna.error("Error when receiving OGData", it)
                    })
            )
        }
    }

    /**
     * Подписывается на уведомления о новом подсвеченном элементе
     *
     * @param chatItem новый подсвеченный элемент
     */
    fun subscribeForHighlighting(chatItem: ChatItem, vararg viewsToHighlight: View) {
        currentChatItem = chatItem
        this.viewsToHighlight = viewsToHighlight
        if (highlightingStream != null) {
            compositeDisposable.add(
                highlightingStream.observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ streamChatItem ->
                        val isOurItem = currentChatItem?.isTheSameItem(streamChatItem) ?: false
                        val needToHighlight = (isOurItem && !isThisItemHighlighted) ||
                            (isThisItemHighlighted && !isOurItem)
                        if (needToHighlight) {
                            changeHighlighting(isOurItem)
                        }
                    }, {
                        LoggerEdna.error("Error when trying to get highlighted item", it)
                    })
            )
        }
    }

    val config: Config by lazy { Config.getInstance() }
    val style = config.chatStyle

    protected fun subscribe(event: Disposable): Boolean {
        if (compositeDisposable.isDisposed) {
            compositeDisposable = CompositeDisposable()
        }
        return compositeDisposable.add(event)
    }

    @ColorInt
    fun getColorInt(@ColorRes colorRes: Int): Int {
        return ContextCompat.getColor(itemView.context, colorRes)
    }

    fun setTextColorToViews(views: Array<TextView>, @ColorRes colorRes: Int) {
        for (tv in views) {
            tv.setTextColor(getColorInt(colorRes))
        }
    }

    fun setUpProgressButton(button: CircularProgressButton) {
        val chatStyle = style
        val downloadButtonTintResId = chatStyle.downloadButtonTintResId
        val startDownload = setUpDrawable(chatStyle.startDownloadIconResId, downloadButtonTintResId)
        val inProgress = setUpDrawable(chatStyle.inProgressIconResId, downloadButtonTintResId)
        val completed = setUpDrawable(chatStyle.completedIconResId, downloadButtonTintResId)
        button.setStartDownloadDrawable(startDownload)
        button.setInProgress(inProgress)
        button.setCompletedDrawable(completed)
        button.setBackgroundColorResId(style.downloadButtonBackgroundTintResId)
    }

    fun onClear() {
        compositeDisposable.clear()
    }

    /**
     * Меняет подсветку бэкраунда при выделении
     *
     * @param isHighlighted информация о том, должен ли быть подсвечен
     *     бэкграунд у сообщения
     */
    open fun changeHighlighting(isHighlighted: Boolean) {
        val views = viewsToHighlight ?: arrayOf(itemView.rootView)
        views.forEach {
            it.setBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    if (isHighlighted) style.chatHighlightingColor else R.color.ecc_transparent
                )
            )
        }

        isThisItemHighlighted = isHighlighted
    }

    /**
     * Подсвечивает ссылки, email, номера телефонов. Если поле formattedText
     * внутри phrase не будет пустым, производится форматирование текста.
     *
     * @param textView вью, где необходимо произвести обработку - подсветку, форматирование
     * @param formattedText форматированный текст с markdown
     * @param usualText обычный текст без форматирования
     * @param url url, содержащийся в сообщении (если известен)
     */
    protected fun highlightOperatorText(
        textView: TextView,
        formattedText: String? = null,
        usualText: String? = null,
        url: String? = null,
        emails: List<String> = arrayListOf()
    ) {
        if (!formattedText.isNullOrBlank()) {
            (textView as? BubbleMessageTextView)?.let {
                val emailLinksPairs = ArrayList<Pair<String?, View.OnClickListener>>()
                emails.forEach { email ->
                    emailLinksPairs.add(
                        Pair(
                            email,
                            View.OnClickListener {
                                val intent = Intent(Intent.ACTION_VIEW)
                                val data = Uri.parse("mailto:$email")
                                intent.data = data
                                startActivity(itemView.context, intent, null)
                            }
                        )
                    )
                }
                it.setFormattedText(
                    formattedText,
                    true,
                    emailLinksPairs
                )
            } ?: run {
                setMovementMethod(textView)
                textView.setText(usualText?.trimIndent(), TextView.BufferType.NORMAL)
            }
        } else if (!usualText.isNullOrEmpty()) {
            textView.setText(usualText.trimIndent(), TextView.BufferType.NORMAL)
            setTextWithHighlighting(
                textView,
                style.incomingMarkdownConfiguration.isLinkUnderlined,
                url
            )
        }
    }

    /**
     * Подсчвечивает ссылки, email, номера телефонов.
     *
     * @param textView вью, где необходимо произвести подсветку
     * @param phrase текст для отображение во вью
     * @param url url, содержащийся в сообщении (если известен)
     */
    protected fun highlightClientText(
        textView: BubbleMessageTextView,
        phrase: String,
        url: String? = null
    ) {
        textView.setText(phrase, TextView.BufferType.NORMAL)
        setTextWithHighlighting(
            textView,
            style.outgoingMarkdownConfiguration.isLinkUnderlined,
            url
        )
    }

    /**
     * Возвращает нужный ресурс [Drawable] в зависимости от кода ошибки
     *
     * @param code код ошибки
     */
    protected fun getErrorImageResByErrorCode(code: ErrorStateEnum) = when (code) {
        ErrorStateEnum.DISALLOWED -> R.drawable.ecc_im_wrong_file
        ErrorStateEnum.TIMEOUT -> R.drawable.ecc_im_unexpected
        ErrorStateEnum.UNEXPECTED -> R.drawable.ecc_im_unexpected
        ErrorStateEnum.ANY -> R.drawable.ecc_im_unexpected
    }

    /**
     * Возвращает нужный текстовый ресурс [StringRes] в зависимости от кода
     * ошибки
     *
     * @param code код ошибки
     */
    protected fun getErrorStringResByErrorCode(code: ErrorStateEnum) = when (code) {
        ErrorStateEnum.DISALLOWED -> R.string.ecc_disallowed_error_during_load_file
        ErrorStateEnum.TIMEOUT -> R.string.ecc_timeout_error_during_load_file
        ErrorStateEnum.UNEXPECTED -> R.string.ecc_some_error_during_load_file
        ErrorStateEnum.ANY -> R.string.ecc_some_error_during_load_file
    }

    /**
     * Прячет лэйаут изображения, отображает картинку с ошибкой и присваивает
     * ей ресурс ошибки из стилей
     *
     * @param imageLayout - контейнер с изображением
     * @param errorImage - картинка с ошибкой
     */
    protected fun showErrorImage(imageLayout: ViewGroup, errorImage: ImageView) {
        imageLayout.invisible()
        errorImage.visible()
        errorImage.setImageResource(style.imagePlaceholder)
    }

    /**
     * Прячет изображение с ошибкой, отображает картинку
     *
     * @param imageLayout - контейнер с изображением
     * @param errorImage - картинка с ошибкой
     */
    protected fun hideErrorImage(imageLayout: ViewGroup, errorImage: ImageView) {
        errorImage.gone()
        imageLayout.visible()
    }

    protected fun bindOGData(messageText: String?): ExtractedLink? {
        var extractedLink: ExtractedLink? = null

        val link = if (messageText != null) {
            extractedLink = UrlUtils.extractLink(messageText)
            if (extractedLink != null && extractedLink.isEmail) {
                null
            } else if (extractedLink?.link != null && !extractedLink.link!!.startsWith("http")) {
                "https://${extractedLink.link}"
            } else {
                extractedLink?.link
            }
        } else {
            null
        }
        ogDataContent?.url = link ?: ""

        if (ogDataContent?.ogDataLayout?.get()?.tag == link) {
            return extractedLink
        } else {
            openGraphParser?.getCachedContents(link)?.let {
                openGraphParser.openGraphParsingStream.onNext(it)
            } ?: hideOGView()
        }

        coroutineScope.launch(Dispatchers.Main) {
            if (openGraphParser != null) {
                val requestJob = async(Dispatchers.IO) {
                    openGraphParser.getContents(link, messageText)
                }
                openGraphParser.openGraphParsingStream.onNext(requestJob.await())
            }
        }

        return extractedLink
    }

    private fun onOgDataReceived(ogData: OGData) {
        ogDataContent?.let { ogDataContent ->
            if (ogData.messageText == ogDataContent.messageText) {
                ogDataContent.ogDataLayout.get()?.let { ogDataLayout ->
                    val ogImage: ImageView = ogDataLayout.findViewById(R.id.og_image)

                    if (ogData.isEmpty()) {
                        hideOGView()
                        return
                    }

                    val ogTitle: TextView = ogDataLayout.findViewById(R.id.og_title)
                    val ogDescription: TextView = ogDataLayout.findViewById(R.id.og_description)
                    val ogUrl: BubbleMessageTextView = ogDataLayout.findViewById(R.id.og_url)

                    setColorToDivider(ogDataContent)
                    showOGView()
                    setTimeStamp(ogDataContent)
                    setOgDataTitle(ogData, ogTitle)
                    setOgDataDescription(ogData, ogDescription)
                    setOgDataUrl(ogUrl, ogData)
                    setOgDataImage(ogData, ogImage)

                    viewUtils.setClickListener(
                        ogDataLayout,
                        View.OnClickListener { _ ->
                            UrlUtils.openUrl(
                                ogDataLayout.context,
                                ogDataContent.url
                            )
                        }
                    )

                    ogDataLayout.tag = ogDataContent.url
                }

                ogDataContent.ogDataLayout.clear()
                ogDataContent.ogTimeStampView.clear()
            }
        }
    }

    private fun setColorToDivider(ogDataContent: OGDataContent) {
        ogDataContent.ogDataLayout.get()?.findViewById<View>(R.id.ogDivider)?.let { divider ->
            val color = if (this is UserPhraseViewHolder) {
                style.incomingMessageBubbleColor
            } else {
                style.outgoingMessageBubbleColor
            }
            divider.setBackgroundColor(ContextCompat.getColor(itemView.context, color))
        }
    }

    private fun setTimeStamp(ogDataContent: OGDataContent) {
        ogDataContent.ogTimeStampView.get()?.let { timeStampView ->
            timeStampView.visible()
            if (this is ConsultPhraseHolder) {
                if (style.incomingImageTimeColor != 0) {
                    timeStampView.setTextColor(getColorInt(style.incomingImageTimeColor))
                }
                if (style.incomingMessageTimeTextSize != 0) {
                    timeStampView.textSize =
                        itemView.context.resources.getDimension(style.incomingMessageTimeTextSize)
                }
                if (style.incomingImageTimeBackgroundColor != 0 && timeStampView.background != null) {
                    timeStampView.background.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            getColorInt(style.incomingImageTimeBackgroundColor),
                            BlendModeCompat.SRC_ATOP
                        )
                }
            } else {
                if (style.outgoingImageTimeColor != 0) {
                    timeStampView.setTextColor(getColorInt(style.outgoingImageTimeColor))
                }
                if (style.outgoingMessageTimeTextSize != 0) {
                    timeStampView.textSize =
                        itemView.context.resources.getDimension(style.outgoingMessageTimeTextSize)
                }
                if (style.outgoingImageTimeBackgroundColor != 0) {
                    timeStampView.background.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            getColorInt(style.outgoingImageTimeBackgroundColor),
                            BlendModeCompat.SRC_ATOP
                        )
                }
            }
        }
    }

    private fun setOgDataTitle(ogData: OGData, ogTitle: TextView) {
        if (ogData.title.isNotEmpty()) {
            ogTitle.visibility = View.VISIBLE
            ogTitle.text = ogData.title
            ogTitle.setTypeface(ogTitle.typeface, Typeface.BOLD)
            ogTitle.setTextColor(textColor)
        } else {
            ogTitle.visibility = View.GONE
        }
    }

    private fun setOgDataDescription(ogData: OGData, ogDescription: TextView) {
        if (ogData.description.isNotEmpty()) {
            ogDescription.visibility = View.VISIBLE
            ogDescription.text = ogData.description
            ogDescription.setTextColor(textColor)
        } else {
            ogDescription.visibility = View.GONE
        }
    }

    private fun setOgDataUrl(ogUrl: BubbleMessageTextView, ogData: OGData) {
        val url = (ogData.url.ifEmpty { ogDataContent?.url }).let {
            "${Uri.parse(it).scheme}://${Uri.parse(it).host}"
        }
        ogUrl.setText(url, TextView.BufferType.NORMAL)
        ogUrl.setTextColor(textColor)
    }

    private fun setOgDataImage(ogData: OGData, ogImage: ImageView) {
        if (UrlUtils.isValidUrl(ogData.imageUrl)) {
            ogImage.visible()
            if (ogImage.tag != ogData.imageUrl) {
                ogImage.loadImage(
                    ogData.imageUrl,
                    errorDrawableResId = style.imagePlaceholder,
                    isExternalImage = true,
                    scales = listOf(ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.FIT_XY),
                    callback = object : ImageLoader.ImageLoaderCallback {
                        override fun onImageLoaded() {
                            super.onImageLoaded()
                            ogImage.tag = ogData.imageUrl
                        }

                        override fun onImageLoadError() {
                            ogImage.gone()
                        }
                    }
                )
            }
        } else {
            ogImage.gone()
        }
    }

    private fun showOGView() {
        ogDataContent?.ogDataLayout?.get()?.visible()
        ogDataContent?.ogTimeStampView?.get()?.visible()
        ogDataContent?.mainTimeStampView?.get()?.invisible()
    }

    private fun hideOGView() {
        ogDataContent?.ogDataLayout?.get()?.gone()
        ogDataContent?.ogTimeStampView?.get()?.gone()
        ogDataContent?.ogDataLayout?.get()?.tag = ""
        ogDataContent?.mainTimeStampView?.get()?.visible()
    }

    private val textColor: Int
        get() {
            val color = if (this is UserPhraseViewHolder) {
                style.outgoingMessageTextColor
            } else {
                style.incomingMessageTextColor
            }
            return ContextCompat.getColor(itemView.context, color)
        }

    private fun setTextWithHighlighting(
        textView: TextView,
        isUnderlined: Boolean,
        url: String? = null
    ) {
        setMovementMethod(textView)
        linksHighlighter.highlightAllTypeOfLinks(textView, url, isUnderlined)
    }

    private fun setMovementMethod(textView: TextView) {
        textView.movementMethod = NoLongClickMovementMethod.getInstance()
    }

    private fun setUpDrawable(@DrawableRes iconResId: Int, @ColorRes colorRes: Int): Drawable? {
        val drawable = AppCompatResources.getDrawable(itemView.context, iconResId)?.mutate()
        ColorsHelper.setDrawableColor(itemView.context, drawable, colorRes)
        return drawable
    }

    protected fun getString(@StringRes stringId: Int): String? {
        itemView.context?.let {
            return it.getString(stringId)
        }
        return null
    }

    protected fun initAnimation(view: ImageView, isIncomingMessage: Boolean) {
        val colorRes = if (isIncomingMessage) {
            style.incomingMessageLoaderColor
        } else {
            style.outgoingMessageLoaderColor
        }
        initAnimation(view, colorRes)
    }

    protected fun initAnimation(view: ImageView, @ColorRes colorOfLoader: Int) {
        view.setImageResource(R.drawable.ecc_im_loading_themed)
        ColorsHelper.setTint(
            itemView.context,
            view,
            colorOfLoader
        )
        rotateAnim.duration = 3000
        rotateAnim.repeatCount = Animation.INFINITE
        view.animation = rotateAnim
        rotateAnim.start()
    }

    protected fun cancelAnimation() {
        rotateAnim.cancel()
    }

    protected fun setPaddings(isIncomingMessage: Boolean, layout: ViewGroup) {
        val resources = itemView.context.resources
        if (isIncomingMessage) {
            layout.setPadding(
                resources.getDimensionPixelSize(style.bubbleIncomingPaddingLeft),
                resources.getDimensionPixelSize(style.bubbleIncomingPaddingTop),
                resources.getDimensionPixelSize(style.bubbleIncomingPaddingRight),
                resources.getDimensionPixelSize(style.bubbleIncomingPaddingBottom)
            )
        } else {
            layout.setPadding(
                resources.getDimensionPixelSize(style.bubbleOutgoingPaddingLeft),
                resources.getDimensionPixelSize(style.bubbleOutgoingPaddingTop),
                resources.getDimensionPixelSize(style.bubbleOutgoingPaddingRight),
                resources.getDimensionPixelSize(style.bubbleOutgoingPaddingBottom)
            )
        }
    }

    protected fun setLayoutMargins(isIncomingMessage: Boolean, layout: ViewGroup) {
        val resources = itemView.context.resources
        val layoutParams = layout.layoutParams as ViewGroup.MarginLayoutParams
        if (isIncomingMessage) {
            layoutParams.marginStart = resources.getDimensionPixelSize(style.bubbleIncomingMarginLeft)
            layoutParams.marginEnd = resources.getDimensionPixelSize(style.bubbleIncomingMarginRight)
            layoutParams.topMargin = resources.getDimensionPixelSize(style.bubbleIncomingMarginTop)
            layoutParams.bottomMargin = resources.getDimensionPixelSize(style.bubbleIncomingMarginBottom)
        } else {
            layoutParams.marginStart = resources.getDimensionPixelSize(style.bubbleOutgoingMarginLeft)
            layoutParams.marginEnd = resources.getDimensionPixelSize(style.bubbleOutgoingMarginRight)
            layoutParams.topMargin = resources.getDimensionPixelSize(style.bubbleOutgoingMarginTop)
            layoutParams.bottomMargin = resources.getDimensionPixelSize(style.bubbleOutgoingMarginBottom)
        }
        layout.layoutParams = layoutParams
        layout.invalidate()
        layout.requestLayout()
    }

    protected fun showAvatar(
        consultAvatar: ImageView,
        consultPhrase: ConsultPhrase,
        onAvatarClickListener: View.OnClickListener
    ) {
        consultAvatar.setOnClickListener(onAvatarClickListener)
        if (consultPhrase.isAvatarVisible) {
            consultAvatar.visible()
            if (!consultPhrase.avatarPath.isNullOrEmpty()) {
                consultAvatar.loadImage(
                    FileUtils.convertRelativeUrlToAbsolute(consultPhrase.avatarPath),
                    listOf(ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.FIT_XY),
                    errorDrawableResId = R.drawable.ecc_operator_avatar_placeholder,
                    modifications = listOf(ImageModifications.CircleCropModification),
                    noPlaceholder = true
                )
            } else {
                consultAvatar.setImageResource(style.defaultOperatorAvatar)
            }
        } else {
            consultAvatar.invisible()
        }
    }

    protected fun fileNameFromDescription(fileDescription: FileDescription?, callback: (fileName: String?) -> Unit) {
        if (fileDescription == null) {
            callback(null)
            return
        }

        if (fileDescription.incomingName != null) {
            callback(fileDescription.incomingName)
        } else {
            coroutineScope.launch {
                val fileName = FileUtils.getFileName(fileDescription.fileUri)
                withMainContext { callback(fileName) }
            }
        }
    }

    internal fun getImageViewSize(): Int {
        if (imageViewSize == null) {
            calculateImageSize()
        }
        return imageViewSize!!
    }

    private fun calculateImageSize() {
        val chatStyle = Config.getInstance().chatStyle
        val resources = itemView.context.resources
        val screenWidth = ScreenSizeGetter().getScreenSize(itemView.context).width
        val incomingBorderLeft = resources.getDimensionPixelSize(chatStyle.incomingImageLeftBorderSize)
        val incomingBorderRight = resources.getDimensionPixelSize(chatStyle.incomingImageRightBorderSize)
        val isIncomingBordersNotSet = incomingBorderLeft == 0 && incomingBorderRight == 0
        val outgoingBorderLeft = resources.getDimensionPixelSize(chatStyle.outgoingImageLeftBorderSize)
        val outgoingBorderRight = resources.getDimensionPixelSize(chatStyle.outgoingImageRightBorderSize)
        val outgoingMarginLeft = resources.getDimensionPixelSize(chatStyle.bubbleOutgoingMarginLeft)
        val outgoingMarginRight = resources.getDimensionPixelSize(chatStyle.bubbleOutgoingMarginRight)
        val incomingMarginLeft = resources.getDimensionPixelSize(chatStyle.bubbleIncomingMarginLeft)
        val incomingMarginRight = resources.getDimensionPixelSize(chatStyle.bubbleIncomingMarginRight)
        val outgoingPaddingLeft = resources.getDimensionPixelSize(chatStyle.bubbleOutgoingPaddingLeft)
        val outgoingPaddingRight = resources.getDimensionPixelSize(chatStyle.bubbleOutgoingPaddingRight)
        val incomingPaddingLeft = resources.getDimensionPixelSize(chatStyle.bubbleIncomingPaddingLeft)
        val incomingPaddingRight = resources.getDimensionPixelSize(chatStyle.bubbleIncomingPaddingRight)
        val isOutgoingBordersNotSet = outgoingBorderLeft == 0 && outgoingBorderRight == 0

        val incomingBordersAndMargins = if (isIncomingBordersNotSet) {
            incomingMarginRight + incomingMarginLeft + incomingPaddingLeft + incomingPaddingRight
        } else {
            incomingBorderLeft + incomingBorderRight + incomingMarginRight + incomingMarginLeft
        }

        val outgoingBordersAndMargins = if (isOutgoingBordersNotSet) {
            outgoingMarginRight + outgoingMarginLeft + outgoingPaddingLeft + outgoingPaddingRight
        } else {
            outgoingBorderLeft + outgoingBorderRight + outgoingMarginRight + outgoingMarginLeft
        }

        imageViewSize = if (incomingBordersAndMargins > outgoingBordersAndMargins) {
            screenWidth - incomingBordersAndMargins
        } else {
            screenWidth - outgoingBordersAndMargins
        }
    }

    protected fun setImageSize(view: View) {
        val size = getImageViewSize()
        val params: ViewGroup.LayoutParams = view.layoutParams
        params.width = size
        params.height = size
        view.layoutParams = params
    }

    protected fun getFileDescriptionText(fileName: String?, fileDescription: FileDescription): String {
        return (fileName ?: "file") +
            if (fileDescription.size > 0) {
                fileDescription.size.toFileSize().trimIndent()
            } else {
                ""
            }
    }

    protected fun showQuote(
        quote: Quote,
        onQuoteClickListener: View.OnClickListener,
        quoteLayout: LinearLayout,
        quoteTextHeader: TextView,
        quoteTextDescription: TextView,
        quoteTextTimeStamp: TextView,
        quoteFileImage: ImageView,
        quoteProgressButton: CircularProgressButton
    ) {
        quoteLayout.isVisible = true
        quoteTextHeader.text = if (quote.phraseOwnerTitle == null) {
            itemView.context
                .getString(R.string.ecc_I)
        } else {
            quote.phraseOwnerTitle
        }
        quoteProgressButton.isVisible = false
        quoteTextDescription.text = quote.text
        quoteTextTimeStamp.text = itemView.context
            .getString(R.string.ecc_sent_at, quoteSdf.format(Date(quote.timeStamp)))
        viewUtils.setClickListener(quoteLayout, onQuoteClickListener)
        val quoteFileDescription = quote.fileDescription
        if (quoteFileDescription != null) {
            if (FileUtils.isVoiceMessage(quoteFileDescription)) {
                quoteTextDescription.setText(R.string.ecc_voice_message)
            } else {
                if (FileUtils.isImage(quote.fileDescription)) {
                    quoteFileImage.visibility = View.VISIBLE
                    val fileUri = quoteFileDescription.fileUri?.toString() ?: quoteFileDescription.downloadPath
                    if (!fileUri.isNullOrEmpty()) {
                        quoteFileImage.loadImage(
                            quoteFileDescription.downloadPath,
                            listOf(ImageView.ScaleType.FIT_CENTER, ImageView.ScaleType.CENTER_CROP),
                            style.imagePlaceholder,
                            autoRotateWithExif = true
                        )
                    } else {
                        quoteFileImage.setImageResource(style.imagePlaceholder)
                    }
                    quoteFileImage.setOnClickListener(onQuoteClickListener)
                } else {
                    quoteProgressButton.isVisible = true
                    fileNameFromDescription(quoteFileDescription) { fileName ->
                        quoteTextDescription.text = getFileDescriptionText(fileName, quoteFileDescription)
                    }
                    quoteProgressButton.setOnClickListener(onQuoteClickListener)
                    quoteProgressButton.setProgress(if (quoteFileDescription.fileUri != null) 100 else quoteFileDescription.downloadProgress)
                }
            }
        }
    }

    protected fun getPreviewUri(fileDescription: FileDescription?): Uri? {
        fileDescription?.let {
            val file = File(FileDownloader.getDownloadDir(itemView.context), generateFileName(fileDescription))
            if (file.exists()) {
                return fileProvider.getUriForFile(itemView.context, file)
            }
        }
        return null
    }

    companion object {
        val statuses = HashMap<Long, MessageStatus?>()
        var imageViewSize: Int? = null
    }
}
