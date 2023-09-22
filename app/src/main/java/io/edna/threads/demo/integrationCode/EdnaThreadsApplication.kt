package io.edna.threads.demo.integrationCode

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import io.edna.threads.demo.BuildConfig
import io.edna.threads.demo.appCode.business.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class EdnaThreadsApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@EdnaThreadsApplication)
            modules(appModule)
        }

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = !BuildConfig.DEBUG
    }
}
