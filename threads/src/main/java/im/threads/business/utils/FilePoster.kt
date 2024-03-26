package im.threads.business.utils // ktlint-disable filename

import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import im.threads.business.chatUpdates.ChatUpdateProcessor
import im.threads.business.config.BaseConfig
import im.threads.business.imageLoading.ImageLoader
import im.threads.business.logger.LoggerEdna
import im.threads.business.models.FileDescription
import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.rest.queries.DatastoreApi
import im.threads.business.rest.queries.ThreadsApi
import im.threads.business.serviceLocator.core.inject
import im.threads.business.transport.InputStreamRequestBody
import im.threads.business.transport.TransportException
import im.threads.business.utils.FileUtils.getFileName
import im.threads.business.utils.FileUtils.getMimeType
import im.threads.business.utils.FileUtils.isImage
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

private const val PHOTO_RESIZE_MAX_SIDE = 1600

fun postFile(fileDescription: FileDescription, clientId: String?): String? {
    val chatUpdateProcessor: ChatUpdateProcessor by inject()
    if (!clientId.isNullOrBlank()) {
        try {
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
        } catch (exc: Exception) {
            chatUpdateProcessor.postError(TransportException(exc.message))
            return null
        }
    }

    chatUpdateProcessor.postError(TransportException("No client id"))
    return null
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

    val response = try {
        DatastoreApi.get().upload(part, agent, token.encodeUrl())?.execute()
    } catch (exc: Exception) {
        chatUpdateProcessor.postError(TransportException(exc.message))
        null
    }

    response?.let {
        if (it.isSuccessful) {
            response.body()?.let { fileUploadResponse ->
                chatUpdateProcessor.postUploadResult(fileDescription.apply { state = AttachmentStateEnum.READY })
                return fileUploadResponse.result
            }
        } else {
            response.errorBody()?.let { responseBody ->
                chatUpdateProcessor.postUploadResult(fileDescription.apply { state = AttachmentStateEnum.ERROR })
                val errorMessage = "Code: ${response.code()}, message: ${response.message()}. ${responseBody.string()}"
                if (errorMessage.isNotEmpty()) {
                    showErrorMessageLog(errorMessage)
                    throw IOException(errorMessage)
                }
            }
        }
    }

    LoggerEdna.error(ThreadsApi.REST_TAG, "Sending file error. Response: $response")
    throw IOException(response.toString())
}

private fun getFileRequestBody(uri: Uri, mimeType: String): RequestBody {
    if (isImage(uri)) {
        getJpegRequestBody(uri)?.let {
            return it
        }
    }
    return InputStreamRequestBody(
        mimeType.toMediaTypeOrNull(),
        BaseConfig.getInstance().context.contentResolver,
        uri
    )
}

private fun isJpeg(uri: Uri): Boolean {
    try {
        BaseConfig.getInstance().context.contentResolver.openInputStream(uri)?.use { iStream ->
            val inputData = getBytes(iStream)
            return inputData[0] == (-1).toByte() && inputData[1] == (-40).toByte() && inputData[2] == (-1).toByte()
        }
    } catch (e: IOException) {
    }
    return false
}

private fun getJpegRequestBody(uri: Uri?): RequestBody? {
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
        .getBitmapSync(BaseConfig.getInstance().context)
    val downsizedImageFile = File(BaseConfig.getInstance().context.cacheDir, getFileName(uri))
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
            LoggerEdna.error("downsizeImage error: $e")
            it.recycle()
            downsizedImageFile.delete()
            return null
        }
    }
    return null
}

private fun showErrorMessageLog(message: String) {
    LoggerEdna.error(ThreadsApi.REST_TAG, "Sending file error. Reason: $message.")
}
