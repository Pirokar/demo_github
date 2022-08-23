package im.threads.business.models.enums

enum class AttachmentStateEnum(val state: String) {
    ANY("ANY"),
    READY("READY"),
    PENDING("PENDING"),
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
