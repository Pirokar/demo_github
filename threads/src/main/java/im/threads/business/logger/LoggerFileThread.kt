package im.threads.business.logger

import android.util.Log
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
    private val queueBuffer = ArrayList<LogData>()
    private val delayBetweenCheckMs = 3000L
    private val maxQueueListSize = 50
    private val tag = LoggerFileThread::class.simpleName

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

    private val logComparator = java.util.Comparator<LogData> { data1, data2 ->
        if (data1.time < data2.time) {
            -1
        } else if (data1.time == data2.time) {
            0
        } else {
            1
        }
    }

    override fun run() {
        Log.d(tag, "started")
        super.run()
        Log.d(tag, "super call passed")
        currentThread().uncaughtExceptionHandler = UncaughtExceptionHandler { _, throwable ->
            throwable.printStackTrace()
            isRunning = false
        }
        try {
            while (true) {
                Log.d(tag, "getting log from queue")
                var log = queue.take()
                queueBuffer.add(log)
                collectParams(log)
                while (queue.poll(2, TimeUnit.SECONDS).also { log = it } != null) {
                    Log.d(tag, "getting log from queue with 2 seconds")
                    queueBuffer.add(log)
                    checkQueueList()
                    collectParams(log)
                }
                closeWriter()
                startFilesStoring()
            }
        } catch (e: InterruptedException) {
            LoggerEdna.error("file logger service thread is interrupted", e)
        }
        LoggerEdna.debug("file logger service thread stopped")
        isRunning = false
    }

    private fun checkQueueList() {
        val currentTime = System.currentTimeMillis()
        val maxSizeReached = queueBuffer.size > maxQueueListSize
        val maxTimeReached = currentTime - queueBuffer[queueBuffer.lastIndex].time > delayBetweenCheckMs
        val isFlush = queueBuffer[queueBuffer.lastIndex].flush

        if (maxSizeReached || maxTimeReached || isFlush) {
            Log.d(tag, "writing logs to file")
            queueBuffer.sortWith(logComparator)
            queueBuffer.indices.forEach {
                val value = queueBuffer[it]
                if (it == queueBuffer.lastIndex) {
                    value.flush = true
                }
                logLine(value)
            }
            queueBuffer.clear()
        }
    }

    private fun collectParams(log: LogData) {
        Log.d(tag, "collecting params")

        retentionPolicy = log.retentionPolicy
        fileMaxCount = log.maxFileCount
        fileMaxSize = log.maxTotalSize
    }

    private fun logLine(log: LogData) {
        Log.d(tag, "logging line")

        check(!log.fileName.isNullOrBlank()) { "invalid file name: [${log.fileName}]" }
        check(!log.dirPath.isNullOrBlank()) { "invalid directory path: [${log.dirPath}]" }

        if (log.line.isNullOrBlank()) {
            Log.d(tag, "line is empty")
            return
        }

        val dir = File(log.dirPath)
        if (!ensureDirIsCorrect(dir)) {
            Log.d(tag, "dir is incorrect")
            return
        }

        val file = File(log.dirPath, log.fileName)
        val bufferedWriter = getWriter(file)
        try {
            Log.d(tag, "adding line to file")
            bufferedWriter.write(log.line)
            bufferedWriter.write("\n")
            if (log.flush) {
                Log.d(tag, "flushing to file")
                bufferedWriter.flush()
            }
        } catch (e: IOException) {
            LoggerEdna.error(LoggerConst.TAG, e)
        }
    }

    private fun getWriter(file: File): BufferedWriter {
        return if (writer == null || file.absolutePath != filePath) {
            Log.d(tag, "creating writer")
            closeWriter()
            ensureFileIsCorrect(file)
            writer = createWriter(file)
            writer!!
        } else {
            writer!!
        }
    }

    private fun createWriter(file: File): BufferedWriter {
        Log.d(tag, "closing writer")
        return BufferedWriter(FileWriter(file, true))
    }

    private fun startFilesStoring() {
        if (filePath.isNullOrBlank()) {
            Log.d(tag, "filePath is null")
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
        Log.d(tag, "storeFilesByCount")
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
        Log.d(tag, "storeFilesByCount, $successCount deleted files")

        LoggerEdna.debug(
            LoggerConst.TAG,
            "house keeping complete: file count [${files.size} -> ${files.size - successCount}]"
        )
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun storeFilesBySize(maxSize: Long) {
        Log.d(tag, "storeFilesBySize")
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
                Log.d(tag, "storeFilesBySize, file is deleted")
                newSize -= size
                if (newSize <= maxSize) {
                    break
                }
            }
        }

        LoggerEdna.debug("house keeping complete: total size [$totalSize -> $newSize]")
    }

    private fun ensureDirIsCorrect(dir: File): Boolean {
        if (dir.exists()) {
            Log.d(tag, "dir exists")
            if (dir.isDirectory) {
                Log.d(tag, "dir is directory")
                return true
            }
            if (!dir.delete()) {
                LoggerEdna.warning("failed to delete dir on log path: [${dir.absolutePath}]")
                return false
            }
        }
        if (!dir.mkdir()) {
            LoggerEdna.warning("failed to create log dir: [${dir.absolutePath}]")
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
        Log.d(tag, "closing writer")
        try {
            writer?.close()
        } catch (e: IOException) {
            LoggerEdna.error(LoggerConst.TAG, e)
        } finally {
            writer = null
        }
    }

    companion object {
        @Volatile var isRunning = false
    }
}
