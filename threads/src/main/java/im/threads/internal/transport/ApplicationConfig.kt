package im.threads.internal.transport

import im.threads.internal.utils.PrefUtils

class ApplicationConfig(
    private val threadsGateProviderUid: String,
    private val threadsGateHuaweiProviderUid: String?
) {

    fun getCloudPair(): CloudPair {
        return when {
            PrefUtils.fcmToken != null -> CloudPair(
                threadsGateProviderUid,
                PrefUtils.fcmToken
            )
            threadsGateHuaweiProviderUid != null && PrefUtils.hcmToken != null -> CloudPair(
                threadsGateHuaweiProviderUid,
                PrefUtils.hcmToken
            )
            else -> CloudPair(threadsGateProviderUid, null)
        }
    }
}

data class CloudPair(val providerUid: String, val token: String?)
