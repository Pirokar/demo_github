package im.threads.internal.domain.logger

import android.util.SparseArray

/**
 * Представляет собой уровни логгирования, соответствуют [android.util.Log]
 */
enum class LoggerLevel(val value: Int) {
    VERBOSE(0),
    DEBUG(1),
    INFO(2),
    WARNING(3),
    ERROR(4);

    companion object {
        private val levelNames = object : SparseArray<String>(5) {
            init {
                append(LoggerLevel.VERBOSE.value, "verbose")
                append(LoggerLevel.DEBUG.value, "debug")
                append(LoggerLevel.INFO.value, "info")
                append(LoggerLevel.WARNING.value, "warning")
                append(LoggerLevel.ERROR.value, "error")
            }
        }

        fun getLevelName(level: LoggerLevel): String = levelNames[level.value]
    }
}
