package im.threads.internal.retrofit;

import java.util.List;

import im.threads.internal.model.FileUploadResponse;
import im.threads.internal.model.HistoryResponse;
import im.threads.internal.model.SettingsResponse;
import im.threads.internal.opengraph.OGResponse;
import okhttp3.MultipartBody;
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

    String API_VERSION = "12";

    @Multipart
    @PUT("files")
    Call<FileUploadResponse> upload(
            @Part MultipartBody.Part file,
            @Header("X-Client-Token") String token
    );

    @GET("history/v" + API_VERSION)
    Call<HistoryResponse> history(
            @Header("X-Client-Token") String token,
            @Query("before") String beforeDate,
            @Query("count") Integer count,
            @Query("libVersion") String version
    );

    @GET("opengraph")
    Call<OGResponse> openGraph(@Query(value = "href", encoded = true) String url);

    @POST("messages/read")
    Call<Void> markMessageAsRead(@Body List<String> ids);

    @GET("v" + API_VERSION + "/chat/settings?channelType=MOBILE&auth=false")
    Call<SettingsResponse> settings();
}
