package im.threads.business.models

import androidx.core.util.ObjectsCompat
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RequestResolveThreadTest {

    @Test
    fun whenIsTheSameItem_thenCorrect() {
        val requestResolveThread1 = RequestResolveThread("uuid", 0L, 0L, 0L, false)
        val requestResolveThread2 = RequestResolveThread("uuid", 0L, 0L, 0L, false)
        Assert.assertTrue(requestResolveThread1.isTheSameItem(requestResolveThread2))

        val requestResolveThread3 = RequestResolveThread("different uuid", 0L, 0L, 0L, false)
        Assert.assertFalse(requestResolveThread1.isTheSameItem(requestResolveThread3))
    }

    @Test
    fun whenEquals_thenCorrect() {
        val requestResolveThread1 = RequestResolveThread("uuid", 0L, 0L, 0L, false)
        val requestResolveThread2 = RequestResolveThread("uuid", 0L, 0L, 0L, false)
        Assert.assertTrue(requestResolveThread1.equals(requestResolveThread2))

        val requestResolveThread3 = RequestResolveThread("different uuid", 0L, 0L, 0L, false)
        Assert.assertFalse(requestResolveThread1.equals(requestResolveThread3))
    }

    @Test
    fun whenHashCode_thenCorrect() {
        val requestResolveThread = RequestResolveThread("uuid", 0L, 0L, 0L, false)
        val expectedHashCode = ObjectsCompat.hash("uuid", 0L, 0L, 0L)
        Assert.assertEquals(expectedHashCode, requestResolveThread.hashCode())
    }
}
