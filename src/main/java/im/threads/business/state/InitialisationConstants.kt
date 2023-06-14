package im.threads.business.state

import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.serviceLocator.core.inject

class InitialisationConstants {
    companion object {
        private val preferences: Preferences by inject()

        var chatState: ChatState
            get() {
                return preferences.get(PreferencesCoreKeys.CHAT_STATE, ChatState.LOGGED_OUT) ?: ChatState.LOGGED_OUT
            }
            set(value) {
                preferences.save(PreferencesCoreKeys.CHAT_STATE, value)
            }
        var isHistoryLoaded = false
        private val isDeviceRegistered: Boolean
            get() {
                return !preferences.get<String>(PreferencesCoreKeys.DEVICE_ADDRESS).isNullOrBlank()
            }

        fun onLogout() {
            chatState = ChatState.LOGGED_OUT
            isHistoryLoaded = false
        }

        fun isChatReady(): Boolean {
            return chatState > ChatState.LOGGED_OUT && isDeviceRegistered
        }
    }
}
