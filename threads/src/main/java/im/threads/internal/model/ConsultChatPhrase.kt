package im.threads.internal.model

import android.text.TextUtils

open class ConsultChatPhrase(var avatarPath: String?, var consultId: String?) {
    fun hasAvatar(): Boolean {
        return !TextUtils.isEmpty(avatarPath)
    }
}
