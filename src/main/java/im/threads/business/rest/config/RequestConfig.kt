package im.threads.business.rest.config

/**
 * Настройки Socket и Http клиентов.
 */
class RequestConfig {
    val socketClientSettings = SocketClientSettings()

    val picassoHttpClientSettings = HttpClientSettings()

    @Deprecated("Please use threadsApiHttpClientSettings instead")
    val authHttpClientSettings = HttpClientSettings(connectTimeoutMillis = 2_000)

    val threadsApiHttpClientSettings = HttpClientSettings(connectTimeoutMillis = 60_000)

    override fun toString(): String {
        return "Request config:\n" +
            "  SocketClientSettings:\n" +
            "    resendIntervalMillis: ${socketClientSettings.resendIntervalMillis}\n" +
            "    resendPingIntervalMillis: ${socketClientSettings.resendPingIntervalMillis}\n" +
            "    connectTimeoutMillis: ${socketClientSettings.connectTimeoutMillis}\n" +
            "    readTimeoutMillis: ${socketClientSettings.readTimeoutMillis}\n" +
            "    writeTimeoutMillis: ${socketClientSettings.writeTimeoutMillis}\n" +
            "    sendIntervalMillis: ${socketClientSettings.sendIntervalMillis}\n" +
            "  PicassoHttpClientSettings:\n" +
            "    connectTimeoutMillis: ${picassoHttpClientSettings.connectTimeoutMillis}\n" +
            "    resendPingIntervalMillis: ${picassoHttpClientSettings.readTimeoutMillis}\n" +
            "    connectTimeoutMillis: ${picassoHttpClientSettings.writeTimeoutMillis}\n" +
            "  ThreadsHttpClientSettings:\n" +
            "    connectTimeoutMillis: ${threadsApiHttpClientSettings.connectTimeoutMillis}\n" +
            "    resendPingIntervalMillis: ${threadsApiHttpClientSettings.readTimeoutMillis}\n" +
            "    connectTimeoutMillis: ${threadsApiHttpClientSettings.writeTimeoutMillis},"
    }
}

data class SocketClientSettings(
    var resendIntervalMillis: Long = 10_000,
    var resendPingIntervalMillis: Long = 10_000,
    var connectTimeoutMillis: Long = 10_000,
    var readTimeoutMillis: Long = 10_000,
    var writeTimeoutMillis: Long = 10_000,
    var sendIntervalMillis: Long = 40_000
)

data class HttpClientSettings(
    var connectTimeoutMillis: Long = 10_000,
    var readTimeoutMillis: Long = 10_000,
    var writeTimeoutMillis: Long = 10_000
)
