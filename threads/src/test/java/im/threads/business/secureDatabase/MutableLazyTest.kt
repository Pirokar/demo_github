package im.threads.business.secureDatabase

import im.threads.business.extensions.mutableLazy
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MutableLazyTest {
    @Test
    fun whenChangingMutableLazy_thenVarReactForThis() {
        var initializerCalled = 0
        val mutableLazy = mutableLazy {
            initializerCalled++
            "Test"
        }

        assertFalse(mutableLazy.isInitialized())
        assertEquals(0, initializerCalled)

        assertEquals("Test", mutableLazy.value)
        assertTrue(mutableLazy.isInitialized())
        assertEquals(1, initializerCalled)

        mutableLazy.reset()
        assertFalse(mutableLazy.isInitialized())

        assertEquals("Test", mutableLazy.value)
        assertTrue(mutableLazy.isInitialized())
        assertEquals(2, initializerCalled)
    }
}
