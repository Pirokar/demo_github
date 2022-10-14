package im.threads.business.models

enum class ClientNotificationDisplayType {
    ALL, CURRENT_THREAD_ONLY, CURRENT_THREAD_WITH_GROUPING;

    companion object {
        fun fromString(name: String?): ClientNotificationDisplayType {
            return try {
                if (name != null) {
                    valueOf(name)
                } else {
                    CURRENT_THREAD_ONLY
                }
            } catch (ex: IllegalArgumentException) {
                CURRENT_THREAD_ONLY
            }
        }
    }
}
