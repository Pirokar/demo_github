package im.threads.business.useractivity

import im.threads.business.time.TimeSourceImpl

/**
 * Провайдер времени последней активности пользователя.
 */
object UserActivityTimeProvider {

    private lateinit var instance: UserActivityTime

    fun getLastUserActivityTimeCounter(): UserActivityTime {
        initializeLastUserActivity()
        return instance
    }

    fun initializeLastUserActivity() {
        if (!this::instance.isInitialized) {
            instance = UserActivityTimeImpl(TimeSourceImpl())
        }
    }
}
