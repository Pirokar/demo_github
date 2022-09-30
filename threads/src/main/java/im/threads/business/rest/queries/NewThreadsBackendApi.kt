package im.threads.business.rest.queries

import im.threads.business.rest.models.HistoryResponse
import im.threads.business.rest.models.SettingsResponse
import im.threads.business.rest.models.VersionsModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface NewThreadsBackendApi {
    @GET("api/versions")
    fun versions(): Call<VersionsModel?>?

    @GET("api/chat/settings?channelType=MOBILE&auth=false")
    fun settings(): Call<SettingsResponse?>?

    @GET("api/client/history")
    fun history(
        @Header("X-Client-Token") token: String?,
        @Query("before") beforeDate: String?,
        @Query("count") count: Int?,
        @Query("libVersion") version: String?,
        @Query("chatApiVersion") chatApiVersion: String = ThreadsApi.API_VERSION
    ): Call<HistoryResponse?>?

    @POST("api/messages/read")
    fun markMessageAsRead(@Body ids: List<String?>?): Call<Void?>?
}
