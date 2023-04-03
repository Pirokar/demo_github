package im.threads.business.models.enums

enum class AttachmentStateEnum(val state: String) {
    ANY("ANY"),
    PENDING("PENDING"),
    READY("READY"),
    ERROR("ERROR");

    companion object {
        @JvmStatic
        fun attachmentStateEnumFromString(value: String): AttachmentStateEnum {
            try {
                return values().first() { it.state == value }
            } catch (e: NoSuchElementException) {
                return READY
            }
        }
    }
}
