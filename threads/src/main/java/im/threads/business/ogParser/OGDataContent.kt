package im.threads.business.ogParser

import android.view.ViewGroup
import im.threads.ui.widget.textView.BubbleTimeTextView
import java.lang.ref.WeakReference

data class OGDataContent(
    val ogDataLayout: WeakReference<ViewGroup>,
    val ogTimeStampView: WeakReference<BubbleTimeTextView>,
    val mainTimeStampView: WeakReference<BubbleTimeTextView>?,
    val messageText: String?,
    var url: String = ""
)
