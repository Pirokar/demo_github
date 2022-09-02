package im.threads.internal.holders

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.View
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
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.enums.ErrorStateEnum
import im.threads.business.ogParser.OGData
import im.threads.business.ogParser.OGDataContent
import im.threads.business.ogParser.OpenGraphParser
import im.threads.business.ogParser.OpenGraphParserJsoupImpl
import im.threads.internal.Config
import im.threads.internal.markdown.LinkifyLinksHighlighter
import im.threads.internal.markdown.LinksHighlighter
import im.threads.internal.utils.ColorsHelper
import im.threads.internal.utils.UrlUtils
import im.threads.internal.utils.ViewUtils
import im.threads.internal.utils.gone
import im.threads.internal.utils.visible
import im.threads.internal.views.CircularProgressButton
import im.threads.internal.widget.textView.BubbleMessageTextView
import im.threads.ui.utils.NoLongClickMovementMethod
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
    private val highlightingStream: PublishSubject<ChatItem>? = null
) : RecyclerView.ViewHolder(itemView) {
    private var currentChatItem: ChatItem? = null
    private var viewsToHighlight: Array<out View>? = null
    private var isThisItemHighlighted = false
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val linksHighlighter: LinksHighlighter = LinkifyLinksHighlighter()
    private val openGraphParser: OpenGraphParser = OpenGraphParserJsoupImpl()
    private val style = Config.instance.chatStyle
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

    protected fun subscribeForOpenGraphData(ogDataContent: OGDataContent) {
        this.ogDataContent = ogDataContent

        compositeDisposable.add(
            openGraphParsingStream
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onOgDataReceived(it)
                }, {
                    LoggerEdna.error("Error when receiving OGData", it)
                })
        )
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
                    if (isHighlighted) style.chatHighlightingColor else R.color.threads_transparent
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
     */
    protected fun highlightOperatorText(
        textView: TextView,
        phrase: ConsultPhrase
    ) {
        if (phrase.formattedPhrase.isNullOrBlank()) {
            textView.setText(phrase.phraseText, TextView.BufferType.NORMAL)
            setTextWithHighlighting(
                textView,
                style.incomingMarkdownConfiguration.isLinkUnderlined
            )
        } else {
            (textView as? BubbleMessageTextView)?.let {
                setMovementMethod(it)
                it.setFormattedText(phrase.formattedPhrase, true)
            }
        }
    }

    /**
     * Подсчвечивает ссылки, email, номера телефонов.
     * @param textView вью, где необходимо произвести подсветку
     * @param phrase текст для отображение во вью
     */
    protected fun highlightClientText(
        textView: BubbleMessageTextView,
        phrase: String
    ) {
        textView.setText(phrase, TextView.BufferType.NORMAL)
        setTextWithHighlighting(
            textView,
            style.outgoingMarkdownConfiguration.isLinkUnderlined
        )
    }

    /**
     * Возвращает нужный ресурс [Drawable] в зависимости от кода ошибки
     * @param code код ошибки
     */
    protected fun getErrorImageResByErrorCode(code: ErrorStateEnum) = when (code) {
        ErrorStateEnum.DISALLOWED -> R.drawable.im_wrong_file
        ErrorStateEnum.TIMEOUT -> R.drawable.im_unexpected
        ErrorStateEnum.UNEXPECTED -> R.drawable.im_unexpected
        ErrorStateEnum.ANY -> R.drawable.im_unexpected
    }

    /**
     * Возвращает нужный текстовый ресурс [StringRes] в зависимости от кода ошибки
     * @param code код ошибки
     */
    protected fun getErrorStringResByErrorCode(code: ErrorStateEnum) = when (code) {
        ErrorStateEnum.DISALLOWED -> R.string.threads_disallowed_error_during_load_file
        ErrorStateEnum.TIMEOUT -> R.string.threads_timeout_error_during_load_file
        ErrorStateEnum.UNEXPECTED -> R.string.threads_some_error_during_load_file
        ErrorStateEnum.ANY -> R.string.threads_some_error_during_load_file
    }

    protected fun bindOGData(messageText: String?) {
        val link = if (messageText != null) {
            val extractedLink = UrlUtils.extractLink(messageText)?.link
            if (extractedLink != null && !extractedLink.startsWith("http")) {
                "https://$extractedLink"
            } else {
                extractedLink
            }
        } else {
            null
        }
        ogDataContent?.url = link ?: ""

        if (ogDataContent?.ogDataLayout?.tag == link) {
            return
        } else {
            openGraphParser.getCachedContents(link)?.let {
                openGraphParsingStream.onNext(it)
            } ?: hideOGView()
        }

        coroutineScope.launch(Dispatchers.Main) {
            val requestJob = async(Dispatchers.IO) {
                openGraphParser.getContents(link, messageText)
            }
            openGraphParsingStream.onNext(requestJob.await())
        }
    }

    private fun onOgDataReceived(ogData: OGData) {
        ogDataContent?.let { ogDataContent ->
            if (ogData.messageText == ogDataContent.messageText) {
                val ogImage: ImageView = ogDataContent.ogDataLayout.findViewById(R.id.og_image)

                if (ogData.isEmpty()) {
                    hideOGView()
                    return
                }

                val ogTitle: TextView = ogDataContent.ogDataLayout.findViewById(R.id.og_title)
                val ogDescription: TextView = ogDataContent.ogDataLayout.findViewById(R.id.og_description)
                val ogUrl: TextView = ogDataContent.ogDataLayout.findViewById(R.id.og_url)

                showOGView()
                setOgDataTitle(ogData, ogTitle)
                setOgDataDescription(ogData, ogDescription)
                setOgDataUrl(ogUrl, ogData)
                setOgDataImage(ogData, ogImage)

                ViewUtils.setClickListener(
                    ogDataContent.ogDataLayout,
                    View.OnClickListener {
                        UrlUtils.openUrl(
                            ogDataContent.ogDataLayout.context,
                            ogDataContent.url
                        )
                    }
                )

                ogDataContent.ogDataLayout.tag = ogDataContent.url
            }
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

    private fun setOgDataUrl(ogUrl: TextView, ogData: OGData) {
        ogUrl.text = ogData.url.ifEmpty { ogDataContent?.url }
    }

    private fun setOgDataImage(ogData: OGData, ogImage: ImageView) {
        if (UrlUtils.isValidUrl(ogData.imageUrl)) {
            ogImage.visibility = View.VISIBLE
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
                            ogImage.visibility = View.GONE
                        }
                    }
                )
            }
        } else {
            ogImage.visibility = View.GONE
        }
    }

    private fun showOGView() {
        ogDataContent?.ogDataLayout?.visible()
        ogDataContent?.timeStampView?.gone()
    }

    private fun hideOGView() {
        ogDataContent?.ogDataLayout?.gone()
        ogDataContent?.timeStampView?.visible()
        ogDataContent?.ogDataLayout?.tag = ""
    }

    private fun setTextWithHighlighting(textView: TextView, isUnderlined: Boolean) {
        setMovementMethod(textView)
        linksHighlighter.highlightAllTypeOfLinks(textView, isUnderlined)
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
        view.setImageResource(R.drawable.im_loading_themed)
        ColorsHelper.setTint(
            itemView.context,
            view,
            if (isIncomingMessage) {
                style.incomingMessageLoaderColor
            } else {
                style.outgoingMessageLoaderColor
            }
        )
        rotateAnim.duration = 3000
        rotateAnim.repeatCount = Animation.INFINITE
        view.animation = rotateAnim
        rotateAnim.start()
    }

    companion object {
        val openGraphParsingStream = PublishSubject.create<OGData>()
    }
}
