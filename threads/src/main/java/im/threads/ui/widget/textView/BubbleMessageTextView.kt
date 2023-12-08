package im.threads.ui.widget.textView

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import im.threads.R
import im.threads.business.markdown.MarkdownProcessor
import im.threads.business.markdown.MarkwonMarkdownProcessor
import im.threads.ui.config.Config.Companion.getInstance
import im.threads.ui.utils.NoLongClickMovementMethod
import im.threads.ui.widget.CustomFontTextView

class BubbleMessageTextView : CustomFontTextView {
    private var mHasImageInText = false
    private var lastLinePadding = ""
    private var lastLineExtraPaddingSymbolsCount = 0f
    private val chatStyle = getInstance().chatStyle
    private val markdownProcessor: MarkdownProcessor = MarkwonMarkdownProcessor(
        getInstance().context,
        chatStyle.incomingMarkdownConfiguration,
        chatStyle.outgoingMarkdownConfiguration,
        false
    )

    constructor(context: Context?) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        fetchLastLinePadding(context, attrs)
    }

    override fun setTypefaceView(context: Context) {
        val style = getInstance().chatStyle
        if (!TextUtils.isEmpty(style.bubbleMessageFont)) {
            typeface = Typeface.createFromAsset(context.assets, style.bubbleMessageFont)
        } else {
            super.setTypefaceView(context)
        }
    }

    fun bindTimestampView(timeTextView: BubbleTimeTextView) {
        timeTextView.measure(0, 0)
        val timeWidth = timeTextView.measuredWidth * 2
        val paddingBuilder = StringBuilder(" ")
        val bounds = Rect()
        val textPaint: Paint = paint
        var width = 0
        while (width < timeWidth) {
            paddingBuilder.append("_")
            textPaint.getTextBounds(
                paddingBuilder.toString(),
                0,
                paddingBuilder.toString().length,
                bounds
            )
            width = bounds.width()
        }
        var i = 0
        while (i < lastLineExtraPaddingSymbolsCount) {
            paddingBuilder.append("_")
            i++
        }
        lastLinePadding = paddingBuilder.toString().replace("_", SPACE.toString())
    }

    override fun setText(text: CharSequence?, type: BufferType) {
        var textLocal = if (text.isNullOrBlank()) { "" } else { text }
        textLocal = addPadding(textLocal)
        if (mHasImageInText) {
            mHasImageInText = false
        }
        super.setText(textLocal, type)
    }

    fun setFormattedText(
        text: CharSequence?,
        isOperatorMessage: Boolean,
        links: List<Pair<String?, OnClickListener>> = arrayListOf()
    ) {
        var textLocal = if (text.isNullOrBlank()) { "" } else { text }
        textLocal = addPadding(textLocal)
        val spannedText = getSpanned(textLocal, isOperatorMessage)
        if (mHasImageInText) {
            mHasImageInText = false
        }
        val spannableString = SpannableString(spannedText)
        var startIndexOfLink = -1
        for (link in links) {
            if (!link.first.isNullOrBlank()) {
                val clickableSpan = object : ClickableSpan() {
                    override fun updateDrawState(textPaint: TextPaint) {
                        val linksColor = chatStyle.incomingMarkdownConfiguration.linkColor
                            ?: ContextCompat.getColor(
                                this@BubbleMessageTextView.context,
                                chatStyle.incomingMessageLinkColor
                            )
                        textPaint.color = linksColor
                        textPaint.isUnderlineText = chatStyle.incomingMarkdownConfiguration.isLinkUnderlined
                    }

                    override fun onClick(view: View) {
                        Selection.setSelection((view as TextView).text as Spannable, 0)
                        view.invalidate()
                        link.second.onClick(view)
                    }
                }
                startIndexOfLink = spannableString.toString().indexOf(
                    link.first!!,
                    startIndexOfLink + 1
                )
                if (startIndexOfLink >= 0) {
                    spannableString.setSpan(
                        clickableSpan,
                        startIndexOfLink,
                        startIndexOfLink + link.first!!.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
        this.movementMethod = NoLongClickMovementMethod.getInstance()
        this.setText(spannableString, BufferType.SPANNABLE)
    }

    override fun invalidateDrawable(dr: Drawable) {
        if (mHasImageInText) {
            invalidate()
        } else {
            super.invalidateDrawable(dr)
        }
    }

    private fun fetchLastLinePadding(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BubbleMessageTextView)
        lastLineExtraPaddingSymbolsCount = try {
            typedArray.getFloat(
                R.styleable.BubbleMessageTextView_last_line_extra_padding_symbols,
                0f
            )
        } finally {
            typedArray.recycle()
        }
    }

    private fun addPadding(text: CharSequence): CharSequence {
        var textLocal = text
        if (!TextUtils.isEmpty(textLocal) && !TextUtils.isEmpty(lastLinePadding)) {
            var builder: SpannableStringBuilder? = SpannableStringBuilder(textLocal)
            builder = trimEndSpannable(builder)
            builder?.let { textLocal = it.append(lastLinePadding) }
        }
        return textLocal
    }

    private fun getSpanned(text: CharSequence, isOperatorMessage: Boolean): Spanned {
        val spannedText: Spanned = if (isOperatorMessage) {
            markdownProcessor.parseOperatorMessage(text.toString())
        } else {
            markdownProcessor.parseClientMessage(text.toString())
        }
        return spannedText
    }

    private fun trimEndSpannable(spannable: SpannableStringBuilder?): SpannableStringBuilder? {
        if (spannable.isNullOrBlank()) {
            return null
        }
        var shouldTrim = false
        var trimStart = spannable.length - 1
        val trimEnd = spannable.length - 1
        while (spannable[trimStart] == ' ' && trimStart > 0) {
            shouldTrim = true
            trimStart--
        }
        val result: SpannableStringBuilder = if (shouldTrim) {
            spannable.delete(trimStart, trimEnd)
        } else {
            spannable
        }
        return result
    }

    companion object {
        private val SPACE = Html.fromHtml("&#160;")
    }
}
