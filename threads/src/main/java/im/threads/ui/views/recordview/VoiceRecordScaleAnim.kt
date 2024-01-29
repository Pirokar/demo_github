package im.threads.ui.views.recordview

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * Created by Devlomi on 13/12/2017.
 */
internal class VoiceRecordScaleAnim(private val view: View) {
    private var scaleUpTo = 2.0f
    fun setScaleUpTo(scaleUpTo: Float) {
        this.scaleUpTo = scaleUpTo
    }

    fun start() {
        AnimatorSet().apply {
            duration = 150
            interpolator = AccelerateDecelerateInterpolator()
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleY", scaleUpTo),
                ObjectAnimator.ofFloat(view, "scaleX", scaleUpTo)
            )
            start()
        }
    }

    fun stop() {
        AnimatorSet().apply {
            duration = 150
            interpolator = AccelerateDecelerateInterpolator()
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleY", 1.0f),
                ObjectAnimator.ofFloat(view, "scaleX", 1.0f)
            )
            start()
        }
    }
}
