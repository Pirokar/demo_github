package im.threads.utils;

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
            LogUtils.logDev("URL found: " + url);
            links.add(url);
        }

        return links;
    }

}
