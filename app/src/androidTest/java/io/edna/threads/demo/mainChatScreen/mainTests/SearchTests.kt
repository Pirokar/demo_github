package io.edna.threads.demo.mainChatScreen.mainTests

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
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import io.edna.threads.demo.waitListForNotEmpty
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchTests : BaseTestCase() {
    private val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<MainActivity>(intent)

    init {
        applyDefaultUserToDemoApp()
        prepareWsMocks()
    }

    @Test
    fun testSearchInHistory() {
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_text_response))
        openChatFromDemoLoginPage()

        Thread.sleep(2000)

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        ChatMainScreen {
            popupMenuButton.isClickable()
            popupMenuButton.click()

            val menuList = device.findObject(By.clazz("android.widget.ListView")).children
            val searchAction = menuList[0]
            searchAction.click()
            searchAction.recycle()

            searchInput.isVisible()

            searchInput.typeText("Edn")

            searchRecycler {
                waitListForNotEmpty(5000)
                assert(getSize() == 2) { "Размер списка после поиска \"Edn\" должен равняться 2" }

                firstChild<ChatMainScreen.SearchRecyclerItem> {
                    nameTextView.hasText(im.threads.R.string.ecc_I)
                    messageTextView.hasText("Добро пожаловать в наш чат! А кто такие Edna?")
                }
                lastChild<ChatMainScreen.SearchRecyclerItem> {
                    nameTextView.hasText("Оператор5 Фёдоровна")
                    messageTextView.hasText("Edna – современное решение для построения диалога с клиентом")
                }
            }
        }
    }
}
