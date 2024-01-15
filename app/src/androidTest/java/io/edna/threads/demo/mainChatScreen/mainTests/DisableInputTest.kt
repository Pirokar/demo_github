package io.edna.threads.demo.mainChatScreen.mainTests

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.BaseTestCase
import io.edna.threads.demo.appCode.activity.MainActivity
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DisableInputTest : BaseTestCase() {

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
        ThreadsLib.getInstance().disableUserInput(true)
        openChatFromDemoLoginPage()
        ChatMainScreen {
            inputEditView {
                io.edna.threads.demo.assert("Поле для ввода должно быть видимым и неактивным") {
                    isVisible()
                    isDisabled()
                }
            }
            recordButton {
                io.edna.threads.demo.assert("Кнопка аудио сообщения должна быть видимой и неактивной") {
                    isVisible()
                    isDisabled()
                }
            }
            addAttachmentBtn {
                io.edna.threads.demo.assert("Кнопка добавить вложение должна быть видимой и неактивной") {
                    isVisible()
                    isDisabled()
                }
            }
        }
        ThreadsLib.getInstance().disableUserInput(false)
        ChatMainScreen {
            inputEditView {
                io.edna.threads.demo.assert("Поле для ввода должно быть видимым и активным") {
                    isVisible()
                    isEnabled()
                }
            }
            recordButton {
                io.edna.threads.demo.assert("Кнопка аудио сообщения должна быть видимой и активной") {
                    isVisible()
                    isEnabled()
                }
            }
            addAttachmentBtn {
                io.edna.threads.demo.assert("Кнопка добавить вложение должна быть видимой и активной") {
                    isVisible()
                    isEnabled()
                }
            }
        }
    }

    @Test
    fun startWithEnableInputTest() {
        ThreadsLib.getInstance().disableUserInput(false)
        openChatFromDemoLoginPage()
        ChatMainScreen {
            inputEditView {
                io.edna.threads.demo.assert("Поле для ввода должно быть видимым и активным") {
                    isVisible()
                    isEnabled()
                }
            }
            recordButton {
                io.edna.threads.demo.assert("Кнопка аудио сообщения должна быть видимой и активной") {
                    isVisible()
                    isEnabled()
                }
            }
            addAttachmentBtn {
                io.edna.threads.demo.assert("Кнопка добавить вложение должна быть видимой и активной") {
                    isVisible()
                    isEnabled()
                }
            }
        }
        ThreadsLib.getInstance().disableUserInput(true)
        ChatMainScreen {
            inputEditView {
                io.edna.threads.demo.assert("Поле для ввода должно быть видимым и неактивным") {
                    isVisible()
                    isDisabled()
                }
            }
            recordButton {
                io.edna.threads.demo.assert("Кнопка аудио сообщения должна быть видимой и неактивной") {
                    isVisible()
                    isDisabled()
                }
            }
            addAttachmentBtn {
                io.edna.threads.demo.assert("Кнопка добавить вложение должна быть видимой и неактивной") {
                    isVisible()
                    isDisabled()
                }
            }
        }
    }
}
