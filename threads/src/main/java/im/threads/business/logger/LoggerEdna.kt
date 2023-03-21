package im.threads.business.logger

/**
 * Логгер, поддерживающий вывод логов в файл и текущую строку кода
 */
object LoggerEdna {
    @Volatile
    @JvmStatic
    private var logSender: LogSender? = null

    /**
     * Инициализация логгера
     * @param config [LoggerConfig] параметры логгера
     */
    @JvmStatic
    fun init(config: LoggerConfig) {
        LoggerConfig.config = config
        logSender = LogSender(config)
    }

    /**
     * VERBOSE log сообщение
     * @param log текст лога
     */
    @JvmStatic
    fun verbose(log: String) {
        verbose(null, log)
    }

    /**
     * VERBOSE log сообщение
     * @param log текст лога
     * @param tag используется для идентификации источника сообщения
     */
    @JvmStatic
    fun verbose(tag: String?, log: String) {
        logSender?.send(LoggerLevel.VERBOSE, tag, log)
    }

    /**
     * DEBUG log сообщение
     * @param log текст лога
     */
    @JvmStatic
    fun debug(log: String) {
        debug(null, log)
    }

    /**
     * DEBUG log сообщение
     * @param log текст лога
     * @param tag используется для идентификации источника сообщения
     */
    @JvmStatic
    fun debug(tag: String?, log: String) {
        logSender?.send(LoggerLevel.DEBUG, tag, log)
    }

    /**
     * INFO log сообщение
     * @param log текст лога
     */
    @JvmStatic
    fun info(log: String) {
        logSender?.send(LoggerLevel.INFO, null, log)
    }

    /**
     * INFO log сообщение
     * @param log текст лога
     * @param tag используется для идентификации источника сообщения
     */
    @JvmStatic
    fun info(tag: String?, log: String) {
        logSender?.send(LoggerLevel.INFO, tag, log)
    }

    /**
     * WARNING log сообщение
     * @param log текст лога
     */
    @JvmStatic
    fun warning(log: String) {
        warning(null, log)
    }

    /**
     * WARNING log сообщение
     * @param log текст лога
     * @param tag используется для идентификации источника сообщения
     */
    @JvmStatic
    fun warning(tag: String?, log: String) {
        logSender?.send(LoggerLevel.WARNING, tag, log)
    }

    /**
     * ERROR log сообщение
     * @param log текст лога
     */
    @JvmStatic
    fun error(log: String) {
        error(null, log)
    }

    /**
     * ERROR log сообщение
     * @param log текст лога
     * @param tag используется для идентификации источника сообщения
     */
    @JvmStatic
    fun error(tag: String?, log: String) {
        logSender?.send(LoggerLevel.ERROR, tag, log)
    }

    /**
     * ERROR log сообщение
     * @param throwable exception, вызвавший данную ошибку
     */
    @JvmStatic
    fun error(throwable: Throwable?) {
        error(null, throwable)
    }

    /**
     * ERROR log сообщение
     * @param log текст лога
     * @param throwable exception, вызвавший данную ошибку
     */
    @JvmStatic
    fun error(log: String?, throwable: Throwable?) {
        error(null, log, throwable)
    }

    /**
     * ERROR log сообщение
     * @param log текст лога
     * @param tag используется для идентификации источника сообщения
     * @param throwable exception, вызвавший данную ошибку
     */
    @JvmStatic
    fun error(tag: String?, log: String?, throwable: Throwable?) {
        logSender?.send(LoggerLevel.ERROR, tag, log, throwable)
    }
}
