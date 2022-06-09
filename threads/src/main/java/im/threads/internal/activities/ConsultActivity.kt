package im.threads.internal.activities

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.squareup.picasso.Picasso
import im.threads.ChatStyle
import im.threads.R
import im.threads.databinding.ActivityConsultPageBinding
import im.threads.internal.Config
import im.threads.internal.utils.FileUtils.convertRelativeUrlToAbsolute
import im.threads.internal.utils.ThreadsLogger
import im.threads.view.ChatFragment

open class ConsultActivity : BaseActivity() {
    private val binding: ActivityConsultPageBinding by lazy {
        ActivityConsultPageBinding.inflate(layoutInflater)
    }
    private val style: ChatStyle by lazy {
        Config.instance.chatStyle
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideSystemUI()
        setStatusBarColor()
        setContentView(binding.root)
        setConsultImageView()
        setConsultTitle()
        setConsultMoto()
        setupToolbar()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val searchMenuItem = menu.getItem(0)
        val searchMenuSpannable = SpannableString(searchMenuItem.title)
        searchMenuSpannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, style!!.menuItemTextColorResId)),
            0,
            searchMenuSpannable.length,
            0
        )
        searchMenuItem.title = searchMenuSpannable
        val filesAndMedia = menu.getItem(1)
        val filesAndMediaSpannable = SpannableString(filesAndMedia.title)
        filesAndMediaSpannable.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    this,
                    style.menuItemTextColorResId
                )
            ),
            0, filesAndMediaSpannable.length, 0
        )
        filesAndMedia.title = filesAndMediaSpannable
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.threads_menu_main, menu)
        menu.findItem(R.id.files_and_media).isVisible =
            Config.instance.filesAndMediaMenuItemEnabled

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.files_and_media) {
            startActivity(FilesActivity.getStartIntent(this))
            return true
        }
        if (item.itemId == R.id.search) {
            sendBroadcast(Intent(ChatFragment.ACTION_SEARCH_CHAT_FILES))
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    @Suppress("DEPRECATION")
    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        } else {
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
        }
    }

    private fun setStatusBarColor() {
        window.statusBarColor = ContextCompat.getColor(baseContext, R.color.threads_black_transparent)
        val isStatusBarLight = resources.getBoolean(style.windowLightStatusBarResId)
        val isAndroidMOrHigher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

        if (isStatusBarLight && isAndroidMOrHigher) {
            val wic = WindowInsetsControllerCompat(window, window.decorView)
            wic.isAppearanceLightStatusBars = isStatusBarLight
            ViewCompat.getWindowInsetsController(window.decorView)?.apply {
                // Light text == dark status bar
                isAppearanceLightStatusBars = !isStatusBarLight
            }
        }
    }

    private fun setConsultImageView() = with(binding) {
        consultImage.background = AppCompatResources.getDrawable(
            this@ConsultActivity,
            style.defaultOperatorAvatar
        )

        var imagePath = intent.getStringExtra(imageUrlKey)
        if (!imagePath.isNullOrEmpty()) {
            imagePath = convertRelativeUrlToAbsolute(imagePath)
            Picasso.get().load(imagePath).into(consultImage)
        }
    }

    private fun setConsultTitle() = with(binding) {
        val consultTitleString = intent.getStringExtra(titleKey)
        if (null != consultTitleString && consultTitleString != "null") {
            consultTitle.text = consultTitleString
        }
    }

    private fun setConsultMoto() = with(binding) {
        val consultMotoString = intent.getStringExtra(motoKey)
        if (null != consultMotoString && consultMotoString != "null") {
            consultMoto.text = consultMotoString
        }
    }

    private fun setupToolbar() = with(binding) {
        toolbar.title = ""
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.showOverflowMenu()
        toolbar.overflowIcon?.let { overflowDrawable ->
            overflowDrawable.colorFilter = PorterDuffColorFilter(
                ContextCompat.getColor(baseContext, android.R.color.white),
                PorterDuff.Mode.SRC_ATOP
            )
        } ?: run {
            ThreadsLogger.e(TAG, "onCreate", NullPointerException("overflowDrawable is null!"))
        }

        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, resources.getDimension(R.dimen.margin_big).toInt(), 0, 0)
        }
        toolbar.layoutParams = layoutParams
    }

    companion object {
        private const val TAG = "ConsultActivity "
        const val imageUrlKey = "imagePath"
        const val titleKey = "title"
        const val motoKey = "moto"

        @JvmStatic
        fun startActivity(
            activity: Activity?,
            avatarPath: String?,
            name: String?,
            status: String?
        ) {
            val intent = Intent(activity, ConsultActivity::class.java).apply {
                putExtra(imageUrlKey, avatarPath)
                putExtra(titleKey, name)
                putExtra(motoKey, status)
            }

            activity?.startActivity(intent)
        }

        @JvmStatic
        fun startActivity(activity: Activity?) {
            activity?.startActivity(Intent(activity, ConsultActivity::class.java))
        }
    }
}
