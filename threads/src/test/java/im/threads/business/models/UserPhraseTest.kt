package im.threads.business.models

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserPhraseTest {
    @Test
    fun whenIsTheSameItem_thenIsTheSameItemIsReturned() {
        val userPhrase1 = UserPhrase("testId", "testPhrase", null, 1L, null, MessageStatus.SENDING, 1L)
        val userPhrase2 = UserPhrase("testId", "testPhrase", null, 1L, null, MessageStatus.SENDING, 1L)
        Assert.assertTrue(userPhrase1.isTheSameItem(userPhrase2))
    }
}
