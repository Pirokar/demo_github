package im.threads.business.utils

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
import androidx.documentfile.provider.DocumentFile
import im.threads.R
import im.threads.business.config.BaseConfig
import im.threads.business.imageLoading.ImageLoader
import im.threads.business.logger.LoggerEdna
import im.threads.business.logger.LoggerEdna.debug
import im.threads.business.models.CampaignMessage
import im.threads.business.models.FileDescription
import im.threads.business.models.Quote
import im.threads.business.models.UpcomingUserMessage
import im.threads.business.secureDatabase.DatabaseHolder
import im.threads.business.serviceLocator.core.inject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat
import java.util.UUID

object FileUtils {
    private const val JPEG = 0
    private const val PNG = 1
    private const val PDF = 2
    private const val AUDIO = 3
    private const val OTHER_DOC_FORMATS = 4
    private const val UNKNOWN = -1
    private const val UNKNOWN_MIME_TYPE = "*/*"

    @JvmStatic
    fun generateFileName(fd: FileDescription): String {
        val prefix = if (fd.downloadPath != null) {
            UUID.nameUUIDFromBytes(fd.downloadPath?.toByteArray())
        } else {
            ""
        }
        val name = fd.incomingName ?: getFileName(fd.fileUri)
        return "${prefix}_$name"
    }

    @JvmStatic
    fun getFileName(uri: Uri?): String {
        uri?.let {
            BaseConfig.getInstance().context.contentResolver.query(uri, null, null, null, null)
                .use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        return if (index >= 0) {
                            cursor.getString(index)
                        } else {
                            ""
                        }
                    }
                }
        }
        return "threads" + UUID.randomUUID()
    }

    @JvmStatic
    fun getFileSize(uri: Uri): Long {
        BaseConfig.getInstance().context.contentResolver.query(uri, null, null, null, null)
            .use { cursor ->
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

    /**
     * Проверяет, содержит ли fileDescription изображение
     * @param fileDescription объект для проверки
     * @return возвращает резултат `true` в случае, если контейнер содержит изображение
     */
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

    private fun isFileCorrupted(fileDescription: FileDescription): Boolean {
        val actualSize = fileDescription.fileUri?.let {
            getFileSize(it)
        } ?: 0
        return fileDescription.size != actualSize
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
        if (!fd.mimeType.isNullOrBlank()) {
            return fd.mimeType ?: ""
        }

        return if (fd.fileUri != null) {
            getMimeType(fd.fileUri!!)
        } else {
            UNKNOWN_MIME_TYPE
        }
    }

    @JvmStatic
    fun getMimeType(uri: Uri): String {
        val context = BaseConfig.getInstance().context
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
    fun removeFileIfCorrupted(fileDescription: FileDescription?) {
        val database: DatabaseHolder by inject()
        val context = BaseConfig.getInstance().context

        if (fileDescription?.fileUri != null && isFileCorrupted(fileDescription)) {
            DocumentFile
                .fromSingleUri(context, fileDescription.fileUri!!)
                ?.delete()
            val files = FileDownloader.getDownloadDir(context).listFiles { _, name ->
                name.endsWith(fileDescription.incomingName ?: "no name")
            }
            files?.forEach {
                if (it.exists()) {
                    it.delete()
                }
            }
            fileDescription.fileUri = null
            fileDescription.downloadProgress = 0
            database.updateFileDescription(fileDescription)
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun saveToDownloads(fileDescription: FileDescription) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val uri = Uri.parse(fileDescription.downloadPath)
            if (uri != null) {
                val resolver = BaseConfig.getInstance().context.contentResolver
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
            }
        } else {
            val uri = fileDescription.fileUri
            if (uri != null) {
                val outputFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    getFileName(
                        uri!!
                    )
                )
                if (outputFile.exists() || outputFile.createNewFile()) {
                    saveImageToFile(uri, outputFile)
                    val dm =
                        BaseConfig.getInstance().context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    dm.addCompletedDownload(
                        getFileName(uri),
                        BaseConfig.getInstance().context.getString(R.string.ecc_media_description),
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
    }

    internal fun generatePreviewFileName(fileName: String?): String? {
        if (!fileName.isNullOrEmpty()) {
            val fileNameParts = fileName.split(".").toTypedArray()
            if (fileNameParts.size >= 2) {
                return fileNameParts[0] + "_small." + fileNameParts[fileNameParts.size - 1]
            }
        }
        return null
    }

    internal fun isPreviewFileExist(context: Context, fileDescription: FileDescription?): Boolean {
        if (fileDescription != null) {
            val outputFile = File(FileDownloader.getDownloadDir(context), generateFileName(fileDescription))
            return outputFile.exists()
        }
        return false
    }

    /**
     * Конвертирует относительный (неполный) url в абсолютный, подставляя значение datastore
     */
    internal fun String?.toAbsoluteUrl() = convertRelativeUrlToAbsolute(this)

    @JvmStatic
    fun convertRelativeUrlToAbsolute(relativeUrl: String?): String? {
        return if (TextUtils.isEmpty(relativeUrl) || relativeUrl!!.startsWith("http")) {
            relativeUrl
        } else {
            val datastoreUrl = BaseConfig.getInstance().datastoreUrl
            val filesUrl = if (datastoreUrl?.endsWith("/") == true) {
                "${datastoreUrl}files"
            } else {
                "$datastoreUrl/files"
            }
            "$filesUrl/$relativeUrl"
        }
    }

    @JvmStatic
    fun canBeSent(context: Context, uri: Uri): Boolean {
        try {
            context.contentResolver.openInputStream(uri)
                .use { inputStream -> return inputStream != null && inputStream.read() != -1 }
        } catch (e: IOException) {
            LoggerEdna.error("file can't be sent. $e")
            return false
        }
    }

    @JvmStatic
    fun createImageFile(context: Context): File? {
        val filename = "thr" + System.currentTimeMillis() + ".jpg"
        val output = File(context.filesDir, filename)
        debug("File genereated into filesDir : " + output.absolutePath)
        return output
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
        } else {
            getExtensionFromPath(
                fileDescription.downloadPath
            )
        }
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
        } else {
            UNKNOWN
        }
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
    private fun saveImageToFile(uri: Uri?, outputFile: File) {
        ImageLoader
            .get()
            .load(uri.toString())
            .getBitmapSync(BaseConfig.getInstance().context)?.let { bitmap ->
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                try {
                    FileOutputStream(outputFile).use { fileOutputStream ->
                        fileOutputStream.write(byteArrayOutputStream.toByteArray())
                        fileOutputStream.flush()
                        bitmap.recycle()
                    }
                } catch (e: IOException) {
                    LoggerEdna.error("saveToFile error: $e")
                    bitmap.recycle()
                }
            }
    }

    @Throws(IOException::class)
    private fun saveToUri(uri: Uri?, outputUri: Uri?) {
        val resolver = BaseConfig.getInstance().context.contentResolver
        ImageLoader
            .get()
            .load(uri.toString())
            .getBitmapSync(BaseConfig.getInstance().context)?.let { bitmap ->
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
                    LoggerEdna.error("cannot get bitmap in saveToUri: $e")
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
        } else {
            path.substring(path.lastIndexOf(".") + 1)
        }
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
    fun getUpcomingUserMessagesFromSelection(
        uris: List<Uri>,
        inputText: String?,
        fileDescriptionText: String,
        campaignMessage: CampaignMessage?,
        quote: Quote?
    ): List<UpcomingUserMessage> {
        val messages: MutableList<UpcomingUserMessage> = ArrayList()
        var fileUri: Uri = uris[0]
        messages.add(
            UpcomingUserMessage(
                FileDescription(
                    fileDescriptionText,
                    fileUri,
                    getFileSize(fileUri),
                    System.currentTimeMillis()
                ),
                campaignMessage,
                quote,
                inputText?.trim { it <= ' ' },
                inputText?.isLastCopyText() ?: false
            )
        )
        for (i in 1 until uris.size) {
            fileUri = uris[i]
            val fileDescription = FileDescription(
                fileDescriptionText,
                fileUri,
                getFileSize(fileUri),
                System.currentTimeMillis()
            )
            val upcomingUserMessage = UpcomingUserMessage(
                fileDescription,
                null,
                null,
                null,
                false
            )
            messages.add(upcomingUserMessage)
        }

        return messages
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
        setDataSource(BaseConfig.getInstance().context, uri)
    } catch (exc: Exception) {
        try {
            setDataSource(uri.toString(), mutableMapOf<String, String>())
        } catch (exc: Exception) {
            return 0
        }
    }
    return extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
}

fun Long.toFileSize(): String {
    val context = BaseConfig.getInstance().context
    val kb = 1024L
    val mb = kb * kb
    val gb = mb * kb
    val tb = gb * kb

    val dividers = longArrayOf(tb, gb, mb, kb, 1)
    val units = arrayOf(
        context.getString(R.string.ecc_tbytes),
        context.getString(R.string.ecc_gbytes),
        context.getString(R.string.ecc_mbytes),
        context.getString(R.string.ecc_kbytes),
        context.getString(R.string.ecc_bytes)
    )
    if (this < 0) {
        LoggerEdna.error("Invalid file size: $this")
        return "0 ${units[0]}"
    }
    var result = ""
    for (i in dividers.indices) {
        val divider = dividers[i]
        if (this >= divider) {
            result = format(this, divider, units[i])
            break
        }
    }
    return result
}

private fun format(
    value: Long,
    divider: Long,
    unit: String
): String {
    val result = if (divider > 1) value.toDouble() / divider.toDouble() else value.toDouble()
    return DecimalFormat("#,##0.#").format(result).toString() + " " + unit
}
