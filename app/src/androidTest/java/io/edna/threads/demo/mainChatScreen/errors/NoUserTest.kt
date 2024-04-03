package io.edna.threads.demo.mainChatScreen.errors

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import edna.chatcenter.ui.BuildConfig
import edna.chatcenter.ui.R
import edna.chatcenter.ui.visual.activities.ChatActivity
import edna.chatcenter.ui.visual.core.ChatCenterUI
import io.edna.threads.demo.BaseTestCase
import io.edna.threads.demo.appCode.ednaMockThreadsGateProviderUid
import io.edna.threads.demo.assert
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoUserTest : BaseTestCase() {
    private val intent = Intent(ApplicationProvider.getApplicationContext(), ChatActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<ChatActivity>(intent)

    private val chatCenterUI: ChatCenterUI = ChatCenterUI(context)

    init {
        chatCenterUI.init(
            ednaMockThreadsGateProviderUid,
            "",
            config
        )

        BuildConfig.IS_ANIMATIONS_DISABLED.set(true)
        prepareHttpMocks()
        prepareWsMocks()
    }

    @Test
    fun testNoUser() {
        chatCenterUI.deauthorizeUser()
        ChatMainScreen {
            errorImage {
                assert("Изображение с ошибкой должно быть видимо") { isVisible() }
            }
            errorText {
                assert("Текст с ошибкой должен быть видим") { isVisible() }
                assert("Текст с ошибкой не содержит текст: \"${context.getString(R.string.ecc_no_user_id)}\"") {
                    hasText(context.getString(R.string.ecc_no_user_id))
                }
            }
            errorRetryBtn { isVisible() }
        }
    }
}
