package im.threads.internal.utils

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import im.threads.business.logger.LoggerEdna
import im.threads.internal.Config
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.math.floor

class FileDownloader(private val path: String, fileName: String, ctx: Context, private val downloadLister: DownloadLister) {
    private val outputFile: File = File(
        getDownloadDir(ctx),
        generateFileName(
            path,
            fileName
        )
    )

    private var isStopped = false

    fun download() {
        try {
            val url = URL(path)
            Config.instance.sslSocketFactoryConfig?.let {
                HttpsURLConnection.setDefaultSSLSocketFactory(it.sslSocketFactory)
            }
            val urlConnection = if (url.protocol.equals("https", ignoreCase = true)) {
                url.openConnection() as HttpsURLConnection
            } else {
                url.openConnection() as HttpURLConnection
            }
            try {
                urlConnection.requestMethod = "GET"
                urlConnection.setRequestProperty("X-Ext-Client-ID", PrefUtils.clientID)
                if (!TextUtils.isEmpty(PrefUtils.authToken)) {
                    urlConnection.setRequestProperty("Authorization", PrefUtils.authToken)
                }
                if (!TextUtils.isEmpty(PrefUtils.authSchema)) {
                    urlConnection.setRequestProperty("X-Auth-Schema", PrefUtils.authSchema)
                }
                urlConnection.doOutput = false
                urlConnection.useCaches = false
                urlConnection.doInput = true
                urlConnection.connectTimeout = 60000
                urlConnection.readTimeout = 60000

                if (urlConnection is HttpsURLConnection) {
                    urlConnection.setHostnameVerifier { hostname, session -> true }
                }

                val length = getFileLength(urlConnection)
                val fileOutputStream = FileOutputStream(outputFile)
                val `in`: InputStream = BufferedInputStream(urlConnection.inputStream)
                var tempLength: Int
                var bytesRead: Long = 0
                var lastReadTime = System.currentTimeMillis()
                val buffer = ByteArray(1024 * 8)

                while (`in`.read(buffer).also { tempLength = it } > 0 && !isStopped) {
                    fileOutputStream.write(buffer, 0, tempLength)
                    bytesRead += tempLength.toLong()
                    if (length != null && System.currentTimeMillis() > lastReadTime + 500) {
                        val progress = floor(bytesRead.toDouble() / length * 100.0).toInt()
                        lastReadTime = System.currentTimeMillis()
                        downloadLister.onProgress(progress.toDouble())
                    }
                }

                fileOutputStream.flush()
                fileOutputStream.close()

                if (!isStopped) {
                    downloadLister.onComplete(outputFile)
                }
            } catch (e: Exception) {
                LoggerEdna.error("1 ", e)
                downloadLister.onFileDownloadError(e)
            } finally {
                urlConnection.disconnect()
            }
        } catch (e: Exception) {
            LoggerEdna.error("2 ", e)
            downloadLister.onFileDownloadError(e)
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

    interface DownloadLister {
        fun onProgress(progress: Double)
        fun onComplete(file: File)
        fun onFileDownloadError(e: Exception?)
    }

    companion object {
        @JvmStatic
        fun getDownloadDir(ctx: Context): File {
            return ctx.filesDir
        }

        fun generateFileName(path: String, fileName: String): String {
            val sb = StringBuilder()
            sb.append(getFileName(fileName))
                .append("(")
                .append(Uri.parse(path).lastPathSegment)
                .append(")")
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
