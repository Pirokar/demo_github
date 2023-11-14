package im.threads.business.rest.queries

import im.threads.business.config.BaseConfig
import im.threads.business.models.FileUploadResponse
import im.threads.business.rest.models.ConfigResponse
import im.threads.business.rest.models.HistoryResponse
import im.threads.business.rest.models.SearchResponse
import im.threads.business.rest.models.VersionsModel
import im.threads.business.serviceLocator.core.inject
import im.threads.business.utils.ClientUseCase
import im.threads.business.utils.encodeUrl
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call

class ThreadsApi(
    private val oldThreadsApi: OldThreadsBackendApi? = null,
    private val newThreadsApi: NewThreadsBackendApi? = null,
    private val datastoreApi: ThreadsDatastoreApi? = null
) {
    private val clientInfo: ClientUseCase by inject()

    fun versions(): Call<VersionsModel?>? {
        return if (BaseConfig.getInstance().isNewChatCenterApi) {
            newThreadsApi?.versions()
        } else {
            oldThreadsApi?.versions()
        }
    }

    fun config(): Call<ConfigResponse?>? {
        return if (BaseConfig.getInstance().isNewChatCenterApi) {
            newThreadsApi?.config(API_VERSION)
        } else {
            oldThreadsApi?.config(API_VERSION)
        }
    }

    fun history(
        token: String?,
        beforeDate: String? = null,
        afterDate: String? = null,
        count: Int?,
        version: String?
    ): Call<HistoryResponse?>? {
        return if (BaseConfig.getInstance().isNewChatCenterApi) {
            newThreadsApi?.history(getHeadersMap(token), beforeDate, afterDate, count, version)
        } else {
            oldThreadsApi?.history(
                getHeadersMap(token),
                beforeDate,
                afterDate,
                count,
                version,
                API_VERSION
            )
        }
    }

    fun search(
        token: String?,
        searchString: String?,
        page: Int = 1
    ): Call<SearchResponse?>? {
        if (searchString == null) {
            return null
        }

        return if (BaseConfig.getInstance().isNewChatCenterApi) {
            newThreadsApi?.search(getHeadersMap(token), searchString, page)
        } else {
            oldThreadsApi?.search(getHeadersMap(token), searchString, page)
        }
    }

    fun markMessageAsRead(ids: List<String?>?): Call<Void?>? {
        return if (BaseConfig.getInstance().isNewChatCenterApi) {
            newThreadsApi?.markMessageAsRead(ids)
        } else {
            oldThreadsApi?.markMessageAsRead(ids)
        }
    }

    fun upload(file: MultipartBody.Part?, agent: RequestBody?, token: String): Call<FileUploadResponse?>? {
        return datastoreApi?.upload(file, agent, "$SIGNATURE_STRING$token".encodeUrl())
    }

    private fun getHeadersMap(token: String?): Map<String, String?> {
        val isClientIdEncrypted = clientInfo.getUserInfo()?.clientIdEncrypted == true
        return if (isClientIdEncrypted) {
            mapOf(Pair("X-Client-Token", token))
        } else {
            mapOf(
                Pair("X-Client-Token", token?.encodeUrl()),
                Pair("X-Header-Encoding", "url")
            )
        }
    }

    companion object {
        const val API_VERSION = "18"
        const val REST_TAG = "RestQuery"
        private const val SIGNATURE_STRING = "edna_79e621ac_a76a_4d36_b490_6758c43fa3d1:"
    }
}
