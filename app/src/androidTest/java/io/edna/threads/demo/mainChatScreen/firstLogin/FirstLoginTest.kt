package io.edna.threads.demo.mainChatScreen.firstLogin

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit.WireMockRule
import im.threads.business.transport.threadsGate.ThreadsGateTransport
import io.edna.threads.demo.TestCaseWithLoginInfo
import io.edna.threads.demo.TestMessages
import io.edna.threads.demo.appCode.activity.MainActivity
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import io.edna.threads.demo.kaspressoSreens.DemoLoginScreen
import kotlinx.coroutines.launch
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirstLoginTest : TestCaseWithLoginInfo() {
    private val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<MainActivity>(intent)

    @get:Rule
    val wireMockRule = WireMockRule(port)

    init {
        applyDefaultUserToDemoApp()
        // initUserDirectly()
        prepareMocks()
        coroutineScope.launch {
            ThreadsGateTransport.transportUpdatedChannel.collect {
                it.client = okHttpClient
                it.webSocket = null
            }
        }
    }

    @Before
    fun before() {
        wireMockRule.stubFor(
            WireMock.get(WireMock.urlEqualTo("/history"))
                .willReturn(
                    WireMock.aResponse()
                        .withBody(TestMessages.emptyHistoryMessage)
                        .withHeader("Content-Type", "application/json")
                )
        )
    }

    @Test
    fun firstLogin() {
        val textToSend = "Hello, Edna! This is a test message"

        DemoLoginScreen {
            loginButton {
                click()
            }
        }
        ChatMainScreen {
            inputEditView { isVisible() }
            welcomeScreen { isVisible() }

            inputEditView.typeText(textToSend)
            sendMessageBtn {
                isVisible()
                click()
            }
            recyclerView {
                isVisible()
                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    itemText.containsText(textToSend)
                }
            }
        }
    }
}
