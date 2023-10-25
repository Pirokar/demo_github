package im.threads.business

enum class AuthMethod() {
    HEADERS, COOKIES;

    override fun toString() = this.name.lowercase()

    companion object {
        fun fromString(stringValue: String?) =
            values().firstOrNull { it.name.equals(stringValue, true) } ?: HEADERS
    }
}
