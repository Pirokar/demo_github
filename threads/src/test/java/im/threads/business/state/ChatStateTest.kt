package im.threads.business.state

import androidx.test.core.app.ApplicationProvider
import im.threads.business.preferences.Preferences
import im.threads.business.utils.ClientUseCase
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ChatStateTest {

    private lateinit var chatState: ChatState
    private lateinit var preferences: Preferences
    private lateinit var clientUseCase: ClientUseCase

    @Before
    fun setUp() {
        preferences = Preferences(ApplicationProvider.getApplicationContext())
        clientUseCase = ClientUseCase()
        chatState = ChatState(preferences, clientUseCase)
    }

    @Test
    fun whenChangeState_thenStateIsChanged() {
        chatState.changeState(ChatStateEnum.LOGGING_IN)
        assertEquals(ChatStateEnum.LOGGING_IN, chatState.getCurrentState())
    }

    @Test
    fun whenOnLogout_thenStateIsLoggedOut() {
        chatState.onLogout()
        assertEquals(ChatStateEnum.LOGGED_OUT, chatState.getCurrentState())
    }
}
