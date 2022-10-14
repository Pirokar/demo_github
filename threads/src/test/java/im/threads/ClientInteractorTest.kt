package im.threads

import androidx.test.core.app.ApplicationProvider
import im.threads.business.config.BaseConfig
import im.threads.business.rest.config.RequestConfig
import im.threads.business.utils.ClientUseCase
import im.threads.business.utils.preferences.PrefUtilsBase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ClientInteractorTest {
    private val clientInteractor = ClientUseCase()

    @Before
    fun before() {
        val testUrl = "https://testurl.ru"

        BaseConfig.instance = BaseConfig(
            ApplicationProvider.getApplicationContext(),
            testUrl,
            testUrl,
            testUrl,
            "provider",
            null,
            true,
            null,
            null,
            null,
            false,
            0,
            0,
            RequestConfig(),
            arrayListOf()
        )
    }

    @Test
    fun givenNewUserId_whenInitClientId_thenUserIdChanged() {
        val testClientId = "testClientId123"

        PrefUtilsBase.setNewClientId(testClientId)
        clientInteractor.initClientId()

        check(PrefUtilsBase.clientID == testClientId)
    }
}
