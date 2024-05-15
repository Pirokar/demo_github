package edna.chatcenter.demo.mainChatScreen.mainTests

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import edna.chatcenter.demo.BaseTestCase
import edna.chatcenter.demo.appCode.activity.MainActivity
import edna.chatcenter.demo.assert
import edna.chatcenter.demo.kaspressoSreens.ChatMainScreen
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
    fun inputTest() {
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
