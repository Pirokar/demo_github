package im.threads.ui.holders

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
import im.threads.business.ogParser.OpenGraphParser
import im.threads.business.utils.FileUtils
import im.threads.ui.utils.gone
import im.threads.ui.utils.invisible
import im.threads.ui.utils.visible
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
        R.layout.item_image_from_consult,
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

    private val consultAvatar = itemView.findViewById<ImageView>(R.id.consultAvatar).apply {
        layoutParams.height = itemView.context.resources.getDimension(style.operatorAvatarSize).toInt()
        layoutParams.width = itemView.context.resources.getDimension(style.operatorAvatarSize).toInt()
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
        val layoutParams = loaderLayout.layoutParams as RelativeLayout.LayoutParams
        layoutParams.setMargins(
            res.getDimensionPixelSize(style.bubbleIncomingMarginLeft),
            res.getDimensionPixelSize(style.bubbleIncomingMarginTop),
            res.getDimensionPixelSize(style.bubbleIncomingMarginRight),
            res.getDimensionPixelSize(style.bubbleIncomingMarginBottom)
        )
        loaderLayout.layoutParams = layoutParams

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
        imageLayout.invisible()
        errorTextView.gone()
        fileNameTextView.text = fileDescription.incomingName
        initAnimation(loader, true)
    }

    private fun showErrorLayout(fileDescription: FileDescription) {
        loaderLayout.isVisible = true
        errorTextView.isVisible = true
        imageLayout.invisible()
        loader.setImageResource(getErrorImageResByErrorCode(fileDescription.errorCode))
        fileNameTextView.text = fileDescription.incomingName
        val errorString = getString(getErrorStringResByErrorCode(fileDescription.errorCode))
        errorTextView.text = errorString
        rotateAnim.cancel()
    }

    private fun showCommonLayout(fileDescription: FileDescription) {
        imageLayout.visible()
        loaderLayout.gone()
        errorTextView.gone()
        rotateAnim.cancel()
        val fileUri = if (fileDescription.fileUri?.toString()?.isNotBlank() == true) {
            fileDescription.fileUri.toString()
        } else {
            fileDescription.downloadPath
        }
        val isStateReady = fileDescription.state == AttachmentStateEnum.READY
        if (isStateReady && fileUri != null && !fileDescription.isDownloadError) {
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
                    errorDrawableResId = R.drawable.threads_operator_avatar_placeholder,
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
