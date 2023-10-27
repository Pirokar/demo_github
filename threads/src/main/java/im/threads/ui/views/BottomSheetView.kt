package im.threads.ui.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import im.threads.R
import im.threads.ui.config.Config.Companion.getInstance
import im.threads.ui.utils.ColorsHelper
import im.threads.ui.utils.FileHelper.isFileExtensionsEmpty
import im.threads.ui.utils.FileHelper.isJpgAllow
import im.threads.ui.utils.ViewUtils
import im.threads.ui.utils.gone
import im.threads.ui.utils.visible

class BottomSheetView : LinearLayout {
    private var buttonsListener: ButtonsListener? = null
    private lateinit var camera: Button
    private lateinit var file: Button
    private lateinit var gallery: Button
    private lateinit var send: Button
    private val chatStyle = getInstance().chatStyle
    private val viewUtils = ViewUtils()

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.ecc_view_bottom_attachment_sheet,
            this,
            true
        )
        camera = findViewById(R.id.camera)
        viewUtils.setCompoundDrawablesWithIntrinsicBoundsCompat(
            camera,
            chatStyle.attachmentCameraIconResId,
            ViewUtils.DrawablePosition.TOP
        )
        camera.setOnClickListener { buttonsListener?.onCameraClick() }
        gallery = findViewById(R.id.gallery)
        viewUtils.setCompoundDrawablesWithIntrinsicBoundsCompat(
            gallery,
            chatStyle.attachmentGalleryIconResId,
            ViewUtils.DrawablePosition.TOP
        )
        gallery.setOnClickListener { buttonsListener?.onGalleryClick() }
        file = findViewById(R.id.file)
        viewUtils.setCompoundDrawablesWithIntrinsicBoundsCompat(
            file,
            chatStyle.attachmentFileIconResId,
            ViewUtils.DrawablePosition.TOP
        )
        file.setOnClickListener { buttonsListener?.onFilePickerClick() }
        send = findViewById(R.id.send)
        viewUtils.setCompoundDrawablesWithIntrinsicBoundsCompat(
            send,
            chatStyle.attachmentSendIconResId,
            ViewUtils.DrawablePosition.TOP
        )
        send.setOnClickListener {
            buttonsListener?.onSendClick()
        }
        if (isFileExtensionsEmpty()) {
            file.gone()
        } else {
            file.visible()
        }
        if (isJpgAllow()) {
            camera.visible()
            gallery.visible()
        } else {
            camera.visible()
            gallery.visible()
        }
        setBackgroundColor(context.resources.getColor(android.R.color.white))
    }

    fun setButtonsTint(@ColorRes colorRes: Int) {
        val textColor: Int = if (colorRes == 0) {
            chatStyle.inputTextColor
        } else {
            colorRes
        }
        val color = ContextCompat.getColor(context, textColor)
        file.setTextColor(color)
        camera.setTextColor(color)
        gallery.setTextColor(color)
        send.setTextColor(color)
        val drawables = ArrayList<Drawable>()
        drawables.addAll(listOf(*file.compoundDrawables))
        drawables.addAll(listOf(*camera.compoundDrawables))
        drawables.addAll(listOf(*gallery.compoundDrawables))
        drawables.addAll(listOf(*send.compoundDrawables))
        for (drawable in drawables) {
            ColorsHelper.setDrawableColor(file.context, drawable, colorRes)
        }
    }

    fun setButtonsListener(listener: ButtonsListener?) {
        buttonsListener = listener
    }

    interface ButtonsListener {
        fun onCameraClick()
        fun onGalleryClick()
        fun onFilePickerClick()
        fun onSendClick()
    }
}
