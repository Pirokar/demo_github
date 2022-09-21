package im.threads.business.utils

import android.content.Context
import android.net.Uri
import im.threads.business.config.BaseConfig
import im.threads.business.logger.LoggerEdna
import im.threads.business.utils.preferences.PrefUtilsBase
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.math.floor

const val DOWNLOAD_PROGRESS_DELTA_TIME_MILLIS = 50
const val MAX_DOWNLOAD_PROGRESS = 100
const val DELTA_DOWNLOAD_PROGRESS = 2

class FileDownloader(
    private val path: String,
    fileName: String,
    private val ctx: Context,
    private val downloadListener: DownloadListener
) {
    private val outputFile: File = File(
        getDownloadDir(ctx),
        generateFileName(
            fileName
        )
    )

    private var isStopped = false

    fun download() {
        try {
            val url = URL(path)

            Uri.parse(path).lastPathSegment?.let { lastPath ->
                val files = getDownloadDir(ctx).listFiles { _, name ->
                    name.contains(lastPath)
                }
                if (!files.isNullOrEmpty()) {
                    downloadListener.onProgress(100.0)
                    downloadListener.onComplete(files[0])

                    return
                }
            }

            BaseConfig.instance.sslSocketFactoryConfig?.let {
                HttpsURLConnection.setDefaultSSLSocketFactory(it.sslSocketFactory)
            }
            val urlConnection = if (url.protocol.equals("https", ignoreCase = true)) {
                url.openConnection() as HttpsURLConnection
            } else {
                url.openConnection() as HttpURLConnection
            }
            try {
                urlConnection.requestMethod = "GET"
                urlConnection.setRequestProperty("X-Ext-Client-ID", PrefUtilsBase.clientID)
                if (!PrefUtilsBase.authToken.isNullOrBlank()) {
                    urlConnection.setRequestProperty("Authorization", PrefUtilsBase.authToken)
                }
                if (!PrefUtilsBase.authSchema.isNullOrBlank()) {
                    urlConnection.setRequestProperty("X-Auth-Schema", PrefUtilsBase.authSchema)
                }
                urlConnection.doOutput = false
                urlConnection.useCaches = false
                urlConnection.doInput = true
                urlConnection.connectTimeout = 60000
                urlConnection.readTimeout = 60000

                if (urlConnection is HttpsURLConnection) {
                    urlConnection.setHostnameVerifier { _, _ -> true }
                }

                val length = getFileLength(urlConnection)
                val fileOutputStream = FileOutputStream(outputFile)
                val `in`: InputStream = BufferedInputStream(urlConnection.inputStream)
                var tempLength: Int
                var bytesRead: Long = 0
                var lastReadTime = System.currentTimeMillis()
                var lastReadProgress = 0
                val buffer = ByteArray(1024 * 8)

                while (`in`.read(buffer).also { tempLength = it } > 0 && !isStopped) {
                    fileOutputStream.write(buffer, 0, tempLength)
                    bytesRead += tempLength.toLong()
                    if (System.currentTimeMillis() > lastReadTime + DOWNLOAD_PROGRESS_DELTA_TIME_MILLIS) {
                        if (length != null) {
                            val progress =
                                floor(bytesRead.toDouble() / length * MAX_DOWNLOAD_PROGRESS).toInt()
                            lastReadTime = System.currentTimeMillis()
                            downloadListener.onProgress(progress.toDouble())
                        } else {
                            lastReadProgress += DELTA_DOWNLOAD_PROGRESS
                            if (lastReadProgress >= MAX_DOWNLOAD_PROGRESS) {
                                lastReadProgress = 0
                            }
                            downloadListener.onProgress(lastReadProgress.toDouble())
                        }
                    }
                }

                fileOutputStream.flush()
                fileOutputStream.close()

                if (!isStopped) {
                    downloadListener.onComplete(outputFile)
                }
            } catch (e: Exception) {
                LoggerEdna.error("1 ", e)
                downloadListener.onFileDownloadError(e)
            } finally {
                urlConnection.disconnect()
            }
        } catch (e: Exception) {
            LoggerEdna.error("2 ", e)
            downloadListener.onFileDownloadError(e)
        }
    }

    fun stop() {
        isStopped = true
    }

    private fun getFileLength(urlConnection: HttpURLConnection): Long? {
        try {
            val values = urlConnection.headerFields["Content-Length"]
            if (values != null && values.isNotEmpty()) {
                return values[0]?.toLong()
            }
        } catch (e: Exception) {
            LoggerEdna.error("download", e)
        }
        return null
    }

    interface DownloadListener {
        fun onProgress(progress: Double)
        fun onComplete(file: File)
        fun onFileDownloadError(e: Exception?)
    }

    companion object {
        @JvmStatic
        fun getDownloadDir(ctx: Context): File {
            return ctx.filesDir
        }

        fun generateFileName(fileName: String): String {
            val sb = StringBuilder()
            sb.append(getFileName(fileName))
            val ext = getFileExtension(fileName)
            if (ext != null) {
                sb.append(ext)
            }
            return sb.toString()
        }

        private fun getFileExtension(path: String?): String? {
            return if (path != null && path.lastIndexOf('.') != -1) {
                path.substring(path.lastIndexOf('.'))
            } else null
        }

        private fun getFileName(fileName: String): String {
            return if (fileName.lastIndexOf('.') != -1) {
                fileName.substring(0, fileName.lastIndexOf('.'))
            } else fileName
        }
    }
}
