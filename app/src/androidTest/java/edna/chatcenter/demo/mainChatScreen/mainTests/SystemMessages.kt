package edna.chatcenter.demo.mainChatScreen.mainTests

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withText
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
                assert(getSize() > 0) { "Неверное количество сообщений в списке" }

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
    fun surveyDisplayTextTest() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        sendMessageToSocket(TestMessages.surveyDisplayTextMock)

        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(getSocketTimeout() + 1000)
                assert("Список сообщений должен быть видим") { isVisible() }

                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    questionText {
                        assert("Текст \"Насколько вам все понравилось?\" должен быть видим") { isVisible() }
                        assert("Текст \"Насколько вам все понравилось?\" не соответствует отображаемому") {
                            hasText("Насколько вам все понравилось?")
                        }
                    }
                    ratingStars {
                        assert("Звезды рейтинга в сообщении должны быть видимы") { isVisible() }
                    }
                }
            }
        }
    }

    @Test
    fun surveyEmptyDisplayTextTest() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        sendMessageToSocket(TestMessages.surveyEmptyDisplayTextMock)

        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(getSocketTimeout() + 1000)
                assert("Список сообщений должен быть видим") { isVisible() }

                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    questionText {
                        assert("Текст \"Оцените насколько мы решили ваш вопрос\" должен быть видим") { isVisible() }
                        assert("Текст \"Оцените насколько мы решили ваш вопрос\" не соответствует отображаемому") {
                            hasText("Оцените насколько мы решили ваш вопрос")
                        }
                    }
                }
            }
        }
    }

    @Test
    fun surveyNoDisplayTextTest() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        sendMessageToSocket(TestMessages.surveyNoDisplayTextMock)

        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(getSocketTimeout() + 1000)
                assert("Список сообщений должен быть видим") { isVisible() }

                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    questionText {
                        assert("Текст \"Оцените насколько мы решили ваш вопрос\" должен быть видим") { isVisible() }
                        assert("Текст \"Оцените насколько мы решили ваш вопрос\" не соответствует отображаемому") {
                            hasText("Оцените насколько мы решили ваш вопрос")
                        }
                    }
                }
            }
        }
    }

    @Test
    fun surveyThumbsDisplayTextTest() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        sendMessageToSocket(TestMessages.thumbsDisplayTextMock)

        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(getSocketTimeout() + 1000)
                assert("Список сообщений должен быть видим") { isVisible() }

                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    assert("Текст должен быть \"Мы хороши?\"") { questionText.hasText("Мы хороши?") }
                    assert("Палец вверх должен быть видим и кликабелен") {
                        thumbUp::isVisible
                        thumbUp::isClickable
                    }
                    assert("Палец вниз должен быть видим и кликабелен") {
                        thumbDown::isVisible
                        thumbDown::isClickable
                    }
                }
            }
        }
    }

    @Test
    fun surveyThumbsEmptyDisplayTextTest() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        sendMessageToSocket(TestMessages.thumbsEmptyDisplayTextMock)

        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(getSocketTimeout() + 1000)
                assert("Список сообщений должен быть видим") { isVisible() }

                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    assert("Текст должен быть \"Доброжелательность\"") { questionText.hasText("Доброжелательность") }
                }
            }
        }
    }

    @Test
    fun surveyThumbsNoDisplayTextTest() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        sendMessageToSocket(TestMessages.thumbsNoDisplayTextMock)

        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(getSocketTimeout() + 1000)
                assert("Список сообщений должен быть видим") { isVisible() }

                lastChild<ChatMainScreen.ChatRecyclerItem> {
                    assert("Текст должен быть \"Доброжелательность\"") { questionText.hasText("Доброжелательность") }
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
        sendOperatorIsTypingMessage()

        ChatMainScreen {
            toolbarSubtitle {
                val typingText = context.getString(edna.chatcenter.ui.R.string.ecc_typing)
                assert("Подзаголовок тулбара должен быть видимый") { isVisible() }
                assert("Подзаголовок тулбара должен содержать текст: $typingText") {
                    hasText(
                        typingText
                    )
                }
            }
        }
    }

    @Test
    fun buttonsSurveyHasAllTexts() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        sendMessageToSocket(TestMessages.buttonsSurveyMock)

        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(5000)
                assert("Список сообщений должен быть видим") { isVisible() }
                assert("Содержит текст \"Доброжелательность\"") {
                    hasDescendant {
                        withText("Доброжелательность")
                    }
                }
                assert("Содержит текст \"Text 1\"") {
                    hasDescendant {
                        withText("Text 1")
                    }
                }
                assert("Содержит текст \"Text 1\"") {
                    hasDescendant {
                        withText("Text 2")
                    }
                }
                assert("Text 3-1") {
                    hasDescendant {
                        withText("Text 3-1")
                    }
                }
                assert("Содержит текст \"Text 3-2\"") {
                    hasDescendant {
                        withText("Text 3-2")
                    }
                }
                assert("Содержит текст \"Text 3-3\"") {
                    hasDescendant {
                        withText("Text 3-3")
                    }
                }
                assert("Содержит текст \"Text 3-4\"") {
                    hasDescendant {
                        withText("Text 3-4")
                    }
                }
                assert("Содержит текст \"Text 3-5\"") {
                    hasDescendant {
                        withText("Text 3-5")
                    }
                }
                assert("Содержит текст \"Text 3-6\"") {
                    hasDescendant {
                        withText("Text 3-6")
                    }
                }
                assert("Содержит текст \"Text 3-7\"") {
                    hasDescendant {
                        withText("Text 3-7")
                    }
                }
                assert("Содержит текст \"Text 3-8\"") {
                    hasDescendant {
                        withText("Text 3-8")
                    }
                }
                assert("Содержит текст \"Text 3-9\"") {
                    hasDescendant {
                        withText("Text 3-9")
                    }
                }
                assert("Содержит текст \"Text 3-10\"") {
                    hasDescendant {
                        withText("Text 3-10")
                    }
                }
                assert("Содержит текст \"Text 1\"") {
                    hasDescendant {
                        withText("Text 2")
                    }
                }
            }
        }
    }

    @Test
    fun whenClickToButtonInSurvey_ThenItDisappears() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        sendMessageToSocket(TestMessages.buttonsSurveyMock)

        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(5000)
                assert("Список сообщений должен быть видим") { isVisible() }

                onView(withText("Text 1")).perform(
                    ViewActions.click()
                )

                Thread.sleep(500)

                assert("Не содержит опрос с текстом \"Доброжелательность\"") {
                    hasNotDescendant {
                        withText("Доброжелательность")
                    }
                }
            }
        }
    }

    @Test
    fun whenInputIsBlockedBySurveyButtons_ThenItBlocked() {
        prepareHttpMocks()
        openChatFromDemoLoginPage()
        sendMessageToSocket(TestMessages.buttonsSurveyMock)

        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(5000)
                assert("Список сообщений должен быть видим") { isVisible() }
            }

            inputEditView.click()
            assert("Поле ввода не должно быть активно") {
                inputEditView.isNotFocused()
            }
        }
    }
}
