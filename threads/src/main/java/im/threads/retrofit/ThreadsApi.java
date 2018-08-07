package im.threads.retrofit;

import im.threads.model.FileUploadResponse;
import im.threads.model.HistoryResponse;
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
 * Created by Admin on 28.03.2017.
 * описание серверных методов
 */

public interface ThreadsApi {

    public static String API_VERSION = "v3";

    @Multipart
    @PUT("files")
    Call<FileUploadResponse> upload(
            @Part MultipartBody.Part file,
            @Header("X-Client-Token") String token
    );

    @GET("history/" + API_VERSION)
    Call<HistoryResponse> history(
            @Header("X-Client-Token") String token,
            @Query("start") Long start,
            @Query("count") Long count,
            @Query("libVersion") String version
    );

    @GET()
    Call<ResponseBody> getUrlResponseBody (@Url String url);
}
