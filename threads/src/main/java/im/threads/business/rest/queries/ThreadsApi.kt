package im.threads.business.rest.queries

import im.threads.business.config.BaseConfig
import im.threads.business.models.FileUploadResponse
import im.threads.business.rest.models.HistoryResponse
import im.threads.business.rest.models.SettingsResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call

class ThreadsApi(
    private val oldThreadsApi: OldThreadsBackendApi? = null,
    private val newThreadsApi: NewThreadsBackendApi? = null,
    private val datastoreApi: ThreadsDatastoreApi? = null
) {
    fun settings(): Call<SettingsResponse?>? {
        return if (BaseConfig.instance.newChatCenterApi) {
            newThreadsApi?.settings()
        } else {
            oldThreadsApi?.settings()
        }
    }

    fun history(
        token: String?,
        beforeDate: String?,
        count: Int?,
        version: String?
    ): Call<HistoryResponse?>? {
        return if (BaseConfig.instance.newChatCenterApi) {
            newThreadsApi?.history(token, beforeDate, count, version)
        } else {
            oldThreadsApi?.history(
                token,
                beforeDate,
                count,
                version,
                API_VERSION
            )
        }
    }

    fun markMessageAsRead(ids: List<String?>?): Call<Void?>? {
        return if (BaseConfig.instance.newChatCenterApi) {
            newThreadsApi?.markMessageAsRead(ids)
        } else {
            oldThreadsApi?.markMessageAsRead(ids)
        }
    }

    fun upload(file: MultipartBody.Part?, agent: RequestBody?, token: String): Call<FileUploadResponse?>? {
        return datastoreApi?.upload(file, agent, SIGNATURE_STRING + token)
    }

    companion object {
        const val API_VERSION = "14"
        const val REST_TAG = "RestQuery"
        private const val SIGNATURE_STRING = "super-duper-signature-string:"
    }
}
