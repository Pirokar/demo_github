package im.threads.ui.holders

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.isVisible
import im.threads.R
import im.threads.business.imageLoading.ImageLoader
import im.threads.business.imageLoading.ImageModifications
import im.threads.business.imageLoading.loadImage
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.FileDescription
import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.utils.FileUtils
import im.threads.ui.utils.invisible
import im.threads.ui.utils.visible
import io.reactivex.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageFromConsultViewHolder(
    parent: ViewGroup,
    private val maskedTransformation: ImageModifications.MaskedModification,
    highlightingStream: PublishSubject<ChatItem>
) : BaseHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_image_from_consult,
        parent,
        false
    ),
    highlightingStream
) {

    private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val timeStampTextView = itemView.findViewById<TextView>(R.id.timeStamp).apply {
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

    private val image: ImageView =
        itemView.findViewById<ImageView>(R.id.image).also { applyParams(it) }
    private val errorTextView: TextView = itemView.findViewById(R.id.errorText)
    private val loaderLayout: LinearLayout = itemView.findViewById(R.id.loaderLayout)
    private val commonLayout: RelativeLayout = itemView.findViewById(R.id.commonLayout)
    private val fileNameTextView: TextView = itemView.findViewById(R.id.fileName)
    private val loader: ImageView = itemView.findViewById(R.id.loader)
    private val loaderImage: ImageView = itemView.findViewById(R.id.loaderImage)
    private val rootLayout: LinearLayout = itemView.findViewById(R.id.rootLayout)

    private val consultAvatar = itemView.findViewById<ImageView>(R.id.consultAvatar).apply {
        layoutParams.height =
            itemView.context.resources.getDimension(style.operatorAvatarSize)
                .toInt()
        layoutParams.width =
            itemView.context.resources.getDimension(style.operatorAvatarSize)
                .toInt()
    }

    fun onBind(
        consultPhrase: ConsultPhrase,
        highlighted: Boolean,
        buttonClickListener: View.OnClickListener,
        onLongClickListener: OnLongClickListener,
        onAvatarClickListener: View.OnClickListener
    ) {
        subscribeForHighlighting(consultPhrase, rootLayout)
        applyBubbleLayoutStyle()
        timeStampTextView.setOnClickListener(buttonClickListener)
        timeStampTextView.setOnLongClickListener(onLongClickListener)
        timeStampTextView.text = sdf.format(Date(consultPhrase.timeStamp))
        rootLayout.setOnClickListener(buttonClickListener)
        rootLayout.setOnLongClickListener(onLongClickListener)
        consultAvatar.setOnClickListener(onAvatarClickListener)
        image.setOnClickListener(buttonClickListener)
        image.setOnLongClickListener(onLongClickListener)
        showAvatar(consultPhrase)
        consultPhrase.fileDescription?.let {
            when (it.state) {
                AttachmentStateEnum.PENDING -> {
                    showLoaderLayout(it)
                }
                AttachmentStateEnum.ERROR -> {
                    showErrorLayout(it)
                }
                else -> {
                    showCommonLayout(it)
                }
            }
        } ?: run {
            image.setImageResource(0)
        }
        rootLayout.apply {
            setBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    if (highlighted) style.chatHighlightingColor else R.color.threads_transparent
                )
            )
        }
    }

    private fun applyBubbleLayoutStyle() {
        val res = itemView.context.resources
        loaderLayout.background = AppCompatResources.getDrawable(
            itemView.context,
            style.incomingMessageBubbleBackground
        )
        loaderLayout.setPadding(
            res.getDimensionPixelSize(style.bubbleIncomingPaddingBottom),
            res.getDimensionPixelSize(style.bubbleIncomingPaddingTop),
            res.getDimensionPixelSize(style.bubbleIncomingPaddingRight),
            res.getDimensionPixelSize(style.bubbleIncomingPaddingBottom)
        )
        loaderLayout.background.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                getColorInt(style.incomingMessageBubbleColor),
                BlendModeCompat.SRC_ATOP
            )
    }

    private fun applyParams(imageView: ImageView) {
        val bubbleLeftMarginDp = itemView.context.resources.getDimension(R.dimen.margin_quarter)
        val bubbleLeftMarginPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            bubbleLeftMarginDp,
            itemView.resources.displayMetrics
        ).toInt()
        val lp = imageView.layoutParams as RelativeLayout.LayoutParams
        lp.setMargins(bubbleLeftMarginPx, lp.topMargin, lp.rightMargin, lp.bottomMargin)
        imageView.layoutParams = lp
    }

    private fun showLoadImageAnimation() {
        loaderImage.isVisible = true
        initAnimation(loaderImage, true)
        rotateAnim.start()
    }

    private fun stopLoadImageAnimation() {
        loaderImage.isVisible = false
        rotateAnim.cancel()
    }

    private fun showLoaderLayout(fileDescription: FileDescription) {
        loaderLayout.isVisible = true
        commonLayout.isVisible = false
        errorTextView.isVisible = false
        fileNameTextView.text = fileDescription.incomingName
        initAnimation(loader, true)
    }

    private fun showErrorLayout(fileDescription: FileDescription) {
        loaderLayout.isVisible = true
        errorTextView.isVisible = true
        commonLayout.isVisible = false
        loader.setImageResource(getErrorImageResByErrorCode(fileDescription.errorCode))
        fileNameTextView.text = fileDescription.incomingName
        val errorString = getString(getErrorStringResByErrorCode(fileDescription.errorCode))
        errorTextView.text = errorString
        rotateAnim.cancel()
    }

    private fun showCommonLayout(fileDescription: FileDescription) {
        commonLayout.isVisible = true
        loaderLayout.isVisible = false
        errorTextView.isVisible = false
        rotateAnim.cancel()
        val fileUri = if (fileDescription.fileUri?.toString()?.isNotBlank() == true) {
            fileDescription.fileUri.toString()
        } else {
            fileDescription.downloadPath
        }
        val isStateReady = fileDescription.state == AttachmentStateEnum.READY
        if (isStateReady && fileUri != null && !fileDescription.isDownloadError) {
            showLoadImageAnimation()
            image.loadImage(
                fileUri,
                listOf(ImageView.ScaleType.FIT_XY, ImageView.ScaleType.CENTER_CROP),
                style.imagePlaceholder,
                autoRotateWithExif = true,
                modifications = listOf(maskedTransformation),
                callback = object : ImageLoader.ImageLoaderCallback {
                    override fun onImageLoaded() {
                        stopLoadImageAnimation()
                    }

                    override fun onImageLoadError() {
                        stopLoadImageAnimation()
                    }
                }
            )
        } else if (fileDescription.isDownloadError) {
            stopLoadImageAnimation()
            image.setImageResource(style.imagePlaceholder)
        }
    }

    private fun showAvatar(consultPhrase: ConsultPhrase) {
        if (consultPhrase.isAvatarVisible) {
            consultAvatar.visible()
            consultPhrase.avatarPath?.let {
                consultAvatar.loadImage(
                    FileUtils.convertRelativeUrlToAbsolute(it),
                    listOf(ImageView.ScaleType.FIT_XY, ImageView.ScaleType.CENTER_INSIDE),
                    modifications = listOf(ImageModifications.CircleCropModification)
                )
            } ?: run {
                consultAvatar.setImageResource(style.defaultOperatorAvatar)
            }
        } else {
            consultAvatar.invisible()
        }
    }
}
