package im.threads.business.models.enums

enum class ApiVersionEnum(val version: String) {
    V15("15"),
    V16("16"),
    V17("17"),
    V18("18");

    override fun toString(): String {
        return version
    }

    companion object {
        @JvmStatic
        fun createApiVersionEnum(version: String): ApiVersionEnum {
            return try {
                ApiVersionEnum.values().first() { it.version == version }
            } catch (ex: IllegalArgumentException) {
                V15
            }
        }
    }
}
