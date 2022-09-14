package im.threads.business.models

import android.os.Parcelable
import com.google.gson.JsonObject
import kotlinx.parcelize.Parcelize

@Parcelize
data class ConsultInfo(
    var name: String? = null,
    var id: String? = null,
    var status: String? = null,
    var organizationUnit: String? = null,
    var photoUrl: String? = null,
    var role: String? = null
) : Parcelable {

    override fun toString(): String {
        return "ConsultInfo{" +
            "name='" + name + '\'' +
            ", id=" + id +
            ", status='" + status + '\'' +
            ", organizationUnit='" + organizationUnit + '\'' +
            ", photoUrl='" + photoUrl + '\'' +
            ", role='" + role + '\'' +
            '}'
    }

    fun toJson(): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty("name", name)
        jsonObject.addProperty("status", status)
        jsonObject.addProperty("id", id)
        jsonObject.addProperty("photoUrl", photoUrl)
        jsonObject.addProperty("role", role)
        return jsonObject
    }
}
