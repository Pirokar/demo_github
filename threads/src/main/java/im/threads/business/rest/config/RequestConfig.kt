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
    var resendIntervalMillis: Int = 10_000,
    var resendPingIntervalMillis: Int = 10_000,
    var connectTimeoutMillis: Int = 10_000,
    var readTimeoutMillis: Int = 10_000,
    var writeTimeoutMillis: Int = 10_000
)

data class HttpClientSettings(
    var connectTimeoutMillis: Int = 10_000,
    var readTimeoutMillis: Int = 10_000,
    var writeTimeoutMillis: Int = 10_000
)
