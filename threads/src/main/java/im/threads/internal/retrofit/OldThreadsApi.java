package im.threads.internal.retrofit;

import java.util.List;

import im.threads.internal.model.FileUploadResponse;
import im.threads.internal.model.HistoryResponse;
import im.threads.internal.model.SettingsResponse;
import im.threads.internal.opengraph.OGResponse;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * описание серверных методов
 */
public interface OldThreadsApi {

    public static String API_VERSION = "14";

    @Multipart
    @PUT("files")
    Call<FileUploadResponse> upload(
            @Part MultipartBody.Part file,
            @Part("externalClientId") RequestBody externalClientId,
            @Header("X-Client-Token") String token
    );

    @GET("history")
    Call<HistoryResponse> history(
            @Header("X-Client-Token") String token,
            @Query("before") String beforeDate,
            @Query("count") Integer count,
            @Query("libVersion") String version,
            @Query("chatApiVersion") String chatApiVersion
    );

    @GET("opengraph")
    Call<OGResponse> openGraph(@Query(value = "href", encoded = true) String url);

    @POST("messages/read")
    Call<Void> markMessageAsRead(@Body List<String> ids);

    @GET("v" + API_VERSION + "/chat/settings?channelType=MOBILE&auth=false")
    Call<SettingsResponse> settings();
}
