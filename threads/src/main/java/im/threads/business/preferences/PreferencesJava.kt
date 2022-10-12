package im.threads.business.preferences

import im.threads.business.UserInfoBuilder
import im.threads.business.serviceLocator.core.inject

class PreferencesJava {
    private val preferences: Preferences by inject()

    fun save(key: String, value: Any?) {
        preferences.save(key, value)
    }

    fun getUserInfo(): UserInfoBuilder? {
        return preferences.get(PreferencesCoreKeys.USER_INFO)
    }
}
