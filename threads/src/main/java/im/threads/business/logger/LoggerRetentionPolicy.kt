package im.threads.business.logger

/**
 * Политика хранения лог данных в файле
 */
enum class LoggerRetentionPolicy(val value: Int) {
    /**
     * Дефолтное значение, при включенном хранении в файл должно быть изменено на
     *  [FILE_COUNT] или [TOTAL_SIZE]
     */
    NONE(0),

    /**
     * Хранит логи по сессиям приложения. Файлов может быть несколько, регулирутся через [LoggerConfig]
     *  Имя файла начинается с времени первого лога
     */
    FILE_COUNT(1),

    /**
     * Хранит логи в одном файле. Размер файла решулируется через [LoggerConfig]
     *  Имя файла начинается с времени первого лога
     */
    TOTAL_SIZE(2);
}