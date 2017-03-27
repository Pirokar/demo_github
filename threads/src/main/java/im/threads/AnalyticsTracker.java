package im.threads;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import im.threads.model.ChatStyle;
import im.threads.utils.PrefUtils;


public class AnalyticsTracker {
    private static final String TAG = "AnalyticsTracker ";
    private static AnalyticsTracker instance;
    private GoogleAnalytics analytics;
    private Tracker tracker;
    private HitBuilders.EventBuilder eventBuilder;
    private boolean dryRun = false;

    public static AnalyticsTracker getInstance(Context ctx, String GATrackerId) {
        if (instance == null) instance = new AnalyticsTracker(ctx, GATrackerId);
        return instance;
    }

    private AnalyticsTracker(Context ctx, String GATrackerId) {
        Log.i(TAG, "AnalyticsTracker: GATrackerId =" + GATrackerId);
        analytics = null;
        try {
            analytics = GoogleAnalytics.getInstance(ctx);
            analytics.setLocalDispatchPeriod(1);
            ChatStyle style = PrefUtils.getIncomingStyle(ctx);
            if (style!=null) analytics.setDryRun(style.isGAEnabled);
            if (!analytics.isInitialized()) analytics.initialize();
            tracker = analytics.newTracker(GATrackerId);
            if (!tracker.isInitialized()) tracker.initialize();
            eventBuilder = new HitBuilders.EventBuilder();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void chatWasOpened(String clientId) {
        Log.i(TAG, "chatWasOpened: " + clientId);
       if(null != tracker) tracker.send(eventBuilder.setCategory("chat_opening").setAction(clientId).build());
    }

    public void setTextWasSent() {
        Log.i(TAG, "setTextWasSent: ");
        if(null != tracker) tracker.send(eventBuilder.setCategory("message_sending").setAction("text").build());
        nullifyBuilder();
    }

    public void setFileWasSent() {
        Log.i(TAG, "setFileWasSent: ");
        if(null != tracker) tracker.send(eventBuilder.setCategory("message_sending").setAction("file").build());
        nullifyBuilder();
    }

    public void setQuoteWasSent() {
        Log.i(TAG, "setQuoteWasSent: ");
        if(null != tracker)  tracker.send(eventBuilder.setCategory("message_sending").setAction("quote").build());
        nullifyBuilder();
    }

    public void setAttachmentWasOpened() {
        Log.i(TAG, "setAttachmentWasOpened: ");
        if(null != tracker) tracker.send(eventBuilder.setCategory("screen_opening").setAction("attachments").build());
        nullifyBuilder();
    }

    public void setTextSearchWasOpened() {
        Log.i(TAG, "setTextSearchWasOpened: ");
        if(null != tracker) tracker.send(eventBuilder.setCategory("screen_opening").setAction("search").setLabel("text").build());
        nullifyBuilder();
    }

    public void setImageSearchWasOpened() {
        Log.i(TAG, "setImageSearchWasOpened: ");
        if(null != tracker) tracker.send(eventBuilder.setCategory("screen_opening").setAction("search").setLabel("image").build());
        nullifyBuilder();
    }

    public void setFileSearchWasOpened() {
        Log.i(TAG, "setFileSearchWasOpened: ");
        if(null != tracker) tracker.send(eventBuilder.setCategory("screen_opening").setAction("search").setLabel("file").build());
        nullifyBuilder();
    }

    public void setConsultScreenOpened() {
        Log.i(TAG, "setConsultScreenOpened: ");
        if(null != tracker) tracker.send(eventBuilder.setCategory("screen_opening").setAction("participant").build());
        nullifyBuilder();
    }

    public void setCameraWasOpened() {
        Log.i(TAG, "setCameraWasOpened: ");
        if(null != tracker) tracker.send(eventBuilder.setCategory("screen_opening").setAction("camera").build());
        nullifyBuilder();
    }

    public void setGalleryWasOpened() {
        Log.i(TAG, "setGalleryWasOpened: ");
        if(null != tracker) tracker.send(eventBuilder.setCategory("screen_opening").setAction("photo_library").build());
        nullifyBuilder();
    }

    public void setConsultMessageWasReceived() {
        Log.i(TAG, "setConsultMessageWasReceived: ");
        if(null != tracker) tracker.send(eventBuilder.setCategory("push_message").build());
        nullifyBuilder();
    }

    public void setUserEnteredChat() {
        Log.i(TAG, "setUserEnteredChat: ");
        if(null != tracker) tracker.send(eventBuilder.setCategory("inout_chat_screen").setAction("in").build());
        nullifyBuilder();
    }

    public void setUserLeftChat() {
        Log.i(TAG, "setUserLeftChat: ");
        if(null != tracker) tracker.send(eventBuilder.setCategory("inout_chat_screen").setAction("out").build());
        nullifyBuilder();
    }

    public void setUserUploadedFile(long sizeKb) {
        Log.i(TAG, "setUserUploadedFile: " + sizeKb + "kb");
        if(null != tracker) tracker.send(eventBuilder.setCategory("file_loading").setAction(String.valueOf(sizeKb)).build());
        nullifyBuilder();
    }


    public void setCopyWasSent() {
        Log.i(TAG, "setCopyWasSent: ");
        tracker.send(eventBuilder.setCategory("message_sending").setAction("copy").build());
        nullifyBuilder();
    }
    private void nullifyBuilder(){
        if (eventBuilder==null)return;
        eventBuilder.setCategory(null);
        eventBuilder.setAction(null);
        eventBuilder.setLabel(null);
    }
}
