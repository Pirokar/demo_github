package im.threads.retrofit;

import im.threads.model.FileUploadResponse;
import im.threads.model.HistoryResponse;
import im.threads.opengraph.OGData;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * описание серверных методов
 */
public interface ThreadsApi {

    String API_VERSION = "v6";

    @Multipart
    @PUT("files")
    Call<FileUploadResponse> upload(
            @Part MultipartBody.Part file,
            @Header("X-Client-Token") String token
    );

    @GET("history/" + API_VERSION)
    Call<HistoryResponse> history(
            @Header("X-Client-Token") String token,
            @Query("before") String beforeDate,
            @Query("count") Integer count,
            @Query("libVersion") String version
    );

    @GET()
    Call<OGData> getOGData(@Url String url);

    @GET("/proxy")
    Call<ResponseBody> getOGDataProxy(@Query("href") String url);
}
