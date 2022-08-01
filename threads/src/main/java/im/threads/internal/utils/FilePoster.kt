package im.threads.internal.utils

import android.accounts.NetworkErrorException
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import im.threads.internal.Config
import im.threads.internal.imageLoading.ImageLoader
import im.threads.internal.model.ErrorResponse
import im.threads.internal.model.FileDescription
import im.threads.internal.retrofit.DatastoreApiGenerator
import im.threads.internal.transport.InputStreamRequestBody
import im.threads.internal.utils.FileUtils.getFileName
import im.threads.internal.utils.FileUtils.getMimeType
import im.threads.internal.utils.PrefUtils.Companion.clientID
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
private const val FILE_POSTER_TAG = "FilePoster"

fun postFile(fileDescription: FileDescription): String? {
    val token = clientID
    ThreadsLogger.i(FILE_POSTER_TAG, "token = $token")
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
        if (!fileDescription.downloadPath.isNullOrEmpty()) {
            return fileDescription.downloadPath
        }
    }
    throw NetworkErrorException()
}

private fun sendFile(uri: Uri, mimeType: String, token: String): String {
    ThreadsLogger.i(FILE_POSTER_TAG, "sendFile: $uri")
    val part: MultipartBody.Part = MultipartBody.Part.createFormData(
        "file",
        URLEncoder.encode(getFileName(uri), "utf-8"),
        getFileRequestBody(uri, mimeType)
    )
    val agent: RequestBody = token.toRequestBody("text/plain".toMediaTypeOrNull())
    val response = DatastoreApiGenerator.getApi().upload(part, agent, token)?.execute()
    response?.let {
        if (it.isSuccessful) {
            response.body()?.let { fileUploadResponse ->
                return fileUploadResponse.result
            }
        } else {
            response.errorBody()?.let { responseBody ->
                val errorBody: ErrorResponse =
                    Config.instance.gson.fromJson(responseBody.string(), ErrorResponse::class.java)
                if (!errorBody.message.isNullOrEmpty()) {
                    throw IOException(errorBody.code)
                }
            }
        }
    }
    ThreadsLogger.e(FILE_POSTER_TAG, "response = $response")
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
        Config.instance.context.contentResolver,
        uri
    )
}

private fun isJpeg(uri: Uri): Boolean {
    try {
        Config.instance.context.contentResolver.openInputStream(uri)?.use { iStream ->
            val inputData = getBytes(iStream) // JPEG(JFIF) header: FF D8 FF
            return inputData[0] == (-1).toByte() && inputData[1] == (-40).toByte() && inputData[2] == (-1).toByte()
        }
    } catch (e: IOException) {
    }
    return false
}

private fun getJpegRequestBody(uri: Uri?): RequestBody? {
    ThreadsLogger.i(FILE_POSTER_TAG, "sendFile: $uri")
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

@Throws(IOException::class)
private fun compressImage(uri: Uri?): File? {
    val bitmap = ImageLoader.get()
        .load(uri.toString())
        .resize(PHOTO_RESIZE_MAX_SIDE, PHOTO_RESIZE_MAX_SIDE)
        .onlyScaleDown()
        .scales(ImageView.ScaleType.CENTER_INSIDE)
        .autoRotateWithExif(true)
        .getBitmapSync(Config.instance.context)
    val downsizedImageFile = File(Config.instance.context.cacheDir, getFileName(uri))
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
            ThreadsLogger.e(TAG, "downsizeImage", e)
            it.recycle()
            downsizedImageFile.delete()
            return null
        }
    }
    return null
}
