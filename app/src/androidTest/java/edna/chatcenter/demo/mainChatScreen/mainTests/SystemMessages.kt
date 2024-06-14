package edna.chatcenter.demo.mainChatScreen.mainTests

import android.content.Intent
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import edna.chatcenter.demo.BaseTestCase
import edna.chatcenter.demo.R
import edna.chatcenter.demo.TestMessages
import edna.chatcenter.demo.appCode.activity.MainActivity
import edna.chatcenter.demo.assert
import edna.chatcenter.demo.kaspressoSreens.ChatMainScreen
import edna.chatcenter.demo.waitListForNotEmpty
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SystemMessages : BaseTestCase() {
    private val intent = Intent(context, MainActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<MainActivity>(intent)

    private val textToSend = "Hello, Edna! This is a test message"

    init {
        applyDefaultUserToDemoApp()
        prepareWsMocks()
    }

    @Test
    fun systemMessagesTest() {
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_full_system_response))
        openChatFromDemoLoginPage()
        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(5000)
                assert("Список сообщений должен быть видим") { isVisible() }
                assert(getSize() == 11) { "Неверное количество сообщений в списке" }

                firstChild<ChatMainScreen.ChatRecyclerItem> {
                    itemText {
                        assert("Первый элемент списка должен быть видимым") { isVisible() }
                        assert("Первый элемент списка должен содержать число 24") { containsText("24") }
                        assert("Первый элемент списка должен содержать число 2023") { containsText("2023") }
                    }
                }
            }
        }
    }

    @Test
    fun historySurvey1Test() {
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_system_survey_1))
        openChatFromDemoLoginPage()
        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(getSocketTimeout() + 1000)
                assert("Список сообщений должен быть видим") { isVisible() }

                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    surveyHeader {
                        assert("Текст отзыва должен быть видим") { isVisible() }
                        assert("Текст \"Оцените наше обслуживание\" не соответствует отображаемому") {
                            hasText("Оцените наше обслуживание")
                        }
                    }

                    oneThumb {
                        assert(
                            "Иконка с пальцем вверх должна быть видимой",
                            ::isVisible
                        )
                    }
                }
            }
        }
    }

    @Test
    fun historySurvey2Test() {
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_system_survey_2))
        openChatFromDemoLoginPage()
        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(getSocketTimeout() + 1000)
                assert("Список сообщений должен быть видим") { isVisible() }

                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    assert("Элемент \"Звезда\" должен быть видимый") { star.isVisible() }
                    assert("Общее число звезд должно быть видимо") { totalStarsCount.isVisible() }
                    assert("Число проставленных звезд должно равняться 3") { rateStarsCount.hasText("3") }
                    assert("Общее число звезд должно равняться 5") { totalStarsCount.hasText("5") }

                    fromTextSurvey {
                        assert("Текст \"${context.getString(edna.chatcenter.ui.R.string.ecc_from)}\" должен быть видим") { isVisible() }
                        assert("Текст \"${context.getString(edna.chatcenter.ui.R.string.ecc_from)}\" не соответствует отображаемому") {
                            hasText(edna.chatcenter.ui.R.string.ecc_from)
                        }
                    }

                    surveyHeader {
                        assert("Текст отзыва должен быть видим") { isVisible() }
                        assert("Текст \"Оцените насколько мы решили ваш вопрос\" не соответствует отображаемому") {
                            hasText("Оцените насколько мы решили ваш вопрос")
                        }
                    }
                }
            }
        }
    }

    @Test
    fun historySurvey3Test() {
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_system_survey_3))
        openChatFromDemoLoginPage()
        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(getSocketTimeout() + 1000)
                assert("Список сообщений должен быть видим") { isVisible() }

                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    assert("Элемент \"Звезда\" должен быть видимый") { star.isVisible() }
                    assert("Общее число звезд должно быть видимо") { totalStarsCount.isVisible() }
                    assert("Число проставленных звезд должно равняться 4") { rateStarsCount.hasText("4") }
                    assert("Общее число звезд должно равняться 5") { totalStarsCount.hasText("5") }

                    fromTextSurvey {
                        assert("Текст \"${context.getString(edna.chatcenter.ui.R.string.ecc_from)}\" должен быть видим") { isVisible() }
                        assert("Текст \"${context.getString(edna.chatcenter.ui.R.string.ecc_from)}\" не соответствует отображаемому") {
                            hasText(edna.chatcenter.ui.R.string.ecc_from)
                        }
                    }

                    surveyHeader {
                        assert("Текст отзыва должен быть видим") { isVisible() }
                        assert("Текст \"Оцените насколько внимательным был наш сотрудник\" не соответствует отображаемому") {
                            hasText("Оцените насколько внимательным был наш сотрудник")
                        }
                    }
                }
            }
        }
    }

    @Test
    fun historySurvey4Test() {
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_system_survey_4))
        openChatFromDemoLoginPage()
        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(getSocketTimeout() + 1000)
                assert("Список сообщений должен быть видим") { isVisible() }

                childAt<ChatMainScreen.ChatRecyclerItem>(1) {
                    val text = context.getString(edna.chatcenter.ui.R.string.ecc_ask_to_rate)
                    assert("Текст должен быть \"$text") { askForRateText.hasText(text) }
                    assert("Палец вверх должен быть видим и кликабелен") {
                        thumbUp::isVisible
                        thumbUp::isClickable
                    }
                    assert("Палец вниз должен быть видим и кликабелен") {
                        thumbDown::isVisible
                        thumbDown::isClickable
                    }
                }

                childAt<ChatMainScreen.ChatRecyclerItem>(2) {
                    assert("Текст должен быть \"Оцените насколько мы решили ваш вопрос\"") {
                        askForRateText.hasText("Оцените насколько мы решили ваш вопрос")
                    }
                }

                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    assert("Текст должен быть \"Оцените насколько хорош был наш сотрудник\"") {
                        askForRateText.hasText("Оцените насколько хорош был наш сотрудник")
                    }
                    assert("Звезды рейтинга в сообщении с индексом 9 должны быть видимы") { ratingStars.isVisible() }
                }
            }
        }
    }

    @Test
    fun waitingTimeTest() {
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_errors_response))
        openChatFromDemoLoginPage()

        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(30000)
                assert("Список сообщений должен быть видим") { isVisible() }
                val textToCheck = "Среднее время ожидания ответа составляет 2 минуты"
                assert("Список должен содержать сообщение: \"$textToCheck\"") {
                    hasDescendant { withText(textToCheck) }
                }
            }
        }
    }

    @Test
    fun typingTest() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        sendHelloMessageFromUser()

        ChatMainScreen {
            chatItemsRecyclerView {
                sendMessageToSocket(TestMessages.typingMessage)
                assert("Список должен содержать сообщение: \"${context.getString(edna.chatcenter.ui.R.string.ecc_typing)}\"") {
                    hasDescendant { withText(context.getString(edna.chatcenter.ui.R.string.ecc_typing)) }
                }
            }
        }
    }
}
