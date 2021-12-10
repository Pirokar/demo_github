package im.threads.internal.transport

enum class CloudMessagingType {
    FCM,
    HCM;

    fun fromString(name: String): CloudMessagingType {
        return valueOf(name)
    }
}
