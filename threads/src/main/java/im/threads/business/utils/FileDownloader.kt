package im.threads.business.utils

import android.content.Context
import im.threads.business.UserInfoBuilder
import im.threads.business.config.BaseConfig
import im.threads.business.logger.LoggerEdna
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.transport.AuthHeadersProvider
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import javax.net.ssl.HttpsURLConnection
import kotlin.math.floor

const val DOWNLOAD_PROGRESS_DELTA_TIME_MILLIS = 50
const val MAX_DOWNLOAD_PROGRESS = 100
const val DELTA_DOWNLOAD_PROGRESS = 2

class FileDownloader(
    private val path: String,
    private val fileName: String,
    private val ctx: Context,
    private val downloadListener: DownloadListener,
    private val preferences: Preferences,
    private val authHeadersProvider: AuthHeadersProvider
) {
    private val outputFile: File = File(
        getDownloadDir(ctx),
        fileName
    )

    private var isStopped = false

    fun download() {
        try {
            val url = URL(path)

            val files = getDownloadDir(ctx).listFiles { _, name ->
                name == fileName
            }
            if (!files.isNullOrEmpty()) {
                downloadListener.onProgress(100.0)
                downloadListener.onComplete(files[0])
                return
            }

            BaseConfig.instance.sslSocketFactoryConfig?.let {
                HttpsURLConnection.setDefaultSSLSocketFactory(it.sslSocketFactory)
            }
            val urlConnection = if (url.protocol.equals("file", ignoreCase = true)) {
                url.openConnection()
            } else if (url.protocol.equals("https", ignoreCase = true)) {
                url.openConnection() as HttpsURLConnection
            } else {
                url.openConnection() as HttpURLConnection
            }
            val userInfo = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)
            try {
                (urlConnection as? HttpURLConnection)?.requestMethod = "GET"
                if (userInfo?.clientId != null) {
                    urlConnection.setRequestProperty("X-Ext-Client-ID", userInfo.clientId)
                }
                authHeadersProvider.setHeadersToUrlConnection(userInfo, urlConnection)
                urlConnection.doOutput = false
                urlConnection.useCaches = false
                urlConnection.doInput = true
                urlConnection.connectTimeout = 60000
                urlConnection.readTimeout = 60000

                if (urlConnection is HttpsURLConnection) {
                    urlConnection.setHostnameVerifier { _, _ -> true }
                }

                var inputFile: File? = null
                val length = if (urlConnection is HttpURLConnection) {
                    getFileLength(urlConnection)
                } else {
                    inputFile = getFileFromFileUrl(urlConnection)
                    inputFile?.length() ?: 0L
                }
                val fileOutputStream = FileOutputStream(outputFile)
                val `in`: InputStream = if (inputFile == null) {
                    BufferedInputStream(urlConnection.inputStream)
                } else {
                    BufferedInputStream(FileInputStream(inputFile))
                }
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
                } else {
                    LoggerEdna.debug("Download stopped.")
                }
            } catch (e: Exception) {
                LoggerEdna.error("First catch called: $e.")
                downloadListener.onFileDownloadError(e)
            } finally {
                (urlConnection as? HttpURLConnection)?.disconnect()
            }
        } catch (e: Exception) {
            LoggerEdna.error("Second catch called: $e")
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
            LoggerEdna.error("download. getFileLength() error: $e")
        }
        return null
    }

    private fun getFileFromFileUrl(urlConnection: URLConnection): File? {
        return try {
            getFileFromAssets(urlConnection.url.path.split("/").last())
        } catch (exc: NoSuchElementException) {
            null
        }
    }

    private fun getFileFromAssets(fileName: String): File = File(ctx.cacheDir, fileName)
        .also {
            if (!it.exists()) {
                it.outputStream().use { cache ->
                    ctx.assets.open("test_files/$fileName").use { inputStream ->
                        inputStream.copyTo(cache)
                    }
                }
            }
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
    }
}
