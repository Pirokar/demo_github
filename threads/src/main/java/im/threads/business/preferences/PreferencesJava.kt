package im.threads.business.preferences

import im.threads.business.UserInfoBuilder
import im.threads.business.serviceLocator.core.inject

class PreferencesJava {
    private val preferences: Preferences by inject()

    fun getUserInfo(): UserInfoBuilder? = preferences.get(PreferencesCoreKeys.USER_INFO)

    fun getThreadId(): Long? = preferences.get(PreferencesCoreKeys.THREAD_ID)

    fun setThreadId(threadId: Long?) {
        preferences.save(PreferencesCoreKeys.THREAD_ID, threadId)
    }
}
