package edna.chatcenter.demo.mainChatScreen.mainTests

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import edna.chatcenter.demo.BaseTestCase
import edna.chatcenter.demo.R
import edna.chatcenter.demo.appCode.activity.MainActivity
import edna.chatcenter.demo.assert
import edna.chatcenter.demo.kaspressoSreens.ChatMainScreen
import edna.chatcenter.demo.waitListForNotEmpty
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

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        ChatMainScreen {
            chatItemsRecyclerView.waitListForNotEmpty(5000)

            assert("Кнопка открытия верхнего должна быть кликабельной") { popupMenuButton.isClickable() }
            popupMenuButton.click()

            assert("Поле поиска должно быть видимо") { searchInput.isVisible() }

            searchInput.typeText("Edn")

            searchRecycler {
                waitListForNotEmpty(5000)
                assert(getSize() == 2) { "Размер списка после поиска \"Edn\" должен равняться 2" }

                firstChild<ChatMainScreen.SearchRecyclerItem> {
                    assert("Первый элемент в списке должен содержать имя \"Вы\"") {
                        nameTextView.hasText(edna.chatcenter.ui.R.string.ecc_you)
                    }
                    assert("Первый элемент в списке не содержит правильный текст") {
                        messageTextView.hasText("Добро пожаловать в наш чат! А кто такие Edna?")
                    }
                }
                lastChild<ChatMainScreen.SearchRecyclerItem> {
                    assert("Последний элемент в списке должен содержать имя \"Оператор5 Фёдоровна\"") {
                        nameTextView.hasText("Оператор5 Фёдоровна")
                    }
                    assert("Последний элемент в списке не содержит правильный текст") {
                        messageTextView.hasText("Edna – современное решение для построения диалога с клиентом")
                    }
                }
                firstChild<ChatMainScreen.SearchRecyclerItem> { click() }
            }
            chatItemsRecyclerView {
                childAt<ChatMainScreen.ChatRecyclerItem>(5) {
                    assert("Искомый элемент не виден", ::isVisible, ::isDisplayed)
                }
            }
        }
    }

    @Test
    fun testSearchClear() {
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_text_response))
        openChatFromDemoLoginPage()

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        ChatMainScreen {
            assert("Кнопка открытия верхнего должна быть кликабельной") { popupMenuButton.isClickable() }
            popupMenuButton.click()

            assert("Поле поиска должно быть видимо") { searchInput.isVisible() }
            searchInput.typeText("Edn")

            assert("Список результатов поиска должен быть виден") { searchRecycler.isVisible() }
            searchInput.clearText()
            assert("Список результатов поиска должен быть не виден") { searchRecycler.isGone() }
            assert("Список сообщений должен быть виден") { chatItemsRecyclerView.isVisible() }
            searchInput.typeText("Edn")
            assert("Список результатов поиска должен быть виден") { searchRecycler.isVisible() }
            closeSoftKeyboard()
            pressBack()
            assert("Список результатов поиска должен быть не виден") { searchRecycler.isGone() }
            assert("Список сообщений должен быть виден") { chatItemsRecyclerView.isVisible() }
        }
    }
}
