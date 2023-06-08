package im.threads.business.state

import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatState(private val preferences: Preferences) {

    private var stateChannel = MutableStateFlow(
        preferences.get(PreferencesCoreKeys.CHAT_STATE, ChatStateEnum.LOGGED_OUT) ?: ChatStateEnum.LOGGED_OUT
    )

    var initChatCorrelationId: String = ""

    fun changeState(state: ChatStateEnum) {
        preferences.save(PreferencesCoreKeys.CHAT_STATE, state)
        stateChannel.value = state
    }

    fun getCurrentState() = stateChannel.value

    fun getStateFlow(): StateFlow<ChatStateEnum> = stateChannel

    fun onLogout() {
        changeState(ChatStateEnum.LOGGED_OUT)
    }

    fun isChatReady(): Boolean {
        return getCurrentState() > ChatStateEnum.INIT_USER_SENT
    }
}
