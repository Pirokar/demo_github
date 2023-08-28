package im.threads.ui.views.search

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.coroutineScope
import im.threads.business.chatUpdates.ChatUpdateProcessor
import im.threads.business.serviceLocator.core.inject
import im.threads.databinding.EccViewSearchbarBinding
import im.threads.ui.extensions.lifecycle
import im.threads.ui.utils.DebouncingTextWatcher
import im.threads.ui.utils.hideKeyboard
import im.threads.ui.utils.showKeyboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Search bar. Включает поле ввода, прогресс бар и кнопку очистки поля ввода
 */
internal class SearchBarView : ConstraintLayout {
    private lateinit var binding: EccViewSearchbarBinding
    private val chatUpdateProcessor: ChatUpdateProcessor by inject()
    private val coroutineScope: CoroutineScope? by lazy { lifecycle()?.coroutineScope }
    private var searchQueryChannel: MutableStateFlow<String?>? = null
    private var searchJob: Job? = null

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
    }

    private fun initSearchListener() {
        binding.searchInput.addTextChangedListener(
            DebouncingTextWatcher(lifecycle()) {
                coroutineScope?.launch { searchQueryChannel?.value = it ?: "" }
            }
        )
    }

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        return binding.searchInput.requestFocus(direction, previouslyFocusedRect)
    }

    /**
     * Устанавливает канал, в который пишется текущий ввод в поле поиска.
     * Передайте данный канал также в [SearchListView].
     * @param channel канал для проброса значения поля ввода
     */
    fun setSearchChannel(channel: MutableStateFlow<String?>?) {
        searchQueryChannel = channel
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
}
