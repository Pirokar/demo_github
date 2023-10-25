package im.threads.serviceLocatorTest

import im.threads.business.serviceLocator.core.inject
import im.threads.business.serviceLocator.core.startEdnaLocator
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ServiceLocatorTest {
    @Before
    fun before() {
        startEdnaLocator { modules(mainTestSLModule, supplementaryTestSLModule) }
    }

    @Test
    fun givenServiceLocator_whenInjectingClass1_thenClassIsAvailable() {
        val class1: SLTestClass1? by inject()
        assert(class1 != null)
    }

    @Test
    fun givenServiceLocator_whenInjectingClass2_thenClassIsAvailable() {
        val class2: SLTestClass2? by inject()
        assert(class2 != null)
    }

    @Test
    fun givenServiceLocator_whenInjectingClass3_thenClassIsAvailable() {
        val class3: SLTestClass1? by inject()
        assert(class3 != null)
    }
}
