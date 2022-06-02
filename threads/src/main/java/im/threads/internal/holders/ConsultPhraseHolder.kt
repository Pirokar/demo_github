package im.threads.internal.holders

import android.graphics.PorterDuff
import android.graphics.Typeface
import android.text.SpannableString
import android.text.TextUtils
import android.text.format.Formatter
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.text.util.LinkifyCompat
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import im.threads.R
import im.threads.internal.Config
import im.threads.internal.formatters.RussianFormatSymbols
import im.threads.internal.markdown.MarkdownProcessorHolder
import im.threads.internal.model.ConsultPhrase
import im.threads.internal.opengraph.OGData
import im.threads.internal.opengraph.OGDataProvider
import im.threads.internal.utils.CircleTransformation
import im.threads.internal.utils.FileUtils
import im.threads.internal.utils.FileUtils.isImage
import im.threads.internal.utils.ThreadsLogger
import im.threads.internal.utils.UrlUtils
import im.threads.internal.utils.ViewUtils
import im.threads.internal.views.CircularProgressButton
import im.threads.internal.widget.text_view.BubbleMessageTextView
import im.threads.internal.widget.text_view.BubbleTimeTextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * layout/item_consultant_text_with_file.xml
 */
class ConsultPhraseHolder(parent: ViewGroup) : BaseHolder(
    LayoutInflater.from(parent.context)
        .inflate(R.layout.item_consultant_text_with_file, parent, false)
) {
    private val style = Config.instance.chatStyle
    private val timeStampSdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var quoteSdf = if (Locale.getDefault().language.equals("ru", ignoreCase = true)) {
        SimpleDateFormat("dd MMMM yyyy", RussianFormatSymbols())
    } else {
        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    }

    private val fileRow: View = itemView.findViewById(R.id.right_text_row)
    private val mCircularProgressButton =
        itemView.findViewById<CircularProgressButton>(R.id.button_download).apply {
            setBackgroundColorResId(style.chatBackgroundColor)
        }
    private val mFileImage = itemView.findViewById<ImageView>(R.id.file_image)
    private val rightTextHeader: TextView = itemView.findViewById(R.id.to)
    private val mImage: ImageView = itemView.findViewById(R.id.image)
    private val mRightTextDescr: TextView = itemView.findViewById(R.id.file_specs)
    private val rightTextFileStamp: TextView = itemView.findViewById(R.id.send_at)
    private val mTimeStampTextView =
        itemView.findViewById<BubbleTimeTextView>(R.id.timestamp).apply {
            setTextColor(getColorInt(style.incomingMessageTimeColor))
        }
    private val mPhraseTextView = itemView.findViewById<BubbleMessageTextView>(R.id.text).apply {
        setLinkTextColor(getColorInt(style.incomingMessageLinkColor))
    }
    private val mConsultAvatar = itemView.findViewById<ImageView>(R.id.consult_avatar).apply {
        layoutParams.height =
            itemView.context.resources.getDimension(style.operatorAvatarSize)
                .toInt()
        layoutParams.width =
            itemView.context.resources.getDimension(style.operatorAvatarSize)
                .toInt()
    }
    private val filterView = itemView.findViewById<View>(R.id.filter).apply {
        setBackgroundColor(
            ContextCompat.getColor(
                itemView.context,
                style.chatHighlightingColor
            )
        )
    }
    private val secondFilterView = itemView.findViewById<View>(R.id.filter_bottom).apply {
        setBackgroundColor(
            ContextCompat.getColor(
                itemView.context,
                style.chatHighlightingColor
            )
        )
    }
    private val mPhraseFrame: View = itemView.findViewById(R.id.phrase_frame)
    private val ogDataLayout: ViewGroup = itemView.findViewById(R.id.og_data_layout)
    private val ogImage: ImageView = itemView.findViewById(R.id.og_image)
    private val ogTitle: TextView = itemView.findViewById(R.id.og_title)
    private val ogDescription: TextView = itemView.findViewById(R.id.og_description)
    private val ogUrl: TextView = itemView.findViewById(R.id.og_url)
    private val ogTimestamp = itemView.findViewById<TextView>(R.id.og_timestamp).apply {
        setTextColor(getColorInt(style.incomingMessageTimeColor))
    }

    private val context = parent.context

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
            background.setColorFilter(
                getColorInt(style.incomingMessageBubbleColor),
                PorterDuff.Mode.SRC_ATOP
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
        itemView.findViewById<View>(R.id.delimeter)
            .setBackgroundColor(getColorInt(style.chatToolbarColorResId))
        setTextColorToViews(
            arrayOf(
                mPhraseTextView,
                rightTextHeader,
                mRightTextDescr,
                rightTextFileStamp
            ),
            style.incomingMessageTextColor
        )
        setUpProgressButton(mCircularProgressButton)
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
        val phrase = consultPhrase.phraseText?.trim()
        val quote = consultPhrase.quote
        val fileDescription = consultPhrase.fileDescription
        ViewUtils.setClickListener(itemView as ViewGroup, onRowLongClickListener)
        mConsultAvatar.setImageBitmap(null)
        val timeText = timeStampSdf.format(Date(consultPhrase.timeStamp))
        mTimeStampTextView.text = timeText
        ogTimestamp.text = timeText
        if (phrase == null) {
            mPhraseTextView.visibility = View.GONE
        } else {
            mPhraseTextView.bindTimestampView(mTimeStampTextView)
            mPhraseTextView.visibility = View.VISIBLE
            val deepLink = UrlUtils.extractDeepLink(phrase)
            val url = UrlUtils.extractLink(phrase)
            when {
                deepLink != null -> {
                    mPhraseTextView.setOnClickListener { UrlUtils.openUrl(context, deepLink) }
                }
                url != null -> {
                    mPhraseTextView.setOnClickListener { UrlUtils.openUrl(context, url) }
                }
                else -> {
                    mPhraseTextView.setOnClickListener(null)
                }
            }
            when {
                consultPhrase.formattedPhrase != null -> {
                    mPhraseTextView.autoLinkMask = 0
                    mPhraseTextView.text = MarkdownProcessorHolder.getMarkdownProcessor()
                        .parse(consultPhrase.formattedPhrase.trim { it <= ' ' })
                    mPhraseTextView.movementMethod = LinkMovementMethod.getInstance()
                }
                deepLink != null -> {
                    val text = SpannableString(phrase)
                    LinkifyCompat.addLinks(text, UrlUtils.DEEPLINK_URL, "")
                    mPhraseTextView.text = text
                }
                url != null -> {
                    val text = SpannableString(phrase)
                    LinkifyCompat.addLinks(text, UrlUtils.WEB_URL, "")
                    mPhraseTextView.text = text
                }
                else -> {
                    mPhraseTextView.text = phrase
                }
            }
            if (url != null) {
                val ogData = consultPhrase.ogData
                if (ogData == null) {
                    loadOGData(consultPhrase, url)
                } else {
                    bindOGData(ogData, url)
                }
                ViewUtils.setClickListener(
                    ogDataLayout,
                    View.OnClickListener { v: View? ->
                        UrlUtils.openUrl(
                            itemView.getContext(),
                            url
                        )
                    }
                )
            } else {
                hideOGView()
            }
        }
        mImage.visibility = View.GONE
        if (quote == null) {
            fileRow.visibility = View.GONE
        } else {
            fileRow.visibility = View.VISIBLE
            ViewUtils.setClickListener(fileRow as ViewGroup, onQuoteClickListener)
            mFileImage.visibility = View.GONE
            mCircularProgressButton.visibility = View.GONE
            rightTextHeader.text = if (quote.phraseOwnerTitle == null) itemView.getContext()
                .getString(R.string.threads_I) else quote.phraseOwnerTitle
            mRightTextDescr.text = quote.text
            rightTextFileStamp.text = itemView.getContext()
                .getString(R.string.threads_sent_at, quoteSdf.format(Date(quote.timeStamp)))
            val quoteFileDescription = quote.fileDescription
            if (quoteFileDescription != null) {
                if (FileUtils.isVoiceMessage(quoteFileDescription)) {
                    mRightTextDescr.setText(R.string.threads_voice_message)
                } else {
                    if (isImage(quote.fileDescription)) {
                        mFileImage.visibility = View.VISIBLE
                        Picasso.get()
                            .load(quoteFileDescription.downloadPath)
                            .error(style.imagePlaceholder)
                            .fit()
                            .centerCrop()
                            .into(mFileImage)
                        mFileImage.setOnClickListener(onQuoteClickListener)
                    } else {
                        mCircularProgressButton.visibility = View.VISIBLE
                        val fileSize = quoteFileDescription.size
                        mRightTextDescr.text =
                            FileUtils.getFileName(quoteFileDescription) + if (fileSize > 0) """
     
     ${Formatter.formatFileSize(itemView.getContext(), fileSize)}
                        """.trimIndent() else ""
                        mCircularProgressButton.setOnClickListener(onQuoteClickListener)
                        mCircularProgressButton.setProgress(if (quoteFileDescription.fileUri != null) 100 else quoteFileDescription.downloadProgress)
                    }
                }
            }
        }
        if (fileDescription != null) {
            if (FileUtils.isImage(fileDescription)) {
                fileRow.visibility = View.GONE
                mCircularProgressButton.visibility = View.GONE
                mImage.visibility = View.VISIBLE
                mImage.setOnClickListener(imageClickListener)
                Picasso.get()
                    .load(fileDescription.downloadPath)
                    .into(getPicassoTargetForView(mImage, style.imagePlaceholder))
            } else {
                fileRow.visibility = View.VISIBLE
                ViewUtils.setClickListener(fileRow as ViewGroup, null as View.OnClickListener?)
                mCircularProgressButton.visibility = View.VISIBLE
                mCircularProgressButton.setOnClickListener(fileClickListener)
                rightTextHeader.text =
                    if (fileDescription.from == null) "" else fileDescription.from
                if (!TextUtils.isEmpty(rightTextHeader.text)) {
                    rightTextHeader.visibility = View.VISIBLE
                } else {
                    rightTextHeader.visibility = View.GONE
                }
                val fileSize = fileDescription.size
                mRightTextDescr.text =
                    FileUtils.getFileName(fileDescription) + if (fileSize > 0) """  
     ${Formatter.formatFileSize(itemView.getContext(), fileSize)}
                """.trimIndent() else ""
                rightTextFileStamp.text = itemView.getContext().getString(
                    R.string.threads_sent_at,
                    quoteSdf.format(Date(fileDescription.timeStamp))
                )
                mCircularProgressButton.setProgress(if (fileDescription.fileUri != null) 100 else fileDescription.downloadProgress)
            }
        }
        if (fileDescription == null && quote == null) {
            fileRow.visibility = View.GONE
        }
        if (fileDescription != null || quote != null) {
            mPhraseFrame.layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        } else {
            mPhraseFrame.layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT
        }
        if (consultPhrase.isAvatarVisible) {
            mConsultAvatar.visibility = View.VISIBLE
            mConsultAvatar.setOnClickListener(onAvatarClickListener)
            showDefIcon()
            if (!TextUtils.isEmpty(consultPhrase.avatarPath)) {
                Picasso.get()
                    .load(FileUtils.convertRelativeUrlToAbsolute(consultPhrase.avatarPath))
                    .fit()
                    .noPlaceholder()
                    .centerCrop()
                    .transform(CircleTransformation())
                    .into(mConsultAvatar)
            }
        } else {
            mConsultAvatar.visibility = View.INVISIBLE
        }
        filterView.visibility = if (highlighted) View.VISIBLE else View.INVISIBLE
        secondFilterView.visibility = if (highlighted) View.VISIBLE else View.INVISIBLE
    }

    private fun showDefIcon() {
        mConsultAvatar.setImageResource(style.defaultOperatorAvatar)
    }

    private fun loadOGData(chatItem: ConsultPhrase, url: String) {
        hideOGView()
        subscribe(
            OGDataProvider.getInstance().getOGData(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { ogData: OGData ->
                        ThreadsLogger.d(TAG, "OGData for url: $url\n received: $ogData")
                        if (!ogData.isEmpty) {
                            chatItem.ogData = ogData
                            chatItem.ogUrl = url
                        }
                        bindOGData(ogData, url)
                    },
                    { e: Throwable ->
                        ThreadsLogger.e(TAG, "OpenGraph data load failed: ", e)
                    }
                )
        )
    }

    private fun bindOGData(ogData: OGData, url: String) {
        if (ogData.areTextsEmpty()) {
            hideOGView()
            return
        }
        showOGView()
        if (!TextUtils.isEmpty(ogData.title)) {
            ogTitle.visibility = View.VISIBLE
            ogTitle.text = ogData.title
            ogTitle.setTypeface(ogTitle.typeface, Typeface.BOLD)
        } else {
            ogTitle.visibility = View.GONE
        }
        if (!TextUtils.isEmpty(ogData.description)) {
            ogDescription.visibility = View.VISIBLE
            ogDescription.text = ogData.description
        } else {
            ogDescription.visibility = View.GONE
        }
        ogUrl.text = if (!TextUtils.isEmpty(ogData.url)) ogData.url else url
        if (TextUtils.isEmpty(ogData.imageUrl)) {
            ogImage.visibility = View.GONE
        } else {
            Picasso.get()
                .load(ogData.imageUrl)
                .fetch(object : Callback {
                    override fun onSuccess() {
                        ogImage.visibility = View.VISIBLE
                        Picasso.get()
                            .load(ogData.imageUrl)
                            .error(style.imagePlaceholder)
                            .fit()
                            .centerInside()
                            .into(ogImage)
                    }

                    override fun onError(e: Exception) {
                        ogImage.visibility = View.GONE
                        ThreadsLogger.d(TAG, "Could not load OpenGraph image: " + e.message)
                    }
                })
        }
    }

    private fun showOGView() {
        ogDataLayout.visibility = View.VISIBLE
        mTimeStampTextView.visibility = View.GONE
    }

    private fun hideOGView() {
        ogDataLayout.visibility = View.GONE
        mTimeStampTextView.visibility = View.VISIBLE
    }

    companion object {
        private val TAG = ConsultPhraseHolder::class.java.simpleName
    }
}
