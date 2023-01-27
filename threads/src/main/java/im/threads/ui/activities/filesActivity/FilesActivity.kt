package im.threads.ui.activities.filesActivity

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import im.threads.R
import im.threads.business.models.FileDescription
import im.threads.business.secureDatabase.DatabaseHolder
import im.threads.business.serviceLocator.core.inject
import im.threads.business.utils.Balloon
import im.threads.databinding.EccActivityFilesAndMediaBinding
import im.threads.ui.ChatStyle
import im.threads.ui.activities.BaseActivity
import im.threads.ui.adapters.filesAndMedia.FilesAndMediaAdapter
import im.threads.ui.adapters.filesAndMedia.FilesAndMediaAdapter.OnFileClick
import im.threads.ui.config.Config
import im.threads.ui.utils.ColorsHelper
import im.threads.ui.utils.gone
import im.threads.ui.utils.hideKeyboard
import im.threads.ui.utils.setColorFilter
import im.threads.ui.utils.showKeyboard
import im.threads.ui.utils.visible

/**
 * Показывает список файлов, которые присутствовали в диалоге с оператором с обоих сторон
 */
internal class FilesActivity : BaseActivity(), OnFileClick {
    private val binding: EccActivityFilesAndMediaBinding by lazy {
        EccActivityFilesAndMediaBinding.inflate(layoutInflater)
    }
    private val config: Config by lazy {
        Config.getInstance()
    }
    private val database: DatabaseHolder by inject()
    private val filesViewModel: FilesViewModel by viewModels {
        FilesViewModel.Factory(config.context, database)
    }
    private var filesAndMediaAdapter: FilesAndMediaAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideSystemUI()
        setStatusBarColor()
        setContentView(binding.root)
        initToolbar()
        setOnSearchClickAction()
        setOnSearchTextChanged()
        setActivityStyle(config.getChatStyle())
        subscribeForNewIntents()
        subscribeForDownloadProgress()
        requestFiles()
    }

    override fun onBackPressed() {
        if (binding.searchEditText.visibility == View.VISIBLE) {
            closeSearch()
        } else {
            super.onBackPressed()
        }
    }

    override fun onFileClick(fileDescription: FileDescription?) {
        filesViewModel.onFileClick(fileDescription)
    }

    override fun onDownloadFileClick(fileDescription: FileDescription?) {
        filesViewModel.onDownloadFileClick(fileDescription)
    }

    private fun onFileReceive(descriptions: List<FileDescription?>?) = with(binding) {
        if (descriptions != null && descriptions.isNotEmpty()) {
            searchButton.visibility = View.VISIBLE
            emptyListLayout.visibility = View.GONE
            filesRecycler.visibility = View.VISIBLE
            filesAndMediaAdapter = FilesAndMediaAdapter(
                descriptions,
                this@FilesActivity,
                this@FilesActivity
            )
            filesRecycler.adapter = filesAndMediaAdapter
        }
    }

    @Suppress("DEPRECATION")
    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }

    private fun setStatusBarColor() {
        val statusBarColor = ContextCompat.getColor(baseContext, config.getChatStyle().mediaAndFilesStatusBarColorResId)
        val isStatusBarLight = resources.getBoolean(config.getChatStyle().mediaAndFilesWindowLightStatusBarResId)
        super.setStatusBarColor(isStatusBarLight, statusBarColor)
    }

    private fun initToolbar() = with(binding) {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        val isShadowVisible = resources.getBoolean(config.getChatStyle().isChatTitleShadowVisible)
        toolbarShadow.visibility = if (isShadowVisible) View.VISIBLE else View.INVISIBLE
        if (!isShadowVisible) {
            val noElevation = 0f
            toolbar.elevation = noElevation
        }
        ColorsHelper.setTint(this@FilesActivity, backButton, config.getChatStyle().chatToolbarTextColorResId)
        title.setTextColor(ContextCompat.getColor(this@FilesActivity, config.getChatStyle().chatToolbarTextColorResId))
        initToolbarTextPosition()
        setClickForBackBtn()
    }

    private fun initToolbarTextPosition() = with(binding) {
        val isToolbarTextCentered = Config.getInstance().getChatStyle().isToolbarTextCentered
        val gravity = if (isToolbarTextCentered) Gravity.CENTER else Gravity.CENTER_VERTICAL
        title.gravity = gravity
    }

    private fun setClickForBackBtn() {
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setOnSearchClickAction() = with(binding) {
        searchButton.setOnClickListener {
            if (searchEditText.visibility == View.VISIBLE) {
                closeSearch()
            } else {
                showSearch()
            }
        }
    }

    private fun closeSearch() = with(binding) {
        searchEditText.setText("")
        searchEditText.hideKeyboard()
        searchEditText.gone()
        title.visible()
        filesAndMediaAdapter?.undoClear()
    }

    private fun showSearch() = with(binding) {
        searchEditText.visible()
        searchEditText.requestFocus()
        title.gone()
        filesAndMediaAdapter?.backupAndClear()
        searchEditText.setText("")
        searchEditText.showKeyboard(100)
    }

    private fun setOnSearchTextChanged() = with(binding) {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(text: Editable) {
                search(text.toString())
            }
        })
        searchEditText.setOnEditorActionListener { textView: TextView, actionId: Int, _: KeyEvent? ->
            if (searchEditText.visibility == View.VISIBLE && actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(textView.text.toString())
                return@setOnEditorActionListener true
            } else {
                return@setOnEditorActionListener false
            }
        }
    }

    private fun requestFiles() {
        filesViewModel.getFilesAsync()
    }

    private fun search(searchString: String) = with(binding) {
        filesAndMediaAdapter?.filter(searchString) {
            if (filesAndMediaAdapter?.isNotEmpty() == true) {
                filesRecycler.scrollToPosition(0)
            }
        }
    }

    private fun setActivityStyle(style: ChatStyle) = with(binding) {
        root.setBackgroundColor(
            ContextCompat.getColor(
                baseContext,
                style.mediaAndFilesScreenBackgroundColor
            )
        )
        toolbar.setBackgroundColor(
            ContextCompat.getColor(
                baseContext,
                style.mediaAndFilesToolbarColorResId
            )
        )
        searchButton.setColorFilter(
            ContextCompat.getColor(
                baseContext,
                style.mediaAndFilesToolbarTextColorResId
            ),
            PorterDuff.Mode.SRC_ATOP
        )
        searchEditText.setTextColor(getColorInt(style.mediaAndFilesToolbarTextColorResId))
        toolbar.navigationIcon?.mutate()?.setColorFilter(
            ContextCompat.getColor(baseContext, style.mediaAndFilesToolbarTextColorResId)
        )
        searchEditText.setHintTextColor(getColorInt(style.mediaAndFilesToolbarHintTextColor))
        setUpTextViewStyle(
            emptyListHeader,
            style.emptyMediaAndFilesHeaderText,
            style.emptyMediaAndFilesHeaderTextSize,
            style.emptyMediaAndFilesHeaderTextColor,
            style.emptyMediaAndFilesHeaderFontPath
        )
        setUpTextViewStyle(
            emptyListDescription,
            style.emptyMediaAndFilesDescriptionText,
            style.emptyMediaAndFilesDescriptionTextSize,
            style.emptyMediaAndFilesDescriptionTextColor,
            style.emptyMediaAndFilesDescriptionFontPath
        )
    }

    private fun subscribeForNewIntents() {
        filesViewModel.intentLiveData.observe(this) { startIntent(it) }
    }

    private fun subscribeForDownloadProgress() {
        filesViewModel.filesFlowLiveData.observe(this) { onDownloadProgress(it) }
    }

    private fun setUpTextViewStyle(
        textView: TextView,
        @StringRes textResId: Int,
        @DimenRes sizeResId: Int,
        @ColorRes colorResId: Int,
        fontPath: String?
    ) = with(binding) {
        textView.setText(textResId)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(sizeResId))
        textView.setTextColor(getColorInt(colorResId))
        if (!TextUtils.isEmpty(fontPath)) {
            textView.typeface = Typeface.createFromAsset(assets, fontPath)
        }
    }

    private fun startIntent(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Balloon.show(this, getString(R.string.ecc_file_not_supported))
        }
    }

    private fun onDownloadProgress(filesFlow: FilesFlow) {
        when (filesFlow) {
            is FilesFlow.UpdatedProgress -> {
                filesAndMediaAdapter?.updateProgress(filesFlow.fileDescription)
            }
            is FilesFlow.DownloadError -> {
                filesAndMediaAdapter?.onDownloadError(filesFlow.fileDescription)
            }
            is FilesFlow.FilesReceived -> {
                onFileReceive(filesFlow.files)
            }
        }
    }

    companion object {
        /**
         * Запускает FilesActivity
         * @param activity активити, из которой будет произведен запуск FilesActivity.
         */
        @JvmStatic
        fun startActivity(activity: Activity?) {
            activity?.startActivity(Intent(activity, FilesActivity::class.java))
        }
    }
}
