package im.threads.business.models

import android.os.Parcelable
import com.google.gson.JsonObject
import kotlinx.parcelize.Parcelize

@Parcelize
data class ConsultInfo(
    val name: String? = null,
    val id: String? = null,
    val status: String? = null,
    val organizationUnit: String? = null,
    val photoUrl: String? = null,
    val role: String? = null
) : Parcelable {

    override fun toString(): String {
        return """ConsultInfo{
            name='$name',
            id=$id,
            status='$status',
            organizationUnit='$organizationUnit',
            photoUrl='$photoUrl',
            role='$role'
            }
        """.trim()
    }

    fun toJson(): JsonObject {
        return JsonObject().apply {
            addProperty("name", name)
            addProperty("status", status)
            addProperty("id", id)
            addProperty("photoUrl", photoUrl)
            addProperty("role", role)
        }
    }
}
