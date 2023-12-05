package im.threads.business.models

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SurveyTest {

    @Test
    fun whenUuidIsSet_thenUuidIsReturned() {
        val survey = Survey(
            "testUuid",
            1L,
            null,
            1L,
            MessageStatus.SENDING,
            read = false,
            displayMessage = false
        )
        Assert.assertEquals("testUuid", survey.uuid)
    }

    @Test
    fun whenSendingIdIsSet_thenSendingIdIsReturned() {
        val survey = Survey(
            "testUuid",
            1L,
            null,
            1L,
            MessageStatus.SENDING,
            read = false,
            displayMessage = false
        )
        Assert.assertEquals(1L, survey.sendingId)
    }

    @Test
    fun whenHideAfterIsSet_thenHideAfterIsReturned() {
        val survey = Survey(
            "testUuid",
            1L,
            1000L,
            1L,
            MessageStatus.SENDING,
            read = false,
            displayMessage = false
        )
        Assert.assertEquals(1000L, survey.hideAfter)
    }

    @Test
    fun whenTimeStampIsSet_thenTimeStampIsReturned() {
        val survey = Survey(
            "testUuid",
            1L,
            null,
            1L,
            MessageStatus.SENDING,
            read = false,
            displayMessage = false
        )
        Assert.assertEquals(1L, survey.timeStamp)
    }

    @Test
    fun whenIsDisplayMessageIsSet_thenIsDisplayMessageIsReturned() {
        val survey = Survey(
            "testUuid",
            1L,
            null,
            1L,
            MessageStatus.SENDING,
            read = false,
            displayMessage = true
        )
        Assert.assertTrue(survey.isDisplayMessage)
    }

    @Test
    fun whenSentStateIsSet_thenSentStateIsReturned() {
        val survey = Survey(
            "testUuid",
            1L,
            null,
            1L,
            MessageStatus.SENDING,
            read = false,
            displayMessage = false
        )
        Assert.assertEquals(MessageStatus.SENDING, survey.sentState)
    }

    @Test
    fun whenIsReadIsSet_thenIsReadIsReturned() {
        val survey = Survey(
            "testUuid",
            1L,
            null,
            1L,
            MessageStatus.SENDING,
            read = true,
            displayMessage = false
        )
        Assert.assertTrue(survey.isRead)
    }

    @Test
    fun whenThreadIdIsSet_thenThreadIdIsReturned() {
        val survey = Survey(
            "testUuid",
            1L,
            null,
            1L,
            MessageStatus.SENDING,
            read = false,
            displayMessage = false
        )
        Assert.assertNull(survey.threadId)
    }
}
