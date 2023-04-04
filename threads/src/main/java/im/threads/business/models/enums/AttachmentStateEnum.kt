package im.threads.business.models.enums

enum class AttachmentStateEnum(val state: String) {
    ANY("ANY"),
    PENDING("PENDING"),
    ERROR("ERROR"),
    READY("READY");

    companion object {
        @JvmStatic
        fun fromString(value: String): AttachmentStateEnum {
            return try {
                values().first() { it.state == value }
            } catch (e: NoSuchElementException) {
                READY
            }
        }
    }
}
