package im.threads.business.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import im.threads.business.logger.core.LoggerEdna;

public final class DateHelper {
    private static String SERVER_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static String SERVER_DATE_TIMEZONE = "UTC";

    private static SimpleDateFormat sdf = new SimpleDateFormat(SERVER_DATE_FORMAT, Locale.getDefault());

    static {
        sdf.setTimeZone(TimeZone.getTimeZone(SERVER_DATE_TIMEZONE));
    }

    public static synchronized long getMessageTimestampFromDateString(String dateString) {
        Date date = new Date();
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            LoggerEdna.error("getMessageTimestampFromDateString", e);
        }
        return date.getTime();
    }

    public static synchronized String getMessageDateStringFromTimestamp(long timestamp) {
        return sdf.format(new Date(timestamp));
    }
}
