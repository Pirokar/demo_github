package io.edna.threads.demo.mainChatScreen.mainTests

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.edna.threads.demo.BaseTestCase
import io.edna.threads.demo.R
import io.edna.threads.demo.TestMessages
import io.edna.threads.demo.appCode.activity.MainActivity
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
                isVisible()
                assert(getSize() == 11)
                firstChild<ChatMainScreen.ChatRecyclerItem> {
                    itemText.isVisible()
                    itemText.containsText("24")
                    itemText.containsText("2023")
                }
                childAt<ChatMainScreen.ChatRecyclerItem>(4) {
                    star.isVisible()
                    totalStarsCount.isVisible()
                    rateStarsCount.hasText("1")
                    totalStarsCount.hasText("1")
                    fromTextSurvey.isVisible()
                    fromTextSurvey.hasText(im.threads.R.string.ecc_from)
                    surveyHeader.isVisible()
                    surveyHeader.hasText("Оцените наше обслуживание")
                }
                childAt<ChatMainScreen.ChatRecyclerItem>(5) {
                    star.isVisible()
                    totalStarsCount.isVisible()
                    rateStarsCount.hasText("3")
                    totalStarsCount.hasText("5")
                    fromTextSurvey.isVisible()
                    fromTextSurvey.hasText(im.threads.R.string.ecc_from)
                    surveyHeader.isVisible()
                    surveyHeader.hasText("Оцените насколько мы решили ваш вопрос")
                }
                childAt<ChatMainScreen.ChatRecyclerItem>(6) {
                    star.isVisible()
                    totalStarsCount.isVisible()
                    rateStarsCount.hasText("4")
                    totalStarsCount.hasText("5")
                    fromTextSurvey.isVisible()
                    fromTextSurvey.hasText(im.threads.R.string.ecc_from)
                    surveyHeader.isVisible()
                    surveyHeader.hasText("Оцените насколько внимательным был наш сотрудник")
                }
                childAt<ChatMainScreen.ChatRecyclerItem>(8) {
                    askForRateText.hasText(im.threads.R.string.ecc_ask_to_rate)
                    thumbUp.isVisible()
                    thumbUp.isClickable()
                    thumbDown.isVisible()
                    thumbDown.isClickable()
                }
                childAt<ChatMainScreen.ChatRecyclerItem>(9) {
                    askForRateText.hasText("Оцените насколько мы решили ваш вопрос")
                    ratingStars.isVisible()
                }
                childAt<ChatMainScreen.ChatRecyclerItem>(10) {
                    askForRateText.hasText("Оцените насколько внимательным был наш сотрудник")
                    ratingStars.isVisible()
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
                isVisible()
                hasDescendant { withText("Среднее время ожидания ответа составляет 2 минуты") }
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
                hasDescendant { withText(context.getString(im.threads.R.string.ecc_typing)) }
            }
        }
    }
}
