package im.threads.business

import im.threads.business.utils.ClientUseCase
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ClientUseCaseTest {
    private val clientUseCase = ClientUseCase()

    @Test
    fun givenNewUserId_whenSaveIt_thenUserIdIsNotEmpty() {
        val testClientId = "testClientId123"

        clientUseCase.saveUserInfo(UserInfoBuilder(testClientId))
        clientUseCase.initClientId()

        check(clientUseCase.getUserInfo()?.clientId == testClientId)
    }
}
