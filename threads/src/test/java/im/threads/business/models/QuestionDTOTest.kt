package im.threads.business.models

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class QuestionDTOTest {

    @Test
    fun whenHasRate_thenCorrect() {
        val questionDTO = QuestionDTO()
        questionDTO.rate = null
        Assert.assertFalse(questionDTO.hasRate())

        questionDTO.rate = 5
        Assert.assertTrue(questionDTO.hasRate())
    }

    @Test
    fun whenGenerateCorrelationId_thenCorrelationIdIsUUID() {
        val questionDTO = QuestionDTO()
        questionDTO.generateCorrelationId()
        Assert.assertNotNull(questionDTO.correlationId)
        Assert.assertTrue(UUID.fromString(questionDTO.correlationId).toString() == questionDTO.correlationId)
    }
}
