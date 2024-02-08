package im.threads.business.logger

import im.threads.business.extensions.fullLogString

internal class LogSender(private val loggerConfig: LoggerConfig?) {
    private val currentPackageName = this.javaClass.`package`?.name
        ?: "im.threads.internal.domain.logger"

    fun send(
        level: LoggerLevel,
        tag: String?,
        log: String?,
        throwable: Throwable? = null
    ) {
        if (level == LoggerLevel.ERROR) {
            log(
                level,
                tag,
                getErrorString(log, throwable)
            )
        } else {
            log(level, tag, log ?: "")
        }
    }

    private fun log(
        level: LoggerLevel,
        tag: String?,
        log: String
    ) {
        loggerConfig?.let { config ->
            if (isMinLevelDoesNotMatch(config, level)) return

            val currentTag = getCurrentTag(tag, level)

            logToConsole(config, level, currentTag, log)
            logToFile(config, level, currentTag, log)
        }
    }

    private fun logToConsole(
        config: LoggerConfig,
        level: LoggerLevel,
        currentTag: String,
        log: String
    ) {
        val logger = config.builder.logger
        when (level) {
            LoggerLevel.VERBOSE -> logger.v(currentTag, log)
            LoggerLevel.DEBUG -> logger.d(currentTag, log)
            LoggerLevel.INFO -> logger.i(currentTag, log)
            LoggerLevel.WARNING -> logger.w(currentTag, log)
            LoggerLevel.ERROR -> logger.e(currentTag, log)
            else -> {}
        }
    }

    private fun logToFile(
        config: LoggerConfig,
        level: LoggerLevel,
        currentTag: String,
        log: String
    ) {
        if (config.builder.logToFile && !config.builder.dirPath.isNullOrBlank()) {
            val timeMs = System.currentTimeMillis()
            val fileName = config.builder.fileName
                ?: config.builder.formatter.formatFileName(timeMs)
            val line = config.builder.formatter.formatLine(
                timeMs,
                LoggerLevel.getLevelName(level),
                currentTag,
                log
            )
            val isFlush = level >= LoggerLevel.ERROR
            FileLogger.instance().logFile(
                config.builder.context,
                fileName,
                config.builder.dirPath,
                line,
                timeMs,
                config.builder.retentionPolicy,
                config.builder.maxFileCount,
                config.builder.maxSize,
                isFlush
            )
        }
    }

    private fun getErrorString(log: String?, throwable: Throwable?): String {
        val stringBuilder = StringBuilder()

        if (!log.isNullOrBlank()) {
            stringBuilder.append(log)
        }

        if (throwable != null) {
            stringBuilder.append(throwable.fullLogString())
        }

        return stringBuilder.toString()
    }

    private fun getLineName(level: LoggerLevel): String {
        val stackTrace = Thread.currentThread().stackTrace
        val stackTraceElementIdx = getLineIndex(stackTrace)

        return if (stackTraceElementIdx > -1) {
            val stackTraceElement = stackTrace[stackTraceElementIdx]
            val classNameParts = stackTraceElement.className.split(".")
            val className = classNameParts[classNameParts.lastIndex]

            "[$className:${stackTraceElement.lineNumber}]"
        } else {
            "Line is undefined"
        }
    }

    private fun getLineIndex(stackTrace: Array<StackTraceElement>): Int {
        val lastIndexOfExtraThread = stackTrace.indexOfLast {
            it.toString().contains(currentPackageName)
        }
        return if (lastIndexOfExtraThread == stackTrace.size - 1) {
            -1
        } else {
            lastIndexOfExtraThread + 1
        }
    }

    private fun getCurrentTag(tag: String?, level: LoggerLevel): String {
        val currentTag = tag ?: ""
        return if (currentTag.isBlank()) {
            "ELog ${getLineName(level)}"
        } else {
            "${currentTag.trim()} ELog ${getLineName(level)}"
        }
    }

    private fun isMinLevelDoesNotMatch(config: LoggerConfig, level: LoggerLevel): Boolean {
        return level < config.builder.minLevel
    }
}
