package im.threads.business.logger

import android.content.Context
import android.content.Intent
import im.threads.business.utils.Balloon
import im.threads.business.utils.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Архивирует папку с логами и шэрит в получатель
 */
class LogZipSender(private val context: Context, private val fileProvider: FileProvider) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     * Архивирует папку с логами и шэрит в получатель
     */
    fun shareLogs() {
        val dirPath = LoggerConfig.config?.builder?.dirPath
        if (dirPath != null) {
            coroutineScope.launch {
                val destinationFile = File(context.filesDir, "zippedLogs.zip")
                zipFolder(dirPath, destinationFile.absolutePath)

                withContext(Dispatchers.Main) {
                    shareLogs(destinationFile)
                }
            }
        } else {
            Balloon.show(context, "Cannot send logs. Logs directory is null")
        }
    }

    private fun zipFolder(srcFolder: String, destZipFile: String?) {
        val folder = File(srcFolder)
        if (folder.isDirectory) {
            val files = folder.listFiles()?.map { it.absolutePath }?.toTypedArray()
            if (files != null && destZipFile != null) {
                zip(files, destZipFile)
            }
        }
    }

    private fun zip(filesToZip: Array<String>, zipFileName: String?) {
        val buffer = 80000
        try {
            var origin: BufferedInputStream?
            val dest = FileOutputStream(zipFileName)
            val out = ZipOutputStream(
                BufferedOutputStream(
                    dest
                )
            )
            val data = ByteArray(buffer)
            for (i in filesToZip.indices) {
                LoggerEdna.verbose("Compress", "Adding: ${filesToZip[i]}")
                val inputStream = FileInputStream(filesToZip[i])
                origin = BufferedInputStream(inputStream)
                val entry = ZipEntry(filesToZip[i].substring(filesToZip[i].lastIndexOf("/") + 1))
                out.putNextEntry(entry)
                var count: Int
                while (origin.read(data).also { count = it } != -1) {
                    out.write(data, 0, count)
                }
                origin.close()
            }
            out.close()
            LoggerEdna.info("Logs zipped")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareLogs(file: File) {
        val sharingIntent = Intent(Intent.ACTION_SEND)

        LoggerEdna.info("Sharing zipped logs with path: ${file.absolutePath}")

        if (file.exists()) {
            sharingIntent.type = "application/zip"
            sharingIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            sharingIntent.action = Intent.ACTION_SEND
            sharingIntent.putExtra(Intent.EXTRA_STREAM, fileProvider.getUriForFile(context, file))
            context.startActivity(Intent.createChooser(sharingIntent, "Share file"))
        }
    }
}
