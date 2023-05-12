package im.threads.android.core

import android.app.PendingIntent
import android.content.Context
import android.text.TextUtils
import androidx.multidex.MultiDexApplication
import com.pandulapeter.beagle.logOkHttp.BeagleOkHttpLogger
import im.threads.android.R
import im.threads.android.data.Card
import im.threads.android.di.appModule
import im.threads.android.push.HCMTokenRefresher.requestToken
import im.threads.android.ui.BottomNavigationActivity
import im.threads.android.useCases.developerOptions.DebugMenuUseCase
import im.threads.android.utils.PrefUtilsApp.getCards
import im.threads.android.utils.PrefUtilsApp.getTheme
import im.threads.android.utils.PrefUtilsApp.getTransportConfig
import im.threads.business.AuthMethod
import im.threads.business.core.UnreadMessagesCountListener
import im.threads.business.logger.LoggerConfig
import im.threads.business.logger.LoggerRetentionPolicy
import im.threads.ui.config.ConfigBuilder
import im.threads.ui.core.PendingIntentCreator
import im.threads.ui.core.ThreadsLib
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import okhttp3.Interceptor
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.io.File

class ThreadsDemoApplication : MultiDexApplication() {
    private var disposable: Disposable? = null
    private val developerOptions: DebugMenuUseCase by inject()

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        startKoin {
            androidContext(applicationContext)
            modules(appModule)
        }

        developerOptions.initServer()

        disposable = Completable.fromAction { requestToken(this) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}) {}

        // PushController.getInstance(this).init() - only for mailing

        val loggerConfig = LoggerConfig.Builder(this)
            .logToFile()
            .dir(File(this.filesDir, "logs"))
            .retentionPolicy(LoggerRetentionPolicy.TOTAL_SIZE)
            .maxTotalSize(5242880)
            .build()

        val configBuilder = ConfigBuilder(this)
            .pendingIntentCreator(CustomPendingIntentCreator())
            .unreadMessagesCountListener(object : UnreadMessagesCountListener {
                override fun onUnreadMessagesCountChanged(count: Int) {
                    unreadMessagesSubject.onNext(
                        count
                    )
                }
            })
            .surveyCompletionDelay(2000)
            .historyLoadingCount(50)
            .isDebugLoggingEnabled(true)
            .showAttachmentsButton()
            .certificateRawResIds(listOf(R.raw.edna))
            .networkInterceptor((BeagleOkHttpLogger.logger as? Interceptor?))
            .enableLogging(loggerConfig)

        if (developerOptions.getCurrentServer().isSSLPinningDisabled) {
            configBuilder.disableSSLPinning()
        }

        val transportConfig = getTransportConfig(this)
        if (transportConfig != null) {
            configBuilder.serverBaseUrl(transportConfig.baseUrl)
                .datastoreUrl(transportConfig.datastoreUrl)
                .threadsGateUrl(transportConfig.threadsGateUrl)
                .threadsGateProviderUid(transportConfig.threadsGateProviderUid)

            if (transportConfig.isNewChatCenterApi) {
                configBuilder.setNewChatCenterApi()
            }
        }

        developerOptions.configureDebugMenu()
        ThreadsLib.init(configBuilder)
    }

    override fun onTerminate() {
        super.onTerminate()
        disposable?.dispose()
    }

    private class CustomPendingIntentCreator : PendingIntentCreator {
        override fun create(context: Context, appMarker: String?): PendingIntent? {
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
                        AuthMethod.HEADERS.toString(),
                        getTheme(context)
                    )
                }
            } else {
                // This is an example of creating pending intent for single-chat app
                val clientCards = getCards(context)
                if (clientCards.isNotEmpty()) {
                    val (userId, clientData, appMarker1, clientIdSignature, authToken, authSchema) = clientCards[0]
                    return BottomNavigationActivity.createPendingIntent(
                        context,
                        userId,
                        clientData,
                        appMarker1,
                        clientIdSignature,
                        authToken,
                        authSchema,
                        AuthMethod.COOKIES.toString(),
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
