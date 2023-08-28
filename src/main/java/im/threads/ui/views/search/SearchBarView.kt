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
import kotlinx.coroutines.launch

internal class SearchBarView : ConstraintLayout {
    private lateinit var binding: EccViewSearchbarBinding
    private val chatUpdateProcessor: ChatUpdateProcessor by inject()
    private val coroutineScope: CoroutineScope? by lazy { lifecycle()?.coroutineScope }
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
                coroutineScope?.launch { chatUpdateProcessor.searchQueryChannel.value = it ?: "" }
            }
        )
    }

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        return binding.searchInput.requestFocus(direction, previouslyFocusedRect)
    }

    fun clearSearch() {
        binding.searchInput.setText("")
    }

    fun showKeyboard(delay: Long) {
        binding.searchInput.showKeyboard(delay)
    }

    fun hideKeyboard(delay: Long) {
        binding.searchInput.hideKeyboard(delay)
    }
}
