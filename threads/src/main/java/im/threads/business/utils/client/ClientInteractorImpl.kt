package im.threads.business.utils.client

import android.text.TextUtils
import im.threads.business.logger.LoggerEdna
import im.threads.business.utils.preferences.PrefUtilsBase

class ClientInteractorImpl : ClientInteractor {
    override fun initClientId() {
        val newClientId = PrefUtilsBase.newClientID
        val oldClientId = PrefUtilsBase.clientID
        LoggerEdna.info("getInstance newClientId = $newClientId, oldClientId = $oldClientId")
        if (newClientId == oldClientId) {
            // clientId has not changed
            PrefUtilsBase.setNewClientId("")
        } else if (!TextUtils.isEmpty(newClientId)) {
            PrefUtilsBase.setClientId(newClientId!!)
        }
    }
}
