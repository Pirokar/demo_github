package im.threads.business.core

import androidx.test.core.app.ApplicationProvider
import im.threads.business.AuthMethod
import im.threads.business.UserInfoBuilder
import im.threads.business.config.BaseConfigBuilder
import im.threads.business.config.ConfigTestBaseClass
import im.threads.business.models.CampaignMessage
import im.threads.business.models.enums.CurrentUiTheme
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.rest.queries.BackendApi
import im.threads.business.rest.queries.DatastoreApi
import im.threads.business.serviceLocator.core.inject
import im.threads.business.state.ChatState
import im.threads.business.state.ChatStateEnum
import im.threads.business.utils.ClientUseCase
import im.threads.ui.config.ConfigBuilder
import im.threads.ui.core.ThreadsLib
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class ThreadsLibBaseTest : ConfigTestBaseClass() {

    @Before
    override fun before() {
        super.before()
        try {
            ThreadsLibBase.getInstance()
        } catch (exc: Exception) {
            ThreadsLibBase.init(
                BaseConfigBuilder(ApplicationProvider.getApplicationContext()).apply {
                    serverBaseUrl(ednaMockUrl)
                    datastoreUrl(ednaMockUrl)
                    threadsGateUrl(ednaMockThreadsGateUrl)
                    threadsGateProviderUid(ednaMockThreadsGateProviderUid)
                }
            )
        }
    }

    @Test
    fun whenInitializingUser_thenItIsInitialized() {
        ThreadsLibBase.getInstance().initUser(
            UserInfoBuilder("2333"),
            false
        )
        assert(ThreadsLibBase.getInstance().isUserInitialized)
    }

    @Test
    fun whenSetUiThemeDark_thenItSavedToPreferences() {
        ThreadsLibBase.getInstance().currentUiTheme = CurrentUiTheme.DARK
        assert(ThreadsLibBase.getInstance().currentUiTheme == CurrentUiTheme.DARK)
    }

    @Test
    fun whenSetUiThemeLight_thenItSavedToPreferences() {
        ThreadsLibBase.getInstance().currentUiTheme = CurrentUiTheme.LIGHT
        assert(ThreadsLibBase.getInstance().currentUiTheme == CurrentUiTheme.LIGHT)
    }

    @Test
    fun whenSetUiThemeSystem_thenItSavedToPreferences() {
        ThreadsLibBase.getInstance().currentUiTheme = CurrentUiTheme.SYSTEM
        assert(ThreadsLibBase.getInstance().currentUiTheme == CurrentUiTheme.SYSTEM)
    }

    @Test
    fun whenForceRegister_thenChatStateChangedToLoggingIn() {
        val chatState: ChatState by inject()

        ThreadsLibBase.getInstance().initUser(
            UserInfoBuilder("2334"),
            true
        )
        assert(chatState.getCurrentState() >= ChatStateEnum.LOGGING_IN && ThreadsLibBase.isForceRegistration)
    }

    @Test
    fun whenLogoutClient_thenClientNotExists() {
        initThreadsLib()
        ThreadsLibBase.getInstance().initUser(
            UserInfoBuilder("2333"),
            false
        )
        assert(ThreadsLibBase.getInstance().isUserInitialized)

        ThreadsLibBase.getInstance().logoutClient()
        assert(!ThreadsLibBase.getInstance().isUserInitialized)
    }

    @Test
    fun whenSetCampaignMessage_thenMessageExistsInPreferences() {
        val preferences: Preferences by inject()
        preferences.sharedPreferences.edit().clear().commit()

        val date = Date()
        val campaignMessage = CampaignMessage(
            "testMessage",
            "TestUser",
            date,
            "sdfsdf-dsfsdf-sfsdf",
            433492847L,
            date,
            1,
            "TestCampaign",
            1
        )
        ThreadsLibBase.getInstance().setCampaignMessage(campaignMessage)
        val saveMessage = preferences.get<CampaignMessage>(PreferencesCoreKeys.CAMPAIGN_MESSAGE)
        assert(saveMessage?.chatMessageId == campaignMessage.chatMessageId)
    }

    @Test
    fun whenUpdateAuthData_thenDataUpdatedInClientInfo() {
        val clientUseCase: ClientUseCase by inject()
        val token = "sddswewwe2323swds"
        val authSchema = "retail"
        val authMethod = AuthMethod.HEADERS

        initThreadsLib()
        ThreadsLibBase.getInstance().initUser(
            UserInfoBuilder("2335"),
            false
        )
        ThreadsLibBase.getInstance().updateAuthData(token, authSchema, authMethod)
        clientUseCase.getUserInfo()!!.apply {
            assert(
                this.authToken == token &&
                    this.authSchema == authSchema &&
                    this.authMethod == authMethod
            )
        }
    }

    @Test
    fun whenChangeServerSettings_thenApiIsCreated() {
        initThreadsLib()
        ThreadsLibBase.changeServerSettings(
            ednaMockUrl,
            ednaMockUrl,
            ednaMockThreadsGateUrl,
            ednaMockThreadsGateProviderUid,
            null,
            false
        )
        BackendApi.get()
        DatastoreApi.get()
    }

    private fun initThreadsLib() {
        try {
            ThreadsLib.getInstance()
        } catch (exc: Exception) {
            val configBuilder = ConfigBuilder(ApplicationProvider.getApplicationContext()).apply {
                serverBaseUrl(ednaMockUrl)
                datastoreUrl(ednaMockUrl)
                threadsGateUrl(ednaMockThreadsGateUrl)
                threadsGateProviderUid(ednaMockThreadsGateProviderUid)
            }
            ThreadsLib.cleanLibInstance()
            ThreadsLib.init(configBuilder)
        }
    }
}
