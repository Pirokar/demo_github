package im.threads.business.time

/**
 * Интерфейс для получения текущего времени.
 */
interface TimeSource {

    /** Получить текущее время в миллисекундах. */
    fun getCurrentTime(): Long
}
