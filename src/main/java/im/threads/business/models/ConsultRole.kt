package im.threads.business.models

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
        fun consultRoleFromString(role: String) =
            try {
                values().first { it.role == role }
            } catch (exception: NoSuchElementException) {
                OPERATOR
            }
    }
}
