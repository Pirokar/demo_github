package im.threads.business.models

import im.threads.business.formatters.SpeechStatus
import im.threads.business.models.enums.ModificationStateEnum
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConsultPhraseTest {

    private val consultPhrase = ConsultPhrase(
        id = "Test ID",
        fileDescription = null,
        modified = ModificationStateEnum.EDITED,
        quote = null,
        consultName = "Test Consult Name",
        phraseText = "Test Phrase Text",
        formattedPhrase = "Test Formatted Phrase",
        date = 123456789L,
        consultId = "Test Consult ID",
        avatarPath = "Test Avatar Path",
        read = true,
        status = "Test Status",
        sex = true,
        threadId = 123L,
        quickReplies = null,
        isBlockInput = false,
        speechStatus = SpeechStatus.NO_SPEECH_STATUS,
        role = ConsultRole.OPERATOR
    )

    @Test
    fun whenIsOnlyImage_thenReturnsFalse() {
        assertEquals(false, consultPhrase.isOnlyImage)
    }

    @Test
    fun whenIsOnlyDoc_thenReturnsFalse() {
        assertEquals(false, consultPhrase.isOnlyDoc)
    }

    @Test
    fun whenIsVoiceMessage_thenReturnsFalse() {
        assertEquals(false, consultPhrase.isVoiceMessage)
    }

    @Test
    fun whenIsTheSameItemWithSameID_thenReturnsTrue() {
        val otherItem = ConsultPhrase(
            id = "Test ID",
            fileDescription = null,
            modified = ModificationStateEnum.EDITED,
            quote = null,
            consultName = "Other Consult Name",
            phraseText = "Other Phrase Text",
            formattedPhrase = "Other Formatted Phrase",
            date = 987654321L,
            consultId = "Other Consult ID",
            avatarPath = "Other Avatar Path",
            read = false,
            status = "Other Status",
            sex = false,
            threadId = 456L,
            quickReplies = null,
            isBlockInput = true,
            speechStatus = SpeechStatus.NO_SPEECH_STATUS,
            role = ConsultRole.OPERATOR
        )
        assertTrue(consultPhrase.isTheSameItem(otherItem))
    }
}
