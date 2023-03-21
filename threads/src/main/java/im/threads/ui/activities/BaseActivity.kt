package im.threads.ui.activities

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.MetricAffectingSpan
import android.text.style.TypefaceSpan
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsControllerCompat
import im.threads.BuildConfig
import im.threads.R
import im.threads.business.imageLoading.ImageLoader
import im.threads.business.useractivity.UserActivityTimeProvider.getLastUserActivityTimeCounter
import im.threads.ui.config.Config
import im.threads.ui.utils.ScreenSizeGetter
import im.threads.ui.utils.TypefaceSpanEdna
import im.threads.ui.utils.typefaceSpanCompatV28

/** Родитель для всех Activity библиотеки */
abstract class BaseActivity : AppCompatActivity() {

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            try {
                Config.getInstance()
            } catch (ex: NullPointerException) {
                Toast.makeText(
                    this,
                    "Config instance is not initialized. Called from business logic?",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        calculateSizeOfScreen()
    }

    override fun onStart() {
        super.onStart()
        invalidateOptionsMenu()
    }

    override fun onStop() {
        ImageLoader.clearLoader()
        super.onStop()
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

    protected fun setTitle(text: String, titleTextView: TextView? = null) {
        val style = Config.getInstance().getChatStyle()
        val textColor = ContextCompat.getColor(this, style.chatToolbarTextColorResId)
        val fontSize = resources.getDimensionPixelSize(R.dimen.ecc_text_big)
        var typeface: Typeface? = null
        style.defaultFontRegular?.let { typefaceUri ->
            Typeface.createFromAsset(assets, typefaceUri)?.let {
                typeface = Typeface.create(it, Typeface.NORMAL)
            }
        }
        supportActionBar?.apply {
            val titleText = SpannableString(text)
            applyToolbarTextStyle(textColor, fontSize, typeface, titleText)
            if (titleTextView != null) {
                titleTextView.text = titleText
            } else {
                title = titleText
            }
        }
    }

    protected fun applyToolbarTextStyle(
        textColor: Int,
        fontSize: Int,
        typeface: Typeface?,
        titleText: SpannableString
    ) {
        val length = titleText.length
        titleText.setSpan(
            ForegroundColorSpan(textColor),
            0,
            length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        titleText.setSpan(
            AbsoluteSizeSpan(fontSize, false),
            0,
            length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        typeface?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                titleText.setSpan(
                    TypefaceSpan(it),
                    0,
                    length,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            } else {
                titleText.setSpan(
                    it.getTypefaceSpan(),
                    0,
                    length,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
        }
    }

    private fun Typeface.getTypefaceSpan(): MetricAffectingSpan {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            typefaceSpanCompatV28(this)
        } else {
            TypefaceSpanEdna(this)
        }
    }

    private fun calculateSizeOfScreen() {
        val screenSizeGetter = ScreenSizeGetter()
        Config.getInstance().screenSize = screenSizeGetter.getScreenSize(this)
    }
}
