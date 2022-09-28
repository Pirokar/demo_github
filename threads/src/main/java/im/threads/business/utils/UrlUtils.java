package im.threads.business.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import im.threads.business.models.ExtractedLink;

public final class UrlUtils {

    public static final Pattern DEEPLINK_URL = Pattern.compile("[a-z0-9+.-]+://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|*]*");
    /**
     * Valid UCS characters defined in RFC 3987. Excludes space characters.
     */
    private static final String UCS_CHAR = "[" +
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
            "&&[^\u00A0[\u2000-\u200A]\u2028\u2029\u202F\u3000]]";
    private static final String IP_ADDRESS_STRING =
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9]))";
    /**
     * Valid characters for IRI label defined in RFC 3987.
     */
    private static final String LABEL_CHAR = "a-zA-Z0-9" + UCS_CHAR;
    /**
     * Valid characters for IRI TLD defined in RFC 3987.
     */
    private static final String TLD_CHAR = "a-zA-Z" + UCS_CHAR;
    /**
     * RFC 1035 Section 2.3.4 limits the labels to a maximum 63 octets.
     */
    private static final String IRI_LABEL =
            "[" + LABEL_CHAR + "](?:[" + LABEL_CHAR + "_\\-]{0,4048}[" + LABEL_CHAR + "]){0,1}";
    /**
     * /**
     * RFC 3492 references RFC 1034 and limits Punycode algorithm output to 63 characters.
     */
    private static final String PUNYCODE_TLD = "xn\\-\\-[\\w\\-]{0,58}\\w";
    private static final String TLD = "(" + PUNYCODE_TLD + "|" + "[" + TLD_CHAR + "]{2,63}" + ")";
    private static final String HOST_NAME = "(" + IRI_LABEL + "\\.)+" + TLD;
    private static final String DOMAIN_NAME_STR = "(" + HOST_NAME + "|" + IP_ADDRESS_STRING + ")";
    private static final String PROTOCOL = "(?i:http|https|rtsp|webview)://";
    /* A word boundary or end of input.  This is to stop foo.sure from matching as foo.su */
    private static final String WORD_BOUNDARY = "(?:\\/|\\b|$|^)";
    private static final String USER_INFO = "(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)"
            + "\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_"
            + "\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@";
    private static final String PORT_NUMBER = "\\:\\d{1,5}";
    private static final String PATH_AND_QUERY = "[/?](?:(?:[" + LABEL_CHAR
            + ";/?:@&=#~"  // plus optional query params
            + "\\-.\\+!\\*'\\(\\),_\\$])|(?:%[a-fA-F0-9]{2}))*";
    /**
     * Regular expression pattern to match most part of RFC 3987
     * Internationalized URLs, aka IRIs.
     */
    public static final Pattern WEB_URL = Pattern.compile("("
            + "("
            + "(?:" + PROTOCOL + "(?:" + USER_INFO + ")?" + ")?"
            + "(?:" + DOMAIN_NAME_STR + ")"
            + "(?:" + PORT_NUMBER + ")?"
            + ")"
            + "(" + PATH_AND_QUERY + ")?"
            + WORD_BOUNDARY
            + ")");
    private static final Pattern WEB_URL_PATTERN = Patterns.WEB_URL;

    private static final String[] imageExtensions = new String[] { ".jpg", ".png", ".gif", ".tiff", ".raw" };

    @Nullable
    public static ExtractedLink extractLink(@NonNull String text) {
        return getLink(text);
    }

    @Nullable
    public static String extractDeepLink(@NonNull String text) {
        Matcher deeplinkMatcher = DEEPLINK_URL.matcher(text);
        if (deeplinkMatcher.find()) {
            return deeplinkMatcher.group();
        }
        return null;
    }

    public static Boolean isValidUrl(@NonNull String url) {
        return getLink(url) != null;
    }

    private static ExtractedLink getLink(@NonNull String text) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }

        Matcher m = WEB_URL.matcher(text);
        if (m.find()) {
            String url = m.group();
            boolean isEmailOnly = !isTextContainsNotOnlyEmail(text, url);
            return new ExtractedLink(trimInvalidUrlCharacters(url), isEmailOnly);
        }

        Matcher matcherRegular = WEB_URL_PATTERN.matcher(text);
        if (matcherRegular.find()) {
            String url = matcherRegular.group();
            boolean isEmailOnly = !isTextContainsNotOnlyEmail(text, url);
            return new ExtractedLink(trimInvalidUrlCharacters(url), isEmailOnly);
        }
        return null;
    }

    @Nullable
    public static String extractImageMarkdownLink(@NonNull String text) {
        if (!text.contains("](http")) return null;
        ExtractedLink extractedLink = extractLink(text);
        if (extractedLink == null || extractedLink.getLink() == null) return null;
        String link = extractedLink.getLink();

        link = link.toLowerCase(Locale.getDefault());
        for (String extension : imageExtensions) {
            if (link.contains(extension)) {
                return link;
            }
        }
        return null;
    }

    public static void openUrl(@NonNull Context context, @NonNull String url) {
        Uri uri = Uri.parse(url);
        if (TextUtils.isEmpty(uri.getScheme())) {
            uri = Uri.parse("https://" + url);
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
        if (browserIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(browserIntent);
        } else {
            Toast.makeText(context, "No application support this type of link", Toast.LENGTH_SHORT).show();
        }
    }

    private static String trimInvalidUrlCharacters(String url) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }

        while (url.length() > 0 && !url.substring(url.length() - 1).matches("\\w+")) {
            url = url.substring(0, url.length() - 1);
        }

        return url;
    }

    private static Boolean isTextContainsNotOnlyEmail(String text, String url) {
        int indexOfUrl = 0;

        while (true) {
            indexOfUrl = text.indexOf(url);
            if (indexOfUrl == 0 || (indexOfUrl > 0 && text.charAt(indexOfUrl - 1) != '@')) {
                return true;
            }
            if (indexOfUrl >= 0) {
                text = text.substring(indexOfUrl + url.length());
            } else {
                return false;
            }
        }
    }
}
