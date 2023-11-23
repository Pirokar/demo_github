package im.threads.business.config

import androidx.test.core.app.ApplicationProvider
import im.threads.business.core.ContextHolder
import im.threads.business.serviceLocator.core.startEdnaLocator
import im.threads.business.serviceLocator.coreSLModule
import im.threads.ui.serviceLocator.uiSLModule

open class ConfigTestBaseClass() {
    protected val ednaMockScheme = "http"
    protected val ednaMockHost = "localhost"
    protected val ednaMockPort = 8080
    protected val ednaMockUrl = "$ednaMockScheme://$ednaMockHost:$ednaMockPort/"
    protected val ednaMockThreadsGateUrl = "ws://$ednaMockHost:$ednaMockPort/gate/socket"
    protected val ednaMockThreadsGateProviderUid = "TEST_93jLrtnipZsfbTddRfEfbyfEe5LKKhTl"

    open fun before() {
        ContextHolder.context = ApplicationProvider.getApplicationContext()
        startEdnaLocator { modules(coreSLModule, uiSLModule) }
    }
}
