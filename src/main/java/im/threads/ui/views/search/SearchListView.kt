package im.threads.ui.views.search

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import im.threads.business.config.BaseConfig
import im.threads.business.extensions.plus
import im.threads.business.logger.LoggerEdna
import im.threads.business.rest.models.SearchResponse
import im.threads.business.rest.queries.BackendApi
import im.threads.databinding.EccViewSearchListBinding
import im.threads.ui.adapters.search.SearchListViewAdapter
import im.threads.ui.config.Config
import im.threads.ui.utils.gone
import im.threads.ui.utils.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.ceil

internal class SearchListView : FrameLayout {
    private lateinit var binding: EccViewSearchListBinding
    private var searchChannel: MutableStateFlow<String?>? = null
    private var loadingChannel: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var searchChannelCoroutineScope: CoroutineScope? = null
    private var searchListViewAdapter: SearchListViewAdapter? = null
    private var lastSearchResults: SearchResponse? = null
    private var lastSearchString: String? = null

    private var onScrollListener = object : OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            onListScrolled()
        }
    }

    private var onListItemClickCallback: ((uuid: String, date: String?) -> Unit)? = null

    private val invisibleMessagesCount = 3

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
        searchListViewAdapter = SearchListViewAdapter { messageUuid, date ->
            messageUuid?.let { onListItemClickCallback?.invoke(it, date) }
        }
        binding.searchListView.adapter = searchListViewAdapter
        initNoResultsView()
        subscribeForListScroll()
    }

    /**
     * Устанавливает коллбэк при нажатии на элемент из списка
     * @param listener слушатель события нажатия на элемент из списка. Передает uuid выбранного элемента
     */
    fun setOnClickListener(listener: (uuid: String, date: String?) -> Unit) {
        onListItemClickCallback = listener
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
        if (loadingChannel != null) {
            this.loadingChannel = loadingChannel
        }
        subscribeForSearchChannel()
    }

    private fun subscribeForSearchChannel() {
        searchChannelCoroutineScope?.cancel()
        searchChannelCoroutineScope = CoroutineScope(Dispatchers.Main)
        searchChannelCoroutineScope?.launch {
            searchChannel?.collect { draftSearchString ->
                val searchString = draftSearchString?.trim()
                val isSubscriptionAlive = isActive && isAttachedToWindow
                if (isSubscriptionAlive && !searchString.isNullOrEmpty() && searchString.length > 2) {
                    loadSearchResults(searchString, 1)
                } else if (isSubscriptionAlive) {
                    lastSearchResults = null
                    lastSearchString = null
                }
            }
        }
    }

    @Synchronized
    private fun setLoadingChannelValue(value: Boolean) {
        loadingChannel.value = value
    }

    private fun loadSearchResults(searchString: String, page: Int) {
        if (searchString.isBlank() || loadingChannel.value) return

        setLoadingChannelValue(true)

        val query = BackendApi.get().search(
            BaseConfig.getInstance().transport.getToken(),
            searchString,
            page
        )

        if (query == null) {
            setLoadingChannelValue(false)
            return
        }

        query.enqueue(object : Callback<SearchResponse?> {
            override fun onResponse(
                call: Call<SearchResponse?>,
                response: Response<SearchResponse?>
            ) {
                if (response.isSuccessful) {
                    val searchResults = response.body()
                    lastSearchResults =
                        if (!lastSearchString.isNullOrBlank() && searchString == lastSearchString!!) {
                            lastSearchResults plus searchResults
                        } else {
                            searchResults
                        }
                    lastSearchString = searchString
                    searchListViewAdapter?.updateData(lastSearchResults?.content)
                    setLoadingChannelValue(false)
                    checkListSize()
                }
            }

            override fun onFailure(call: Call<SearchResponse?>, t: Throwable) {
                setLoadingChannelValue(false)
                LoggerEdna.error("Error when search", t)
            }
        })
    }

    private fun checkListSize() {
        if (searchListViewAdapter?.itemCount == 0) {
            binding.noResultsView.visible()
        } else {
            binding.noResultsView.gone()
        }
    }

    private fun isLastVisibleItemPosition(): Boolean {
        return if (isAttachedToWindow) {
            val lastVisiblePosition =
                (binding.searchListView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            searchListViewAdapter?.let { adapter ->
                adapter.itemCount - 1 - lastVisiblePosition < invisibleMessagesCount
            } ?: false
        } else {
            false
        }
    }

    private fun initNoResultsView() = with(binding) {
        val chatStyle = Config.getInstance().chatStyle
        val context = noResultsImage.context

        noResultsImage.setImageDrawable(
            ContextCompat.getDrawable(noResultsImage.context, chatStyle.searchResultNoItemsImageDrawable)
        )
        noResultsTextView.text = context.getString(chatStyle.searchResultNoItemsText)
        noResultsTextView.setTextColor(ContextCompat.getColor(context, chatStyle.searchResultNoItemsTextColor))
    }

    private fun subscribeForListScroll() {
        binding.searchListView.removeOnScrollListener(onScrollListener)
        binding.searchListView.addOnScrollListener(onScrollListener)
    }

    private fun onListScrolled() {
        val total = lastSearchResults?.total ?: 0
        val countPerPage = 20
        val pages = lastSearchResults?.pages ?: 0
        val currentPage = ceil((lastSearchResults?.content?.size ?: 0) / countPerPage.toDouble())

        if (isLastVisibleItemPosition() && pages - currentPage > 0) {
            loadSearchResults(lastSearchString ?: "", currentPage.toInt() + 1)
        }
    }
}
