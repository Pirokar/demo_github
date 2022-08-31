package im.threads.business.ogParser

import android.view.ViewGroup
import android.widget.TextView

data class OGDataContent(
    val ogDataLayout: ViewGroup,
    val timeStampView: TextView,
    val messageText: String?,
    var url: String = ""
)
