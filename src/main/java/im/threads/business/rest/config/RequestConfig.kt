package im.threads.business.rest.config

/**
 * Настройки Socket и Http клиентов.
 */
class RequestConfig {
    val socketClientSettings = SocketClientSettings()
    val picassoHttpClientSettings = HttpClientSettings()
    val authHttpClientSettings = HttpClientSettings(connectTimeoutMillis = 2_000)
    val threadsApiHttpClientSettings = HttpClientSettings(connectTimeoutMillis = 60_000)
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
