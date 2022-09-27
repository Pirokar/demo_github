package im.threads.business.useractivity

import im.threads.business.time.TimeSource

/**
 * Реализация счётчика времени от последней активности пользователя.
 *
 * @constructor создание реализации счётчика
 * @param timeSource интерфейс для получения текущего времени
 */
class UserActivityTimeImpl(
    private val timeSource: TimeSource
) : UserActivityTime {

    private var lastActivityTime = timeSource.getCurrentTime()

    override fun updateLastUserActivityTime() {
        lastActivityTime = timeSource.getCurrentTime()
    }

    override fun getSecondsSinceLastActivity(): Long {
        return (timeSource.getCurrentTime() - lastActivityTime) / MS_IN_SECOND
    }

    companion object {
        private const val MS_IN_SECOND = 1000
    }
}
