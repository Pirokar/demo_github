package im.threads.business.rest.queries

import im.threads.business.rest.models.HistoryResponse
import im.threads.business.rest.models.SettingsResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * описание серверных методов
 */
interface OldThreadsBackendApi {
    @GET("history")
    fun history(
        @Header("X-Client-Token") token: String?,
        @Query("before") beforeDate: String?,
        @Query("count") count: Int?,
        @Query("libVersion") version: String?,
        @Query("chatApiVersion") chatApiVersion: String?
    ): Call<HistoryResponse?>?

    @POST("messages/read")
    fun markMessageAsRead(@Body ids: List<String?>?): Call<Void?>?

    @GET("v${ThreadsApi.API_VERSION}/chat/settings?channelType=MOBILE&auth=false")
    fun settings(): Call<SettingsResponse?>?
}
