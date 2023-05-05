package im.threads.ui.utils

class InitialisationConstants {
    companion object {
        var isLogoutHappened = true
        var isHistoryLoaded = false
        var isDeviceRegistered = false
        var isInitChatSent = false
        var isEnvironmentMessageSent = false

        fun onLogout() {
            isLogoutHappened = true
            isHistoryLoaded = false
            isDeviceRegistered = false
            isInitChatSent = false
            isEnvironmentMessageSent = false
        }

        fun isChatReady(): Boolean {
            return !isLogoutHappened && isDeviceRegistered && isInitChatSent && isEnvironmentMessageSent
        }

        fun isChatReadyAndHistoryLoaded(): Boolean {
            return isChatReady() && isHistoryLoaded
        }
    }
}
