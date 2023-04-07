package im.threads.business.transport

enum class CloudMessagingType {
    FCM,
    HCM;

    fun fromString(name: String): CloudMessagingType {
        return valueOf(name)
    }
}
