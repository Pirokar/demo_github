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
    private val socketTimeout: Long by lazy {
        try {
            BaseConfig.getInstance().requestConfig.socketClientSettings.connectTimeoutMillis
        } catch (exc: Exception) {
            10000L
        }
    }

    private var coroutineScope: CoroutineScope? = null

    private val stateChannel by lazy {
        MutableStateFlow(
            ChatStateEvent(
                preferences.get(PreferencesCoreKeys.CHAT_STATE, ChatStateEnum.LOGGED_OUT) ?: ChatStateEnum.LOGGED_OUT
            )
        )
    }

    var initChatCorrelationId: String = ""

    fun changeState(state: ChatStateEnum) {
        if (state >= getCurrentState() || state < ChatStateEnum.DEVICE_REGISTERED) {
            preferences.save(PreferencesCoreKeys.CHAT_STATE, state)
            stateChannel.value = ChatStateEvent(state)
            observeState(state, socketTimeout)
        }
    }

    private fun changeState(event: ChatStateEvent, timeout: Long) {
        preferences.save(PreferencesCoreKeys.CHAT_STATE, event.state)
        stateChannel.value = event
        if (!event.isTimeout) {
            observeState(event.state, timeout)
        }
    }

    private fun observeState(state: ChatStateEnum, timeout: Long) {
        if (state < ChatStateEnum.ATTACHMENT_SETTINGS_LOADED) {
            startTimeoutObserver(state, timeout)
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
        return getCurrentState() >= ChatStateEnum.ATTACHMENT_SETTINGS_LOADED
    }

    private fun startTimeoutObserver(state: ChatStateEnum, timeout: Long) {
        val startTime = System.currentTimeMillis()
        val delayTime = 500L

        coroutineScope?.cancel()
        coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope?.launch {
            if (!state.isLastObservableState()) {
                while (getCurrentState().value <= (state.value + 1) && isActive) {
                    delay(delayTime)
                    if (System.currentTimeMillis() - startTime > timeout) {
                        changeState(ChatStateEvent(getCurrentState(), true), timeout)
                        break
                    }
                }
            }
        }
    }

    private fun stopTimeoutObserver() {
        coroutineScope?.cancel()
    }
}
