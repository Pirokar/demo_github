package im.threads.internal.model

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

enum class ErrorStateEnum(val state: String) {
    ANY("ANY"),
    DISALLOWED("DS-1"),
    TIMEOUT("DS-10"),
    Unexpected("DS-100");

    companion object {
        @JvmStatic
        fun errorStateStateEnumFromString(value: String): ErrorStateEnum {
            try {
                return values().first() { it.state == value }
            } catch (e: NoSuchElementException) {
                return ANY
            }
        }
    }
}
