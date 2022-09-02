package im.threads.ui.activities

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsControllerCompat
import im.threads.business.useractivity.UserActivityTimeProvider.getLastUserActivityTimeCounter

/**
 * Родитель для всех Activity библиотеки
 */
abstract class BaseActivity : AppCompatActivity() {

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        invalidateOptionsMenu()
    }

    @ColorInt
    protected fun getColorInt(@ColorRes color: Int): Int {
        return ContextCompat.getColor(this, color)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val timeCounter = getLastUserActivityTimeCounter()
        if (MotionEvent.ACTION_DOWN == ev.action) {
            timeCounter.updateLastUserActivityTime()
        }
        return super.dispatchTouchEvent(ev)
    }

    protected fun setStatusBarColor(isStatusBarLight: Boolean, color: Int) {
        window.statusBarColor = color

        val isAndroidMOrHigher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        if (isStatusBarLight && isAndroidMOrHigher) {
            val wic = WindowInsetsControllerCompat(window, window.decorView)
            wic.isAppearanceLightStatusBars = isStatusBarLight
            ViewCompat.getWindowInsetsController(window.decorView)?.apply {
                isAppearanceLightStatusBars = !isStatusBarLight
            }
        }
    }
}
