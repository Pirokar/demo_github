package im.threads.business.utils

import im.threads.business.models.ConsultConnectionMessage
import im.threads.business.models.ConsultInfo
import im.threads.business.preferences.Preferences

class ConsultWriter(private val preferences: Preferences) {
    var isSearchingConsult: Boolean
        get() = preferences.get("${ConsultWriter::class.java}$SEARCHING_CONSULT}") ?: false
        set(isSearching) {
            preferences.save("${ConsultWriter::class.java}$SEARCHING_CONSULT}", isSearching)
        }

    fun setCurrentConsultInfo(message: ConsultConnectionMessage) {
        val consultId = message.consultId
        preferences.save(OPERATOR_ID, consultId)
        preferences.save(OPERATOR_STATUS + consultId, message.status)
        preferences.save(OPERATOR_NAME + consultId, message.name)
        preferences.save(OPERATOR_TITLE + consultId, message.title)
        preferences.save(OPERATOR_ORG_UNIT + consultId, message.orgUnit)
        preferences.save(OPERATOR_ROLE + consultId, message.role)
        preferences.save(OPERATOR_PHOTO + consultId, message.avatarPath)
    }

    private fun getName(id: String): String? {
        return preferences.get("$OPERATOR_NAME$id")
    }

    private fun getStatus(id: String): String? {
        return preferences.get("$OPERATOR_STATUS$id")
    }

    private fun getOrgUnit(id: String): String? {
        return preferences.get("$OPERATOR_ORG_UNIT$id")
    }

    private fun getRole(id: String): String? {
        return preferences.get("$OPERATOR_ROLE$id")
    }

    private fun getPhotoUrl(id: String?): String? {
        return preferences.get("$OPERATOR_PHOTO$id")
    }

    fun getCurrentPhotoUrl(): String? {
        return if (getCurrentConsultId() != null) {
            getPhotoUrl(getCurrentConsultId())
        } else {
            null
        }
    }

    fun getCurrentConsultId(): String? {
        return preferences.get(OPERATOR_ID)
    }

    fun setCurrentConsultLeft() {
        preferences.save<String>(OPERATOR_ID, null)
    }

    fun isConsultConnected(): Boolean {
        return !preferences.get<String>(OPERATOR_ID).isNullOrBlank()
    }

    fun getConsultInfo(id: String): ConsultInfo {
        return ConsultInfo(
            getName(id),
            id,
            getStatus(id),
            getOrgUnit(id),
            getRole(id),
            getPhotoUrl(id)
        )
    }

    fun getCurrentConsultInfo(): ConsultInfo? {
        val currentId = getCurrentConsultId()
        return currentId?.let { getConsultInfo(it) }
    }

    companion object {
        private const val OPERATOR_STATUS = "OPERATOR_STATUS"
        private const val OPERATOR_NAME = "OPERATOR_NAME"
        private const val OPERATOR_TITLE = "OPERATOR_TITLE"
        private const val OPERATOR_ORG_UNIT = "OPERATOR_ORG_UNIT"
        private const val OPERATOR_ROLE = "OPERATOR_ROLE"
        private const val OPERATOR_PHOTO = "OPERATOR_PHOTO"
        private const val OPERATOR_ID = "OPERATOR_ID"
        private const val SEARCHING_CONSULT = "SEARCHING_CONSULT"
    }
}
