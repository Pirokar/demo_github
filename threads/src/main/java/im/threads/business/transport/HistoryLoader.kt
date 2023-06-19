package im.threads.business.transport

import android.content.Context
import androidx.annotation.WorkerThread
import com.google.gson.Gson
import im.threads.business.config.BaseConfig
import im.threads.business.logger.LoggerEdna.error
import im.threads.business.models.MessageFromHistory
import im.threads.business.rest.models.HistoryResponse
import im.threads.business.rest.queries.BackendApi.Companion.get
import im.threads.business.rest.queries.ThreadsApi
import im.threads.business.utils.AppInfoHelper
import im.threads.business.utils.DateHelper
import java.io.IOException

class HistoryLoader(private val context: Context) {
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
        val historyMock = getHistoryMock(context)
        if (context.applicationInfo.packageName == "io.edna.threads.demo" && historyMock.isNotEmpty()) {
            return Gson().fromJson(historyMock, HistoryResponse::class.java)
        }

        var count = count
        val token = BaseConfig.instance.transport.getToken()
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

            threadsApi.history(token, beforeDate, count, AppInfoHelper.getLibVersion())?.execute()?.body()
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

    companion object {
        fun getHistoryMock(context: Context): String {
            val preferences = context.getSharedPreferences("ecc_demo_json_preference", Context.MODE_PRIVATE)
            return preferences.getString("ecc_demo_json_preference_key", "") ?: ""
        }
    }
}
