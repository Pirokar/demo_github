package im.threads.business.transport.models

import android.text.TextUtils

class Operator {
    val id: Long = 0
    val name: String? = null
    val alias: String? = null
    val status: String? = null
    val photoUrl: String? = null
    val gender: String? = null
    val organizationUnit: String? = null
    val role: String? = null
    val aliasOrName: String?
        get() = if (!TextUtils.isEmpty(alias)) {
            alias
        } else {
            name
        }
}
