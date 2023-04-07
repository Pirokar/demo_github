package im.threads.business.time

/**
 * Реализация интерфейса получения текущего времени.
 */
class TimeSourceImpl : TimeSource {
    override fun getCurrentTime() = System.currentTimeMillis()
}
