package im.threads.business.models.enums

enum class ModificationStateEnum(val state: String) {
    ANY("ANY"),
    EDITED("EDITED"),
    DELETED("DELETED");

    companion object {
        @JvmStatic
        fun fromString(value: String?): ModificationStateEnum {
            return try {
                values().first() { it.state == value }
            } catch (e: NoSuchElementException) {
                ANY
            }
        }
    }
}
