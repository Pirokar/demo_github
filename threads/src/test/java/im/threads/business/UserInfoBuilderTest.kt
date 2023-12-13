package im.threads.business

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserInfoBuilderTest {

    @Test
    fun whenSetAuthData_thenAuthTokenAndAuthSchemaAndAuthMethodAreSet() {
        val userInfoBuilder = UserInfoBuilder("testClientId")
        userInfoBuilder.setAuthData("testToken", "testSchema", AuthMethod.HEADERS)
        assertEquals("testToken", userInfoBuilder.authToken)
        assertEquals("testSchema", userInfoBuilder.authSchema)
        assertEquals(AuthMethod.HEADERS, userInfoBuilder.authMethod)
    }

    @Test
    fun whenSetClientIdSignature_thenClientIdSignatureIsSet() {
        val userInfoBuilder = UserInfoBuilder("testClientId")
        userInfoBuilder.setClientIdSignature("testSignature")
        assertEquals("testSignature", userInfoBuilder.clientIdSignature)
    }

    @Test
    fun whenSetUserName_thenUserNameIsSet() {
        val userInfoBuilder = UserInfoBuilder("testClientId")
        userInfoBuilder.setUserName("testUserName")
        assertEquals("testUserName", userInfoBuilder.userName)
    }

    @Test
    fun whenSetData_thenClientDataIsSet() {
        val userInfoBuilder = UserInfoBuilder("testClientId")
        userInfoBuilder.setData("testData")
        assertEquals("testData", userInfoBuilder.clientData)
    }

    @Test
    fun whenSetClientData_thenClientDataIsSet() {
        val userInfoBuilder = UserInfoBuilder("testClientId")
        userInfoBuilder.setClientData("testClientData")
        assertEquals("testClientData", userInfoBuilder.clientData)
    }

    @Test
    fun whenSetAppMarker_thenAppMarkerIsSet() {
        val userInfoBuilder = UserInfoBuilder("testClientId")
        userInfoBuilder.setAppMarker("testAppMarker")
        assertEquals("testAppMarker", userInfoBuilder.appMarker)
    }

    @Test
    fun whenSetClientIdEncrypted_thenClientIdEncryptedIsSet() {
        val userInfoBuilder = UserInfoBuilder("testClientId")
        userInfoBuilder.setClientIdEncrypted(true)
        assert(userInfoBuilder.clientIdEncrypted)
    }
}
