package io.edna.threads.demo.mainChatScreen.errors

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import im.threads.BuildConfig
import im.threads.R
import im.threads.ui.activities.ChatActivity
import im.threads.ui.config.ConfigBuilder
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.BaseTestCase
import io.edna.threads.demo.assert
import io.edna.threads.demo.integrationCode.ednaMockThreadsGateProviderUid
import io.edna.threads.demo.integrationCode.ednaMockThreadsGateUrl
import io.edna.threads.demo.integrationCode.ednaMockUrl
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoUserTest : BaseTestCase() {
    private val intent = Intent(ApplicationProvider.getApplicationContext(), ChatActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<ChatActivity>(intent)

    init {
        ThreadsLib.cleanLibInstance()

        val configBuilder = ConfigBuilder(context)
            .isDebugLoggingEnabled(true)
            .serverBaseUrl(ednaMockUrl)
            .datastoreUrl(ednaMockUrl)
            .threadsGateUrl(ednaMockThreadsGateUrl)
            .threadsGateProviderUid(ednaMockThreadsGateProviderUid)
            .setNewChatCenterApi()
        ThreadsLib.init(configBuilder)

        BuildConfig.IS_ANIMATIONS_DISABLED.set(true)
        prepareWsMocks()
    }

    @Test
    fun testNoUser() {
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
