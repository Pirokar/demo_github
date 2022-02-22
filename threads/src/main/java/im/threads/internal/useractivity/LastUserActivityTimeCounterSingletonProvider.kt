package im.threads.internal.useractivity

import im.threads.internal.utils.time.TimeSourceImpl

/**
 * Провайдер синглтона счётчика времени последней активности пользователя.
 *
 * @author Роман Агниев
 * @since 14.02.2022
 */
object LastUserActivityTimeCounterSingletonProvider {

    private lateinit var instance: LastUserActivityTimeCounter

    fun getLastUserActivityTimeCounter(): LastUserActivityTimeCounter {
        if (!this::instance.isInitialized) {
            instance = LastUserActivityTimeCounterImpl(TimeSourceImpl())
        }
        return instance
    }
}