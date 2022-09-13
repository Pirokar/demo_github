package im.threads.business.transport

import androidx.annotation.WorkerThread
import com.google.gson.Gson
import im.threads.business.config.BaseConfig
import im.threads.business.logger.LoggerEdna.error
import im.threads.business.logger.LoggerEdna.info
import im.threads.business.models.MessageFromHistory
import im.threads.business.rest.models.HistoryResponse
import im.threads.business.rest.queries.BackendApi.Companion.get
import im.threads.business.rest.queries.ThreadsApi
import im.threads.business.utils.AppInfoHelper
import im.threads.business.utils.DateHelper
import retrofit2.Call
import retrofit2.Response
import java.io.IOException

object HistoryLoader {
    private var lastLoadedTimestamp: Long? = null

    /**
     * метод обертка для запроса истории сообщений
     * выполняется синхронно
     *
     * @param beforeTimestamp timestamp сообщения от которого грузить, null если с начала
     * @param count           количество сообщений для загрузки
     */
    @WorkerThread
    @Throws(Exception::class)
    fun getHistorySync(beforeTimestamp: Long?, count: Int?): HistoryResponse? {
        var count = count
        val token = BaseConfig.instance.transport.token
        if (count == null) {
            count = BaseConfig.instance.historyLoadingCount
        }
        return if (token.isNotEmpty()) {
            val threadsApi = get()
            val beforeDate = if (beforeTimestamp == null) {
                null
            } else {
                DateHelper.getMessageDateStringFromTimestamp(beforeTimestamp)
            }

            showStartLoadingHistoryLog(token, beforeDate, count)

            val call = threadsApi.history(token, beforeDate, count, AppInfoHelper.getLibVersion())
            val response = call?.execute()
            val body = response?.body()

            showHistoryLoadedLog(call, response, body)

            body
        } else {
            error(ThreadsApi.REST_TAG, "Loading history - token is empty!")
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

    private fun showStartLoadingHistoryLog(token: String, beforeDate: String?, count: Int?) {
        info(
            ThreadsApi.REST_TAG,
            "Loading history. token: $token, beforeDate: $beforeDate, count: $count," +
                " version: ${AppInfoHelper.getLibVersion()}, chatApiVersion: ${ThreadsApi.API_VERSION}," +
                " isNewChatCenterApi: ${BaseConfig.instance.newChatCenterApi}"
        )
    }

    private fun showHistoryLoadedLog(
        call: Call<HistoryResponse?>?,
        response: Response<HistoryResponse?>?,
        body: HistoryResponse?
    ) {
        val responseBody = try {
            Gson().toJson(body)
        } catch (exc: Exception) {
            val error = response?.errorBody()?.string() ?: "no error message"
            "Loading history - error when getting response body. Error message: $error.\n" +
                "Exception: $exc"
        }
        info(
            ThreadsApi.REST_TAG,
            "Loading history done. Call is null = ${call == null}. Response: ${response?.raw()}." +
                "Body: $responseBody"
        )
    }
}
