package im.threads.business.logger

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
class LogZipSender(private val context: Context) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     * Архивирует папку с логами и шэрит в получатель
     */
    fun shareLogs() {
        val dirPath = LoggerConfig.config?.builder?.dirPath
        if (dirPath != null) {
            coroutineScope.launch {
                val destPath = File(context.filesDir, "logsZip")
                if (!destPath.exists()) {
                    destPath.mkdir()
                }
                val destinationFile = "${destPath.absolutePath}/zippedLogs.zip"
                zipFolder(dirPath, destinationFile)

                withContext(Dispatchers.Main) {
                    shareLogs(destinationFile)
                }
            }
        } else {
            Toast.makeText(
                context,
                "Cannot send logs. Logs directory is null",
                Toast.LENGTH_SHORT
            ).show()
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareLogs(path: String) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        val sharingUri = Uri.parse(path)

        sharingIntent.type = "application/zip"
        sharingIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        sharingIntent.putExtra(Intent.EXTRA_STREAM, sharingUri)
        context.startActivity(Intent.createChooser(sharingIntent, "Share file"))
    }
}
