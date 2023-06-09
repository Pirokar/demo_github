package im.threads.business.state

import im.threads.business.config.BaseConfig
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ChatState(private val preferences: Preferences) {
    private val timeout = try {
        BaseConfig.instance.requestConfig.socketClientSettings.connectTimeoutMillis
    } catch (exc: Exception) {
        10000L
    }

    private var coroutineScope: CoroutineScope? = null

    private var stateChannel = MutableStateFlow(
        ChatStateEvent(
            preferences.get(PreferencesCoreKeys.CHAT_STATE, ChatStateEnum.LOGGED_OUT) ?: ChatStateEnum.LOGGED_OUT
        )
    )

    var initChatCorrelationId: String = ""

    fun changeState(state: ChatStateEnum) {
        preferences.save(PreferencesCoreKeys.CHAT_STATE, state)
        stateChannel.value = ChatStateEvent(state)
        observeState(state)
    }

    private fun changeState(event: ChatStateEvent) {
        preferences.save(PreferencesCoreKeys.CHAT_STATE, event.state)
        stateChannel.value = event
        observeState(event.state)
    }

    private fun observeState(state: ChatStateEnum) {
        if (state < ChatStateEnum.INIT_USER_SENT) {
            startTimeoutObserver(state)
        } else {
            stopTimeoutObserver()
        }
    }

    fun getCurrentState() = stateChannel.value.state

    fun getStateFlow(): StateFlow<ChatStateEvent> = stateChannel

    fun onLogout() {
        changeState(ChatStateEnum.LOGGED_OUT)
    }

    fun isChatReady(): Boolean {
        return getCurrentState() >= ChatStateEnum.INIT_USER_SENT
    }

    private fun startTimeoutObserver(state: ChatStateEnum) {
        val startTime = System.currentTimeMillis()
        val delayTime = 500L

        coroutineScope?.cancel()
        coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope?.launch {
            while (getCurrentState() <= state && isActive) {
                delay(delayTime)
                if (System.currentTimeMillis() - startTime > timeout) {
                    changeState(ChatStateEvent(getCurrentState(), true))
                    break
                }
            }
        }
    }

    private fun stopTimeoutObserver() {
        coroutineScope?.cancel()
    }
}
