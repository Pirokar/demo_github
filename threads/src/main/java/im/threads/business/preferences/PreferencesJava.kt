package im.threads.business.preferences

import im.threads.business.serviceLocator.core.inject

class PreferencesJava {
    private val preferences: Preferences by inject()

    fun getThreadId(): Long? = preferences.get(PreferencesCoreKeys.THREAD_ID)
}
