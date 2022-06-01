package im.threads.android.core

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import androidx.multidex.MultiDexApplication
import com.edna.android.push_lite.PushController
import com.pandulapeter.beagle.Beagle
import com.pandulapeter.beagle.common.configuration.Behavior
import com.pandulapeter.beagle.common.configuration.toText
import com.pandulapeter.beagle.logCrash.BeagleCrashLogger
import com.pandulapeter.beagle.logOkHttp.BeagleOkHttpLogger
import com.pandulapeter.beagle.modules.AppInfoButtonModule
import com.pandulapeter.beagle.modules.BugReportButtonModule
import com.pandulapeter.beagle.modules.DeviceInfoModule
import com.pandulapeter.beagle.modules.KeylineOverlaySwitchModule
import com.pandulapeter.beagle.modules.LifecycleLogListModule
import com.pandulapeter.beagle.modules.NetworkLogListModule
import com.pandulapeter.beagle.modules.TextModule
import im.threads.ConfigBuilder
import im.threads.ThreadsLib
import im.threads.ThreadsLib.PendingIntentCreator
import im.threads.android.R
import im.threads.android.data.Card
import im.threads.android.di.appModule
import im.threads.android.push.HCMTokenRefresher.requestToken
import im.threads.android.ui.BottomNavigationActivity
import im.threads.android.ui.developer_options.DeveloperOptionsActivity
import im.threads.android.use_cases.developer_options.DeveloperOptionsInteractor
import im.threads.android.use_cases.developer_options.DeveloperOptionsUseCase
import im.threads.android.utils.PrefUtilsApp.getCards
import im.threads.android.utils.PrefUtilsApp.getTheme
import im.threads.android.utils.PrefUtilsApp.getTransportConfig
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import okhttp3.Interceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ThreadsDemoApplication : MultiDexApplication() {
    private var disposable: Disposable? = null
    private lateinit var developerOptions: DeveloperOptionsUseCase

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        developerOptions = DeveloperOptionsInteractor(appContext)

        startKoin {
            androidContext(applicationContext)
            modules(appModule)
        }

        checkCurrentServer()

        disposable = Completable.fromAction { requestToken(this) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}) {}

        PushController.getInstance(this).init()

        val configBuilder = ConfigBuilder(this)
            .pendingIntentCreator(CustomPendingIntentCreator())
            .unreadMessagesCountListener { t: Int -> unreadMessagesSubject.onNext(t) }
            .surveyCompletionDelay(2000)
            .historyLoadingCount(50)
            .isDebugLoggingEnabled(true)
            .certificateRawResIds(listOf(R.raw.edna))
            .networkInterceptor((BeagleOkHttpLogger.logger as? Interceptor?))

        val transportConfig = getTransportConfig(this)
        if (transportConfig != null) {
            configBuilder.serverBaseUrl(transportConfig.baseUrl)
                .threadsGateUrl(transportConfig.threadsGateUrl)
                .threadsGateProviderUid(transportConfig.threadsGateProviderUid)
                .threadsGateHCMProviderUid(transportConfig.threadsGateHCMProviderUid)
        }

        ThreadsLib.init(configBuilder)
        Beagle.initialize(
            this,
            behavior = Behavior(
                bugReportingBehavior = Behavior.BugReportingBehavior(
                    crashLoggers = listOf(BeagleCrashLogger)
                ),
                networkLogBehavior = Behavior.NetworkLogBehavior(
                    networkLoggers = listOf(BeagleOkHttpLogger)
                )
            )
        )
        Beagle.set(
            AppInfoButtonModule(getString(R.string.about_app).toText()),
            TextModule(
                getString(R.string.developer_options),
                TextModule.Type.BUTTON,
                onItemSelected = {
                    val intent = Intent(
                        this, DeveloperOptionsActivity::class.java
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(intent)
                }
            ),
            NetworkLogListModule(),
            LifecycleLogListModule(),
            KeylineOverlaySwitchModule(),
            DeviceInfoModule(),
            BugReportButtonModule(),
        )
    }

    override fun onTerminate() {
        super.onTerminate()
        disposable?.dispose()
    }

    private fun checkCurrentServer() {
        if (developerOptions.isServerNotSet()) {
            developerOptions.makeDefaultInit()
        }
    }

    private class CustomPendingIntentCreator : PendingIntentCreator {
        override fun create(context: Context, appMarker: String): PendingIntent? {
            if (!TextUtils.isEmpty(appMarker)) {
                val clientCards = getCards(context)
                var pushClientCard: Card? = null
                for (clientCard in clientCards) {
                    if (appMarker.equals(clientCard.appMarker, ignoreCase = true)) {
                        pushClientCard = clientCard
                    }
                }
                if (pushClientCard != null) {
                    return BottomNavigationActivity.createPendingIntent(
                        context,
                        pushClientCard.userId,
                        pushClientCard.clientData,
                        pushClientCard.appMarker,
                        pushClientCard.clientIdSignature,
                        pushClientCard.authToken,
                        pushClientCard.authSchema,
                        getTheme(context)
                    )
                }
            } else {
                // This is an example of creating pending intent for single-chat app
                val clientCards = getCards(context)
                if (!clientCards.isEmpty()) {
                    val (userId, clientData, appMarker1, clientIdSignature, authToken, authSchema) = clientCards[0]
                    return BottomNavigationActivity.createPendingIntent(
                        context,
                        userId,
                        clientData,
                        appMarker1,
                        clientIdSignature,
                        authToken,
                        authSchema,
                        getTheme(context)
                    )
                }
            }
            return null
        }
    }

    companion object {
        @JvmStatic
        val unreadMessagesSubject = BehaviorSubject.create<Int>()
        @JvmStatic
        lateinit var appContext: Context
    }
}
