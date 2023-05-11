package im.threads.ui.utils

import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.serviceLocator.core.inject

class InitialisationConstants {
    companion object {
        private val preferences: Preferences by inject()

        var isLogoutHappened: Boolean
            get() {
                return preferences.get(PreferencesCoreKeys.LOGOUT_HAPPENED, true) ?: true
            }
            set(value) {
                preferences.save(PreferencesCoreKeys.LOGOUT_HAPPENED, value)
            }
        var isHistoryLoaded = false
        private val isDeviceRegistered: Boolean
            get() {
                return !preferences.get<String>(PreferencesCoreKeys.DEVICE_ADDRESS).isNullOrBlank()
            }

        fun onLogout() {
            isLogoutHappened = true
            isHistoryLoaded = false
        }

        fun isChatReady(): Boolean {
            return !isLogoutHappened && isDeviceRegistered
        }

        fun isChatReadyAndHistoryLoaded(): Boolean {
            return isChatReady() && isHistoryLoaded
        }
    }
}
