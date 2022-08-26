package im.threads.internal.holders

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
import im.threads.ChatStyle
import im.threads.R
import im.threads.internal.Config
import im.threads.internal.imageLoading.ImageLoader
import im.threads.internal.imageLoading.ImageModifications
import im.threads.internal.imageLoading.loadImage
import im.threads.internal.model.AttachmentStateEnum
import im.threads.internal.model.ConsultPhrase
import im.threads.internal.model.FileDescription
import im.threads.internal.utils.FileUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageFromConsultViewHolder(
    parent: ViewGroup,
    private val maskedTransformation: ImageModifications.MaskedModification
) :
    BaseHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_image_from_consult,
            parent,
            false
        )
    ) {

    private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val style: ChatStyle = Config.instance.chatStyle

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
        val style = Config.instance.chatStyle
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
            consultAvatar.isVisible = true
            consultPhrase.avatarPath?.let {
                consultAvatar.loadImage(
                    FileUtils.convertRelativeUrlToAbsolute(it),
                    listOf(ImageView.ScaleType.FIT_XY, ImageView.ScaleType.CENTER_INSIDE),
                    modifications = listOf(ImageModifications.CircleCropModification),
                    callback = object : ImageLoader.ImageLoaderCallback {
                        override fun onImageLoaded() {
                            consultAvatar.setImageResource(style.defaultOperatorAvatar)
                        }
                    }
                )
            } ?: run {
                consultAvatar.setImageResource(style.defaultOperatorAvatar)
            }
        } else {
            consultAvatar.isVisible = false
        }
    }
}
