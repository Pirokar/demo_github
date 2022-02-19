package im.threads.internal.utils.time

/**
 * Реализация интерфейса получения текущего времени.
 *
 * @author Роман Агниев
 * @since 14.02.2022
 */
class TimeSourceImpl : TimeSource {

    override fun getCurrentTime() = System.currentTimeMillis()

}