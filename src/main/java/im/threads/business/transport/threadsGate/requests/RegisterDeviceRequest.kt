package im.threads.business.transport.threadsGate.requests

import im.threads.business.transport.threadsGate.Action

class RegisterDeviceRequest(correlationId: String?, data: Data?) : BaseRequest<RegisterDeviceRequest.Data?>(
    Action.REGISTER_DEVICE,
    correlationId,
    data
) {
    class Data(
        private val appPackage: String,
        private val appVersion: String,
        private val providerUid: String,
        private val pnsPushAddress: String?,
        private val deviceUid: String,
        private val osName: String,
        private val osVersion: String,
        private val locale: String,
        private val timeZone: String,
        private val deviceName: String,
        private val deviceModel: String,
        private val deviceAddress: String?,
        private val clientId: String?
    )
}
