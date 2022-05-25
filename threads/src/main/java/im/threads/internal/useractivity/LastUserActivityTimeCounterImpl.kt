package im.threads.internal.useractivity

import im.threads.internal.utils.time.TimeSource

/**
 * Реализация счётчика времени от последней активности пользователя.
 *
 * @constructor создание реализации счётчика
 * @param timeSource интерфейс для получения текущего времени
 *
 * @author Роман Агниев
 * @since 14.02.2022
 */
class LastUserActivityTimeCounterImpl(
    private val timeSource: TimeSource
) : LastUserActivityTimeCounter {

    private var lastActivityTime = timeSource.getCurrentTime()

    override fun updateLastUserActivityTime() {
        lastActivityTime = timeSource.getCurrentTime()
    }

    override fun getSecondsSinceLastActivity() =
        (timeSource.getCurrentTime() - lastActivityTime) / MS_IN_SECOND

    companion object {
        private const val MS_IN_SECOND = 1000
    }
}
