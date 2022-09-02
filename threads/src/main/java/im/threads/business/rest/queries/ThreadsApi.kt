package im.threads.business.rest.queries

import im.threads.business.rest.models.HistoryResponse
import im.threads.business.rest.models.SettingsResponse
import im.threads.internal.Config
import im.threads.internal.model.FileUploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call

class ThreadsApi(
    private val oldThreadsApi: OldThreadsBackendApi? = null,
    private val newThreadsApi: NewThreadsBackendApi? = null,
    private val datastoreApi: ThreadsDatastoreApi? = null
) {
    fun settings(): Call<SettingsResponse?>? {
        return if (Config.instance.newChatCenterApi) {
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
        return if (Config.instance.newChatCenterApi) {
            newThreadsApi?.history(token, beforeDate, count, version)
        } else {
            oldThreadsApi?.history(
                token,
                beforeDate,
                count,
                version,
                OldThreadsBackendApi.API_VERSION
            )
        }
    }

    fun markMessageAsRead(ids: List<String?>?): Call<Void?>? {
        return if (Config.instance.newChatCenterApi) {
            newThreadsApi?.markMessageAsRead(ids)
        } else {
            oldThreadsApi?.markMessageAsRead(ids)
        }
    }

    fun upload(file: MultipartBody.Part?, agent: RequestBody?, token: String): Call<FileUploadResponse?>? {
        return datastoreApi?.upload(file, agent, SIGNATURE_STRING + token)
    }

    companion object {
        private const val SIGNATURE_STRING = "super-duper-signature-string:"
    }
}
