package im.threads.business.rest.queries

import im.threads.business.models.FileUploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part

interface ThreadsDatastoreApi {
    @Multipart
    @PUT("files")
    fun upload(
        @Part file: MultipartBody.Part?,
        @Part("externalClientId") externalClientId: RequestBody?,
        @Header("X-Client-Token") token: String?
    ): Call<FileUploadResponse?>?
}
