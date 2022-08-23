package im.threads.internal.activities.filesActivity

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
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import im.threads.ChatStyle
import im.threads.R
import im.threads.business.models.FileDescription
import im.threads.business.secureDatabase.DatabaseHolder
import im.threads.databinding.ActivityFilesAndMediaBinding
import im.threads.internal.Config
import im.threads.internal.activities.BaseActivity
import im.threads.internal.adapters.filesAndMedia.FilesAndMediaAdapter
import im.threads.internal.adapters.filesAndMedia.FilesAndMediaAdapter.OnFileClick
import im.threads.internal.utils.Keyboard
import im.threads.internal.utils.setColorFilter

/**
 * Показывает список файлов, которые присутствовали в диалоге с оператором с обоих сторон
 */
internal class FilesActivity : BaseActivity(), OnFileClick {
    private val binding: ActivityFilesAndMediaBinding by lazy {
        ActivityFilesAndMediaBinding.inflate(layoutInflater)
    }
    private val filesViewModel: FilesViewModel by viewModels {
        FilesViewModel.Factory(Config.instance.context, DatabaseHolder.getInstance())
    }
    private val style = Config.instance.chatStyle
    private var filesAndMediaAdapter: FilesAndMediaAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideSystemUI()
        setStatusBarColor()
        setContentView(binding.root)
        initToolbar()
        setOnSearchClickAction()
        setOnSearchTextChanged()
        setActivityStyle(Config.instance.chatStyle)
        subscribeForNewIntents()
        subscribeForDownloadProgress()
        requestFiles()
    }

    override fun onBackPressed() {
        if (binding.searchEditText.visibility == View.VISIBLE) {
            binding.searchEditText.setText("")
            binding.searchEditText.visibility = View.GONE
            binding.toolbar.title = getString(R.string.threads_files_and_media)
            filesAndMediaAdapter?.undoClear()
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
        val statusBarColor = ContextCompat.getColor(baseContext, style.mediaAndFilesStatusBarColorResId)
        val isStatusBarLight = resources.getBoolean(style.mediaAndFilesWindowLightStatusBarResId)

        super.setStatusBarColor(isStatusBarLight, statusBarColor)
    }

    private fun initToolbar() = with(binding) {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        val isShadowVisible = resources.getBoolean(style.isChatTitleShadowVisible)
        toolbarShadow.visibility = if (isShadowVisible) View.VISIBLE else View.INVISIBLE
        if (!isShadowVisible) {
            val noElevation = 0f
            toolbar.elevation = noElevation
        }
    }

    private fun setOnSearchClickAction() = with(binding) {
        searchButton.setOnClickListener {
            if (searchEditText.visibility == View.VISIBLE) {
                searchEditText.setText("")
                searchEditText.visibility = View.GONE
                toolbar.title = getString(R.string.threads_files_and_media)
                filesAndMediaAdapter?.undoClear()
            } else {
                searchEditText.visibility = View.VISIBLE
                searchEditText.requestFocus()
                toolbar.title = ""
                filesAndMediaAdapter?.backupAndClear()
                searchEditText.setText("")
                Keyboard.show(baseContext, searchEditText, 100)
            }
        }
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

    private fun search(searchString: String) {
        filesAndMediaAdapter?.filter(searchString)
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
            Toast.makeText(
                baseContext,
                getString(R.string.threads_file_not_supported),
                Toast.LENGTH_SHORT
            ).show()
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
