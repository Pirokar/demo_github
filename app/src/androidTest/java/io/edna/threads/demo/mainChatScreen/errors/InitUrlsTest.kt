package io.edna.threads.demo.mainChatScreen.errors

import im.threads.ui.config.ConfigBuilder
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.BaseTestCase
import org.junit.Test

class InitUrlsTest : BaseTestCase() {

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
