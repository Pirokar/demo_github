package edna.chatcenter.demo.appCode.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserInfo(
    var userId: String? = null,
    var userData: String? = null,
    var signature: String? = null,
    var authorizationHeader: String? = null,
    var xAuthSchemaHeader: String? = null,
    var isShowMenu: Boolean = false
) : Parcelable {

    override fun toString() = "$userId," +
        "$userData," +
        "$signature," +
        "$authorizationHeader," +
        "$xAuthSchemaHeader," +
        "$isShowMenu"

    fun isAllFieldsFilled(): Boolean {
        return !userId.isNullOrEmpty()
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is UserInfo) {
            return other.userId == userId &&
                other.signature == signature &&
                other.authorizationHeader == authorizationHeader &&
                other.xAuthSchemaHeader == xAuthSchemaHeader &&
                other.userData == userData
        }
        return false
    }

    fun clone(): UserInfo {
        return UserInfo(
            userId,
            userData,
            signature,
            authorizationHeader,
            xAuthSchemaHeader,
            isShowMenu
        )
    }
}
