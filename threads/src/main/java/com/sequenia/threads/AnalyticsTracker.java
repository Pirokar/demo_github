package com.sequenia.threads;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.sequenia.threads.utils.PrefUtils;


public class AnalyticsTracker {
    private static final String TAG = "AnalyticsTracker ";
    private static AnalyticsTracker instance;
    private GoogleAnalytics analytics;
    private Tracker tracker;
    private HitBuilders.EventBuilder eventBuilder;

    public static AnalyticsTracker getInstance(Context ctx, String GATrackerId) {
        if (instance == null) instance = new AnalyticsTracker(ctx, GATrackerId);
        return instance;
    }

    private AnalyticsTracker(Context ctx, String GATrackerId) {
        Log.i(TAG, "AnalyticsTracker: GATrackerId =" + GATrackerId);
        analytics = GoogleAnalytics.getInstance(ctx);
        analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
        analytics.setLocalDispatchPeriod(1);
        if (!analytics.isInitialized()) analytics.initialize();
        tracker = analytics.newTracker(GATrackerId);
        if (!tracker.isInitialized()) tracker.initialize();
        eventBuilder = new HitBuilders.EventBuilder();
    }

    public void chatWasOpened(String clientId) {
        Log.i(TAG, "chatWasOpened: " + clientId);
        tracker.send(eventBuilder.setCategory("chat_opening").setAction(clientId).build());
    }

    public void setTextWasSent() {
        Log.i(TAG, "setTextWasSent: ");
        tracker.send(eventBuilder.setCategory("message_sending").setAction("text").build());
        nullifyBuilder();
    }

    public void setFileWasSent() {
        Log.i(TAG, "setFileWasSent: ");
        tracker.send(eventBuilder.setCategory("message_sending").setAction("file").build());
        nullifyBuilder();
    }

    public void setQuoteWasSent() {
        Log.i(TAG, "setQuoteWasSent: ");
        tracker.send(eventBuilder.setCategory("message_sending").setAction("quote").build());
        nullifyBuilder();
    }

    public void setAttachmentWasOpened() {
        Log.i(TAG, "setAttachmentWasOpened: ");
        tracker.send(eventBuilder.setCategory("screen_opening").setAction("attachments").build());
        nullifyBuilder();
    }

    public void setTextSearchWasOpened() {
        Log.i(TAG, "setTextSearchWasOpened: ");
        tracker.send(eventBuilder.setCategory("screen_opening").setAction("search").setLabel("text").build());
        nullifyBuilder();
    }

    public void setImageSearchWasOpened() {
        Log.i(TAG, "setImageSearchWasOpened: ");
        tracker.send(eventBuilder.setCategory("screen_opening").setAction("search").setLabel("image").build());
        nullifyBuilder();
    }

    public void setFileSearchWasOpened() {
        Log.i(TAG, "setFileSearchWasOpened: ");
        tracker.send(eventBuilder.setCategory("screen_opening").setAction("search").setLabel("file").build());
        nullifyBuilder();
    }

    public void setConsultScreenOpened() {
        Log.i(TAG, "setConsultScreenOpened: ");
        tracker.send(eventBuilder.setCategory("screen_opening").setAction("participant").build());
        nullifyBuilder();
    }

    public void setCameraWasOpened() {
        Log.i(TAG, "setCameraWasOpened: ");
        tracker.send(eventBuilder.setCategory("screen_opening").setAction("camera").build());
        nullifyBuilder();
    }

    public void setGalleryWasOpened() {
        Log.i(TAG, "setGalleryWasOpened: ");
        tracker.send(eventBuilder.setCategory("screen_opening").setAction("photo_library").build());
        nullifyBuilder();
    }

    public void setConsultMessageWasReceived() {
        Log.i(TAG, "setConsultMessageWasReceived: ");
        tracker.send(eventBuilder.setCategory("push_message").build());
        nullifyBuilder();
    }

    public void setUserEnteredChat() {
        Log.i(TAG, "setUserEnteredChat: ");
        tracker.send(eventBuilder.setCategory("inout_chat_screen").setAction("in").build());
        nullifyBuilder();
    }

    public void setUserLeftChat() {
        Log.i(TAG, "setUserLeftChat: ");
        tracker.send(eventBuilder.setCategory("inout_chat_screen").setAction("out").build());
        nullifyBuilder();
    }

    public void setUserUploadedFile(long sizeKb) {
        Log.i(TAG, "setUserUploadedFile: " + sizeKb);
        tracker.send(eventBuilder.setCategory("file_loading").setAction(String.valueOf(sizeKb)).build());
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
