package im.threads.ui.views.recordview

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import androidx.vectordrawable.graphics.drawable.AnimatorInflaterCompat
import im.threads.R
import im.threads.ui.utils.ColorFilterMode
import im.threads.ui.utils.applyColorFilter

internal class VoiceRecordAnimationHelper(
    private val context: Context,
    private val basketImg: ImageView?,
    private val smallBlinkingMic: ImageView?,
    private var recordButtonGrowingAnimationEnabled: Boolean
) {
    private val animatedVectorDrawable: AnimatedVectorDrawableCompat? =
        AnimatedVectorDrawableCompat.create(context, R.drawable.recv_basket_animated)
    private var alphaAnimation: AlphaAnimation? = null
    private var onBasketAnimationEndListener: VoiceRecordOnBasketAnimationEnd? = null
    private var isBasketAnimating = false
    private var isStartRecorded = false
    private var micX = 0f
    private var micY = 0f
    private var micAnimation: AnimatorSet? = null
    private var translateAnimation1: TranslateAnimation? = null
    private var translateAnimation2: TranslateAnimation? = null
    private var handler1: Handler? = null
    private var handler2: Handler? = null

    fun setTrashIconColor(color: Int) {
        animatedVectorDrawable?.applyColorFilter(color, ColorFilterMode.SRC_IN)
    }

    fun setRecordButtonGrowingAnimationEnabled(recordButtonGrowingAnimationEnabled: Boolean) {
        this.recordButtonGrowingAnimationEnabled = recordButtonGrowingAnimationEnabled
    }

    @SuppressLint("RestrictedApi")
    fun animateBasket(basketInitialY: Float) {
        isBasketAnimating = true
        clearAlphaAnimation(false)

        if (micX == 0f && smallBlinkingMic != null) {
            micX = smallBlinkingMic.x
            micY = smallBlinkingMic.y
        }
        micAnimation = AnimatorInflaterCompat.loadAnimator(
            context,
            R.animator.ecc_delete_mic_animation
        ) as AnimatorSet
        micAnimation?.setTarget(smallBlinkingMic) // set the view you want to animate
        translateAnimation1 = TranslateAnimation(0f, 0f, basketInitialY, basketInitialY - 90)
        translateAnimation1?.duration = 250
        translateAnimation2 = TranslateAnimation(0f, 0f, basketInitialY - 130, basketInitialY)
        translateAnimation2?.duration = 350
        micAnimation?.start()
        basketImg?.setImageDrawable(animatedVectorDrawable)
        handler1 = Handler(Looper.getMainLooper())
        handler1?.postDelayed({
            basketImg?.visibility = View.VISIBLE
            basketImg?.startAnimation(translateAnimation1)
        }, 350)
        translateAnimation1?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                animatedVectorDrawable?.start()
                handler2 = Handler(Looper.getMainLooper())
                handler2?.postDelayed({
                    basketImg?.startAnimation(translateAnimation2)
                    smallBlinkingMic?.visibility = View.INVISIBLE
                    basketImg?.visibility = View.INVISIBLE
                }, 450)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        translateAnimation2?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                basketImg?.visibility = View.INVISIBLE
                isBasketAnimating = false

                if (onBasketAnimationEndListener != null && !isStartRecorded) {
                    onBasketAnimationEndListener?.onAnimationEnd()
                }
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    fun resetBasketAnimation() {
        if (isBasketAnimating) {
            translateAnimation1?.reset()
            translateAnimation1?.cancel()
            translateAnimation2?.reset()
            translateAnimation2?.cancel()
            micAnimation?.cancel()
            smallBlinkingMic?.clearAnimation()
            basketImg?.clearAnimation()
            handler1?.removeCallbacksAndMessages(null)
            handler2?.removeCallbacksAndMessages(null)
            basketImg?.visibility = View.INVISIBLE
            smallBlinkingMic?.x = micX
            smallBlinkingMic?.y = micY
            smallBlinkingMic?.visibility = View.GONE
            isBasketAnimating = false
        }
    }

    fun clearAlphaAnimation(hideView: Boolean) {
        alphaAnimation?.cancel()
        alphaAnimation?.reset()
        if (smallBlinkingMic != null) {
            smallBlinkingMic.clearAnimation()
            if (hideView) {
                smallBlinkingMic.visibility = View.GONE
            }
        }
    }

    fun animateSmallMicAlpha() {
        alphaAnimation = AlphaAnimation(0.0f, 1.0f)
        alphaAnimation?.duration = 500
        alphaAnimation?.repeatMode = Animation.REVERSE
        alphaAnimation?.repeatCount = Animation.INFINITE
        smallBlinkingMic?.startAnimation(alphaAnimation)
    }

    fun moveRecordButtonAndSlideToCancelBack(
        recordBtn: VoiceRecordButton?,
        slideToCancelLayout: FrameLayout?,
        initialX: Float,
        initialY: Float,
        difX: Float,
        setY: Boolean
    ) {
        val positionAnimator = ValueAnimator.ofFloat(recordBtn?.x ?: 0f, initialX)
        positionAnimator.interpolator = AccelerateDecelerateInterpolator()
        positionAnimator.addUpdateListener { animation ->
            val x = animation.animatedValue as Float
            recordBtn?.x = x
            if (setY) {
                recordBtn?.y = initialY
            }
        }
        if (recordButtonGrowingAnimationEnabled) {
            recordBtn?.stopScale()
        }

        positionAnimator.duration = 0
        positionAnimator.start()

        if (difX != 0f) {
            val x = initialX - difX
            slideToCancelLayout?.animate()
                ?.x(x)
                ?.setDuration(0)
                ?.start()
        }
    }

    fun resetSmallMic() {
        smallBlinkingMic?.apply {
            alpha = 1.0f
            scaleX = 1.0f
            scaleY = 1.0f
        }
    }

    fun setOnBasketAnimationEndListener(onBasketAnimationEndListener: VoiceRecordOnBasketAnimationEnd?) {
        this.onBasketAnimationEndListener = onBasketAnimationEndListener
    }

    fun onAnimationEnd() {
        onBasketAnimationEndListener?.onAnimationEnd()
    }

    fun setStartRecorded(startRecorded: Boolean) {
        isStartRecorded = startRecorded
    }
}
