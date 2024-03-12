package io.edna.threads.demo.mainChatScreen.mainTests

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.edna.threads.demo.BaseTestCase
import io.edna.threads.demo.appCode.activity.MainActivity
import io.edna.threads.demo.assert
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EnabledInputTest : BaseTestCase(true) {

    private val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<MainActivity>(intent)

    init {
        applyDefaultUserToDemoApp()
        prepareHttpMocks()
        prepareWsMocks()
    }

    @Test
    fun startWithDisableInputTest() {
        openChatFromDemoLoginPage()
        ChatMainScreen {
            inputEditView {
                assert("Поле для ввода должно быть видимым и активным") {
                    isVisible()
                    isEnabled()
                }
            }
            recordButton {
                assert("Кнопка аудио сообщения должна быть видимой и активной") {
                    isVisible()
                    isEnabled()
                }
            }
            addAttachmentBtn {
                assert("Кнопка добавить вложение должна быть видимой и активной") {
                    isVisible()
                    isEnabled()
                }
            }
        }
    }
}
