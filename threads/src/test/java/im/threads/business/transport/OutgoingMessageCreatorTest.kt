package im.threads.business.transport

import android.content.SharedPreferences
import im.threads.business.UserInfoBuilder
import im.threads.business.models.ConsultInfo
import im.threads.business.models.MessageStatus
import im.threads.business.models.QuestionDTO
import im.threads.business.models.Survey
import im.threads.business.models.UserPhrase
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.utils.AppInfo
import im.threads.business.utils.ClientUseCase
import im.threads.business.utils.DeviceInfo
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OutgoingMessageCreatorTest {
    @Mock
    private lateinit var deviceInfo: DeviceInfo

    @Mock
    private lateinit var appInfo: AppInfo

    @Mock
    private lateinit var clientUseCase: ClientUseCase

    @Mock
    private lateinit var preferences: Preferences

    @Mock
    private lateinit var sharedPreferences: SharedPreferences

    init {
        MockitoAnnotations.openMocks(this)
    }

    private val outgoingMessageCreator = OutgoingMessageCreator(
        preferences,
        clientUseCase,
        appInfo,
        deviceInfo
    )

    @Before
    fun setup() {
        `when`(preferences.sharedPreferences).thenReturn(sharedPreferences)
    }

    @Test
    fun whenUserInfoExists_thenCreateInitChatMessage() {
        val userInfo = UserInfoBuilder("testClientId")
        userInfo.setClientData("testData")
        userInfo.setAppMarker("testAppMarker")
        `when`(clientUseCase.getUserInfo()).thenReturn(userInfo)
        `when`(sharedPreferences.getString(PreferencesCoreKeys.DEVICE_ADDRESS, null)).thenReturn("testDeviceAddress")

        val result = outgoingMessageCreator.createInitChatMessage()

        assertEquals("testClientId", result.get(MessageAttributes.CLIENT_ID).asString)
        assertEquals("INIT_CHAT", result.get(MessageAttributes.TYPE).asString)
        assertEquals("testData", result.get(MessageAttributes.DATA).asString)
        assertEquals("testAppMarker", result.get(MessageAttributes.APP_MARKER_KEY).asString)
    }

    @Test
    fun whenUserInfoIsNull_thenCreateInitChatMessage() {
        `when`(clientUseCase.getUserInfo()).thenReturn(null)
        `when`(sharedPreferences.getString(PreferencesCoreKeys.DEVICE_ADDRESS, null)).thenReturn("testDeviceAddress")

        val result = outgoingMessageCreator.createInitChatMessage()

        assert(result.get(MessageAttributes.CLIENT_ID).isJsonNull)
        assertEquals("INIT_CHAT", result.get(MessageAttributes.TYPE).asString)
        assert(result.get(MessageAttributes.DATA).isJsonNull)
        assert(result.get(MessageAttributes.APP_MARKER_KEY).isJsonNull)
    }

    @Test
    fun whenUserInfoExists_thenCreateClientInfoMessage() {
        val userInfo = UserInfoBuilder("testClientId")
        userInfo.setClientData("testData")
        userInfo.setAppMarker("testAppMarker")
        `when`(clientUseCase.getUserInfo()).thenReturn(userInfo)
        `when`(sharedPreferences.getString(PreferencesCoreKeys.DEVICE_ADDRESS, null)).thenReturn("testDeviceAddress")
        `when`(deviceInfo.osVersion).thenReturn("testOsVersion")
        `when`(deviceInfo.deviceName).thenReturn("testDeviceName")
        `when`(deviceInfo.ipAddress).thenReturn("testIpAddress")
        `when`(appInfo.appVersion).thenReturn("testAppVersion")
        `when`(appInfo.appName).thenReturn("testAppName")
        `when`(appInfo.appId).thenReturn("testAppId")
        `when`(appInfo.libVersion).thenReturn("testLibVersion")

        val result = outgoingMessageCreator.createClientInfoMessage("testLocale", true)

        assertEquals("testClientId", result.get(MessageAttributes.CLIENT_ID).asString)
        assertEquals("testData", result.get(MessageAttributes.DATA).asString)
        assertEquals("testAppMarker", result.get(MessageAttributes.APP_MARKER_KEY).asString)
        assertEquals("testOsVersion", result.get("osVersion").asString)
        assertEquals("testDeviceName", result.get("device").asString)
        assertEquals("testIpAddress", result.get("ip").asString)
        assertEquals("testAppVersion", result.get("appVersion").asString)
        assertEquals("testAppName", result.get("appName").asString)
        assertEquals("testAppId", result.get(MessageAttributes.APP_BUNDLE_KEY).asString)
        assertEquals("testLibVersion", result.get("libVersion").asString)
        assertEquals("testLocale", result.get("clientLocale").asString)
        assertEquals("CLIENT_INFO", result.get(MessageAttributes.TYPE).asString)
        assertEquals(true, result.get("preRegister").asBoolean)
    }

    @Test
    fun whenUserInfoIsNull_thenCreateClientInfoMessage() {
        `when`(clientUseCase.getUserInfo()).thenReturn(null)
        `when`(sharedPreferences.getString(PreferencesCoreKeys.DEVICE_ADDRESS, null)).thenReturn("testDeviceAddress")
        `when`(deviceInfo.osVersion).thenReturn("testOsVersion")
        `when`(deviceInfo.deviceName).thenReturn("testDeviceName")
        `when`(deviceInfo.ipAddress).thenReturn("testIpAddress")
        `when`(appInfo.appVersion).thenReturn("testAppVersion")
        `when`(appInfo.appName).thenReturn("testAppName")
        `when`(appInfo.appId).thenReturn("testAppId")
        `when`(appInfo.libVersion).thenReturn("testLibVersion")

        val result = outgoingMessageCreator.createClientInfoMessage("testLocale", false)

        assert(result.get(MessageAttributes.CLIENT_ID).isJsonNull)
        assertEquals(null, result.get(MessageAttributes.DATA))
        assert(result.get(MessageAttributes.CLIENT_ID).isJsonNull)
        assertEquals("testOsVersion", result.get("osVersion").asString)
        assertEquals("testDeviceName", result.get("device").asString)
        assertEquals("testIpAddress", result.get("ip").asString)
        assertEquals("testAppVersion", result.get("appVersion").asString)
        assertEquals("testAppName", result.get("appName").asString)
        assertEquals("testAppId", result.get(MessageAttributes.APP_BUNDLE_KEY).asString)
        assertEquals("testLibVersion", result.get("libVersion").asString)
        assertEquals("testLocale", result.get("clientLocale").asString)
        assertEquals("CLIENT_INFO", result.get(MessageAttributes.TYPE).asString)
        assertEquals(null, result.get("preRegister"))
    }

    @Test
    fun whenUserInfoExists_thenCreateMessageTyping() {
        val userInfo = UserInfoBuilder("testClientId")
        userInfo.setAppMarker("testAppMarker")
        `when`(clientUseCase.getUserInfo()).thenReturn(userInfo)
        `when`(sharedPreferences.getString(PreferencesCoreKeys.DEVICE_ADDRESS, null)).thenReturn("testDeviceAddress")

        val result = outgoingMessageCreator.createMessageTyping("testInput")

        assertEquals("TYPING", result.get(MessageAttributes.TYPE).asString)
        assertEquals("testInput", result.get(MessageAttributes.TYPING_DRAFT).asString)
        assertEquals("testAppMarker", result.get(MessageAttributes.APP_MARKER_KEY).asString)
    }

    @Test
    fun whenUserInfoIsNull_thenCreateMessageTyping() {
        `when`(clientUseCase.getUserInfo()).thenReturn(null)
        `when`(sharedPreferences.getString(PreferencesCoreKeys.DEVICE_ADDRESS, null)).thenReturn("testDeviceAddress")

        val result = outgoingMessageCreator.createMessageTyping("testInput")

        assertEquals("TYPING", result.get(MessageAttributes.TYPE).asString)
        assertEquals("testInput", result.get(MessageAttributes.TYPING_DRAFT).asString)
        assert(result.get(MessageAttributes.APP_MARKER_KEY).isJsonNull)
    }

    @Test
    fun whenSurveyHasQuestions_thenCreateRatingDoneMessage() {
        val userInfo = UserInfoBuilder("testClientId")
        userInfo.setAppMarker("testAppMarker")
        `when`(clientUseCase.getUserInfo()).thenReturn(userInfo)

        val questionDTO = QuestionDTO()
        questionDTO.id = 1L
        questionDTO.rate = 5
        questionDTO.text = "testText"
        val survey = Survey("testUuid", 555L, arrayListOf(questionDTO), null, 0L, false, MessageStatus.SENT, false)

        val result = outgoingMessageCreator.createRatingDoneMessage(survey)

        assertEquals("testClientId", result.get(MessageAttributes.CLIENT_ID).asString)
        assertEquals(555L, result.get("sendingId").asLong)
        assertEquals(1L, result.get("questionId").asLong)
        assertEquals(5, result.get("rate").asInt)
        assertEquals("testText", result.get("text").asString)
        assertEquals("testAppMarker", result.get(MessageAttributes.APP_MARKER_KEY).asString)
    }

    @Test
    fun whenSurveyHasNoQuestions_thenCreateRatingDoneMessage() {
        val userInfo = UserInfoBuilder("testClientId")
        userInfo.setAppMarker("testAppMarker")
        `when`(clientUseCase.getUserInfo()).thenReturn(userInfo)

        val survey = Survey("testUuid", 555L, arrayListOf(), null, 0L, false, MessageStatus.SENT, false)

        val result = outgoingMessageCreator.createRatingDoneMessage(survey)

        assertEquals("testClientId", result.get(MessageAttributes.CLIENT_ID).asString)
        assertEquals(555L, result.get("sendingId").asLong)
        assertEquals(null, result.get("questionId"))
        assertEquals(null, result.get("rate"))
        assertEquals(null, result.get("text"))
        assertEquals("testAppMarker", result.get(MessageAttributes.APP_MARKER_KEY).asString)
    }

    @Test
    fun whenUserInfoExists_thenCreateResolveThreadMessage() {
        val userInfo = UserInfoBuilder("testClientId")
        userInfo.setAppMarker("testAppMarker")
        `when`(clientUseCase.getUserInfo()).thenReturn(userInfo)

        val result = outgoingMessageCreator.createResolveThreadMessage()

        assertEquals("testClientId", result.get(MessageAttributes.CLIENT_ID).asString)
        assertEquals("CLOSE_THREAD", result.get(MessageAttributes.TYPE).asString)
        assertEquals("testAppMarker", result.get(MessageAttributes.APP_MARKER_KEY).asString)
    }

    @Test
    fun whenUserInfoExists_thenCreateReopenThreadMessage() {
        val userInfo = UserInfoBuilder("testClientId")
        userInfo.setAppMarker("testAppMarker")
        `when`(clientUseCase.getUserInfo()).thenReturn(userInfo)

        val result = outgoingMessageCreator.createReopenThreadMessage()

        assertEquals("testClientId", result.get(MessageAttributes.CLIENT_ID).asString)
        assertEquals("REOPEN_THREAD", result.get(MessageAttributes.TYPE).asString)
        assertEquals("testAppMarker", result.get(MessageAttributes.APP_MARKER_KEY).asString)
    }

    @Test
    fun whenUserInfoExists_thenCreateMessageClientOffline() {
        val userInfo = UserInfoBuilder("testClientId")
        userInfo.setAppMarker("testAppMarker")
        `when`(clientUseCase.getUserInfo()).thenReturn(userInfo)

        val result = outgoingMessageCreator.createMessageClientOffline("testClientId")

        assertEquals("testClientId", result.get(MessageAttributes.CLIENT_ID).asString)
        assertEquals("CLIENT_OFFLINE", result.get(MessageAttributes.TYPE).asString)
        assertEquals("testAppMarker", result.get(MessageAttributes.APP_MARKER_KEY).asString)
    }

    @Test
    fun whenUserInfoExists_thenCreateMessageUpdateLocation() {
        val userInfo = UserInfoBuilder("testClientId")
        userInfo.setAppMarker("testAppMarker")
        `when`(clientUseCase.getUserInfo()).thenReturn(userInfo)

        val result = outgoingMessageCreator.createMessageUpdateLocation(1.0, 1.0, "testLocale")

        assertEquals("testClientId", result.get(MessageAttributes.CLIENT_ID).asString)
        assertEquals("UPDATE_LOCATION", result.get(MessageAttributes.TYPE).asString)
        assertEquals("{\"coordinates\":\"1.0, 1.0\"}", result.get(MessageAttributes.DATA).asString)
        assertEquals("testAppMarker", result.get(MessageAttributes.APP_MARKER_KEY).asString)
    }

    @Test
    fun whenUserInfoIsNull_thenCreateResolveThreadMessage() {
        `when`(clientUseCase.getUserInfo()).thenReturn(null)

        val result = outgoingMessageCreator.createResolveThreadMessage()

        assert(result.get(MessageAttributes.CLIENT_ID).isJsonNull)
        assertEquals("CLOSE_THREAD", result.get(MessageAttributes.TYPE).asString)
        assert(result.get(MessageAttributes.APP_MARKER_KEY).isJsonNull)
    }

    @Test
    fun whenUserInfoIsNull_thenCreateReopenThreadMessage() {
        `when`(clientUseCase.getUserInfo()).thenReturn(null)

        val result = outgoingMessageCreator.createReopenThreadMessage()

        assert(result.get(MessageAttributes.CLIENT_ID).isJsonNull)
        assertEquals("REOPEN_THREAD", result.get(MessageAttributes.TYPE).asString)
        assert(result.get(MessageAttributes.APP_MARKER_KEY).isJsonNull)
    }

    @Test
    fun whenUserInfoIsNull_thenCreateMessageClientOffline() {
        `when`(clientUseCase.getUserInfo()).thenReturn(null)

        val result = outgoingMessageCreator.createMessageClientOffline("testClientId")

        assertEquals("testClientId", result.get(MessageAttributes.CLIENT_ID).asString)
        assertEquals("CLIENT_OFFLINE", result.get(MessageAttributes.TYPE).asString)
        assert(result.get(MessageAttributes.APP_MARKER_KEY).isJsonNull)
    }

    @Test
    fun whenUserInfoIsNull_thenCreateMessageUpdateLocation() {
        `when`(clientUseCase.getUserInfo()).thenReturn(null)

        val result = outgoingMessageCreator.createMessageUpdateLocation(1.0, 1.0, "testLocale")

        assert(result.get(MessageAttributes.CLIENT_ID).isJsonNull)
        assertEquals("UPDATE_LOCATION", result.get(MessageAttributes.TYPE).asString)
        assertEquals("{\"coordinates\":\"1.0, 1.0\"}", result.get(MessageAttributes.DATA).asString)
        assert(result.get(MessageAttributes.APP_MARKER_KEY).isJsonNull)
    }

    @Test
    fun whenUserInfoExistsAndUserPhraseIsNotNull_thenCreateUserPhraseMessage() {
        val userInfo = UserInfoBuilder("testClientId")
        userInfo.setAppMarker("testAppMarker")
        `when`(clientUseCase.getUserInfo()).thenReturn(userInfo)

        val userPhrase = UserPhrase("testUuid", "testInput", null, 0L, null, MessageStatus.SENT, null)
        val consultInfo = ConsultInfo()
        val quoteMfmsFilePath = "testQuoteMfmsFilePath"
        val mfmsFilePath = "testMfmsFilePath"

        val result = outgoingMessageCreator.createUserPhraseMessage(userPhrase, consultInfo, quoteMfmsFilePath, mfmsFilePath)

        assertEquals("testClientId", result.get(MessageAttributes.CLIENT_ID).asString)
        assertEquals("MESSAGE", result.get(MessageAttributes.TYPE).asString)
        assertEquals("testInput", result.get(MessageAttributes.TEXT).asString)
        assertEquals("testAppMarker", result.get(MessageAttributes.APP_MARKER_KEY).asString)
    }

    @Test
    fun whenUserInfoExistsAndUserPhraseIsNull_thenCreateUserPhraseMessage() {
        val userInfo = UserInfoBuilder("testClientId")
        userInfo.setAppMarker("testAppMarker")
        `when`(clientUseCase.getUserInfo()).thenReturn(userInfo)

        val userPhrase = UserPhrase("testUuid", null, null, 0L, null, MessageStatus.SENT, null)
        val consultInfo = ConsultInfo()
        val quoteMfmsFilePath = "testQuoteMfmsFilePath"
        val mfmsFilePath = "testMfmsFilePath"

        val result = outgoingMessageCreator.createUserPhraseMessage(userPhrase, consultInfo, quoteMfmsFilePath, mfmsFilePath)

        assertEquals("testClientId", result.get(MessageAttributes.CLIENT_ID).asString)
        assertEquals("MESSAGE", result.get(MessageAttributes.TYPE).asString)
        assertEquals("", result.get(MessageAttributes.TEXT).asString)
        assertEquals("testAppMarker", result.get(MessageAttributes.APP_MARKER_KEY).asString)
    }

    @Test
    fun whenUserInfoIsNullAndUserPhraseIsNotNull_thenCreateUserPhraseMessage() {
        `when`(clientUseCase.getUserInfo()).thenReturn(null)

        val userPhrase = UserPhrase("testUuid", "testInput", null, 0L, null, MessageStatus.SENT, null)
        val consultInfo = ConsultInfo()
        val quoteMfmsFilePath = "testQuoteMfmsFilePath"
        val mfmsFilePath = "testMfmsFilePath"

        val result = outgoingMessageCreator.createUserPhraseMessage(userPhrase, consultInfo, quoteMfmsFilePath, mfmsFilePath)

        assert(result.get(MessageAttributes.CLIENT_ID).isJsonNull)
        assertEquals("MESSAGE", result.get(MessageAttributes.TYPE).asString)
        assertEquals("testInput", result.get(MessageAttributes.TEXT).asString)
        assert(result.get(MessageAttributes.APP_MARKER_KEY).isJsonNull)
    }

    @Test
    fun whenUserInfoIsNullAndUserPhraseIsNull_thenCreateUserPhraseMessage() {
        `when`(clientUseCase.getUserInfo()).thenReturn(null)

        val userPhrase = UserPhrase("testUuid", null, null, 0L, null, MessageStatus.SENT, null)
        val consultInfo = ConsultInfo()
        val quoteMfmsFilePath = "testQuoteMfmsFilePath"
        val mfmsFilePath = "testMfmsFilePath"

        val result = outgoingMessageCreator.createUserPhraseMessage(userPhrase, consultInfo, quoteMfmsFilePath, mfmsFilePath)

        assert(result.get(MessageAttributes.CLIENT_ID).isJsonNull)
        assertEquals("MESSAGE", result.get(MessageAttributes.TYPE).asString)
        assertEquals("", result.get(MessageAttributes.TEXT).asString)
        assert(result.get(MessageAttributes.APP_MARKER_KEY).isJsonNull)
    }
}
