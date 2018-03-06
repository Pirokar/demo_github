package im.threads.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by broomaservice on 06/03/2018.
 */

public class DateHelper {

    public static String SERVER_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static String SERVER_DATE_TIMEZONE = "UTC";


    public static long getMessageTimestamp (String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat(SERVER_DATE_FORMAT, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone(SERVER_DATE_TIMEZONE));
        Date date = new Date();
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

}
