package im.threads.ui.holders

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
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
import im.threads.business.ogParser.OpenGraphParser
import im.threads.ui.utils.ColorsHelper
import im.threads.ui.utils.gone
import im.threads.ui.utils.invisible
import im.threads.ui.utils.visible
import im.threads.ui.widget.textView.BubbleTimeTextView
import io.reactivex.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageFromConsultViewHolder(
    parent: ViewGroup,
    private val maskedTransformation: ImageModifications.MaskedModification?,
    highlightingStream: PublishSubject<ChatItem>,
    openGraphParser: OpenGraphParser
) : BaseImageHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.ecc_item_image_from_consult,
        parent,
        false
    ),
    highlightingStream,
    openGraphParser,
    true
) {

    private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val errorTextView: TextView = itemView.findViewById(R.id.errorText)
    private val loaderLayout: LinearLayout = itemView.findViewById(R.id.loaderLayout)
    private val fileNameTextView: TextView = itemView.findViewById(R.id.fileName)
    private val loader: ImageView = itemView.findViewById(R.id.loader)
    private val loaderImage: ImageView = itemView.findViewById(R.id.loaderImage)
    private val imageLayoutWithSpace: LinearLayout = itemView.findViewById(R.id.imageLayoutWithSpace)
    private val timeStampLoader: BubbleTimeTextView = itemView.findViewById<BubbleTimeTextView>(R.id.timeStampLoader).apply {
        setTextColor(getColorInt(style.incomingImageTimeColor))
        if (style.incomingMessageTimeTextSize > 0) {
            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                itemView.context.resources.getDimension(style.incomingMessageTimeTextSize)
            )
        }
        background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            getColorInt(style.incomingImageTimeBackgroundColor),
            BlendModeCompat.SRC_ATOP
        )
    }
    private val consultAvatar = itemView.findViewById<ImageView>(R.id.consultAvatar).apply {
        layoutParams.height = itemView.context.resources.getDimension(style.operatorAvatarSize).toInt()
        layoutParams.width = itemView.context.resources.getDimension(style.operatorAvatarSize).toInt()
    }

    init {
        itemView.findViewById<View>(R.id.delimiter)
            .setBackgroundColor(getColorInt(style.incomingDelimitersColor))
        setTextColorToViews(arrayOf(fileNameTextView), style.incomingMessageTextColor)
    }

    fun onBind(
        consultPhrase: ConsultPhrase,
        highlighted: Boolean,
        buttonClickListener: View.OnClickListener,
        onLongClickListener: OnLongClickListener,
        onAvatarClickListener: View.OnClickListener
    ) {
        subscribeForHighlighting(consultPhrase, itemView)
        applyBubbleLayoutStyle()
        ColorsHelper.setTextColor(errorTextView, style.errorMessageTextColor)
        timeStampTextView.setOnClickListener(buttonClickListener)
        timeStampTextView.setOnLongClickListener(onLongClickListener)
        timeStampTextView.text = sdf.format(Date(consultPhrase.timeStamp))
        timeStampLoader.setOnClickListener(buttonClickListener)
        timeStampLoader.setOnLongClickListener(onLongClickListener)
        timeStampLoader.text = sdf.format(Date(consultPhrase.timeStamp))
        rootLayout.setOnClickListener(buttonClickListener)
        rootLayout.setOnLongClickListener(onLongClickListener)
        image.setOnClickListener(buttonClickListener)
        image.setOnLongClickListener(onLongClickListener)
        showAvatar(consultAvatar, consultPhrase, onAvatarClickListener)
        consultPhrase.fileDescription?.let {
            when (it.state) {
                AttachmentStateEnum.PENDING -> showLoaderLayout(it)
                AttachmentStateEnum.ERROR -> showErrorLayout(it)
                else -> {
                    showCommonLayout(it)
                    moveTimeToImageLayout()
                }
            }
        } ?: run {
            image.setImageResource(0)
        }
        rootLayout.apply {
            setBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    if (highlighted) style.chatHighlightingColor else R.color.ecc_transparent
                )
            )
        }
    }

    private fun applyBubbleLayoutStyle() {
        loaderLayout.background = AppCompatResources.getDrawable(
            itemView.context,
            style.incomingMessageBubbleBackground
        )
        setPaddings(true, loaderLayout)
        setLayoutMargins(true, loaderLayout)
        loaderLayout.background.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                getColorInt(style.incomingMessageBubbleColor),
                BlendModeCompat.SRC_ATOP
            )
    }

    private fun showLoadImageAnimation() {
        loaderImage.isVisible = true
        initAnimation(loaderImage, true)
        rotateAnim.start()
    }

    private fun stopLoadImageAnimation() {
        loaderImage.gone()
        rotateAnim.cancel()
    }

    private fun showLoaderLayout(fileDescription: FileDescription) {
        loaderLayout.visible()
        imageLayoutWithSpace.gone()
        timeStampLoader.visible()
        errorTextView.gone()
        fileNameTextView.text = fileDescription.incomingName
        initAnimation(loader, true)
    }

    private fun showErrorLayout(fileDescription: FileDescription) {
        loaderLayout.isVisible = true
        errorTextView.isVisible = true
        imageLayoutWithSpace.gone()
        timeStampLoader.visible()
        loader.setImageResource(getErrorImageResByErrorCode(fileDescription.errorCode))
        fileNameTextView.text = fileDescription.incomingName
        val errorString = getString(getErrorStringResByErrorCode(fileDescription.errorCode))
        errorTextView.text = errorString
        rotateAnim.cancel()
    }

    private fun showCommonLayout(fileDescription: FileDescription) {
        imageLayoutWithSpace.visible()
        loaderLayout.invisible()
        timeStampLoader.gone()
        errorTextView.gone()
        rotateAnim.cancel()
        val previewUri = getPreviewUri(fileDescription.getPreviewFileDescription())
        val fileUri = if (previewUri?.toString().isNullOrBlank()) {
            fileDescription.getPreviewFileDescription()?.downloadPath
        } else {
            previewUri?.toString()
        }
        val isStateReady = fileDescription.state == AttachmentStateEnum.READY
        if (isStateReady && !fileDescription.isDownloadError) {
            if (!fileUri.isNullOrEmpty()) {
                showLoadImageAnimation()
                ImageLoader.get()
                    .load(fileUri)
                    .autoRotateWithExif(true)
                    .errorDrawableResourceId(style.imagePlaceholder)
                    .scales(ImageView.ScaleType.FIT_XY, ImageView.ScaleType.CENTER_CROP)
                    .modifications(maskedTransformation)
                    .callback(object : ImageLoader.ImageLoaderCallback {
                        override fun onImageLoaded() {
                            stopLoadImageAnimation()
                        }

                        override fun onImageLoadError() {
                            stopLoadImageAnimation()
                        }
                    })
                    .into(image)
            } else {
                image.setImageResource(style.imagePlaceholder)
            }
        } else {
            stopLoadImageAnimation()
            image.setImageResource(style.imagePlaceholder)
        }
    }
}
