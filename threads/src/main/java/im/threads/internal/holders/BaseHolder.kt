package im.threads.internal.holders

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.text.method.LinkMovementMethod
import android.view.View
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
import im.threads.internal.markdown.LinkifyLinksHighlighter
import im.threads.internal.markdown.LinksHighlighter
import im.threads.internal.markdown.MarkdownProcessor
import im.threads.internal.model.ConsultPhrase
import im.threads.internal.model.ErrorStateEnum
import im.threads.internal.utils.ColorsHelper
import im.threads.internal.views.CircularProgressButton
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var compositeDisposable: CompositeDisposable? = CompositeDisposable()
    private val linksHighlighter: LinksHighlighter = LinkifyLinksHighlighter()

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
            textView.text = phrase.phraseText?.trim()
            setTextWithHighlighting(
                textView,
                Config.instance.chatStyle.incomingMarkdownConfiguration.isLinkUnderlined
            )
        } else {
            setTextWithMarkdown(textView, phrase.formattedPhrase)
        }
    }

    /**
     * Подсчвечивает ссылки, email, номера телефонов.
     * @param textView вью, где необходимо произвести подсветку
     * @param phrase текст для отображение во вью
     */
    protected fun highlightClientText(
        textView: TextView,
        phrase: String
    ) {
        textView.text = phrase
        setTextWithHighlighting(
            textView,
            Config.instance.chatStyle.outgoingMarkdownConfiguration.isLinkUnderlined
        )
    }

    private fun setTextWithHighlighting(textView: TextView, isUnderlined: Boolean) {
        setMovementMethod(textView)
        linksHighlighter.highlightAllTypeOfLinks(textView, isUnderlined)
    }

    private fun setTextWithMarkdown(textView: TextView, text: String) {
        setMovementMethod(textView)
        textView.text = MarkdownProcessor.instance.parseOperatorMessage(text.trim { it <= ' ' })
    }

    private fun setMovementMethod(textView: TextView) {
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setUpDrawable(@DrawableRes iconResId: Int, @ColorRes colorRes: Int): Drawable? {
        val drawable = AppCompatResources.getDrawable(itemView.context, iconResId)?.mutate()
        ColorsHelper.setDrawableColor(itemView.context, drawable, colorRes)
        return drawable
    }

    protected fun getErrorImageResByErrorCode(code: ErrorStateEnum) = when (code) {
        ErrorStateEnum.DISALLOWED -> R.drawable.im_wrong_file
        ErrorStateEnum.TIMEOUT -> R.drawable.im_unexpected
        ErrorStateEnum.Unexpected -> R.drawable.im_unexpected
        ErrorStateEnum.ANY -> R.drawable.im_unexpected
    }

    protected fun subscribe(event: Disposable): Boolean {
        if (compositeDisposable?.isDisposed != false) {
            compositeDisposable = CompositeDisposable()
        }
        return compositeDisposable?.add(event) ?: false
    }

    fun onClear() {
        compositeDisposable?.apply {
            dispose()
        }
        compositeDisposable = null
    }
}
