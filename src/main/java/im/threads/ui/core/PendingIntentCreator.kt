package im.threads.ui.core

import android.app.PendingIntent
import android.content.Context

interface PendingIntentCreator {
    fun create(context: Context, appMarker: String?): PendingIntent?
}
