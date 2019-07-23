package im.threads.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import im.threads.internal.ThreadsLogger;

/**
 * Created by broomaservice on 06/03/2018.
 */

public class DateHelper {
    private static final String TAG = DateHelper.class.getSimpleName();

    public static String SERVER_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static String SERVER_DATE_TIMEZONE = "UTC";

    private static SimpleDateFormat sdf = new SimpleDateFormat(SERVER_DATE_FORMAT, Locale.getDefault());

    static {
        sdf.setTimeZone(TimeZone.getTimeZone(SERVER_DATE_TIMEZONE));
    }

    public static long getMessageTimestampFromDateString(String dateString) {

        Date date = new Date();
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            ThreadsLogger.e(TAG, "getMessageTimestampFromDateString", e);
        }
        return date.getTime();
    }

    public static String getMessageDateStringFromTimestamp(long timestamp) {
        return sdf.format(new Date(timestamp));
    }

}
