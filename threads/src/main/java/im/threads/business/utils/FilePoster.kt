package im.threads.business.utils // ktlint-disable filename

import android.accounts.NetworkErrorException
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import com.google.gson.Gson
import im.threads.business.chat_updates.ChatUpdateProcessor
import im.threads.business.config.BaseConfig
import im.threads.business.imageLoading.ImageLoader
import im.threads.business.logger.LoggerEdna
import im.threads.business.models.ErrorResponse
import im.threads.business.models.FileDescription
import im.threads.business.models.FileUploadResponse
import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.rest.queries.DatastoreApi
import im.threads.business.rest.queries.ThreadsApi
import im.threads.business.serviceLocator.core.inject
import im.threads.business.transport.InputStreamRequestBody
import im.threads.business.utils.FileUtils.getFileName
import im.threads.business.utils.FileUtils.getMimeType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URLEncoder

private const val PHOTO_RESIZE_MAX_SIDE = 1600

fun postFile(fileDescription: FileDescription, clientId: String?): String? {
    LoggerEdna.info(ThreadsApi.REST_TAG, "Posting file with token = $clientId")
    if (!clientId.isNullOrBlank()) {
        fileDescription.fileUri?.let {
            if (it.toString().isNotEmpty()) {
                return sendFile(
                    it,
                    getMimeType(it),
                    clientId,
                    fileDescription
                )
            }
        }
        if (!fileDescription.downloadPath.isNullOrBlank()) {
            return fileDescription.downloadPath
        }
    }
    throw NetworkErrorException()
}

private fun sendFile(uri: Uri, mimeType: String, token: String, fileDescription: FileDescription): String {
    val chatUpdateProcessor: ChatUpdateProcessor by inject()
    val type = "file"
    val fileName = getFileName(uri)
    val fileRequestBody = getFileRequestBody(uri, mimeType)
    val part: MultipartBody.Part = MultipartBody.Part.createFormData(
        type,
        fileName,
        fileRequestBody
    )
    val agent: RequestBody = token.toRequestBody("text/plain".toMediaTypeOrNull())

    showSendingFileLog(uri, fileName, fileRequestBody)

    val response = DatastoreApi.get().upload(part, agent, URLEncoder.encode(token, "utf-8"))?.execute()
    response?.let {
        showFileSentLog(it)
        if (it.isSuccessful) {
            response.body()?.let { fileUploadResponse ->
                chatUpdateProcessor.postUploadResult(fileDescription.apply { state = AttachmentStateEnum.READY })
                return fileUploadResponse.result
            }
        } else {
            response.errorBody()?.let { responseBody ->
                chatUpdateProcessor.postUploadResult(fileDescription.apply { state = AttachmentStateEnum.ERROR })
                val errorBody: ErrorResponse =
                    BaseConfig.instance.gson.fromJson(responseBody.string(), ErrorResponse::class.java)
                if (!errorBody.message.isNullOrEmpty()) {
                    showErrorMessageLog(errorBody.message)
                    throw IOException(errorBody.code)
                }
            }
        }
    }

    LoggerEdna.error(ThreadsApi.REST_TAG, "Sending file error. Response: $response")
    throw IOException(response.toString())
}

private fun getFileRequestBody(uri: Uri, mimeType: String): RequestBody {
    if (isJpeg(uri)) {
        getJpegRequestBody(uri)?.let {
            return it
        }
    }
    return InputStreamRequestBody(
        mimeType.toMediaTypeOrNull(),
        BaseConfig.instance.context.contentResolver,
        uri
    )
}

private fun isJpeg(uri: Uri): Boolean {
    try {
        BaseConfig.instance.context.contentResolver.openInputStream(uri)?.use { iStream ->
            val inputData = getBytes(iStream)
            return inputData[0] == (-1).toByte() && inputData[1] == (-40).toByte() && inputData[2] == (-1).toByte()
        }
    } catch (e: IOException) {
    }
    return false
}

private fun getJpegRequestBody(uri: Uri?): RequestBody? {
    LoggerEdna.info("sendFile: $uri")
    val file = compressImage(uri) ?: return null
    return file.asRequestBody("image/jpeg".toMediaTypeOrNull())
}

private fun getBytes(inputStream: InputStream): ByteArray {
    ByteArrayOutputStream().use { byteBuffer ->
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        var len: Int
        while (inputStream.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        return byteBuffer.toByteArray()
    }
}

private fun compressImage(uri: Uri?): File? {
    val bitmap = ImageLoader.get()
        .load(uri.toString())
        .resize(PHOTO_RESIZE_MAX_SIDE, PHOTO_RESIZE_MAX_SIDE)
        .onlyScaleDown()
        .scales(ImageView.ScaleType.CENTER_INSIDE)
        .autoRotateWithExif(true)
        .getBitmapSync(BaseConfig.instance.context)
    val downsizedImageFile = File(BaseConfig.instance.context.cacheDir, getFileName(uri))
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap?.let {
        it.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        try {
            FileOutputStream(downsizedImageFile).use { fileOutputStream ->
                fileOutputStream.write(byteArrayOutputStream.toByteArray())
                fileOutputStream.flush()
                it.recycle()
                return downsizedImageFile
            }
        } catch (e: IOException) {
            LoggerEdna.error("downsizeImage", e)
            it.recycle()
            downsizedImageFile.delete()
            return null
        }
    }
    return null
}

private fun showSendingFileLog(
    uri: Uri,
    fileName: String,
    fileRequestBody: RequestBody
) {
    LoggerEdna.info(
        ThreadsApi.REST_TAG,
        "Sending file. Uri: $uri, fileName: $fileName, requestBody: $fileRequestBody"
    )
}

private fun showFileSentLog(response: Response<FileUploadResponse?>) {
    val responseBody = try {
        Gson().toJson(response.body())
    } catch (exc: Exception) {
        val error = response.errorBody()?.string() ?: "no error message"
        "Sending file error when parsing the body. Response: $response. Error message: $error.\n" +
            "Exception:$exc"
    }

    LoggerEdna.info(
        ThreadsApi.REST_TAG,
        "File has been sent. Response: $response. Body: $responseBody"
    )
}

private fun showErrorMessageLog(message: String) {
    LoggerEdna.error(ThreadsApi.REST_TAG, "Sending file error. Reason: $message.")
}
