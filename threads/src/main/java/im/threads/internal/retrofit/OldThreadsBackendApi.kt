package im.threads.internal.retrofit

import im.threads.internal.model.HistoryResponse
import im.threads.internal.model.SettingsResponse
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

    @GET("v$API_VERSION/chat/settings?channelType=MOBILE&auth=false")
    fun settings(): Call<SettingsResponse?>?

    companion object {
        const val API_VERSION = "14"
    }
}
