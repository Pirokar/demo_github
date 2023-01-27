package im.threads.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import im.threads.R
import im.threads.business.imageLoading.loadImage
import im.threads.business.models.ConsultInfo
import im.threads.business.utils.FileUtils.convertRelativeUrlToAbsolute
import im.threads.databinding.EccActivityConsultPageBinding
import im.threads.ui.config.Config
import im.threads.ui.utils.setColorFilter

internal open class ConsultActivity : BaseActivity() {
    private val binding: EccActivityConsultPageBinding by lazy {
        EccActivityConsultPageBinding.inflate(layoutInflater)
    }
    private val config: Config by lazy { Config.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideSystemUI()
        setStatusBarColor()
        setContentView(binding.root)
        val consultInfo = intent.getParcelableExtra<ConsultInfo>(consultInfoKey)
        consultInfo?.let {
            setConsultAvatar(it)
            setConsultInfo(it)
        }
        setupToolbar()
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
        val statusBarColor = ContextCompat.getColor(baseContext, R.color.ecc_black_transparent)
        val isStatusBarLight = resources.getBoolean(config.getChatStyle().windowLightStatusBarResId)
        super.setStatusBarColor(isStatusBarLight, statusBarColor)
    }

    private fun setConsultAvatar(consultInfo: ConsultInfo) = with(binding) {
        consultImage.background = AppCompatResources.getDrawable(
            this@ConsultActivity,
            config.getChatStyle().defaultOperatorAvatar
        )

        var imagePath = consultInfo.photoUrl
        if (!imagePath.isNullOrEmpty()) {
            imagePath = convertRelativeUrlToAbsolute(imagePath)
            consultImage.loadImage(imagePath)
            consultImage.tag = imagePath
        }
    }

    private fun setConsultInfo(consultInfo: ConsultInfo) = with(binding) {
        setTextForConsultInfo(consultInfo.name, consultTitle)
        setTextForConsultInfo(consultInfo.status, consultStatus)
    }

    private fun setupToolbar() = with(binding) {
        toolbar.title = ""
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.showOverflowMenu()
        toolbar.overflowIcon?.setColorFilter(
            ContextCompat.getColor(
                baseContext,
                config.getChatStyle().chatToolbarTextColorResId
            )
        )

        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, resources.getDimension(R.dimen.ecc_margin_big).toInt(), 0, 0)
        }
        toolbar.layoutParams = layoutParams
    }

    private fun setTextForConsultInfo(text: String?, textView: TextView) {
        if (!text.isNullOrEmpty()) {
            textView.text = text
            textView.isVisible = true
        } else {
            textView.isVisible = false
        }
    }

    companion object {
        const val consultInfoKey = "consultInfoKey"

        /**
         * Запускает текущую активити.
         *
         * @param activity активити, из которой будет произведен запуск ConsultActivity.
         * @param consultInfo контейнер с данными оператора.
         */
        @JvmStatic
        fun startActivity(
            activity: Activity?,
            consultInfo: ConsultInfo
        ) {
            val intent = Intent(activity, ConsultActivity::class.java).apply {
                putExtra(consultInfoKey, consultInfo)
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
