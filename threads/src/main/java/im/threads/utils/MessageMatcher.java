package im.threads.utils;

import android.os.Bundle;

import com.pushserver.android.PushGcmIntentService;

/**
 * Created by yuri on 14.07.2016.
 */
public class MessageMatcher {
    public static final String OPERATOR_JOINED = "OPERATOR_JOINED";
    public static final String OPERATOR_LEFT = "OPERATOR_LEFT";
    private static final String OPERATOR_TYPING = "TYPING";
    public static final String SCHEDULE = "SCHEDULE";
    public static final String RATING_THUMBS = "RATING_THUMBS";
    public static final String RATING_STARS = "RATING_STARS";
    public static final int TYPE_OPERATOR_JOINED = 1;
    public static final int TYPE_OPERATOR_LEFT = 2;
    public static final int TYPE_OPERATOR_TYPING = 3;
    public static final int TYPE_MESSAGE = 4;
    public static final int TYPE_MESSAGES_READ = 5;
    public static final int TYPE_SCHEDULE = 6;
    public static final int TYPE_RATING_THUMBS = 7;
    public static final int TYPE_RATING_STARS = 8;
    public static final int UNKNOWN = -1;

    private MessageMatcher() {
    }

    public static int getType(Bundle bundle) {
        if (bundle == null) {
            return UNKNOWN;
        }
        if (bundle.containsKey("readInMessageIds")){
            return TYPE_MESSAGES_READ;
        }

        if (bundle.getString(PushGcmIntentService.EXTRA_TYPE) != null && OPERATOR_JOINED.equals(bundle.getString(PushGcmIntentService.EXTRA_TYPE))) {
            return TYPE_OPERATOR_JOINED;
        }
        if (bundle.getString(PushGcmIntentService.EXTRA_TYPE) != null && OPERATOR_LEFT.equals(bundle.getString(PushGcmIntentService.EXTRA_TYPE))) {
            return TYPE_OPERATOR_LEFT;
        }
        if (bundle.getString(PushGcmIntentService.EXTRA_TYPE) != null && OPERATOR_TYPING.equals(bundle.getString(PushGcmIntentService.EXTRA_TYPE))) {
            return TYPE_OPERATOR_TYPING;
        }
        if (bundle.getString(PushGcmIntentService.EXTRA_TYPE) != null && SCHEDULE.equals(bundle.getString(PushGcmIntentService.EXTRA_TYPE))) {
            return TYPE_SCHEDULE;
        }
        if (bundle.getString(PushGcmIntentService.EXTRA_TYPE) != null && RATING_THUMBS.equals(bundle.getString(PushGcmIntentService.EXTRA_TYPE))) {
            return TYPE_RATING_THUMBS;
        }
        if (bundle.getString(PushGcmIntentService.EXTRA_TYPE) != null && RATING_STARS.equals(bundle.getString(PushGcmIntentService.EXTRA_TYPE))) {
            return TYPE_RATING_STARS;
        }
        if (bundle.getString(PushGcmIntentService.EXTRA_ALERT) != null && bundle.getString("advisa") == null && bundle.getString("GEO_FENCING") == null && bundle.getString(PushGcmIntentService.EXTRA_TYPE) == null) {
            return TYPE_MESSAGE;
        }
        return UNKNOWN;
    }
}
