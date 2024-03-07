package im.threads.business.transport

import androidx.annotation.WorkerThread
import com.google.gson.Gson
import im.threads.business.config.BaseConfig
import im.threads.business.logger.LoggerEdna.error
import im.threads.business.models.ChatItem
import im.threads.business.models.MessageFromHistory
import im.threads.business.rest.models.HistoryResponse
import im.threads.business.rest.queries.BackendApi.Companion.get
import im.threads.business.rest.queries.ThreadsApi
import im.threads.business.utils.AppInfo
import im.threads.business.utils.DateHelper
import im.threads.business.utils.DemoModeProvider
import im.threads.ui.config.Config
import retrofit2.Response
import java.io.IOException

class HistoryLoader(private val demoModeProvider: DemoModeProvider, private val appInfo: AppInfo) {
    private var lastLoadedTimestamp: Long? = null

    /**
     * метод обертка для запроса истории сообщений
     * выполняется синхронно
     *
     * @param anchorTimestamp timestamp сообщения от которого грузить, null если с начала
     * @param count           количество сообщений для загрузки
     */
    @WorkerThread
    @Throws(Exception::class)
    fun getHistorySync(anchorTimestamp: Long?, count: Int?, isAfterAnchor: Boolean = false): HistoryResponse? {
        if (demoModeProvider.isDemoModeEnabled()) {
            return Gson().fromJson(demoModeProvider.getHistoryMock(), HistoryResponse::class.java)
        }

        val itemsCount = count ?: BaseConfig.getInstance().historyLoadingCount
        val token = BaseConfig.getInstance().transport.getToken()

        if (token.isNotEmpty()) {
            val threadsApi = get()
            val anchorDate = if (anchorTimestamp == null) {
                null
            } else {
                DateHelper.getMessageDateStringFromTimestamp(anchorTimestamp)
            }
            val response = if (isAfterAnchor && anchorDate != null) {
                threadsApi.history(
                    token,
                    afterDate = anchorDate,
                    count = itemsCount,
                    version = appInfo.libVersion
                )?.execute()
            } else {
                threadsApi.history(
                    token,
                    beforeDate = anchorDate,
                    count = itemsCount,
                    version = appInfo.libVersion
                )?.execute()
            }
            if (response?.isSuccessful != true) {
                val message = getNetworkErrorMessage(response)
                error("error loading history: $message")
                throw IOException(message)
            } else {
                return response.body()
            }
        } else {
            error(ThreadsApi.REST_TAG, "Error when loading history - token is empty!")
            throw IOException()
        }
    }

    /**
     * метод обертка для запроса истории сообщений
     * выполняется синхронно
     *
     * @param count         количество сообщений для загрузки
     * @param fromBeginning загружать ли историю с начала или с последнего полученного сообщения
     */
    @WorkerThread
    @Throws(Exception::class)
    fun getHistorySync(count: Int?, fromBeginning: Boolean): HistoryResponse? {
        return getHistorySync(if (fromBeginning) null else lastLoadedTimestamp, count)
    }

    fun setupLastItemIdFromHistory(list: List<MessageFromHistory>?) {
        if (!list.isNullOrEmpty()) {
            lastLoadedTimestamp = list[0].timeStamp
        }
    }

    internal interface HistoryLoadingCallback {
        fun onLoaded(items: List<ChatItem>)
    }

    private fun getNetworkErrorMessage(response: Response<HistoryResponse?>?): String? {
        return if (response?.code() in 400..599) {
            Config.getInstance().context.getString(Config.getInstance().chatStyle.networkErrorText)
        } else {
            null
        }
    }
}
