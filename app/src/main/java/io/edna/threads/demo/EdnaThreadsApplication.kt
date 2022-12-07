package io.edna.threads.demo

import android.app.Application
import im.threads.ui.config.ConfigBuilder
import im.threads.ui.core.ThreadsLib

class EdnaThreadsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val configBuilder = ConfigBuilder(this)
            .surveyCompletionDelay(2000)
            .historyLoadingCount(50)
            .isDebugLoggingEnabled(true)
            .showAttachmentsButton()
        ThreadsLib.init(configBuilder)
    }
}
