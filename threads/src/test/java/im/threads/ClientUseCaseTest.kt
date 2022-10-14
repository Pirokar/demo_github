package im.threads

import androidx.test.core.app.ApplicationProvider
import im.threads.business.config.BaseConfig
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.rest.config.RequestConfig
import im.threads.business.utils.ClientUseCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ClientUseCaseTest {
    private val preferences = Preferences(ApplicationProvider.getApplicationContext())
    private val clientUseCase = ClientUseCase(preferences)

    @Test
    fun givenNewUserId_whenInitClientId_thenUserIdChanged() {
        val testClientId = "testClientId123"

        preferences.save(PreferencesCoreKeys.TAG_NEW_CLIENT_ID, testClientId)
        clientUseCase.initClientId()

        val currentClientId = preferences.get<String>(PreferencesCoreKeys.TAG_NEW_CLIENT_ID)
        check(currentClientId == testClientId)
    }
}
