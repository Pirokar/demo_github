package im.threads.business.controllers

import androidx.test.core.app.ApplicationProvider
import im.threads.business.core.ContextHolder
import im.threads.business.serviceLocator.core.startEdnaLocator
import im.threads.business.serviceLocator.coreSLModule
import im.threads.ui.serviceLocator.uiSLModule
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UnreadMessagesControllerTest {

    @Before
    fun before() {
        ContextHolder.context = ApplicationProvider.getApplicationContext()
        startEdnaLocator { modules(coreSLModule, uiSLModule) }
    }

    @Test
    fun whenIncrementUnreadPush_thenItIncremented() {
        val currentValue = UnreadMessagesController.INSTANCE.unreadMessages
        UnreadMessagesController.INSTANCE.incrementUnreadPush()
        assert(UnreadMessagesController.INSTANCE.unreadMessages == currentValue + 1)
    }

    @Test
    fun whenClearUnreadPush_thenItEqualsToZero() {
        UnreadMessagesController.INSTANCE.incrementUnreadPush()
        UnreadMessagesController.INSTANCE.clearUnreadPush()
        assert(UnreadMessagesController.INSTANCE.unreadMessages == 0)
    }
}
