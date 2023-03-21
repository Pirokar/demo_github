package im.threads.business.models.enums

enum class ErrorStateEnum(val state: String) {
    ANY("ANY"),
    DISALLOWED("DS-1"),
    TIMEOUT("DS-10"),
    UNEXPECTED("DS-100");

    companion object {
        @JvmStatic
        fun errorStateEnumFromString(value: String): ErrorStateEnum {
            return try {
                values().first() { it.state == value }
            } catch (e: NoSuchElementException) {
                ANY
            }
        }
    }
}
