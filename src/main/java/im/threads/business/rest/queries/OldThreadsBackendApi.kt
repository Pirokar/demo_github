package im.threads.business.rest.queries

import im.threads.business.rest.models.ConfigResponse
import im.threads.business.rest.models.HistoryResponse
import im.threads.business.rest.models.SearchResponse
import im.threads.business.rest.models.SettingsResponse
import im.threads.business.rest.models.VersionsModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * описание серверных методов
 */
interface OldThreadsBackendApi {
    @GET("api/versions")
    fun versions(): Call<VersionsModel?>?

    @GET("history")
    fun history(
        @HeaderMap headerMap: Map<String, String?>,
        @Query("before") beforeDate: String?,
        @Query("after") afterDate: String?,
        @Query("count") count: Int?,
        @Query("libVersion") version: String?,
        @Query("chatApiVersion") chatApiVersion: String?
    ): Call<HistoryResponse?>?

    @GET("api/client/search")
    fun search(
        @HeaderMap headerMap: Map<String, String?>,
        @Query("term") searchString: String,
        @Query("page") page: Int,
        @Query("chatApiVersion") chatApiVersion: String = ThreadsApi.API_VERSION
    ): Call<SearchResponse?>

    @POST("messages/read")
    fun markMessageAsRead(@Body ids: List<String?>?): Call<Void?>?

    @GET("v${ThreadsApi.API_VERSION}/chat/settings?channelType=MOBILE&auth=false")
    fun settings(): Call<SettingsResponse?>?

    @GET("/api/chat/config?channelType=MOBILE&auth=true")
    fun config(@Query("chatApiVersion") chatApiVersion: String?): Call<ConfigResponse?>?
}
