package edna.chatcenter.demo

import android.os.Build
import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner

class TestNoAnimRunner : AndroidJUnitRunner() {
    private val animationDisabler = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        AnimationDisabler()
    } else {
        null
    }

    override fun onStart() {
        animationDisabler?.disableAnimations()
        super.onStart()
    }

    override fun finish(resultCode: Int, results: Bundle?) {
        animationDisabler?.enableAnimations()
        super.finish(resultCode, results)
    }
}
