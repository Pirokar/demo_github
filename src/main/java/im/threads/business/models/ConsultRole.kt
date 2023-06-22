package im.threads.business.models

import im.threads.business.logger.LoggerEdna

/**
 * Роль (должность) консультатнта.
 */
enum class ConsultRole(val role: String) {
    BOT("BOT"),
    EXTERNAL_BOT("EXTERNAL_BOT"),
    OPERATOR("OPERATOR"),
    SUPERVISOR("SUPERVISOR"),
    SYSTEM("SYSTEM"),
    INTEGRATION("INTEGRATION");

    companion object {
        @JvmStatic
        fun consultRoleFromString(role: String?) =
            try {
                values().first { it.role == role }
            } catch (exception: NoSuchElementException) {
                LoggerEdna.error("Cannot find consult role for: $role. Applied default \"Operator\"")
                OPERATOR
            }
    }
}
