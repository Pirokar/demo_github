package im.threads.internal.utils.time

/**
 * Интерфейс для получения текущего времени.
 *
 * @author Роман Агниев
 * @since 14.02.2022
 */
interface TimeSource {

    /** Получить текущее время в миллисекундах. */
    fun getCurrentTime(): Long
}
