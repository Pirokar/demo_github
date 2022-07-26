package im.threads.internal.domain.logger

import android.util.SparseArray

/**
 * Представляет собой уровни логгирования, соответствуют [android.util.Log]
 */
enum class LoggerLevel(val value: Int) {
    V(0),
    D(1),
    I(2),
    W(3),
    E(4);

    companion object {
        private val levelNames = object : SparseArray<String>(5) {
            init {
                append(LoggerLevel.V.value, "V")
                append(LoggerLevel.D.value, "D")
                append(LoggerLevel.I.value, "I")
                append(LoggerLevel.W.value, "W")
                append(LoggerLevel.E.value, "E")
            }
        }

        fun getLevelName(level: LoggerLevel): String = levelNames[level.value]
    }
}
