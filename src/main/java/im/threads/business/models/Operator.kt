package im.threads.business.models

import android.text.TextUtils

class Operator {
    var id: Long? = null
    var name: String? = null
    var alias: String? = null
    var status: String? = null
    var role: String? = null
    var orgUnit: String? = null
    var maxThreads: Long? = null
    var photoUrl: String? = null
    var gender = Gender.FEMALE
    val aliasOrName: String?
        get() = if (!TextUtils.isEmpty(alias)) {
            alias
        } else {
            name
        }

    enum class Gender {
        MALE, FEMALE
    }
}
