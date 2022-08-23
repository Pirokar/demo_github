package im.threads.internal.holders

import android.graphics.PorterDuff
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import im.threads.ChatStyle
import im.threads.R
import im.threads.business.imageLoading.ImageLoader
import im.threads.business.imageLoading.ImageModifications
import im.threads.business.imageLoading.loadImage
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.FileDescription
import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.utils.FileUtils
import im.threads.internal.Config
import im.threads.internal.utils.ColorsHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageFromConsultViewHolder(
    private val parent: ViewGroup,
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
    private val rotateAnim = RotateAnimation(
        0f,
        360f,
        Animation.RELATIVE_TO_SELF,
        0.5f,
        Animation.RELATIVE_TO_SELF,
        0.5f
    )

    private val mTimeStampTextView = (itemView.findViewById(R.id.timestamp) as TextView).apply {
        setTextColor(getColorInt(style.incomingImageTimeColor))
        if (style.incomingMessageTimeTextSize > 0) {
            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                itemView.context.resources.getDimension(style.incomingMessageTimeTextSize)
            )
        }
        background.setColorFilter(
            getColorInt(style.incomingImageTimeBackgroundColor),
            PorterDuff.Mode.SRC_ATOP
        )
    }
    private val mTimeStampDuplicateTextView =
        (itemView.findViewById(R.id.timestampDuplicate) as TextView).apply {
            setTextColor(getColorInt(style.incomingImageTimeColor))
            if (style.incomingMessageTimeTextSize > 0) {
                setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    itemView.context.resources.getDimension(style.incomingMessageTimeTextSize)
                )
            }
            background.setColorFilter(
                getColorInt(style.incomingImageTimeBackgroundColor),
                PorterDuff.Mode.SRC_ATOP
            )
        }

    private val mImage: ImageView =
        itemView.findViewById<ImageView>(R.id.image).also { applyParams(it) }
    private val errorText: TextView = itemView.findViewById(R.id.errorText)
    private val loaderLayout: FrameLayout = itemView.findViewById(R.id.loaderLayout)
    private val commonLayout: RelativeLayout = itemView.findViewById(R.id.commonLayout)
    private val bubbleLayout: LinearLayout = itemView.findViewById(R.id.bubble)
    private val fileName: TextView = itemView.findViewById(R.id.fileName)
    private val loader: ImageView = itemView.findViewById(R.id.loader)

    private val mConsultAvatar = (itemView.findViewById(R.id.consult_avatar) as ImageView).apply {
        layoutParams.height =
            itemView.context.resources.getDimension(style.operatorAvatarSize)
                .toInt()
        layoutParams.width =
            itemView.context.resources.getDimension(style.operatorAvatarSize)
                .toInt()
    }
    private val mConsultAvatarDuplicate =
        (itemView.findViewById(R.id.consultAvatarDuplicate) as ImageView).apply {
            layoutParams.height =
                itemView.context.resources.getDimension(style.operatorAvatarSize)
                    .toInt()
            layoutParams.width =
                itemView.context.resources.getDimension(style.operatorAvatarSize)
                    .toInt()
        }
    private val filterView = (itemView.findViewById(R.id.filter) as View).apply {
        setBackgroundColor(
            ContextCompat.getColor(
                itemView.context,
                style.chatHighlightingColor
            )
        )
    }
    private val secondFilterView = (itemView.findViewById(R.id.filter_second) as View).apply {
        setBackgroundColor(
            ContextCompat.getColor(
                itemView.context,
                style.chatHighlightingColor
            )
        )
    }

    fun onBind(
        consultPhrase: ConsultPhrase,
        highlighted: Boolean,
        buttonClickListener: View.OnClickListener,
        onLongClickListener: OnLongClickListener,
        onAvatarClickListener: View.OnClickListener
    ) {
        val fileDescription = consultPhrase.fileDescription
        mTimeStampTextView.setOnClickListener(buttonClickListener)
        mTimeStampTextView.setOnLongClickListener(onLongClickListener)
        mTimeStampTextView.text = sdf.format(Date(consultPhrase.timeStamp))
        mTimeStampDuplicateTextView.setOnClickListener(buttonClickListener)
        mTimeStampDuplicateTextView.setOnLongClickListener(onLongClickListener)
        mTimeStampDuplicateTextView.text = sdf.format(Date(consultPhrase.timeStamp))
        filterView.setOnClickListener(buttonClickListener)
        filterView.setOnLongClickListener(onLongClickListener)
        mImage.setOnClickListener(buttonClickListener)
        mImage.setOnLongClickListener(onLongClickListener)
        mImage.setImageResource(0)
        applyBubbleLayoutStyle()

        if (fileDescription != null) {
            if (fileDescription.state == AttachmentStateEnum.PENDING) {
                showLoaderLayout(fileDescription)
            } else if (fileDescription.state == AttachmentStateEnum.ERROR) {
                showErrorLayout(fileDescription)
            } else {
                showCommonLayout()
                val fileUri = if (fileDescription.fileUri?.toString()?.isNotBlank() == true) {
                    fileDescription.fileUri.toString()
                } else {
                    fileDescription.downloadPath
                }
                val isStateReady = fileDescription.state == AttachmentStateEnum.READY
                if (isStateReady && fileUri != null && !fileDescription.isDownloadError) {
                    mImage.loadImage(
                        fileUri,
                        listOf(ImageView.ScaleType.FIT_XY, ImageView.ScaleType.CENTER_CROP),
                        style.imagePlaceholder,
                        autoRotateWithExif = true,
                        modifications = listOf(maskedTransformation),
                        callback = object : ImageLoader.ImageLoaderCallback {
                            override fun onImageLoaded() {
                            }
                        }
                    )
                } else if (fileDescription.isDownloadError) {
                    mImage.setImageResource(style.imagePlaceholder)
                }
            }
        }
        filterView.visibility = if (highlighted) View.VISIBLE else View.INVISIBLE
        secondFilterView.visibility = if (highlighted) View.VISIBLE else View.INVISIBLE
        val avatarPath = consultPhrase.avatarPath
        if (consultPhrase.isAvatarVisible) {
            mConsultAvatar.visibility = View.VISIBLE
            mConsultAvatarDuplicate.visibility = View.VISIBLE
            mConsultAvatar.setOnClickListener(onAvatarClickListener)
            mConsultAvatarDuplicate.setOnClickListener(onAvatarClickListener)
            mConsultAvatar.setImageResource(style.defaultOperatorAvatar)
            mConsultAvatarDuplicate.setImageResource(style.defaultOperatorAvatar)
            if (avatarPath != null) {
                mConsultAvatar.loadImage(
                    FileUtils.convertRelativeUrlToAbsolute(avatarPath),
                    listOf(ImageView.ScaleType.FIT_XY, ImageView.ScaleType.CENTER_INSIDE),
                    modifications = listOf(ImageModifications.CircleCropModification)
                )
                mConsultAvatarDuplicate.loadImage(
                    FileUtils.convertRelativeUrlToAbsolute(avatarPath),
                    listOf(ImageView.ScaleType.FIT_XY, ImageView.ScaleType.CENTER_INSIDE),
                    modifications = listOf(ImageModifications.CircleCropModification)
                )
            }
        } else {
            mConsultAvatar.visibility = View.INVISIBLE
            mConsultAvatarDuplicate.visibility = View.INVISIBLE
        }
    }

    private fun applyBubbleLayoutStyle() {
        val res = itemView.context.resources
        val style = Config.instance.chatStyle
        bubbleLayout.background = AppCompatResources.getDrawable(
            itemView.context,
            style.incomingMessageBubbleBackground
        )
        bubbleLayout.setPadding(
            res.getDimensionPixelSize(style.bubbleIncomingPaddingBottom),
            res.getDimensionPixelSize(style.bubbleIncomingPaddingTop),
            res.getDimensionPixelSize(style.bubbleIncomingPaddingRight),
            res.getDimensionPixelSize(style.bubbleIncomingPaddingBottom)
        )
        bubbleLayout.background.setColorFilter(
            getColorInt(style.incomingMessageBubbleColor),
            PorterDuff.Mode.SRC_ATOP
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

    private fun showLoaderLayout(fileDescription: FileDescription) {
        loaderLayout.visibility = View.VISIBLE
        commonLayout.visibility = View.GONE
        errorText.visibility = View.GONE
        fileName.text = fileDescription.incomingName
        loader.setImageResource(R.drawable.im_loading_themed)
        ColorsHelper.setTint(
            parent.context,
            loader,
            Config.instance.chatStyle.chatToolbarColorResId
        )
        rotateAnim.duration = 3000
        rotateAnim.repeatCount = Animation.INFINITE
        loader.animation = rotateAnim
    }

    private fun showErrorLayout(fileDescription: FileDescription) {
        errorText.visibility = View.VISIBLE
        loaderLayout.visibility = View.VISIBLE
        commonLayout.visibility = View.GONE
        loader.setImageResource(getErrorImageResByErrorCode(fileDescription.errorCode))
        fileName.text = fileDescription.incomingName
        val errorString = getString(getErrorStringResByErrorCode(fileDescription.errorCode))
        errorText.text = errorString
        rotateAnim.cancel()
        rotateAnim.reset()
    }

    private fun showCommonLayout() {
        commonLayout.visibility = View.VISIBLE
        errorText.visibility = View.GONE
        loaderLayout.visibility = View.GONE
        rotateAnim.cancel()
        rotateAnim.reset()
    }
}
