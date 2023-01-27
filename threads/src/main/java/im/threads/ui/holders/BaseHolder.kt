package im.threads.ui.holders

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import im.threads.R
import im.threads.business.imageLoading.ImageLoader
import im.threads.business.imageLoading.loadImage
import im.threads.business.logger.LoggerEdna
import im.threads.business.markdown.LinkifyLinksHighlighter
import im.threads.business.markdown.LinksHighlighter
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.ExtractedLink
import im.threads.business.models.enums.ErrorStateEnum
import im.threads.business.ogParser.OGData
import im.threads.business.ogParser.OGDataContent
import im.threads.business.ogParser.OpenGraphParser
import im.threads.business.utils.UrlUtils
import im.threads.ui.config.Config
import im.threads.ui.utils.ColorsHelper
import im.threads.ui.utils.NoLongClickMovementMethod
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

abstract class BaseHolder internal constructor(
    itemView: View,
    private val highlightingStream: PublishSubject<ChatItem>? = null,
    private val openGraphParser: OpenGraphParser? = null
) : RecyclerView.ViewHolder(itemView) {
    private var currentChatItem: ChatItem? = null
    private var viewsToHighlight: Array<out View>? = null
    private var isThisItemHighlighted = false
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val linksHighlighter: LinksHighlighter = LinkifyLinksHighlighter()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
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
    val style = config.getChatStyle()

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
        val downloadButtonTintResId = if (chatStyle.chatBodyIconsTint == 0) {
            chatStyle.downloadButtonTintResId
        } else {
            chatStyle.chatBodyIconsTint
        }
        val startDownload = setUpDrawable(chatStyle.startDownloadIconResId, downloadButtonTintResId)
        val inProgress = setUpDrawable(chatStyle.inProgressIconResId, downloadButtonTintResId)
        val completed = setUpDrawable(chatStyle.completedIconResId, downloadButtonTintResId)
        button.setStartDownloadDrawable(startDownload)
        button.setInProgress(inProgress)
        button.setCompletedDrawable(completed)
    }

    fun onClear() {
        compositeDisposable.clear()
    }

    /**
     * Меняет подсветку бэкраунда при выделении
     * @param isHighlighted информация о том, должен ли быть подсвечен бэкграунд у сообщения
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
     * Подсчвечивает ссылки, email, номера телефонов.
     * Если поле formattedText внутри phrase не будет пустым, производится форматирование текста.
     * @param textView вью, где необходимо произвести обработку - подсветку, форматирование
     * @param phrase данные для отображение во вью
     * @param url url, содержащийся в сообщении (если известен)
     */
    protected fun highlightOperatorText(
        textView: TextView,
        phrase: ConsultPhrase,
        url: String? = null
    ) {
        if (phrase.formattedPhrase.isNullOrBlank()) {
            textView.setText(phrase.phraseText, TextView.BufferType.NORMAL)
        } else {
            (textView as? BubbleMessageTextView)?.setFormattedText(phrase.formattedPhrase, true) ?: run {
                textView.setText(phrase.phraseText, TextView.BufferType.NORMAL)
            }
        }
        setTextWithHighlighting(
            textView,
            style.incomingMarkdownConfiguration.isLinkUnderlined,
            url
        )
    }

    /**
     * Подсчвечивает ссылки, email, номера телефонов.
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
     * @param code код ошибки
     */
    protected fun getErrorImageResByErrorCode(code: ErrorStateEnum) = when (code) {
        ErrorStateEnum.DISALLOWED -> R.drawable.ecc_im_wrong_file
        ErrorStateEnum.TIMEOUT -> R.drawable.ecc_im_unexpected
        ErrorStateEnum.UNEXPECTED -> R.drawable.ecc_im_unexpected
        ErrorStateEnum.ANY -> R.drawable.ecc_im_unexpected
    }

    /**
     * Возвращает нужный текстовый ресурс [StringRes] в зависимости от кода ошибки
     * @param code код ошибки
     */
    protected fun getErrorStringResByErrorCode(code: ErrorStateEnum) = when (code) {
        ErrorStateEnum.DISALLOWED -> R.string.ecc_disallowed_error_during_load_file
        ErrorStateEnum.TIMEOUT -> R.string.ecc_timeout_error_during_load_file
        ErrorStateEnum.UNEXPECTED -> R.string.ecc_some_error_during_load_file
        ErrorStateEnum.ANY -> R.string.ecc_some_error_during_load_file
    }

    /**
     * Прячет лэйаут изображения, отображает картинку с ошибкой и присваивает ей ресурс ошибки из стилей
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

                    alignUrlAndTime(ogDataContent, ogUrl)
                    showOGView()
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
                ogDataContent.timeStampView.clear()
            }
        }
    }

    private fun alignUrlAndTime(ogDataContent: OGDataContent, ogTextView: BubbleMessageTextView) {
        ogDataContent.timeStampView.get()?.let { timeStampView ->
            ogTextView.bindTimestampView(timeStampView)
        }
    }

    private fun setOgDataTitle(ogData: OGData, ogTitle: TextView) {
        if (ogData.title.isNotEmpty()) {
            ogTitle.visibility = View.VISIBLE
            ogTitle.text = ogData.title
            ogTitle.setTypeface(ogTitle.typeface, Typeface.BOLD)
        } else {
            ogTitle.visibility = View.GONE
        }
    }

    private fun setOgDataDescription(ogData: OGData, ogDescription: TextView) {
        if (ogData.description.isNotEmpty()) {
            ogDescription.visibility = View.VISIBLE
            ogDescription.text = ogData.description
        } else {
            ogDescription.visibility = View.GONE
        }
    }

    private fun setOgDataUrl(ogUrl: BubbleMessageTextView, ogData: OGData) {
        val url = (ogData.url.ifEmpty { ogDataContent?.url }).let {
            "${Uri.parse(it).scheme}://${Uri.parse(it).host}"
        }
        ogUrl.setText(url, TextView.BufferType.NORMAL)
    }

    private fun setOgDataImage(ogData: OGData, ogImage: ImageView) {
        if (UrlUtils.isValidUrl(ogData.imageUrl)) {
            ogImage.visible()
            if (ogImage.tag != ogData.imageUrl) {
                ogImage.loadImage(
                    ogData.imageUrl,
                    errorDrawableResId = style.imagePlaceholder,
                    isExternalImage = true,
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
        ogDataContent?.timeStampView?.get()?.gone()
    }

    private fun hideOGView() {
        ogDataContent?.ogDataLayout?.get()?.gone()
        ogDataContent?.timeStampView?.get()?.visible()
        ogDataContent?.ogDataLayout?.get()?.tag = ""
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
}
