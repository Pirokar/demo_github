package im.threads.internal.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import im.threads.R
import im.threads.business.imageLoading.loadImage
import im.threads.business.utils.FileUtils.convertRelativeUrlToAbsolute
import im.threads.databinding.ActivityConsultPageBinding
import im.threads.internal.activities.filesActivity.FilesActivity
import im.threads.internal.utils.setColorFilter
import im.threads.ui.config.Config
import im.threads.view.ChatFragment

internal open class ConsultActivity : BaseActivity() {
    private val binding: ActivityConsultPageBinding by lazy {
        ActivityConsultPageBinding.inflate(layoutInflater)
    }
    private val config: Config by lazy {
        Config.getInstance()
    }
    private val isFilesAndMediaEnabled: Boolean
        get() {
            return try {
                Config.instance.filesAndMediaMenuItemEnabled
            } catch (exc: NullPointerException) {
                false
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideSystemUI()
        setStatusBarColor()
        setContentView(binding.root)
        setConsultAvatar()
        setConsultInfo()
        setupToolbar()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val searchMenuItem = menu.getItem(0)
        val searchMenuSpannable = SpannableString(searchMenuItem.title)
        searchMenuSpannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, config.getChatStyle().menuItemTextColorResId)),
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
                    config.getChatStyle().menuItemTextColorResId
                )
            ),
            0,
            filesAndMediaSpannable.length,
            0
        )
        filesAndMedia.title = filesAndMediaSpannable
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.threads_menu_main, menu)
        menu.findItem(R.id.files_and_media).isVisible = isFilesAndMediaEnabled

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.files_and_media -> {
                FilesActivity.startActivity(this)
                true
            }
            R.id.search -> {
                sendBroadcast(Intent(ChatFragment.ACTION_SEARCH_CHAT_FILES))
                finish()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
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
        val statusBarColor = ContextCompat.getColor(baseContext, R.color.threads_black_transparent)
        val isStatusBarLight = resources.getBoolean(config.getChatStyle().windowLightStatusBarResId)
        super.setStatusBarColor(isStatusBarLight, statusBarColor)
    }

    private fun setConsultAvatar() = with(binding) {
        consultImage.background = AppCompatResources.getDrawable(
            this@ConsultActivity,
            config.getChatStyle().defaultOperatorAvatar
        )

        var imagePath = intent.getStringExtra(imageUrlKey)
        if (!imagePath.isNullOrEmpty()) {
            imagePath = convertRelativeUrlToAbsolute(imagePath)
            consultImage.loadImage(imagePath)
            consultImage.tag = imagePath
        }
    }

    private fun setConsultInfo() = with(binding) {
        setTextForConsultInfo(titleKey, consultTitle)
        setTextForConsultInfo(statusKey, consultStatus)
    }

    private fun setupToolbar() = with(binding) {
        toolbar.title = ""
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.showOverflowMenu()
        toolbar.overflowIcon?.setColorFilter(ContextCompat.getColor(baseContext, android.R.color.white))

        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, resources.getDimension(R.dimen.margin_big).toInt(), 0, 0)
        }
        toolbar.layoutParams = layoutParams
    }

    private fun setTextForConsultInfo(intentKey: String, textView: TextView) {
        val text = intent.getStringExtra(intentKey)
        if (null != text && text != "null") {
            textView.text = text
            textView.isVisible = true
        } else {
            textView.isVisible = false
        }
    }

    companion object {
        const val imageUrlKey = "imagePath"
        const val titleKey = "title"
        const val statusKey = "status"

        /**
         * Запускает текущую активити.
         *
         * @param activity активити, из которой будет произведен запуск ConsultActivity.
         * @param avatarPath путь к аватару
         * @param name имя оператора
         * @param status статус оператора.
         */
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
                putExtra(statusKey, status)
            }

            activity?.startActivity(intent)
        }

        /**
         * Запускает текущую активити.
         *
         * @param activity активити, из которой будет произведен запуск ConsultActivity.
         */
        @JvmStatic
        fun startActivity(activity: Activity?) {
            activity?.startActivity(Intent(activity, ConsultActivity::class.java))
        }
    }
}
