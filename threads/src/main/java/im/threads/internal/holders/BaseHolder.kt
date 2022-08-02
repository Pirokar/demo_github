package im.threads.internal.holders

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import im.threads.R
import im.threads.internal.Config
import im.threads.internal.domain.logger.LoggerEdna
import im.threads.internal.domain.ogParser.OGData
import im.threads.internal.domain.ogParser.OpenGraphParser
import im.threads.internal.domain.ogParser.OpenGraphParserJsoupImpl
import im.threads.internal.imageLoading.ImageLoader
import im.threads.internal.imageLoading.loadImage
import im.threads.internal.markdown.LinkifyLinksHighlighter
import im.threads.internal.markdown.LinksHighlighter
import im.threads.internal.model.ConsultPhrase
import im.threads.internal.model.ErrorStateEnum
import im.threads.internal.utils.ColorsHelper
import im.threads.internal.utils.UrlUtils
import im.threads.internal.utils.ViewUtils
import im.threads.internal.views.CircularProgressButton
import im.threads.internal.widget.text_view.BubbleMessageTextView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var compositeDisposable: CompositeDisposable? = CompositeDisposable()
    private val linksHighlighter: LinksHighlighter = LinkifyLinksHighlighter()
    private val openGraphParser: OpenGraphParser = OpenGraphParserJsoupImpl()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    protected fun subscribe(event: Disposable): Boolean {
        if (compositeDisposable?.isDisposed != false) {
            compositeDisposable = CompositeDisposable()
        }
        return compositeDisposable?.add(event) ?: false
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
        val chatStyle = Config.instance.chatStyle
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
        compositeDisposable?.apply {
            dispose()
        }
        compositeDisposable = null
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
                Config.instance.chatStyle.incomingMarkdownConfiguration.isLinkUnderlined
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
            Config.instance.chatStyle.outgoingMarkdownConfiguration.isLinkUnderlined
        )
    }

    /**
     * Возвращает нужный ресурс [Drawable] в зависимости от кода ошибки
     * @param code код ошибки
     */
    protected fun getErrorImageResByErrorCode(code: ErrorStateEnum) = when (code) {
        ErrorStateEnum.DISALLOWED -> R.drawable.im_wrong_file
        ErrorStateEnum.TIMEOUT -> R.drawable.im_unexpected
        ErrorStateEnum.Unexpected -> R.drawable.im_unexpected
        ErrorStateEnum.ANY -> R.drawable.im_unexpected
    }

    /**
     * Обрабатывает показ Open Graph.
     * @param ogDataLayout layout, в котором размещены вьюхи Open Graph
     * @param timeStampView текстовая вьюха для отображения времени
     * @param url ссылка, для которой надо отобразить Open Graph
     */
    protected fun bindOGData(
        ogDataLayout: ViewGroup,
        timeStampView: TextView,
        url: String
    ) {
        val normalizedUrl = if (!url.startsWith("http")) {
            "https://$url"
        } else {
            url
        }
        if (ogDataLayout.tag == normalizedUrl) {
            return
        }

        val ogImage: ImageView = ogDataLayout.findViewById(R.id.og_image)
        ogImage.setImageDrawable(null)

        val ogDataTag = "OgData_Fetching"
        coroutineScope.launch {
            LoggerEdna.info(ogDataTag, "Fetching OgData for url \"$normalizedUrl\"")
            openGraphParser.getContents(normalizedUrl)?.let { ogData ->
                LoggerEdna.info(ogDataTag, "OgData for url \"$normalizedUrl\": $ogData")
                withContext(Dispatchers.Main) {
                    if (ogData.isEmpty()) {
                        hideOGView(ogDataLayout, timeStampView)
                        return@withContext
                    }

                    val ogTitle: TextView = ogDataLayout.findViewById(R.id.og_title)
                    val ogDescription: TextView = ogDataLayout.findViewById(R.id.og_description)
                    val ogUrl: TextView = ogDataLayout.findViewById(R.id.og_url)

                    showOGView(ogDataLayout, timeStampView)
                    setOgDataTitle(ogData, ogTitle)
                    setOgDataDescription(ogData, ogDescription)
                    setOgDataUrl(ogUrl, ogData, normalizedUrl)
                    setOgDataImage(ogData, ogImage)

                    ViewUtils.setClickListener(
                        ogDataLayout,
                        View.OnClickListener {
                            UrlUtils.openUrl(
                                ogDataLayout.getContext(),
                                normalizedUrl
                            )
                        }
                    )

                    ogDataLayout.tag = normalizedUrl
                }
            } ?: run {
                withContext(Dispatchers.Main) {
                    hideOGView(ogDataLayout, timeStampView)
                }
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

    private fun setOgDataUrl(ogUrl: TextView, ogData: OGData, url: String) {
        ogUrl.text = ogData.url.ifEmpty { url }
    }

    private fun setOgDataImage(ogData: OGData, ogImage: ImageView) {
        if (UrlUtils.isValidUrl(ogData.imageUrl)) {
            ogImage.visibility = View.VISIBLE
            ogImage.loadImage(
                ogData.imageUrl,
                errorDrawableResId = Config.instance.chatStyle.imagePlaceholder,
                isExternalImage = true,
                callback = object : ImageLoader.ImageLoaderCallback {
                    override fun onImageLoadError() {
                        ogImage.visibility = View.GONE
                    }
                }
            )
        } else {
            ogImage.visibility = View.GONE
        }
    }

    /**
     * Включает отображение контейнера Open Graph.
     * @param ogDataLayout layout, в котором размещены вьюхи Open Graph
     * @param timeStampView текстовая вьюха для отображения времени
     */
    protected fun showOGView(ogDataLayout: ViewGroup, timeStampView: TextView) {
        ogDataLayout.visibility = View.VISIBLE
        timeStampView.visibility = View.GONE
    }

    /**
     * Прячет контейнер Open Graph (View.Gone)
     * @param ogDataLayout layout, в котором размещены вьюхи Open Graph
     * @param timeStampView текстовая вьюха для отображения времени
     */
    protected fun hideOGView(ogDataLayout: ViewGroup, timeStampView: TextView) {
        ogDataLayout.visibility = View.GONE
        timeStampView.visibility = View.VISIBLE
    }

    private fun setTextWithHighlighting(textView: TextView, isUnderlined: Boolean) {
        setMovementMethod(textView)
        linksHighlighter.highlightAllTypeOfLinks(textView, isUnderlined)
    }

    private fun setMovementMethod(textView: TextView) {
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setUpDrawable(@DrawableRes iconResId: Int, @ColorRes colorRes: Int): Drawable? {
        val drawable = AppCompatResources.getDrawable(itemView.context, iconResId)?.mutate()
        ColorsHelper.setDrawableColor(itemView.context, drawable, colorRes)
        return drawable
    }
}
