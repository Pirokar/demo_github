package im.threads.business.utils

import android.content.ClipData
import android.content.ClipboardManager
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.serviceLocator.core.inject

fun ClipboardManager.copyToBuffer(what: String) {
    val preferences: Preferences by inject()
    setPrimaryClip(ClipData("", arrayOf("text/plain"), ClipData.Item(what)))
    preferences.save(PreferencesCoreKeys.LAST_COPY_TEXT, what)
}

fun String.isLastCopyText(): Boolean {
    val preferences: Preferences by inject()
    return preferences.get<String>(PreferencesCoreKeys.LAST_COPY_TEXT)?.let {
        this.contains(it)
    } ?: false
}
