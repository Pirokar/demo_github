package im.threads.internal.domain.logger

import android.content.Context
import android.util.Log
import java.io.File

/**
 * Конфигурация для логгера
 */
class LoggerConfig(val builder: Builder) {
    class Builder(context: Context) {
        @JvmField
        internal val context: Context = context.applicationContext
        internal val logger = LogcatLogger()
        internal val formatter = LoggerFileFormatter()

        @JvmField
        internal var dirPath: String? = null
        internal var defaultTag: String? = null
        internal var minLevel = LoggerLevel.V
        internal var logToFile = false
        internal var retentionPolicy = LoggerRetentionPolicy.FILE_COUNT
        internal var maxFileCount = LoggerConst.DEFAULT_MAX_FILE_COUNT

        @JvmField
        internal var maxSize = LoggerConst.DEFAULT_MAX_TOTAL_SIZE

        /**
         * Устанавливает директорию для хранения логов
         * @param dir директория хранения файла
         * @return [Builder]
         */
        fun dir(dir: File?): Builder {
            if (dir != null) {
                dirPath = dir.absolutePath
            }
            return this
        }

        /**
         * Определеяет минимальный уровень для логов. По умолчанию [LoggerLevel.V].
         * @param level минимальный уровень для логов
         * @return [Builder]
         */
        fun minLogLevel(level: LoggerLevel): Builder {
            minLevel = level
            return this
        }

        /**
         * Включает логгирование в файл.
         * @return [Builder]
         */
        fun logToFile(): Builder {
            logToFile = true
            return this
        }

        /**
         * Определяет, что происходяит с лог файлами, когда превышен лимит.
         * Поддерживается лимит по количеству файлов или по размеру файла.
         * @param retentionPolicy Значения соответствуют [LoggerRetentionPolicy]
         * @return [Builder]
         */
        fun retentionPolicy(retentionPolicy: LoggerRetentionPolicy): Builder {
            this.retentionPolicy = retentionPolicy
            return this
        }

        /**
         * Определяет максимальное количество файлов для хранения.
         * @param maxFileCount максимальное количество файлов для хранения в папке логов
         * @return [Builder]
         */
        fun maxFileCount(maxFileCount: Int): Builder {
            this.maxFileCount = maxFileCount
            return this
        }

        /**
         * Определяет максимальный размер файла в байтах перед тем, как начнут удаляться старые логи.
         * @param maxSize максимальный размер файла
         * @return [Builder]
         */
        fun maxTotalSize(maxSize: Long): Builder {
            this.maxSize = maxSize
            return this
        }

        /**
         * Собирает билд с указанными параметрами логгера
         * @return [LoggerConfig]
         */
        fun build(): LoggerConfig {
            if (logToFile) {
                if (dirPath.isNullOrEmpty()) {
                    val dir = context.getExternalFilesDir("log")
                    if (dir != null) {
                        dirPath = dir.absolutePath
                    } else {
                        Log.e(LoggerConst.TAG, "failed to get log file directory")
                    }
                }

                when (retentionPolicy) {
                    LoggerRetentionPolicy.FILE_COUNT -> require(maxFileCount > 0) {
                        "max file count must be > 0"
                    }
                    LoggerRetentionPolicy.TOTAL_SIZE -> require(maxSize > 0) {
                        "max total size must be > 0"
                    }
                    LoggerRetentionPolicy.NONE -> require(false) {
                        "Retention policy must be FILE_COUNT or TOTAL_SIZE"
                    }
                }
            }

            return LoggerConfig(this)
        }
    }
}
