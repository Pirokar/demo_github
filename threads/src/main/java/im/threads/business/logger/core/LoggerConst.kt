package im.threads.business.logger.core

/**
 * Константы для логгера
 */
internal interface LoggerConst {
    companion object {
        const val TAG = "Logger"
        const val DEFAULT_MAX_TOTAL_SIZE = (32 * 1024 * 1024).toLong() // 32mb
        const val DEFAULT_MAX_FILE_COUNT = 24 * 7 // ~7 days of restless logging
    }
}
