package im.threads.business.utils

import android.content.Context
import android.os.Build
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections

class DeviceInfo {
    val osVersion: String
        get() = Build.VERSION.RELEASE
    val deviceName: String
        get() = "${Build.MANUFACTURER} ${Build.MODEL}"

    @Suppress("DEPRECATION")
    fun getLocale(ctx: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ctx.resources.configuration.locales[0].toLanguageTag()
        } else {
            ctx.resources.configuration.locale.toLanguageTag()
        }
    }

    val ipAddress: String
        get() {
            try {
                val interfaces: List<NetworkInterface> =
                    Collections.list(NetworkInterface.getNetworkInterfaces())
                for (networkInterface in interfaces) {
                    val addresses: List<InetAddress> = Collections.list(networkInterface.inetAddresses)
                    for (address in addresses) {
                        if (!address.isLoopbackAddress) {
                            val hostAddress = address.hostAddress
                            if (hostAddress != null && hostAddress.indexOf(':') < 0) {
                                return hostAddress // ipV4
                            }
                        }
                    }
                }
            } catch (ignored: Exception) {}

            return ""
        }
}
