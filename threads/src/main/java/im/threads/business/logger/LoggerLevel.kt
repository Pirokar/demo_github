package im.threads.business.logger

import android.util.SparseArray

/**
 * Представляет собой уровни логгирования, соответствуют [android.util.Log]
 */
enum class LoggerLevel(val value: Int) {
    VERBOSE(0),
    DEBUG(1),
    INFO(2),
    WARNING(3),
    ERROR(4),
    FLUSH(5);

    companion object {
        private val levelNames = object : SparseArray<String>(5) {
            init {
                append(VERBOSE.value, "verbose")
                append(DEBUG.value, "debug")
                append(INFO.value, "info")
                append(WARNING.value, "warning")
                append(ERROR.value, "error")
                append(FLUSH.value, "flush")
            }
        }

        fun getLevelName(level: LoggerLevel): String = levelNames[level.value]
    }
}