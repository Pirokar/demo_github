package im.threads.internal.domain.logger

import android.content.Context
import im.threads.internal.domain.logger.LoggerEdna.debug
import im.threads.internal.domain.logger.LoggerEdna.warning
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingDeque

internal class FileLogger {
    internal object InstanceHolder {
        val INSTANCE = FileLogger()
    }

    private val logQueue: BlockingQueue<LogData> = LinkedBlockingDeque()

    fun logFile(
        context: Context?,
        fileName: String?,
        dirPath: String?,
        line: String?,
        retentionPolicy: LoggerRetentionPolicy,
        maxFileCount: Int,
        maxTotalSize: Long,
        flush: Boolean
    ) {
        ensureThread()
        val addResult = logQueue.offer(
            LogData.Builder().context(context)
                .fileName(fileName)
                .dirPath(dirPath)
                .line(line)
                .retentionPolicy(retentionPolicy)
                .maxFileCount(maxFileCount)
                .maxSize(maxTotalSize)
                .flush(flush)
                .build()
        )
        if (!addResult) {
            warning("failed to add to file logger service queue")
        }
    }

    private fun ensureThread() {
        if (!LoggerFileThread.isRunning) {
            synchronized(this) {
                if (!LoggerFileThread.isRunning) {
                    LoggerFileThread.isRunning = true
                    debug("start file logger thread")
                    LoggerFileThread(logQueue).start()
                }
            }
        }
    }

    companion object {
        fun instance(): FileLogger {
            return InstanceHolder.INSTANCE
        }
    }
}
