package im.threads.ui.utils

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.coroutineScope
import im.threads.ui.extensions.lifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * [TextWatcher], предоставляющий результаты ввода спустя определенный период и отменяет
 * предыдущие результаты, если пришла новая строка
 */
internal class DebouncingTextWatcher(
    view: WeakReference<View>,
    private val onDebouncingQueryTextChange: (String?) -> Unit
) : TextWatcher {
    private var debouncePeriod: Long = 500
    private val coroutineScope by lazy { view.get()?.lifecycle()?.coroutineScope }
    private var textWatcherJob: Job? = null

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun afterTextChanged(editable: Editable?) {
        val newText = editable?.toString()
        textWatcherJob?.cancel()
        textWatcherJob = coroutineScope?.launch {
            newText?.let {
                delay(debouncePeriod)
                onDebouncingQueryTextChange(newText)
            }
        }
    }
}
