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
    public static final String SURVEY = "SURVEY";
    public static final String MESSAGE = "MESSAGE";
    public static final String ON_HOLD = "ON_HOLD";
    public static final String NONE = "NONE";
    public static final String MESSAGES_READ = "MESSAGES_READ";
    public static final String OPERATOR_LOOKUP_STARTED = "OPERATOR_LOOKUP_STARTED";
    public static final String CLIENT_BLOCKED = "CLIENT_BLOCKED";
    public static final String THREAD_OPENED = "THREAD_OPENED";
    public static final String THREAD_CLOSED = "THREAD_CLOSED";
    public static final String SCENARIO = "SCENARIO";


    public static final int TYPE_OPERATOR_JOINED = 1;
    public static final int TYPE_OPERATOR_LEFT = 2;
    public static final int TYPE_OPERATOR_TYPING = 3;
    public static final int TYPE_MESSAGE = 4;
    public static final int TYPE_MESSAGES_READ = 5;
    public static final int TYPE_SCHEDULE = 6;
    public static final int TYPE_SURVEY = 7;
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
        if (bundle.getString(PushGcmIntentService.EXTRA_TYPE) != null && SURVEY.equals(bundle.getString(PushGcmIntentService.EXTRA_TYPE))) {
            return TYPE_SURVEY;
        }
        if (bundle.getString(PushGcmIntentService.EXTRA_ALERT) != null && bundle.getString("advisa") == null && bundle.getString("GEO_FENCING") == null && bundle.getString(PushGcmIntentService.EXTRA_TYPE) == null) {
            return TYPE_MESSAGE;
        }
        return UNKNOWN;
    }
}
