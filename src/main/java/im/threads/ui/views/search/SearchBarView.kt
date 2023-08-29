package im.threads.ui.views.search

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.coroutineScope
import im.threads.databinding.EccViewSearchbarBinding
import im.threads.ui.config.Config
import im.threads.ui.extensions.lifecycle
import im.threads.ui.utils.ColorsHelper
import im.threads.ui.utils.hideKeyboard
import im.threads.ui.utils.invisible
import im.threads.ui.utils.isNotVisible
import im.threads.ui.utils.showKeyboard
import im.threads.ui.utils.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Search bar. Включает поле ввода, прогресс бар и кнопку очистки поля ввода
 */
internal class SearchBarView : ConstraintLayout {
    private lateinit var binding: EccViewSearchbarBinding
    private val coroutineScope: CoroutineScope? by lazy { lifecycle()?.coroutineScope }
    private val chatStyle = Config.getInstance().chatStyle
    private var searchChannel: MutableStateFlow<String?>? = null
    private var loadingChannel: MutableStateFlow<Boolean>? = null
    private var debouncePeriod: Long = 500
    private var textWatcherJob: Job? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        binding = EccViewSearchbarBinding.inflate(LayoutInflater.from(context), this, true)
        initSearchListener()
        initClearSearchBtn()
        initLoader()
    }

    private fun initSearchListener() = with(binding) {
        searchInput.addTextChangedListener { editable ->
            val newText = editable?.toString()
            if (!newText.isNullOrEmpty() && searchProgressBar.isNotVisible() && chatStyle.isClearSearchBtnVisible) {
                searchClearButton.visible()
            } else {
                searchClearButton.invisible()
            }
            textWatcherJob?.cancel()
            textWatcherJob = coroutineScope?.launch {
                newText?.let {
                    delay(debouncePeriod)
                    searchChannel?.value = it
                }
            }
        }
    }

    private fun initClearSearchBtn() = with(binding) {
        searchClearButton.setImageResource(chatStyle.searchClearIconDrawable)
        ColorsHelper.setTint(this@SearchBarView.context, searchClearButton, chatStyle.searchClearIconTintColor)
        searchClearButton.setOnClickListener { clearSearch() }
    }

    private fun initLoader() = with(binding) {
        chatStyle.searchLoaderDrawable?.let {
            searchProgressBar.indeterminateDrawable = ContextCompat.getDrawable(this@SearchBarView.context, it)
        }
    }

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        return binding.searchInput.requestFocus(direction, previouslyFocusedRect)
    }

    /**
     * Устанавливает каналы, в которые пишется текущий ввод в поле поиска,
     * а также канал для показа прогресса загрузки результатов.
     * Передайте данные каналы также в [SearchListView].
     * @param searchChannel канал для проброса значения поля ввода
     * @param loadingChannel канал для показа лоадера
     */
    fun setSearchChannels(
        searchChannel: MutableStateFlow<String?>?,
        loadingChannel: MutableStateFlow<Boolean>?
    ) {
        this.searchChannel = searchChannel
        this.loadingChannel = loadingChannel

        subscribeForLoading()
    }

    /**
     * Очищает поле для ввода
     */
    fun clearSearch() {
        binding.searchInput.setText("")
    }

    /**
     * Показывает клавиатуру спустя заданное время
     * @param delay время задержки перед показом клавиатуры
     */
    fun showKeyboard(delay: Long) {
        binding.searchInput.showKeyboard(delay)
    }

    /**
     * Скрывает клавиатуру спустя заданное время
     * @param delay время задержки перед скрытием клавиатуры
     */
    fun hideKeyboard(delay: Long) {
        binding.searchInput.hideKeyboard(delay)
    }

    private fun subscribeForLoading() {
        coroutineScope?.launch {
            loadingChannel?.collect { showLoader ->
                if (showLoader) {
                    binding.searchProgressBar.visible()
                } else {
                    binding.searchProgressBar.invisible()
                }
            }
        }
    }
}
