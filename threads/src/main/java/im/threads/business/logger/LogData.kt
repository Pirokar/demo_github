package im.threads.business.logger

import android.content.Context

/**
 * Контейнер для хранения настроек логов
 */
internal class LogData(builder: Builder) {
    val context: Context?
    val fileName: String?
    val dirPath: String?
    val line: String?
    val time: Long
    val retentionPolicy: LoggerRetentionPolicy
    val maxFileCount: Int
    var maxTotalSize: Long
    var flush: Boolean

    internal class Builder {
        var context: Context? = null
        var fileName: String? = null
        var dirPath: String? = null
        var line: String? = null
        var time: Long = 0
        var retentionPolicy: LoggerRetentionPolicy = LoggerRetentionPolicy.NONE
        var maxFileCount = 0
        var maxTotalSize: Long = 0
        var flush = false

        /**
         * Устанвливает контекст для логгера
         * @param context [android.content.Context]
         */
        fun context(context: Context?): Builder {
            this.context = context
            return this
        }

        /**
         * Устанвливает имя файла для логгера
         * @param fileName абсолютный путь к имени файла
         */
        fun fileName(fileName: String?): Builder {
            this.fileName = fileName
            return this
        }

        /**
         * Устанавливает директорию для логгера
         * @param dirPath абсолютный путь к директории
         */
        fun dirPath(dirPath: String?): Builder {
            this.dirPath = dirPath
            return this
        }

        /**
         * Устанавливает новую строку лога
         * @param line строка лога
         */
        fun line(line: String?): Builder {
            this.line = line
            return this
        }

        /**
         * Устанавливает время лога
         * @param time время лога
         */
        fun time(time: Long): Builder {
            this.time = time
            return this
        }

        /**
         * Устанавливает политику хранения логов в файле в соответствии с [LoggerRetentionPolicy]
         * @param retentionPolicy политика хранения логов, может быть по сессиям или в одном файле
         */
        fun retentionPolicy(retentionPolicy: LoggerRetentionPolicy): Builder {
            this.retentionPolicy = retentionPolicy
            return this
        }

        /**
         * Устанавливает максимальное количество файлов, прежде чем предыдущие файлы начнут удаляться.
         *  Используется только вместе с политикой [LoggerRetentionPolicy.FILE_COUNT]
         *  @param maxFileCount максимальное количество файлов
         */
        fun maxFileCount(maxFileCount: Int): Builder {
            this.maxFileCount = maxFileCount
            return this
        }

        /**
         * Устанавливает максимальный размер файла, прежде чем предыдущие логи начнут удаляться.
         *  Используется только вместе с политикой [LoggerRetentionPolicy.TOTAL_SIZE]
         *  @param maxSize максимальный размер файла в байтах
         */
        fun maxSize(maxSize: Long): Builder {
            maxTotalSize = maxSize
            return this
        }

        /**
         * Устанавливает незамедлительный сброс в файл, минуя буфер
         * @param flush если true, данные не будут копиться в буфере, а будут сразу же записыватсья в файл
         */
        fun flush(flush: Boolean): Builder {
            this.flush = flush
            return this
        }

        /**
         * Возвращает объект [LogData]
         * @return [LogData]
         */
        fun build(): LogData {
            return LogData(this)
        }
    }

    init {
        context = builder.context
        fileName = builder.fileName
        dirPath = builder.dirPath
        line = builder.line
        time = builder.time
        retentionPolicy = builder.retentionPolicy
        maxFileCount = builder.maxFileCount
        maxTotalSize = builder.maxTotalSize
        flush = builder.flush
    }
}
