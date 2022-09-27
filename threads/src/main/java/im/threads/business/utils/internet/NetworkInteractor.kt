package im.threads.business.utils.internet

import android.content.Context

interface NetworkInteractor {
    fun hasNoInternet(context: Context): Boolean
}
