package im.threads.business.models.enums

enum class ApiVersionEnum(val version: String) {
    V15("15"),
    V17("17"),
    V18("18");

    override fun toString(): String {
        return version
    }
}
