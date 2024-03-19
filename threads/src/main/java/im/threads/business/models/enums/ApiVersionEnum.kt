package im.threads.business.models.enums

enum class ApiVersionEnum(val version: String) {
    V16("16"),
    V17("17"),
    V18("18");

    override fun toString(): String {
        return version
    }

    companion object {
        val defaultApiVersionEnum = V16

        @JvmStatic
        fun createApiVersionEnum(version: String): ApiVersionEnum {
            return try {
                ApiVersionEnum.values().first() { it.version == version }
            } catch (ex: IllegalArgumentException) {
                defaultApiVersionEnum
            }
        }
    }
}
