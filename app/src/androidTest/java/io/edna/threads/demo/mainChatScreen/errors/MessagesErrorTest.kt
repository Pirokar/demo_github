package io.edna.threads.demo.mainChatScreen.errors

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import io.edna.threads.demo.BaseTestCase
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.activity.MainActivity
import io.edna.threads.demo.assert
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import io.edna.threads.demo.waitListForNotEmpty
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessagesErrorTest : BaseTestCase() {
    private val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<MainActivity>(intent)

    init {
        applyDefaultUserToDemoApp()
        prepareWsMocks()
    }

    @Test
    fun testErrorsInHistory() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_errors_response))

        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(5000)
                val itemsSize = getSize()
                assert("Список сообщений должен быть видимый") { isVisible() }
                assert("Список должен содержать сообщение об ошибке при загрузке файла") {
                    hasDescendant { containsText(context.getString(im.threads.R.string.ecc_some_error_during_load_file)) }
                }
                childAt<ChatMainScreen.ChatRecyclerItem>(1) {
                    itemTime {
                        assert("Элемент с индексом 1 должен быть видимый и кликабельный", ::isVisible, ::isClickable)
                        click()
                    }
                }
                val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                val menuList = device.findObject(By.clazz("android.widget.ListView")).children

                val deleteAction = menuList[1]
                deleteAction.click()
                deleteAction.recycle()

                Thread.sleep(500)

                assert(getSize() == itemsSize - 1)
            }
        }
    }
}
