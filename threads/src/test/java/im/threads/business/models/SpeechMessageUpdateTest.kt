package im.threads.business.models

import android.net.Uri
import im.threads.business.formatters.SpeechStatus
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SpeechMessageUpdateTest {

    @Test
    fun whenIsTheSameItem_thenCorrect() {
        val speechMessageUpdate1 = SpeechMessageUpdate(
            "uuid",
            SpeechStatus.SUCCESS,
            FileDescription(
                "from",
                Uri.parse("fileUri"),
                0L,
                0L
            )
        )
        val speechMessageUpdate2 = SpeechMessageUpdate(
            "uuid",
            SpeechStatus.SUCCESS,
            FileDescription(
                "from",
                Uri.parse("fileUri"),
                0L,
                0L
            )
        )
        Assert.assertTrue(speechMessageUpdate1.isTheSameItem(speechMessageUpdate2))

        val chatItem = object : ChatItem {
            override val timeStamp: Long = 0L
            override fun isTheSameItem(otherItem: ChatItem?): Boolean = false
            override val threadId: Long? = null
        }
        Assert.assertFalse(speechMessageUpdate1.isTheSameItem(chatItem))
    }

    @Test
    fun whenTimeStamp_thenAlwaysZero() {
        val speechMessageUpdate = SpeechMessageUpdate(
            "uuid",
            SpeechStatus.SUCCESS,
            FileDescription(
                "from",
                Uri.parse("fileUri"),
                0L,
                0L
            )
        )
        Assert.assertEquals(0L, speechMessageUpdate.timeStamp)
    }

    @Test
    fun whenThreadId_thenAlwaysNull() {
        val speechMessageUpdate = SpeechMessageUpdate(
            "uuid",
            SpeechStatus.SUCCESS,
            FileDescription(
                "from",
                Uri.parse("fileUri"),
                0L,
                0L
            )
        )
        Assert.assertNull(speechMessageUpdate.threadId)
    }
}
