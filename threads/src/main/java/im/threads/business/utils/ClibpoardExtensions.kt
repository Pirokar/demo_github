package im.threads.business.utils

import android.content.ClipData
import android.content.ClipboardManager
import im.threads.business.utils.preferences.PrefUtilsBase.lastCopyText

fun ClipboardManager.copyToBuffer(what: String) {
    setPrimaryClip(ClipData("", arrayOf("text/plain"), ClipData.Item(what)))
    lastCopyText = what
}

fun String.isLastCopyText(): Boolean {
    return lastCopyText?.let { this.contains(it) } ?: false
}
