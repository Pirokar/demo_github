package im.threads.internal.holders

import android.graphics.PorterDuff
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import im.threads.ChatStyle
import im.threads.R
import im.threads.internal.Config
import im.threads.internal.model.AttachmentStateEnum
import im.threads.internal.model.ConsultPhrase
import im.threads.internal.utils.CircleTransformation
import im.threads.internal.utils.FileUtils
import im.threads.internal.utils.MaskedTransformation
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageFromConsultViewHolder(
    parent: ViewGroup,
    private val maskedTransformation: MaskedTransformation
) :
    BaseHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_image_from_consult, parent, false)
    ) {

    private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val style: ChatStyle = Config.instance.chatStyle
    private val rotateAnim = RotateAnimation(
        0f, 360f,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
        0.5f
    )

    private val mTimeStampTextView = (itemView.findViewById(R.id.timestamp) as TextView).apply {
        setTextColor(getColorInt(style.incomingImageTimeColor))
        if (style.incomingMessageTimeTextSize > 0)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, itemView.context.resources.getDimension(style.incomingMessageTimeTextSize))
        background.setColorFilter(
            getColorInt(style.incomingImageTimeBackgroundColor),
            PorterDuff.Mode.SRC_ATOP
        )
    }
    private val mImage: ImageView = itemView.findViewById<ImageView>(R.id.image).also { applyParams(it) }
    private val mLoaderImage: ImageView = itemView.findViewById<ImageView>(R.id.loaderImage).also { applyParams(it) }
    private val mConsultAvatar = (itemView.findViewById(R.id.consult_avatar) as ImageView).apply {
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
        filterView.setOnClickListener(buttonClickListener)
        filterView.setOnLongClickListener(onLongClickListener)
        mImage.setOnClickListener(buttonClickListener)
        mImage.setOnLongClickListener(onLongClickListener)
        mImage.setImageResource(0)

        if (fileDescription != null) {
            val fileUri = if (fileDescription.fileUri?.toString()?.isNotBlank() == true) {
                fileDescription.fileUri.toString()
            } else {
                fileDescription.downloadPath
            }
            val isStateReady = fileDescription.state == AttachmentStateEnum.READY
            if (isStateReady && fileUri != null && !fileDescription.isDownloadError) {
                startLoaderAnimation()
                Picasso.get()
                    .load(fileUri)
                    .error(style.imagePlaceholder)
                    .fit()
                    .centerCrop()
                    .transform(maskedTransformation)
                    .into(
                        mImage,
                        object : Callback {
                            override fun onSuccess() {
                                stopLoaderAnimation()
                            }

                            override fun onError(e: Exception?) {}
                        }
                    )
            } else if (!isStateReady) {
                startLoaderAnimation()
            } else if (fileDescription.isDownloadError) {
                mImage.setImageResource(style.imagePlaceholder)
            }
        }
        filterView.visibility = if (highlighted) View.VISIBLE else View.INVISIBLE
        secondFilterView.visibility = if (highlighted) View.VISIBLE else View.INVISIBLE
        val avatarPath = consultPhrase.avatarPath
        if (consultPhrase.isAvatarVisible) {
            mConsultAvatar.visibility = View.VISIBLE
            mConsultAvatar.setOnClickListener(onAvatarClickListener)
            mConsultAvatar.setImageResource(style.defaultOperatorAvatar)
            if (avatarPath != null) {
                Picasso.get()
                    .load(FileUtils.convertRelativeUrlToAbsolute(avatarPath))
                    .fit()
                    .transform(CircleTransformation())
                    .centerInside()
                    .noPlaceholder()
                    .into(mConsultAvatar)
            }
        } else {
            mConsultAvatar.visibility = View.INVISIBLE
        }
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

    private fun startLoaderAnimation() {
        mLoaderImage.visibility = View.VISIBLE
        mImage.visibility = View.INVISIBLE
        rotateAnim.duration = 3000
        rotateAnim.repeatCount = Animation.INFINITE
        mLoaderImage.animation = rotateAnim
        rotateAnim.start()
    }

    private fun stopLoaderAnimation() {
        mLoaderImage.visibility = View.INVISIBLE
        mImage.visibility = View.VISIBLE
        rotateAnim.cancel()
        rotateAnim.reset()
    }
}
