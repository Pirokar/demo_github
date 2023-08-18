package im.threads.business.extensions

class MutableLazy<T>(private val initializer: () -> T) : Lazy<T> {
    private var cached: T? = null
    override val value: T
        get() {
            if (cached == null) {
                cached = initializer()
            }
            @Suppress("UNCHECKED_CAST")
            return cached as T
        }

    fun reset() {
        cached = null
    }

    override fun isInitialized(): Boolean = cached != null
}

fun <T> mutableLazy(value: () -> T) = MutableLazy(value)
