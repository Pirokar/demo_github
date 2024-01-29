package im.threads.ui.views.recordview

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.content.res.AppCompatResources
import im.threads.R
import im.threads.business.utils.ThreadsPermissionChecker
import im.threads.databinding.EccVoiceRecordViewLayoutBinding
import im.threads.ui.utils.dpToPx

/**
 * Created by Devlomi on 24/08/2017.
 */
internal class VoiceRecordView : RelativeLayout, VoiceRecordLockViewListener {
    private var initialRecordButtonX = 0f
    private var initialRecordButtonY = 0f
    private var recordButtonYInWindow = 0f
    private var basketInitialY = 0f
    private var difX = 0f
    internal var cancelBounds = DEFAULT_CANCEL_BOUNDS.toFloat()
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private var context: Context
    private var recordListener: VoiceOnRecordListener? = null
    private var isSwiped = false
    private var isLessThanSecondAllowed = false
    private val player: MediaPlayer? = null
    private var voiceRecordAnimationHelper: VoiceRecordAnimationHelper? = null
    private var isRecordButtonGrowingAnimationEnabled = true
    var isShimmerEffectEnabled = true
    private var timeLimit: Long = -1
    private var runnable: Runnable? = null
    private var handler: Handler? = null
    private var recordButton: VoiceRecordButton? = null
    private var canRecord = true
    private var recordLockView: VoiceRecordLockView? = null
    private var isLockEnabled = false
    var recordLockYInWindow = 0f
    var recordLockXInWindow = 0f
    private var fractionReached = false
    private var currentYFraction = 0f
    private var isLockInSameParent = false

    private var binding: EccVoiceRecordViewLayoutBinding? = null

    constructor(context: Context) : super(context) {
        this.context = context
        init(context, null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.context = context
        init(context, attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.context = context
        init(context, attrs, defStyleAttr, 0)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        binding = EccVoiceRecordViewLayoutBinding.inflate(LayoutInflater.from(context))
        binding?.apply {
            addView(root)
            val viewGroup = root.parent as ViewGroup
            viewGroup.clipChildren = false
            hideViews(true)
            if (attrs != null && defStyleAttr == 0 && defStyleRes == 0) {
                val typedArray = context.obtainStyledAttributes(
                    attrs,
                    R.styleable.VoiceRecordView,
                    defStyleAttr,
                    defStyleRes
                )
                val slideArrowResource = typedArray.getResourceId(R.styleable.VoiceRecordView_slide_to_cancel_arrow, -1)
                val slideToCancelText = typedArray.getString(R.styleable.VoiceRecordView_slide_to_cancel_text)
                val slideMarginRight = typedArray.getDimension(R.styleable.VoiceRecordView_slide_to_cancel_margin_right, 30f).toInt()
                val counterTimeColor = typedArray.getColor(R.styleable.VoiceRecordView_counter_time_color, -1)
                val arrowColor = typedArray.getColor(R.styleable.VoiceRecordView_slide_to_cancel_arrow_color, -1)
                val cancelText = typedArray.getString(R.styleable.VoiceRecordView_cancel_text)
                val cancelMarginRight = typedArray.getDimension(R.styleable.VoiceRecordView_cancel_text_margin_right, 30f).toInt()
                val cancelTextColor = typedArray.getColor(R.styleable.VoiceRecordView_cancel_text_color, -1)
                val cancelBounds = typedArray.getDimensionPixelSize(R.styleable.VoiceRecordView_slide_to_cancel_bounds, -1)
                if (cancelBounds != -1) {
                    setCancelBounds(
                        cancelBounds.toFloat(),
                        false
                    )
                }
                if (slideArrowResource != -1) {
                    val slideArrow = AppCompatResources.getDrawable(getContext(), slideArrowResource)
                    arrow.setImageDrawable(slideArrow)
                }
                if (slideToCancelText != null) slideToCancel.text = slideToCancelText
                if (counterTimeColor != -1) setChronometerCounterTimeColor(counterTimeColor)
                if (arrowColor != -1) setSlideToCancelArrowColor(arrowColor)
                if (cancelText != null) cancelTextView.text = cancelText
                if (cancelTextColor != -1) cancelTextView.setTextColor(cancelTextColor)
                setMarginRight(slideMarginRight, true)
                setCancelMarginRight(cancelMarginRight, true)
                typedArray.recycle()
            }
            voiceRecordAnimationHelper = VoiceRecordAnimationHelper(
                context,
                basketImg,
                smallBlinkingMic,
                isRecordButtonGrowingAnimationEnabled
            )
            cancelTextView.setOnClickListener {
                voiceRecordAnimationHelper?.animateBasket(basketInitialY)
                cancelAndDeleteRecord()
            }
        }
    }

    private fun cancelAndDeleteRecord() {
        if (isTimeLimitValid) {
            removeTimeLimitCallbacks()
        }
        isSwiped = true
        voiceRecordAnimationHelper?.setStartRecorded(false)
        recordListener?.onCancel()
        resetRecord(recordButton)
    }

    private val isTimeLimitValid: Boolean
        get() = timeLimit > 0

    private fun initTimeLimitHandler() {
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            if (!isSwiped) recordListener?.onFinish(elapsedTime, true)
            removeTimeLimitCallbacks()
            voiceRecordAnimationHelper?.setStartRecorded(false)
            if (recordButton != null) {
                resetRecord(recordButton)
            }
            isSwiped = true
        }
    }

    private fun hideViews(hideSmallMic: Boolean) = binding?.apply {
        slideToCancelLayout.visibility = GONE
        counterTime.visibility = GONE
        cancelTextView.visibility = GONE
        if (isLockEnabled && recordLockView != null) {
            recordLockView?.visibility = GONE
        }
        if (hideSmallMic) smallBlinkingMic.visibility = GONE
    }

    private fun showViews() = binding?.apply {
        slideToCancelLayout.visibility = VISIBLE
        smallBlinkingMic.visibility = VISIBLE
        counterTime.visibility = VISIBLE
        if (isLockEnabled) {
            recordLockView?.visibility = VISIBLE
        }
    }

    private fun isLessThanOneSecond(time: Long): Boolean {
        return time <= 1000
    }

    internal fun onActionDown(recordBtn: VoiceRecordButton) = binding?.apply {
        if (!isRecordPermissionGranted) {
            return@apply
        }
        recordListener?.onStart()
        if (isTimeLimitValid && runnable != null) {
            removeTimeLimitCallbacks()
            handler?.postDelayed(runnable!!, timeLimit)
        }
        voiceRecordAnimationHelper?.setStartRecorded(true)
        voiceRecordAnimationHelper?.resetBasketAnimation()
        voiceRecordAnimationHelper?.resetSmallMic()
        if (isRecordButtonGrowingAnimationEnabled) {
            recordBtn.startScale()
        }
        if (isShimmerEffectEnabled) {
            slideToCancelLayout.ensureAnimationStarted()
        }
        initialRecordButtonX = recordBtn.x
        val recordButtonLocation = IntArray(2)
        recordBtn.getLocationInWindow(recordButtonLocation)
        initialRecordButtonY = recordButton?.y ?: 0f
        if (isLockEnabled && recordLockView != null) {
            isLockInSameParent = isLockAndRecordButtonHaveSameParent
            val recordLockLocation = IntArray(2)
            recordLockView?.getLocationInWindow(recordLockLocation)
            recordLockXInWindow = recordLockLocation[0].toFloat()
            recordLockYInWindow =
                if (isLockInSameParent) (recordLockView?.y ?: 0f) else recordLockLocation[1].toFloat()
            recordButtonYInWindow =
                if (isLockInSameParent) (recordButton?.y ?: 0f) else recordButtonLocation[1].toFloat()
        }
        basketInitialY = basketImg.y + 90
        showViews()
        voiceRecordAnimationHelper?.animateSmallMicAlpha()
        counterTime.base = SystemClock.elapsedRealtime()
        startTime = System.currentTimeMillis()
        counterTime.start()
        isSwiped = false
        currentYFraction = 0f
    }

    internal fun onActionMove(recordBtn: VoiceRecordButton, motionEvent: MotionEvent) = binding?.apply {
        if (!canRecord || fractionReached) {
            return@apply
        }
        val time = System.currentTimeMillis() - startTime
        if (!isSwiped) {
            val slideToCancelLayoutX = slideToCancelLayout.x
            val counterTimeRight = counterTime.right

            if (slideToCancelLayoutX != 0f && slideToCancelLayoutX <= counterTimeRight + cancelBounds) {
                if (isLessThanOneSecond(time)) {
                    hideViews(true)
                    voiceRecordAnimationHelper?.clearAlphaAnimation(false)
                    voiceRecordAnimationHelper?.onAnimationEnd()
                } else {
                    hideViews(false)
                    voiceRecordAnimationHelper?.animateBasket(basketInitialY)
                }
                voiceRecordAnimationHelper?.moveRecordButtonAndSlideToCancelBack(
                    recordBtn,
                    slideToCancelLayout,
                    initialRecordButtonX,
                    initialRecordButtonY,
                    difX,
                    isLockEnabled
                )
                counterTime.stop()
                if (isShimmerEffectEnabled) {
                    slideToCancelLayout.stopShimmerAnimation()
                }
                isSwiped = true
                voiceRecordAnimationHelper?.setStartRecorded(false)
                recordListener?.onCancel()
                if (isTimeLimitValid) {
                    removeTimeLimitCallbacks()
                }
            } else {
                if (canMoveX(motionEvent)) {
                    recordBtn.animate()
                        .x(motionEvent.rawX)
                        .setDuration(0)
                        .start()
                    if (difX == 0f) difX = initialRecordButtonX - slideToCancelLayoutX
                    slideToCancelLayout.animate()
                        ?.x(motionEvent.rawX - difX)
                        ?.setDuration(0)
                        ?.start()
                }

                val newY = if (isLockInSameParent) motionEvent.rawY else motionEvent.rawY - recordButtonYInWindow
                if (canMoveY(motionEvent, newY)) {
                    recordBtn.animate()
                        .y(newY)
                        .setDuration(0)
                        .start()
                    val currentY = motionEvent.rawY
                    val minY = recordLockYInWindow
                    val maxY = recordButtonYInWindow
                    var fraction = (currentY - minY) / (maxY - minY)
                    fraction = 1 - fraction
                    currentYFraction = fraction
                    recordLockView?.animateLock(fraction)
                    if (isRecordButtonGrowingAnimationEnabled) {
                        val scale = 1 - fraction + 1
                        recordBtn.animate().scaleX(scale).scaleY(scale).setDuration(0).start()
                    }
                }
            }
        }
    }

    private fun canMoveX(motionEvent: MotionEvent): Boolean {
        return if (motionEvent.rawX < initialRecordButtonX) {
            if (isLockEnabled) {
                currentYFraction <= 0.3
            } else {
                true
            }
        } else {
            false
        }
    }

    private fun canMoveY(motionEvent: MotionEvent, dif: Float): Boolean {
        return if (isLockEnabled) {
            /*
             1. prevent swiping below record button
             2. prevent swiping up if record button is NOT near record Lock's X
             */
            if (isLockInSameParent) {
                motionEvent.rawY < initialRecordButtonY && motionEvent.rawX >= recordLockXInWindow
            } else {
                dif <= initialRecordButtonY && motionEvent.rawX >= recordLockXInWindow
            }
        } else {
            false
        }
    }

    internal fun onActionUp() {
        if (!canRecord || fractionReached) {
            return
        }
        finishAndSaveRecord()
    }

    private fun finishAndSaveRecord() {
        elapsedTime = System.currentTimeMillis() - startTime
        if (!isLessThanSecondAllowed && isLessThanOneSecond(elapsedTime) && !isSwiped) {
            recordListener?.onLessThanSecond()
            removeTimeLimitCallbacks()
            voiceRecordAnimationHelper?.setStartRecorded(false)
        } else {
            if (recordListener != null && !isSwiped) recordListener!!.onFinish(elapsedTime, false)
            removeTimeLimitCallbacks()
            voiceRecordAnimationHelper?.setStartRecorded(false)
        }
        resetRecord(recordButton)
    }

    private fun switchToLockedMode() = binding?.apply {
        cancelTextView.visibility = VISIBLE
        slideToCancelLayout.visibility = GONE
        recordButton?.animate()
            ?.x(initialRecordButtonX)
            ?.y(initialRecordButtonY)
            ?.setDuration(100)
            ?.start()
        if (isRecordButtonGrowingAnimationEnabled) {
            recordButton?.stopScale()
        }
        recordButton?.isListenForRecord = false
        recordButton?.setInLockMode(true)
        recordButton?.changeIconToSend()
    }

    private val isLockAndRecordButtonHaveSameParent: Boolean
        get() {
            if (recordLockView == null) {
                return false
            }
            val lockParent = recordLockView?.parent
            val recordButtonParent = recordButton?.parent
            return if (lockParent == null || recordButtonParent == null) {
                false
            } else {
                lockParent === recordButtonParent
            }
        }

    private fun resetRecord(recordBtn: VoiceRecordButton?) = binding?.apply {
        hideViews(!isSwiped)
        fractionReached = false
        if (!isSwiped) voiceRecordAnimationHelper!!.clearAlphaAnimation(true)
        voiceRecordAnimationHelper?.moveRecordButtonAndSlideToCancelBack(
            recordBtn,
            slideToCancelLayout,
            initialRecordButtonX,
            initialRecordButtonY,
            difX,
            isLockEnabled
        )
        counterTime.stop()
        if (isShimmerEffectEnabled) {
            slideToCancelLayout.stopShimmerAnimation()
        }
        if (isLockEnabled) {
            recordLockView?.reset()
            recordBtn?.changeIconToRecord()
        }
        cancelTextView.visibility = GONE
        recordBtn?.isListenForRecord = true
        recordBtn?.setInLockMode(false)
    }

    private fun removeTimeLimitCallbacks() {
        if (isTimeLimitValid && runnable != null) {
            handler?.removeCallbacks(runnable!!)
        }
    }

    private val isRecordPermissionGranted: Boolean
        get() {
            return ThreadsPermissionChecker.isRecordAudioPermissionGranted(this.context)
        }

    private fun setMarginRight(marginRight: Int, convertToDp: Boolean) = binding?.apply {
        val layoutParams = slideToCancelLayout.layoutParams as LayoutParams
        if (convertToDp) {
            layoutParams.rightMargin = context.dpToPx(marginRight).toInt()
        } else {
            layoutParams.rightMargin = marginRight
        }
        slideToCancelLayout.layoutParams = layoutParams
    }

    private fun setCancelMarginRight(marginRight: Int, convertToDp: Boolean) = binding?.apply {
        val layoutParams = slideToCancelLayout.layoutParams as LayoutParams
        if (convertToDp) {
            layoutParams.rightMargin = context.dpToPx(marginRight).toInt()
        } else {
            layoutParams.rightMargin = marginRight
        }
        cancelTextView.layoutParams = layoutParams
    }

    fun setOnRecordListener(recordListener: VoiceOnRecordListener?) {
        this.recordListener = recordListener
    }

    fun setOnBasketAnimationEndListener(onBasketAnimationEndListener: VoiceRecordOnBasketAnimationEnd?) {
        voiceRecordAnimationHelper?.setOnBasketAnimationEndListener(onBasketAnimationEndListener)
    }

    fun setLessThanSecondAllowed(isAllowed: Boolean) {
        isLessThanSecondAllowed = isAllowed
    }

    fun setSlideToCancelText(text: String?) = binding?.apply {
        slideToCancel.text = text
    }

    fun setSlideToCancelTextColor(color: Int) = binding?.apply {
        slideToCancel.setTextColor(color)
    }

    fun setSmallMicColor(color: Int) = binding?.apply {
        smallBlinkingMic.setColorFilter(color)
    }

    fun setSmallMicIcon(icon: Int) = binding?.apply {
        smallBlinkingMic.setImageResource(icon)
    }

    fun setSlideMarginRight(marginRight: Int) = binding?.apply {
        setMarginRight(marginRight, true)
    }

    fun getCancelBounds(): Float {
        return cancelBounds
    }

    fun setCancelBounds(cancelBounds: Float) {
        setCancelBounds(cancelBounds, true)
    }

    fun setChronometerCounterTimeColor(color: Int) = binding?.apply {
        counterTime.setTextColor(color)
    }

    fun setSlideToCancelArrowColor(color: Int) = binding?.apply {
        arrow.setColorFilter(color)
    }

    private fun setCancelBounds(cancelBounds: Float, convertDpToPixel: Boolean) {
        val bounds = if (convertDpToPixel) context.dpToPx(cancelBounds.toInt()) else cancelBounds
        this.cancelBounds = bounds
    }

    fun isRecordButtonGrowingAnimationEnabled(): Boolean {
        return isRecordButtonGrowingAnimationEnabled
    }

    fun setRecordButtonGrowingAnimationEnabled(recordButtonGrowingAnimationEnabled: Boolean) {
        isRecordButtonGrowingAnimationEnabled = recordButtonGrowingAnimationEnabled
        voiceRecordAnimationHelper!!.setRecordButtonGrowingAnimationEnabled(recordButtonGrowingAnimationEnabled)
    }

    fun getTimeLimit(): Long {
        return timeLimit
    }

    fun setTimeLimit(timeLimit: Long) {
        this.timeLimit = timeLimit
        if (handler != null && runnable != null) {
            removeTimeLimitCallbacks()
        }
        initTimeLimitHandler()
    }

    fun setTrashIconColor(color: Int) {
        voiceRecordAnimationHelper?.setTrashIconColor(color)
    }

    fun setRecordLockImageView(recordLockView: VoiceRecordLockView?) {
        this.recordLockView = recordLockView
        this.recordLockView?.setRecordLockViewListener(this)
        this.recordLockView?.visibility = INVISIBLE
    }

    fun setLockEnabled(lockEnabled: Boolean) {
        isLockEnabled = lockEnabled
    }

    internal fun setRecordButton(recordButton: VoiceRecordButton?) {
        this.recordButton = recordButton
        this.recordButton?.setSendClickListener(object : VoiceRecordOnRecordClickListener {
            override fun onClick(v: View?) {
                finishAndSaveRecord()
            }
        })
    }

    /*
    Use this if you want to Finish And save the Record if user closes the app for example in 'onPause()'
     */
    fun finishRecord() {
        finishAndSaveRecord()
    }

    /*
    Use this if you want to Cancel And delete the Record if user closes the app for example in 'onPause()'
     */
    fun cancelRecord() {
        hideViews(true)
        voiceRecordAnimationHelper!!.clearAlphaAnimation(false)
        cancelAndDeleteRecord()
    }

    override fun onFractionReached() {
        fractionReached = true
        switchToLockedMode()
        if (recordListener != null) {
            recordListener!!.onLock()
        }
    }

    companion object {
        const val DEFAULT_CANCEL_BOUNDS = 8
    }
}
