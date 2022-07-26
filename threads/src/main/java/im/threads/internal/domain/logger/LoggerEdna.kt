package im.threads.internal.domain.logger

import android.util.Log

/**
 * Логгер, поддерживающий вывод логов в файл и текущую строку кода
 */
object LoggerEdna {
    @Volatile
    @JvmStatic
    private var loggerConfig: LoggerConfig? = null

    /**
     * Инициализация логгера
     * @param config [LoggerConfig] параметры логгера
     */
    @JvmStatic
    fun init(config: LoggerConfig) {
        loggerConfig = config
    }

    /**
     * VERBOSE log сообщение
     * @param log текст лога
     */
    @JvmStatic
    fun v(log: String) {
        v(null, log)
    }

    /**
     * VERBOSE log сообщение
     * @param log текст лога
     * @param tag используется для идентификации источника сообщения
     */
    @JvmStatic
    fun v(tag: String?, log: String) {
        log(LoggerLevel.V, tag, log)
    }

    /**
     * DEBUG log сообщение
     * @param log текст лога
     */
    @JvmStatic
    fun d(log: String) {
        d(null, log)
    }

    /**
     * DEBUG log сообщение
     * @param log текст лога
     * @param tag используется для идентификации источника сообщения
     */
    @JvmStatic
    fun d(tag: String?, log: String) {
        log(LoggerLevel.D, tag, log)
    }

    /**
     * INFO log сообщение
     * @param log текст лога
     */
    @JvmStatic
    fun i(log: String) {
        log(LoggerLevel.I, null, log)
    }

    /**
     * INFO log сообщение
     * @param log текст лога
     * @param tag используется для идентификации источника сообщения
     */
    @JvmStatic
    fun i(tag: String?, log: String) {
        log(LoggerLevel.I, tag, log)
    }

    /**
     * WARNING log сообщение
     * @param log текст лога
     */
    @JvmStatic
    fun w(log: String) {
        w(null, log)
    }

    /**
     * WARNING log сообщение
     * @param log текст лога
     * @param tag используется для идентификации источника сообщения
     */
    @JvmStatic
    fun w(tag: String?, log: String) {
        log(LoggerLevel.W, tag, log)
    }

    /**
     * ERROR log сообщение
     * @param log текст лога
     */
    @JvmStatic
    fun e(log: String) {
        e(null, log)
    }

    /**
     * ERROR log сообщение
     * @param log текст лога
     * @param tag используется для идентификации источника сообщения
     */
    @JvmStatic
    fun e(tag: String?, log: String) {
        log(LoggerLevel.E, tag, log)
    }

    /**
     * ERROR log сообщение
     * @param throwable exception, вызвавший данную ошибку
     */
    @JvmStatic
    fun e(throwable: Throwable?) {
        e(null, throwable)
    }

    /**
     * ERROR log сообщение
     * @param log текст лога
     * @param throwable exception, вызвавший данную ошибку
     */
    @JvmStatic
    fun e(log: String?, throwable: Throwable?) {
        e(null, log, throwable)
    }

    /**
     * ERROR log сообщение
     * @param log текст лога
     * @param tag используется для идентификации источника сообщения
     * @param throwable exception, вызвавший данную ошибку
     */
    @JvmStatic
    fun e(tag: String?, log: String?, throwable: Throwable?) {
        val stringBuilder = StringBuilder()

        if (!log.isNullOrBlank()) {
            stringBuilder.append(log)
            stringBuilder.append("\n")
        }

        if (throwable != null) {
            stringBuilder.append(Log.getStackTraceString(throwable))
        }

        log(LoggerLevel.E, tag, stringBuilder.toString())
    }

    private fun log(level: LoggerLevel, tag: String?, log: String) {
        loggerConfig?.let { config ->
            var currentTag = tag ?: ""
            if (level < config.builder.minLevel) {
                return
            }
            currentTag = if (currentTag.isBlank()) {
                "EdnaLogger ${getLineName(level)}"
            } else {
                "${currentTag.trim()} EdnaLogger ${getLineName(level)}"
            }
            val logger = config.builder.logger
            when (level) {
                LoggerLevel.V -> logger.v(currentTag, log)
                LoggerLevel.D -> logger.d(currentTag, log)
                LoggerLevel.I -> logger.i(currentTag, log)
                LoggerLevel.W -> logger.w(currentTag, log)
                LoggerLevel.E -> logger.e(currentTag, log)
            }
            if (config.builder.logToFile && !config.builder.dirPath.isNullOrBlank()) {
                val timeMs = System.currentTimeMillis()
                val fileName = config.builder.formatter.formatFileName(timeMs)
                val line = config.builder.formatter.formatLine(
                    timeMs,
                    LoggerLevel.getLevelName(level),
                    currentTag,
                    log
                )
                val isFlush = level === LoggerLevel.E
                FileLogger.instance().logFile(
                    config.builder.context,
                    fileName,
                    config.builder.dirPath,
                    line,
                    config.builder.retentionPolicy,
                    config.builder.maxFileCount,
                    config.builder.maxSize,
                    isFlush
                )
            }
        }
    }

    private fun getLineName(level: LoggerLevel): String {
        val stackTraceElement = when (level) {
            LoggerLevel.E -> Thread.currentThread().stackTrace[6]
            else -> Thread.currentThread().stackTrace[5]
        }

        val classNameParts = stackTraceElement.className.split(".")
        val className = classNameParts[classNameParts.lastIndex]

        return "[$className:${stackTraceElement.lineNumber}]"
    }
}
