package im.threads.internal.useractivity

/**
 * Счётчик времени от последней активности пользователя.
 *
 * @author Роман Агниев
 * @since 14.02.2022
 */
interface LastUserActivityTimeCounter {

    /** Обновить счётчик последней активности. */
    fun updateLastUserActivityTime()

    /** Получить время в секундах с момента последней активности. */
    fun getSecondsSinceLastActivity(): Long
}
