package im.threads.internal.transport

import im.threads.internal.utils.PrefUtils

class ApplicationConfig(
    private val threadsGateProviderUid: String,
    private val threadsGateHuaweiProviderUid: String?
) {

    fun getCloudPair(): CloudPair {
        return when {
            PrefUtils.getFcmToken() != null -> CloudPair(
                threadsGateProviderUid,
                PrefUtils.getFcmToken()
            )
            threadsGateHuaweiProviderUid != null && PrefUtils.getHcmToken() != null -> CloudPair(
                threadsGateHuaweiProviderUid,
                PrefUtils.getHcmToken()
            )
            else -> CloudPair(threadsGateProviderUid, null)
        }
    }
}

data class CloudPair(val providerUid: String, val token: String?)
