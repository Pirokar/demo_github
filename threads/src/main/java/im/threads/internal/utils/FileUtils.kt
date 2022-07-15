package im.threads.internal.utils

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.webkit.MimeTypeMap
import im.threads.R
import im.threads.internal.Config
import im.threads.internal.imageLoading.ImageLoader
import im.threads.internal.model.FileDescription
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

object FileUtils {
    private val TAG = FileUtils::class.java.simpleName
    private const val JPEG = 0
    private const val PNG = 1
    private const val PDF = 2
    private const val AUDIO = 3
    private const val OTHER_DOC_FORMATS = 4
    private const val UNKNOWN = -1
    private const val UNKNOWN_MIME_TYPE = "*/*"

    @JvmStatic
    fun getFileName(fd: FileDescription): String {
        if (fd.incomingName != null) {
            return fd.incomingName
        } else if (fd.fileUri != null) {
            return getFileName(fd.fileUri!!)
        }
        return ""
    }

    @JvmStatic
    fun getFileName(uri: Uri): String {
        Config.instance.context.contentResolver.query(uri, null, null, null, null).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                return if (index >= 0) {
                    cursor.getString(index)
                } else {
                    ""
                }
            }
        }
        return "threads" + UUID.randomUUID()
    }

    @JvmStatic
    fun getFileSize(uri: Uri): Long {
        Config.instance.context.contentResolver.query(uri, null, null, null, null).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.SIZE)
                return if (index >= 0) {
                    cursor.getLong(index)
                } else {
                    0L
                }
            }
        }
        return 0
    }

    @JvmStatic
    fun isImage(fileDescription: FileDescription?): Boolean {
        return (
            fileDescription != null &&
                (
                    getExtensionFromFileDescription(fileDescription) == JPEG ||
                        getExtensionFromFileDescription(fileDescription) == PNG
                    )
            )
    }

    @JvmStatic
    fun isVoiceMessage(fileDescription: FileDescription?): Boolean {
        return (
            fileDescription != null &&
                getExtensionFromFileDescription(fileDescription) == AUDIO
            )
    }

    @JvmStatic
    fun isDoc(fileDescription: FileDescription?): Boolean {
        return (
            fileDescription != null &&
                (
                    getExtensionFromFileDescription(fileDescription) == PDF ||
                        getExtensionFromFileDescription(fileDescription) == OTHER_DOC_FORMATS
                    )
            )
    }

    @JvmStatic
    fun getMimeType(fd: FileDescription): String {
        if (!TextUtils.isEmpty(fd.mimeType)) {
            return fd.mimeType
        }
        return if (fd.fileUri != null) {
            getMimeType(fd.fileUri!!)
        } else UNKNOWN_MIME_TYPE
    }

    @JvmStatic
    fun getMimeType(uri: Uri): String {
        val context = Config.instance.context
        var type = context.contentResolver.getType(uri)
        if (type == null) {
            type = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()))
        }
        return type ?: UNKNOWN_MIME_TYPE
    }

    @JvmStatic
    fun safeParse(source: String?): Uri? {
        return if (source != null) Uri.parse(source) else null
    }

    @JvmStatic
    @Throws(IOException::class)
    fun saveToDownloads(fileDescription: FileDescription) {
        val uri = fileDescription.fileUri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = Config.instance.context.contentResolver
            val imageCV = ContentValues()
            imageCV.put(MediaStore.Images.Media.DISPLAY_NAME, fileDescription.incomingName)
            imageCV.put(MediaStore.Images.Media.MIME_TYPE, getMimeType(fileDescription))
            val imagesCollection =
                MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            var outputUri = resolver.insert(imagesCollection, imageCV)
            if (outputUri == null) {
                imageCV.put(MediaStore.Images.Media.DISPLAY_NAME, "threads" + UUID.randomUUID())
                outputUri = resolver.insert(imagesCollection, imageCV)
            }
            saveToUri(uri, outputUri)
        } else {
            val outputFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                getFileName(
                    uri!!
                )
            )
            if (outputFile.exists() || outputFile.createNewFile()) {
                saveToFile(uri, outputFile)
                val dm =
                    Config.instance.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm?.addCompletedDownload(
                    getFileName(uri),
                    Config.instance.context.getString(R.string.threads_media_description),
                    true,
                    getMimeType(uri),
                    outputFile.path,
                    outputFile.length(),
                    false
                )
            } else {
                throw FileNotFoundException()
            }
        }
    }

    @JvmStatic
    fun convertRelativeUrlToAbsolute(relativeUrl: String?): String? {
        return if (TextUtils.isEmpty(relativeUrl) || relativeUrl!!.startsWith("http")) {
            relativeUrl
        } else Config.instance.serverBaseUrl + "files/" + relativeUrl
    }

    @JvmStatic
    fun canBeSent(context: Context, uri: Uri): Boolean {
        try {
            context.contentResolver.openInputStream(uri)
                .use { inputStream -> return inputStream != null && inputStream.read() != -1 }
        } catch (e: IOException) {
            ThreadsLogger.e(TAG, "file can't be sent", e)
            return false
        }
    }

    private fun getExtensionFromFileDescription(fileDescription: FileDescription): Int {
        val mimeType = getMimeType(fileDescription)
        if (mimeType != UNKNOWN_MIME_TYPE) {
            val extensionFromMimeType = getExtensionFromMimeType(mimeType)
            if (extensionFromMimeType != UNKNOWN) {
                return extensionFromMimeType
            }
        }
        val extensionFromPath = getExtensionFromPath(fileDescription.incomingName)
        return if (extensionFromPath != UNKNOWN) {
            extensionFromPath
        } else getExtensionFromPath(
            fileDescription.downloadPath
        )
    }

    private fun getExtensionFromPath(path: String?): Int {
        if (path == null || !path.contains(".")) {
            return UNKNOWN
        }
        val extension = path.substring(path.lastIndexOf(".") + 1)
        if (extension.equals("jpg", ignoreCase = true) || extension.equals(
                "jpeg",
                ignoreCase = true
            )
        ) {
            return JPEG
        }
        if (extension.equals("png", ignoreCase = true)) {
            return PNG
        }
        if (extension.equals("pdf", ignoreCase = true)) {
            return PDF
        }
        if (extension.equals("wav", ignoreCase = true) || extension.equals(
                "ogg",
                ignoreCase = true
            )
        ) {
            return AUDIO
        }
        return if (extension.equals("txt", ignoreCase = true) ||
            extension.equals("doc", ignoreCase = true) ||
            extension.equals("docx", ignoreCase = true) ||
            extension.equals("xls", ignoreCase = true) ||
            extension.equals("xlsx", ignoreCase = true) ||
            extension.equals("xlsm", ignoreCase = true) ||
            extension.equals("xltx", ignoreCase = true) ||
            extension.equals("xlt", ignoreCase = true)
        ) {
            OTHER_DOC_FORMATS
        } else UNKNOWN
    }

    private fun getExtensionFromMimeType(mimeType: String): Int {
        return when (mimeType) {
            "image/jpeg" -> JPEG
            "image/png" -> PNG
            "application/pdf" -> PDF
            "audio/wav", "audio/wave", "audio/x-wav", "audio/ogg", "application/ogg" -> AUDIO
            "text/plain", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel.sheet.macroenabled.12", "application/vnd.openxmlformats-officedocument.spreadsheetml.template" -> UNKNOWN
            else -> UNKNOWN
        }
    }

    @Throws(IOException::class)
    private fun saveToFile(uri: Uri?, outputFile: File) {
        ImageLoader
            .get()
            .load(uri.toString())
            .getBitmapSync(Config.instance.context)?.let { bitmap ->
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                try {
                    FileOutputStream(outputFile).use { fileOutputStream ->
                        fileOutputStream.write(byteArrayOutputStream.toByteArray())
                        fileOutputStream.flush()
                        bitmap.recycle()
                    }
                } catch (e: IOException) {
                    ThreadsLogger.e(TAG, "saveToFile", e)
                    bitmap.recycle()
                }
            }
    }

    @Throws(IOException::class)
    private fun saveToUri(uri: Uri?, outputUri: Uri?) {
        val resolver = Config.instance.context.contentResolver
        ImageLoader
            .get()
            .load(uri.toString())
            .getBitmapSync(Config.instance.context)?.let { bitmap ->
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                try {
                    resolver.openOutputStream(outputUri!!).use { fileOutputStream ->
                        if (fileOutputStream != null) {
                            fileOutputStream.write(byteArrayOutputStream.toByteArray())
                            fileOutputStream.flush()
                            bitmap.recycle()
                        }
                    }
                } catch (e: IOException) {
                    ThreadsLogger.e(TAG, "cannot get bitmap in saveToUri", e)
                    bitmap.recycle()
                }
            }
    }

    @JvmStatic
    fun getExtensionFromMediaStore(context: Context?, contentUri: Uri?): String? {
        if (contentUri == null || context == null) {
            return null
        }
        val path = getFileNameFromMediaStore(context, contentUri)
        return if (path == null || !path.contains(".")) {
            null
        } else path.substring(path.lastIndexOf(".") + 1)
    }

    @SuppressLint("NewApi")
    fun getFileNameFromMediaStore(context: Context, uri: Uri?): String? {
        context.contentResolver.query(uri!!, null, null, null, null).use { cursor ->
            if (cursor == null) {
                return null
            }
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            return cursor.getString(nameIndex)
        }
    }

    @JvmStatic
    @SuppressLint("NewApi")
    fun getFileSizeFromMediaStore(context: Context, uri: Uri?): Long {
        context.contentResolver.query(uri!!, null, null, null, null).use { cursor ->
            if (cursor == null) {
                return 0
            }
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            return cursor.getLong(sizeIndex)
        }
    }
}

fun MediaMetadataRetriever.getDuration(uri: Uri): Long {
    try {
        setDataSource(Config.instance.context, uri)
    } catch (exc: Exception) {
        setDataSource(uri.toString(), mutableMapOf<String, String>())
    }
    return extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
}
