package im.threads.internal.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class UrlUtils {

    public static List<String> extractLinks(String text) {
        List<String> links = new ArrayList<>();
        Matcher m = Patterns.WEB_URL.matcher(text);
        while (m.find()) {
            String url = m.group();
            links.add(url);
        }
        return links;
    }

    public static void openUrl(Context context, String url) {
        Uri uri = Uri.parse(url);
        if (TextUtils.isEmpty(uri.getScheme())) {
            uri = Uri.parse("http://" + url);
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
        if (browserIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(browserIntent);
        }
    }
}
