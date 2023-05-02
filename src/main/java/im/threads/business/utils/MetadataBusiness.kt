package im.threads.business.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import im.threads.business.logger.LoggerEdna.error

object MetadataBusiness {
    private const val DATASTORE_URL = "im.threads.getDatastoreUrl"
    private const val SERVER_BASE_URL = "im.threads.getServerUrl"
    private const val THREADS_GATE_URL = "im.threads.threadsGateUrl"
    private const val THREADS_GATE_PROVIDER_UID = "im.threads.threadsGateProviderUid"
    private const val NEW_CHAT_CENTER_API = "im.threads.newChatCenterApi"

    @JvmStatic
    fun getDatastoreUrl(context: Context): String? {
        val metaData = getMetaData(context)
        return if (metaData != null && metaData.containsKey(DATASTORE_URL)) {
            metaData.getString(DATASTORE_URL)
        } else null
    }

    @JvmStatic
    fun getServerBaseUrl(context: Context): String? {
        val metaData = getMetaData(context)
        return if (metaData != null && metaData.containsKey(SERVER_BASE_URL)) {
            metaData.getString(SERVER_BASE_URL)
        } else null
    }

    @JvmStatic
    fun getThreadsGateUrl(context: Context): String? {
        val metaData = getMetaData(context)
        return if (metaData != null && metaData.containsKey(THREADS_GATE_URL)) {
            metaData.getString(THREADS_GATE_URL)
        } else null
    }

    @JvmStatic
    fun getThreadsGateProviderUid(context: Context): String? {
        val metaData = getMetaData(context)
        return if (metaData != null && metaData.containsKey(THREADS_GATE_PROVIDER_UID)) {
            metaData.getString(THREADS_GATE_PROVIDER_UID)
        } else null
    }

    @JvmStatic
    fun getNewChatCenterApi(context: Context): Boolean {
        val metaData = getMetaData(context)
        return if (metaData != null && metaData.containsKey(NEW_CHAT_CENTER_API)) {
            metaData.getBoolean(NEW_CHAT_CENTER_API)
        } else false
    }

    fun getMetaData(context: Context): Bundle? {
        return try {
            val ai = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            ai.metaData
        } catch (e: PackageManager.NameNotFoundException) {
            error("Failed to load self applicationInfo - that's really weird. ", e)
            null
        }
    }
}
