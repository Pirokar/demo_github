package im.threads.internal.model

enum class AttachmentStateEnum(val state: String) {
    ANY("ANY"),
    READY("READY"),
    PENDING("PENDING"),
    ERROR("ERROR")
}

enum class ErrorStateEnum(val state: String) {
    ANY("ANY"),
    DISALLOWED("DS-1"),
    TIMEOUT("DS-10"),
    Unexpected("DS-100")
}