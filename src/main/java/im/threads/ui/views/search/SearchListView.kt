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
     * Устанавливает канал, в котором считывается текущий ввод в поле поиска.
     * Передайте данный канал также в [SearchBarView].
     * @param channel канал для проброса значения поля ввода
     */
    fun setSearchChannel(channel: MutableStateFlow<String?>?) {
        // TODO: subscribe for channel
    }
}
