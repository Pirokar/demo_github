package io.edna.threads.demo.mainChatScreen.mainTests

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.edna.threads.demo.BaseTestCase
import io.edna.threads.demo.R
import io.edna.threads.demo.TestMessages
import io.edna.threads.demo.appCode.activity.MainActivity
import io.edna.threads.demo.assert
import io.edna.threads.demo.kaspressoSreens.ChatMainScreen
import io.edna.threads.demo.waitListForNotEmpty
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SystemMessages : BaseTestCase() {
    private val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<MainActivity>(intent)

    private val textToSend = "Hello, Edna! This is a test message"

    init {
        applyDefaultUserToDemoApp()
        prepareWsMocks()
    }

    @Test
    fun systemMessagesTest() {
        prepareHttpMocks(historyAnswer = readTextFileFromRawResourceId(R.raw.history_system_response))
        openChatFromDemoLoginPage()
        ChatMainScreen {
            chatItemsRecyclerView {
                waitListForNotEmpty(5000)
                assert("Список сообщений должен быть видим") { isVisible() }
                assert(getSize() == 11) { "Неверное количество сообщений в списке" }

                childWith<ChatMainScreen.ChatRecyclerItem> {
                    withDescendant { containsText("Оцените наше обслуживание") }
                }.apply {
                    thumbUp {
                        assert("Иконка с пальцем вверх должна быть видимой и кликабельной", ::isVisible, ::isClickable)
                    }
                    thumbDown {
                        assert("Иконка с пальцем вниз должна быть видимой и кликабельной", ::isVisible, ::isClickable)
                    }
                }
                firstChild<ChatMainScreen.ChatRecyclerItem> {
                    itemText {
                        assert("Первый элемент списка должен быть видимым") { isVisible() }
                        assert("Первый элемент списка должен содержать число 24") { containsText("24") }
                        assert("Первый элемент списка должен содержать число 2023") { containsText("2023") }
                    }
                }
                childAt<ChatMainScreen.ChatRecyclerItem>(4) {
                    assert("Элемент \"Звезда\" должен быть видимый") { star.isVisible() }
                    assert("Общее число звезд должно быть видимо") { totalStarsCount.isVisible() }
                    assert("Число проставленных звезд должно равняться 1") { rateStarsCount.hasText("1") }
                    assert("Общее число звезд должно равняться 1") { totalStarsCount.hasText("1") }

                    fromTextSurvey {
                        assert("Текст \"${context.getString(im.threads.R.string.ecc_from)}\" должен быть видим") { isVisible() }
                        assert("Текст \"${context.getString(im.threads.R.string.ecc_from)}\" не соответствует отображаемому") {
                            hasText(im.threads.R.string.ecc_from)
                        }
                    }

                    surveyHeader {
                        assert("Текст отзыва должен быть видим") { isVisible() }
                        assert("Текст \"Оцените наше обслуживание\" не соответствует отображаемому") {
                            hasText("Оцените наше обслуживание")
                        }
                    }
                }
                childAt<ChatMainScreen.ChatRecyclerItem>(5) {
                    assert("Элемент \"Звезда\" должен быть видимый") { star.isVisible() }
                    assert("Общее число звезд должно быть видимо") { totalStarsCount.isVisible() }
                    assert("Число проставленных звезд должно равняться 3") { rateStarsCount.hasText("3") }
                    assert("Общее число звезд должно равняться 5") { totalStarsCount.hasText("5") }

                    fromTextSurvey {
                        assert("Текст \"${context.getString(im.threads.R.string.ecc_from)}\" должен быть видим") { isVisible() }
                        assert("Текст \"${context.getString(im.threads.R.string.ecc_from)}\" не соответствует отображаемому") {
                            hasText(im.threads.R.string.ecc_from)
                        }
                    }

                    surveyHeader {
                        assert("Текст отзыва должен быть видим") { isVisible() }
                        assert("Текст \"Оцените насколько мы решили ваш вопрос\" не соответствует отображаемому") {
                            hasText("Оцените насколько мы решили ваш вопрос")
                        }
                    }
                }
                childAt<ChatMainScreen.ChatRecyclerItem>(6) {
                    assert("Элемент \"Звезда\" должен быть видимый") { star.isVisible() }
                    assert("Общее число звезд должно быть видимо") { totalStarsCount.isVisible() }
                    assert("Число проставленных звезд должно равняться 4") { rateStarsCount.hasText("4") }
                    assert("Общее число звезд должно равняться 5") { totalStarsCount.hasText("5") }

                    fromTextSurvey {
                        assert("Текст \"${context.getString(im.threads.R.string.ecc_from)}\" должен быть видим") { isVisible() }
                        assert("Текст \"${context.getString(im.threads.R.string.ecc_from)}\" не соответствует отображаемому") {
                            hasText(im.threads.R.string.ecc_from)
                        }
                    }

                    surveyHeader {
                        assert("Текст отзыва должен быть видим") { isVisible() }
                        assert("Текст \"Оцените насколько внимательным был наш сотрудник\" не соответствует отображаемому") {
                            hasText("Оцените насколько внимательным был наш сотрудник")
                        }
                    }
                }
                childAt<ChatMainScreen.ChatRecyclerItem>(9) {
                    assert("Текст \"Оцените насколько мы решили ваш вопрос\" не соответствует отображаемому в сообщении с индексом 9") {
                        askForRateText.hasText("Оцените насколько мы решили ваш вопрос")
                    }
                    assert("Звезды рейтинга в сообщении с индексом 9 должны быть видимы") { ratingStars.isVisible() }
                }
                childAt<ChatMainScreen.ChatRecyclerItem>(10) {
                    assert("Текст \"Оцените насколько внимательным был наш сотрудник\" не соответствует отображаемому в сообщении с индексом 10") {
                        askForRateText.hasText("Оцените насколько внимательным был наш сотрудник")
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
                waitListForNotEmpty(5000)
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
                assert("Список должен содержать сообщение: \"${context.getString(im.threads.R.string.ecc_typing)}\"") {
                    hasDescendant { withText(context.getString(im.threads.R.string.ecc_typing)) }
                }
            }
        }
    }
}
