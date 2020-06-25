package im.threads.internal.retrofit;

import im.threads.internal.model.FileUploadResponse;
import im.threads.internal.model.HistoryResponse;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * описание серверных методов
 */
public interface ThreadsApi {

    String API_VERSION = "9";

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

}
