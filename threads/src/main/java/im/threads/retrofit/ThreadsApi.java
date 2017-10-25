package im.threads.retrofit;

import java.util.List;

import im.threads.model.FileUploadResponse;
import im.threads.model.HistoryResponseV2;
import im.threads.model.MessgeFromHistory;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by Admin on 28.03.2017.
 * описание серверных методов
 */

public interface ThreadsApi {
    @Multipart
    @PUT("files")
    Call<FileUploadResponse> upload(
            @Header("User-Agent") String userAgent,
            @Part MultipartBody.Part file,
            @Header("X-Client-Token") String token
    );

    @GET("history")
    Call<List<MessgeFromHistory>> history(
            @Header("X-Client-Token") String token,
            @Header("User-Agent") String userAgent,
            @Query("start") Long start,
            @Query("count") Long count,
            @Query("libVersion") String version
    );

    @GET("history/v2")
    Call<HistoryResponseV2> historyV2(
            @Header("X-Client-Token") String token,
            @Header("User-Agent") String userAgent,
            @Query("start") Long start,
            @Query("count") Long count,
            @Query("libVersion") String version
    );
}
