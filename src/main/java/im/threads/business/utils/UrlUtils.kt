package im.threads.business.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Patterns
import im.threads.business.models.ExtractedLink
import im.threads.business.utils.Balloon.show
import java.util.Locale
import java.util.regex.Pattern

object UrlUtils {
    val DEEPLINK_URL =
        Pattern.compile("[a-z0-9+.-]+(?<!http)(?<!https)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|*]*")

    /**
     * Valid UCS characters defined in RFC 3987. Excludes space characters.
     */
    private const val UCS_CHAR = "[" +
        "\u00A0-\uD7FF" +
        "\uF900-\uFDCF" +
        "\uFDF0-\uFFEF" +
        "\uD800\uDC00-\uD83F\uDFFD" +
        "\uD840\uDC00-\uD87F\uDFFD" +
        "\uD880\uDC00-\uD8BF\uDFFD" +
        "\uD8C0\uDC00-\uD8FF\uDFFD" +
        "\uD900\uDC00-\uD93F\uDFFD" +
        "\uD940\uDC00-\uD97F\uDFFD" +
        "\uD980\uDC00-\uD9BF\uDFFD" +
        "\uD9C0\uDC00-\uD9FF\uDFFD" +
        "\uDA00\uDC00-\uDA3F\uDFFD" +
        "\uDA40\uDC00-\uDA7F\uDFFD" +
        "\uDA80\uDC00-\uDABF\uDFFD" +
        "\uDAC0\uDC00-\uDAFF\uDFFD" +
        "\uDB00\uDC00-\uDB3F\uDFFD" +
        "\uDB44\uDC00-\uDB7F\uDFFD" +
        "&&[^\u00A0[\u2000-\u200A]\u2028\u2029\u202F\u3000]]"
    private const val IP_ADDRESS_STRING =
        (
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]" +
                "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]" +
                "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}" +
                "|[1-9][0-9]|[0-9]))"
            )

    /**
     * Valid characters for IRI label defined in RFC 3987.
     */
    private const val LABEL_CHAR = "a-zA-Z0-9" + UCS_CHAR

    /**
     * Valid characters for IRI TLD defined in RFC 3987.
     */
    private const val TLD_CHAR = "a-zA-Z" + UCS_CHAR

    /**
     * RFC 1035 Section 2.3.4 limits the labels to a maximum 63 octets.
     */
    private const val IRI_LABEL =
        "[" + LABEL_CHAR + "](?:[" + LABEL_CHAR + "_\\-]{0,4048}[" + LABEL_CHAR + "]){0,1}"

    /**
     * / **
     * RFC 3492 references RFC 1034 and limits Punycode algorithm output to 63 characters.
     */
    private const val PUNYCODE_TLD = "xn\\-\\-[\\w\\-]{0,58}\\w"
    private const val TLD = "(" + PUNYCODE_TLD + "|" + "[" + TLD_CHAR + "]{2,63}" + ")"
    private const val HOST_NAME = "(" + IRI_LABEL + "\\.)+" + TLD
    private const val DOMAIN_NAME_STR = "(" + HOST_NAME + "|" + IP_ADDRESS_STRING + ")"
    private const val PROTOCOL = "(?i:http|https|rtsp|webview)://"

    /* A word boundary or end of input.  This is to stop foo.sure from matching as foo.su */
    private const val WORD_BOUNDARY = "(?:\\/|\\b|$|^)"
    private const val USER_INFO = (
        "(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)" +
            "\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_" +
            "\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@"
        )
    private const val PORT_NUMBER = "\\:\\d{1,5}"
    private const val PATH_AND_QUERY = (
        "[/?](?:(?:[" + LABEL_CHAR +
            ";/?:@&=#~" + // plus optional query params
            "\\-.\\+!\\*'\\(\\),_\\$])|(?:%[a-fA-F0-9]{2}))*"
        )

    /**
     * Regular expression pattern to match most part of RFC 3987
     * Internationalized URLs, aka IRIs.
     */
    @JvmField
    val WEB_URL = Pattern.compile(
        "(" +
            "(" +
            "(?:" + PROTOCOL + "(?:" + USER_INFO + ")?" + ")?" +
            "(?:" + DOMAIN_NAME_STR + ")" +
            "(?:" + PORT_NUMBER + ")?" +
            ")" +
            "(" + PATH_AND_QUERY + ")?" +
            WORD_BOUNDARY +
            ")"
    )
    private val WEB_URL_PATTERN = Patterns.WEB_URL
    val imageExtensions = arrayOf(".jpg", ".png", ".gif", ".tiff", ".raw")

    @JvmStatic
    fun extractLink(text: String): ExtractedLink? {
        return getLink(text)
    }

    @JvmStatic
    fun extractDeepLink(text: String): String? {
        val deeplinkMatcher = DEEPLINK_URL.matcher(text)
        return if (deeplinkMatcher.find()) {
            deeplinkMatcher.group()
        } else {
            null
        }
    }

    @JvmStatic
    fun extractEmailAddresses(text: String): List<String> {
        val emails: ArrayList<String> = ArrayList()
        val matcher = Pattern.compile("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}").matcher(text)
        while (matcher.find()) {
            emails.add(matcher.group())
        }
        return emails
    }

    fun isValidUrl(url: String): Boolean {
        return getLink(url) != null
    }

    private fun getLink(text: String): ExtractedLink? {
        if (TextUtils.isEmpty(text)) {
            return null
        }
        val m = WEB_URL.matcher(text)
        if (m.find()) {
            val url = m.group()
            val isEmailOnly = !isTextContainsNotOnlyEmail(text, url)
            return ExtractedLink(trimInvalidUrlCharacters(url), isEmailOnly)
        }
        val matcherRegular = WEB_URL_PATTERN.matcher(text)
        if (matcherRegular.find()) {
            val url = matcherRegular.group()
            val isEmailOnly = !isTextContainsNotOnlyEmail(text, url)
            return ExtractedLink(trimInvalidUrlCharacters(url), isEmailOnly)
        }
        return null
    }

    fun extractImageMarkdownLink(text: String): String? {
        val extractedLink = extractLink(text)
        if (extractedLink?.link == null || extractedLink.isEmail) return null
        var link = extractedLink.link
        link = link.lowercase(Locale.getDefault())
        for (extension in imageExtensions) {
            if (link.contains(extension)) {
                return link
            }
        }
        return null
    }

    @JvmStatic
    fun openUrl(context: Context, url: String) {
        var uri = Uri.parse(url)
        if (TextUtils.isEmpty(uri.scheme)) {
            uri = Uri.parse("https://$url")
        }
        val browserIntent = Intent(Intent.ACTION_VIEW, uri)
        if (browserIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(browserIntent)
        } else {
            show(context, "No application support this type of link")
        }
    }

    private fun trimInvalidUrlCharacters(url: String): String {
        var url = url
        if (TextUtils.isEmpty(url)) {
            return url
        }
        while (url.isNotEmpty() && !url.substring(url.length - 1).matches(Regex("[/\\w]+"))) {
            url = url.substring(0, url.length - 1)
        }
        return url
    }

    private fun isTextContainsNotOnlyEmail(text: String, url: String): Boolean {
        var text = text
        var indexOfUrl = 0
        while (true) {
            indexOfUrl = text.indexOf(url)
            if (indexOfUrl == 0 || indexOfUrl > 0 && text[indexOfUrl - 1] != '@') {
                return true
            }
            text = if (indexOfUrl >= 0) {
                text.substring(indexOfUrl + url.length)
            } else {
                return false
            }
        }
    }
}
