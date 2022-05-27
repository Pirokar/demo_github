package im.threads.android.core

import android.app.PendingIntent
import android.content.Context
import android.text.TextUtils
import androidx.multidex.MultiDexApplication
import com.edna.android.push_lite.PushController
import com.pandulapeter.beagle.Beagle
import im.threads.ConfigBuilder
import im.threads.ThreadsLib
import im.threads.ThreadsLib.PendingIntentCreator
import im.threads.android.R
import im.threads.android.data.Card
import im.threads.android.push.HCMTokenRefresher.requestToken
import im.threads.android.ui.BottomNavigationActivity
import im.threads.android.utils.PrefUtils.getCards
import im.threads.android.utils.PrefUtils.getTheme
import im.threads.android.utils.PrefUtils.getTransportConfig
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class ThreadsDemoApplication : MultiDexApplication() {
    private var disposable: Disposable? = null

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

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

        val transportConfig = getTransportConfig(this)
        if (transportConfig != null) {
            configBuilder.serverBaseUrl(transportConfig.baseUrl)
                .threadsGateUrl(transportConfig.threadsGateUrl)
                .threadsGateProviderUid(transportConfig.threadsGateProviderUid)
                .threadsGateHCMProviderUid(transportConfig.threadsGateHCMProviderUid)
        }

        ThreadsLib.init(configBuilder)
        Beagle.initialize(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        disposable?.dispose()
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
        var appContext: Context? = null
            private set
    }
}
