package io.edna.threads.demo

import android.app.Application
import io.edna.threads.demo.business.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class EdnaThreadsApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@EdnaThreadsApplication)
            modules(appModule)
        }
    }
}
