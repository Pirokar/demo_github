package io.edna.threads.demo.mainChatScreen.errors

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import im.threads.ui.config.ConfigBuilder
import im.threads.ui.core.ThreadsLib
import org.junit.Test

class TestInitUrls : TestCase() {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private val port = 9100
    private val testServerBaseUrl = "http://localhost:$port/"
    private val testDatastoreUrl = "http://localhost:$port/"
    private val testThreadsGateUrl = "ws://localhost:$port/gate/socket"
    private val testThreadsGateProviderUid = "TEST_93jLrtnipZsfbTddRfEfbyfEe5LKKhTl"

    @Test
    fun testWithoutServerBaseUrl() {
        val configBuilder = ConfigBuilder(context)
            .historyLoadingCount(50)
            .isDebugLoggingEnabled(true)
            .showAttachmentsButton()
            .datastoreUrl(testDatastoreUrl)
            .threadsGateUrl(testThreadsGateUrl)
            .threadsGateProviderUid(testThreadsGateProviderUid)
            .setNewChatCenterApi()

        assert(initWithoutUrlIsPassed(configBuilder))
    }

    @Test
    fun testWithoutDatastoreUrl() {
        val configBuilder = ConfigBuilder(context)
            .historyLoadingCount(50)
            .isDebugLoggingEnabled(true)
            .showAttachmentsButton()
            .serverBaseUrl(testServerBaseUrl)
            .threadsGateUrl(testThreadsGateUrl)
            .threadsGateProviderUid(testThreadsGateProviderUid)
            .setNewChatCenterApi()

        assert(initWithoutUrlIsPassed(configBuilder))
    }

    @Test
    fun testWithoutThreadsGateUrl() {
        val configBuilder = ConfigBuilder(context)
            .historyLoadingCount(50)
            .isDebugLoggingEnabled(true)
            .showAttachmentsButton()
            .serverBaseUrl(testServerBaseUrl)
            .datastoreUrl(testDatastoreUrl)
            .threadsGateProviderUid(testThreadsGateProviderUid)
            .setNewChatCenterApi()

        assert(initWithoutUrlIsPassed(configBuilder))
    }

    @Test
    fun testWithoutThreadsGateProviderUid() {
        val configBuilder = ConfigBuilder(context)
            .historyLoadingCount(50)
            .isDebugLoggingEnabled(true)
            .showAttachmentsButton()
            .serverBaseUrl(testServerBaseUrl)
            .datastoreUrl(testDatastoreUrl)
            .threadsGateUrl(testThreadsGateUrl)
            .setNewChatCenterApi()

        assert(initWithoutUrlIsPassed(configBuilder))
    }

    private fun initWithoutUrlIsPassed(config: ConfigBuilder): Boolean {
        var isErrorHappened = false
        try {
            ThreadsLib.init(config)
        } catch (exc: Exception) {
            isErrorHappened = true
        }

        return isErrorHappened
    }
}
