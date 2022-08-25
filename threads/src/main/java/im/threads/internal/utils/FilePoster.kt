package im.threads.internal.utils // ktlint-disable filename

import android.accounts.NetworkErrorException
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import im.threads.business.config.BaseConfig
import im.threads.business.imageLoading.ImageLoader
import im.threads.business.logger.LoggerEdna
import im.threads.business.models.FileDescription
import im.threads.business.rest.queries.DatastoreApi
import im.threads.business.transport.InputStreamRequestBody
import im.threads.business.utils.FileUtils.getFileName
import im.threads.business.utils.FileUtils.getMimeType
import im.threads.internal.model.ErrorResponse
import im.threads.business.utils.preferences.PrefUtilsBase
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URLEncoder

private const val PHOTO_RESIZE_MAX_SIDE = 1600

fun postFile(fileDescription: FileDescription): String? {
    val token = PrefUtilsBase.clientID
    LoggerEdna.info("token = $token")
    if (token.isNotEmpty()) {
        fileDescription.fileUri?.let {
            if (it.toString().isNotEmpty()) {
                return sendFile(
                    it,
                    getMimeType(it),
                    token
                )
            }
        }
        if (!fileDescription.downloadPath.isNullOrBlank()) {
            return fileDescription.downloadPath
        }
    }
    throw NetworkErrorException()
}

private fun sendFile(uri: Uri, mimeType: String, token: String): String {
    LoggerEdna.info("sendFile: $uri")
    val part: MultipartBody.Part = MultipartBody.Part.createFormData(
        "file",
        URLEncoder.encode(getFileName(uri), "utf-8"),
        getFileRequestBody(uri, mimeType)
    )
    val agent: RequestBody = token.toRequestBody("text/plain".toMediaTypeOrNull())
    val response = DatastoreApi.get().upload(part, agent, token)?.execute()
    response?.let {
        if (it.isSuccessful) {
            response.body()?.let { fileUploadResponse ->
                return fileUploadResponse.result
            }
        } else {
            response.errorBody()?.let { responseBody ->
                val errorBody: ErrorResponse =
                    BaseConfig.instance.gson.fromJson(responseBody.string(), ErrorResponse::class.java)
                if (!errorBody.message.isNullOrEmpty()) {
                    throw IOException(errorBody.code)
                }
            }
        }
    }
    LoggerEdna.error("response = $response")
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
