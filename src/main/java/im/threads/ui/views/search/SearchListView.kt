package im.threads.ui.views.search

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.flow.MutableStateFlow

class SearchListView : ConstraintLayout {
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
        // TODO: init view
    }

    /**
     * Устанавливает каналы, в которых считываются текущий ввод в поле поиска
     * и отправляется статус загрузки результатов.
     * Передайте данные каналы также в [SearchBarView].
     * @param searchChannel канал для проброса значения поля ввода
     * @param loadingChannel канал для показа лоадера
     */
    fun setSearchChannels(
        searchChannel: MutableStateFlow<String?>?,
        loadingChannel: MutableStateFlow<Boolean>?
    ) {
        // TODO: subscribe for channel
    }
}
