package im.threads.business.ogParser

import android.view.ViewGroup
import android.widget.TextView
import java.lang.ref.WeakReference

data class OGDataContent(
    val ogDataLayout: WeakReference<ViewGroup>,
    val timeStampView: WeakReference<TextView>,
    val messageText: String?,
    var url: String = ""
)
