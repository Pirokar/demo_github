package im.threads.ui.views.recordview

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import im.threads.R

/**
 * Created by Devlomi on 13/12/2017.
 */
internal class VoiceRecordButton : AppCompatImageView, OnTouchListener, View.OnClickListener {
    private var scaleAnim: VoiceRecordScaleAnim? = null
    private var recordView: VoiceRecordView? = null
    var isListenForRecord = true
    private var onRecordClickListener: VoiceRecordOnRecordClickListener? = null
    private var sendClickListener: VoiceRecordOnRecordClickListener? = null
    private var isInLockMode = false
    private var micIcon: Drawable? = null
    private var sendIcon: Drawable? = null

    fun setRecordView(recordView: VoiceRecordView?) {
        this.recordView = recordView
        recordView?.setRecordButton(this)
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        var scaleUpTo = 1f
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.VoiceRecordButton)
            val imageResource = typedArray.getResourceId(R.styleable.VoiceRecordButton_mic_icon, -1)
            val sendResource = typedArray.getResourceId(R.styleable.VoiceRecordButton_send_icon, -1)
            scaleUpTo = typedArray.getFloat(R.styleable.VoiceRecordButton_scale_up_to, -1f)
            if (imageResource != -1) {
                setTheImageResource(imageResource)
            }
            if (sendResource != -1) {
                sendIcon = AppCompatResources.getDrawable(getContext(), sendResource)
            }
            typedArray.recycle()
        }
        scaleAnim = VoiceRecordScaleAnim(this)
        if (scaleUpTo > 1) {
            scaleAnim?.setScaleUpTo(scaleUpTo)
        }
        setOnTouchListener(this)
        setOnClickListener(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setClip(this)
    }

    fun setScaleUpTo(scaleTo: Float?) {
        scaleAnim?.setScaleUpTo(scaleTo!!)
    }

    private fun setClip(v: View) {
        if (v.parent == null) {
            return
        }
        if (v is ViewGroup) {
            v.clipChildren = false
            v.clipToPadding = false
        }
        if (v.parent is View) {
            setClip(v.parent as View)
        }
    }

    private fun setTheImageResource(imageResource: Int) {
        val image = AppCompatResources.getDrawable(context, imageResource)
        setImageDrawable(image)
        micIcon = image
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (isListenForRecord) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> recordView?.onActionDown(v as VoiceRecordButton)
                MotionEvent.ACTION_MOVE -> recordView?.onActionMove(v as VoiceRecordButton, event)
                MotionEvent.ACTION_UP -> recordView?.onActionUp()
            }
        }
        return isListenForRecord
    }

    fun startScale() {
        scaleAnim?.start()
    }

    fun stopScale() {
        scaleAnim?.stop()
    }

    fun setOnRecordClickListener(onRecordClickListener: VoiceRecordOnRecordClickListener?) {
        this.onRecordClickListener = onRecordClickListener
    }

    fun setSendClickListener(sendClickListener: VoiceRecordOnRecordClickListener?) {
        this.sendClickListener = sendClickListener
    }

    fun setInLockMode(inLockMode: Boolean) {
        isInLockMode = inLockMode
    }

    fun setSendIconResource(resource: Int) {
        sendIcon = AppCompatResources.getDrawable(context, resource)
    }

    override fun onClick(v: View) {
        if (isInLockMode && sendClickListener != null) {
            sendClickListener!!.onClick(v)
        } else {
            onRecordClickListener?.onClick(v)
        }
    }

    fun changeIconToSend() {
        if (sendIcon != null) {
            setImageDrawable(sendIcon)
        }
    }

    fun changeIconToRecord() {
        if (micIcon != null) {
            setImageDrawable(micIcon)
        }
    }
}
