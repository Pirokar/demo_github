package im.threads.internal.transport

import im.threads.internal.utils.PrefUtils

class ApplicationConfig(
    private val threadsGateProviderUid: String,
    private val threadsGateHuaweiProviderUid: String?
) {

    fun getProviderUid(): String {
        val cloudMessagingType = PrefUtils.getCloudMessagingType()
        if (threadsGateHuaweiProviderUid != null && cloudMessagingType != null && CloudMessagingType.valueOf(
                cloudMessagingType
            ) == CloudMessagingType.HCM
        ) {
            return threadsGateHuaweiProviderUid
        }
        return threadsGateProviderUid
    }

    fun getCloudMessagingToken(): String? {
        val cloudMessagingType = PrefUtils.getCloudMessagingType()
        if (threadsGateHuaweiProviderUid != null && cloudMessagingType != null && CloudMessagingType.valueOf(
                cloudMessagingType
            ) == CloudMessagingType.HCM
        ) {
            return PrefUtils.getHcmToken()
        }
        return PrefUtils.getFcmToken()
    }
}
