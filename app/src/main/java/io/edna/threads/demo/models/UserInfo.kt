package io.edna.threads.demo.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserInfo(
    var nickName: String? = null,
    var userId: String? = null,
    var isShowMenu: Boolean = false
) : Parcelable {

    override fun toString() = "$nickName, $userId"

    fun isAllFieldsFilled(): Boolean {
        return !nickName.isNullOrEmpty() && !userId.isNullOrEmpty()
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is UserInfo) {
            return other.nickName == nickName &&
                other.userId == userId
        }
        return false
    }

    fun clone(): UserInfo {
        return UserInfo(nickName, userId, isShowMenu)
    }
}
