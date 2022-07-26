package im.threads.internal.domain.logger

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.lang.Thread.UncaughtExceptionHandler
import java.util.Arrays
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

/**
 * Thread, который опрашивает очередь на запись данных в файл
 * @param queue очередь на запись данных
 */
internal class LoggerFileThread(private val queue: BlockingQueue<LogData>) : Thread() {
    private var writer: BufferedWriter? = null
    private var filePath: String? = null
    private var retentionPolicy = LoggerRetentionPolicy.NONE
    private var fileMaxCount = 0
    private var fileMaxSize: Long = 0

    private val fileComparator = java.util.Comparator<File> { object1, object2 ->
        val lm1 = object1.lastModified()
        val lm2 = object2.lastModified()

        if (lm1 < lm2) {
            -1
        } else if (lm1 == lm2) {
            0
        } else {
            1
        }
    }

    override fun run() {
        super.run()
        currentThread().uncaughtExceptionHandler = UncaughtExceptionHandler { _, throwable ->
            throwable.printStackTrace()
            isRunning = false
        }
        try {
            while (true) {
                var log = queue.take()
                logLine(log)
                collectParams(log)
                while (queue.poll(2, TimeUnit.SECONDS).also { log = it } != null) {
                    logLine(log)
                    collectParams(log)
                }
                closeWriter()
                startFilesStoring()
            }
        } catch (e: InterruptedException) {
            LoggerEdna.e("file logger service thread is interrupted", e)
        }
        LoggerEdna.d("file logger service thread stopped")
        isRunning = false
    }

    private fun collectParams(log: LogData) {
        retentionPolicy = log.retentionPolicy
        fileMaxCount = log.maxFileCount
        fileMaxSize = log.maxTotalSize
    }

    private fun logLine(log: LogData) {
        check(!log.fileName.isNullOrBlank()) { "invalid file name: [${log.fileName}]" }
        check(!log.dirPath.isNullOrBlank()) { "invalid directory path: [${log.dirPath}]" }

        if (log.line.isNullOrBlank()) {
            return
        }

        val dir = File(log.dirPath)
        if (!ensureDirIsCorrect(dir)) {
            return
        }

        val file = File(log.dirPath, log.fileName)
        val bufferedWriter = getWriter(file)
        try {
            bufferedWriter.write(log.line)
            bufferedWriter.write("\n")
            if (log.flush) {
                bufferedWriter.flush()
            }
        } catch (e: IOException) {
            LoggerEdna.e(LoggerConst.TAG, e)
        }
    }

    private fun getWriter(file: File): BufferedWriter {
        return if (writer == null || file.absolutePath != filePath) {
            closeWriter()
            ensureFileIsCorrect(file)
            writer = createWriter(file)
            writer!!
        } else {
            writer!!
        }
    }

    private fun createWriter(file: File): BufferedWriter {
        return BufferedWriter(FileWriter(file, true))
    }

    private fun startFilesStoring() {
        if (filePath.isNullOrBlank()) {
            return
        }
        if (retentionPolicy == LoggerRetentionPolicy.FILE_COUNT) {
            storeFilesByCount(fileMaxCount)
        } else if (retentionPolicy == LoggerRetentionPolicy.TOTAL_SIZE) {
            storeFilesBySize(fileMaxSize)
        }
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun storeFilesByCount(maxCount: Int) {
        check(maxCount > 0) { "invalid max file count: $maxCount" }

        val file = File(filePath)
        val dir = file.parentFile ?: return
        val files = dir.listFiles()

        if (files == null || files.size <= maxCount) {
            return
        }

        Arrays.sort(files, fileComparator)

        val deleteCount = files.size - maxCount
        var successCount = 0
        for (i in 0 until deleteCount) {
            if (files[i].delete()) {
                successCount++
            }
        }

        LoggerEdna.d(
            LoggerConst.TAG,
            "house keeping complete: file count [${files.size} -> ${files.size - successCount}]"
        )
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun storeFilesBySize(maxSize: Long) {
        check(maxSize > 0) { "invalid max total size: $maxSize" }

        val file = File(filePath)
        val dir = file.parentFile ?: return
        val files = dir.listFiles() ?: return
        var totalSize: Long = 0

        files.forEach {
            totalSize += it.length()
        }

        if (totalSize <= maxSize) {
            return
        }

        Arrays.sort(files, fileComparator)

        var newSize = totalSize
        for (f in files) {
            val size = f.length()
            if (f.delete()) {
                newSize -= size
                if (newSize <= maxSize) {
                    break
                }
            }
        }

        LoggerEdna.d("house keeping complete: total size [$totalSize -> $newSize]")
    }

    private fun ensureDirIsCorrect(dir: File): Boolean {
        if (dir.exists()) {
            if (dir.isDirectory) {
                return true
            }
            if (!dir.delete()) {
                LoggerEdna.w("failed to delete dir on log path: [${dir.absolutePath}]")
                return false
            }
        }
        if (!dir.mkdir()) {
            LoggerEdna.w("failed to create log dir: [${dir.absolutePath}]")
            return false
        }
        return true
    }

    private fun ensureFileIsCorrect(file: File) {
        check(!(file.exists() && !file.isFile && !file.delete())) {
            "failed to delete dir on log file path: [${file.absolutePath}]"
        }
    }

    private fun closeWriter() {
        try {
            writer?.close()
        } catch (e: IOException) {
            LoggerEdna.e(LoggerConst.TAG, e)
        } finally {
            writer = null
        }
    }

    companion object {
        @Volatile var isRunning = false
    }
}
