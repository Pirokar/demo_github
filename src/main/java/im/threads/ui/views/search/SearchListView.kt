package im.threads.ui.views.search

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import im.threads.business.config.BaseConfig
import im.threads.business.extensions.plus
import im.threads.business.logger.LoggerEdna
import im.threads.business.rest.models.SearchResponse
import im.threads.business.rest.queries.BackendApi
import im.threads.databinding.EccViewSearchListBinding
import im.threads.ui.adapters.search.SearchListViewAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class SearchListView : FrameLayout {
    private lateinit var binding: EccViewSearchListBinding
    private var searchChannel: MutableStateFlow<String?>? = null
    private var loadingChannel: MutableStateFlow<Boolean>? = null
    private var searchChannelCoroutineScope: CoroutineScope? = null
    private var searchListViewAdapter: SearchListViewAdapter? = null
    private var lastSearchResults: SearchResponse? = null
    private var lastSearchString: String? = null

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
        binding = EccViewSearchListBinding.inflate(LayoutInflater.from(context), this, true)
        searchListViewAdapter = SearchListViewAdapter { messageUuid ->
            // TODO: open message in chat
        }
        binding.searchListView.adapter = searchListViewAdapter
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
        this.searchChannel = searchChannel
        this.loadingChannel = loadingChannel
        subscribeForSearchChannel()
    }

    private fun subscribeForSearchChannel() {
        searchChannelCoroutineScope?.cancel()
        searchChannelCoroutineScope = CoroutineScope(Dispatchers.IO)
        searchChannelCoroutineScope?.launch {
            searchChannel?.collect { draftSearchString ->
                val searchString = draftSearchString?.trim()
                val isSubscriptionAlive = isActive && isAttachedToWindow
                if (isSubscriptionAlive && !searchString.isNullOrEmpty()) {
                    loadingChannel?.value = true
                    val searchResultResponse = try {
                        BackendApi.get().search(
                            BaseConfig.getInstance().transport.getToken(),
                            searchString
                        )?.execute()
                    } catch (exc: Exception) {
                        loadingChannel?.value = false
                        LoggerEdna.error("Error when search", exc)
                        null
                    }
                    val searchResults = if (searchResultResponse?.isSuccessful == true) {
                        searchResultResponse.body()
                    } else {
                        null
                    }
                    lastSearchResults = if (!lastSearchString.isNullOrBlank() && searchString.contains(lastSearchString!!)) {
                        lastSearchResults plus searchResults
                    } else {
                        searchResults
                    }
                    lastSearchString = searchString
                    withContext(Dispatchers.Main) {
                        searchListViewAdapter?.updateData(lastSearchResults?.content)
                        loadingChannel?.value = false
                    }
                } else if (isSubscriptionAlive) {
                    // TODO: show "no results" window
                }
            }
        }
    }
}
