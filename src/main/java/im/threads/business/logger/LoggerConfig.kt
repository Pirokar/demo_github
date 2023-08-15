package im.threads.business.logger

import android.content.Context
import android.util.Log
import java.io.File

/**
 * Конфигурация для логгера
 */
class LoggerConfig(val builder: Builder) {
    class Builder(context: Context) {
        internal val context: Context = context.applicationContext
        internal val logger = LogcatLogger()
        internal val formatter = LoggerFileFormatter()

        internal var dirPath: String? = null
        internal var fileName: String? = null
        internal var minLevel = LoggerLevel.VERBOSE
        internal var logToFile = false
        internal var retentionPolicy = LoggerRetentionPolicy.FILE_COUNT
        internal var maxFileCount = LoggerConst.DEFAULT_MAX_FILE_COUNT
        internal var maxSize = LoggerConst.DEFAULT_MAX_TOTAL_SIZE

        /**
         * Устанавливает директорию для хранения логов
         * @param dir директория хранения файла
         * @return [Builder]
         */
        fun dir(dir: File?): Builder {
            dirPath = if (dir == null || dir.name.isEmpty()) {
                File(context.filesDir, "logs").absolutePath
            } else {
                dir.absolutePath
            }
            return this
        }

        /**
         * Вместо автоматически генерируемого имени файла можно установить фиксированное имя.
         *  Используется только вместе с [LoggerRetentionPolicy.TOTAL_SIZE]
         *  @param fileName имя файла
         *  @return [Builder]
         */
        fun fileName(fileName: String): Builder {
            if (fileName.isNotEmpty()) {
                this.fileName = fileName
            }
            return this
        }

        /**
         * Определеяет минимальный уровень для логов. По умолчанию [LoggerLevel.VERBOSE].
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

    override fun toString(): String {
        return "Logger config:\n" +
            "  dirPath: ${builder.dirPath}\n" +
            "  fileName: ${builder.fileName}\n" +
            "  minLevel: ${builder.minLevel}\n" +
            "  logToFile: ${builder.logToFile}\n" +
            "  retentionPolicy: ${builder.retentionPolicy.name}\n" +
            "  maxFileCount: ${builder.maxFileCount}\n" +
            "  maxSize: ${builder.maxSize},"
    }

    companion object {
        var config: LoggerConfig? = null
    }
}
