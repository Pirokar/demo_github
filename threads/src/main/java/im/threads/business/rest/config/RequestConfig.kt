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
        return "Request config. " +
            "SocketClientSettings. " +
            "resendIntervalMillis: ${socketClientSettings.resendIntervalMillis} | " +
            "resendPingIntervalMillis: ${socketClientSettings.resendPingIntervalMillis} | " +
            "connectTimeoutMillis: ${socketClientSettings.connectTimeoutMillis} | " +
            "readTimeoutMillis: ${socketClientSettings.readTimeoutMillis} | " +
            "writeTimeoutMillis: ${socketClientSettings.writeTimeoutMillis} | " +
            "sendIntervalMillis: ${socketClientSettings.sendIntervalMillis}\n" +
            "PicassoHttpClientSettings. " +
            "connectTimeoutMillis: ${picassoHttpClientSettings.connectTimeoutMillis} | " +
            "resendPingIntervalMillis: ${picassoHttpClientSettings.readTimeoutMillis} | " +
            "connectTimeoutMillis: ${picassoHttpClientSettings.writeTimeoutMillis}\n" +
            "ThreadsHttpClientSettings. " +
            "connectTimeoutMillis: ${threadsApiHttpClientSettings.connectTimeoutMillis} | " +
            "resendPingIntervalMillis: ${threadsApiHttpClientSettings.readTimeoutMillis} | " +
            "connectTimeoutMillis: ${threadsApiHttpClientSettings.writeTimeoutMillis},"
    }
}

data class SocketClientSettings(
    var resendIntervalMillis: Long = 10_000,
    var resendPingIntervalMillis: Long = 3_000,
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
