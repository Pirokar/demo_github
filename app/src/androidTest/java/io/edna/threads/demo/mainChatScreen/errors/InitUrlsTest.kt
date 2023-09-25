package io.edna.threads.demo.mainChatScreen.errors

import im.threads.business.rest.queries.ednaMockUrl
import im.threads.ui.config.ConfigBuilder
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.BaseTestCase
import io.edna.threads.demo.appCode.ednaMockThreadsGateProviderUid
import io.edna.threads.demo.appCode.ednaMockThreadsGateUrl
import org.junit.Before
import org.junit.Test

class InitUrlsTest : BaseTestCase() {

    @Before
    fun before() {
        ThreadsLib.cleanLibInstance()
    }

    @Test
    fun testWithoutServerBaseUrl() {
        val configBuilder = ConfigBuilder(context)
            .historyLoadingCount(50)
            .isDebugLoggingEnabled(true)
            .showAttachmentsButton()
            .datastoreUrl(ednaMockUrl)
            .threadsGateUrl(ednaMockThreadsGateUrl)
            .threadsGateProviderUid(ednaMockThreadsGateProviderUid)
            .setNewChatCenterApi()

        assert(initWithoutUrlIsPassed(configBuilder))
    }

    @Test
    fun testWithoutDatastoreUrl() {
        val configBuilder = ConfigBuilder(context)
            .historyLoadingCount(50)
            .isDebugLoggingEnabled(true)
            .showAttachmentsButton()
            .serverBaseUrl(ednaMockUrl)
            .threadsGateUrl(ednaMockThreadsGateUrl)
            .threadsGateProviderUid(ednaMockThreadsGateProviderUid)
            .setNewChatCenterApi()

        assert(initWithoutUrlIsPassed(configBuilder))
    }

    @Test
    fun testWithoutThreadsGateUrl() {
        val configBuilder = ConfigBuilder(context)
            .historyLoadingCount(50)
            .isDebugLoggingEnabled(true)
            .showAttachmentsButton()
            .serverBaseUrl(ednaMockUrl)
            .datastoreUrl(ednaMockUrl)
            .threadsGateProviderUid(ednaMockThreadsGateProviderUid)
            .setNewChatCenterApi()

        assert(initWithoutUrlIsPassed(configBuilder))
    }

    @Test
    fun testWithoutThreadsGateProviderUid() {
        val configBuilder = ConfigBuilder(context)
            .historyLoadingCount(50)
            .isDebugLoggingEnabled(true)
            .showAttachmentsButton()
            .serverBaseUrl(ednaMockUrl)
            .datastoreUrl(ednaMockUrl)
            .threadsGateUrl(ednaMockThreadsGateUrl)
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
