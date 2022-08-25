package im.threads.business.transport

import im.threads.business.utils.preferences.PrefUtilsBase

class ApplicationConfig(
    private val threadsGateProviderUid: String,
    private val threadsGateHuaweiProviderUid: String?
) {

    fun getCloudPair(): CloudPair {
        return when {
            PrefUtilsBase.fcmToken != null -> CloudPair(
                threadsGateProviderUid,
                PrefUtilsBase.fcmToken
            )
            threadsGateHuaweiProviderUid != null && PrefUtilsBase.hcmToken != null -> CloudPair(
                threadsGateHuaweiProviderUid,
                PrefUtilsBase.hcmToken
            )
            else -> CloudPair(threadsGateProviderUid, null)
        }
    }
}

data class CloudPair(val providerUid: String, val token: String?)
