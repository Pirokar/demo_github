package im.threads.business.useractivity

/**
 * Счётчик времени от последней активности пользователя.
 */
interface UserActivityTime {

    /** Обновить счётчик последней активности. */
    fun updateLastUserActivityTime()

    /** Получить время в секундах с момента последней активности. */
    fun getSecondsSinceLastActivity(): Long
}
