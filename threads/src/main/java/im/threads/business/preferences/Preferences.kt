package im.threads.business.preferences

interface Preferences {
    fun save(key: String, value: Any?)
    fun <T> get(key: String): T?
}
