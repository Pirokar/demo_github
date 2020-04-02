package im.threads.internal;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;

import im.threads.ChatStyle;
import im.threads.ConfigBuilder;
import im.threads.ThreadsLib;
import im.threads.internal.exceptions.MetaConfigurationException;
import im.threads.internal.transport.Transport;
import im.threads.internal.transport.mfms_push.MFMSPushTransport;
import im.threads.internal.transport.threads_gate.ThreadsGateTransport;
import im.threads.internal.utils.MetaDataUtils;
import im.threads.internal.utils.PrefUtils;
import im.threads.internal.utils.ThreadsLogger;

public final class Config {

    private static final String TAG = Config.class.getSimpleName();
    public static Config instance;

    @NonNull
    public final Context context;

    private volatile ChatStyle chatStyle = null;

    @NonNull
    public final ThreadsLib.PendingIntentCreator pendingIntentCreator;
    @Nullable
    public final ThreadsLib.UnreadMessagesCountListener unreadMessagesCountListener;
    @NonNull
    public final Transport transport;

    public final boolean isDebugLoggingEnabled;
    /**
     * set history loading count
     */
    public final int historyLoadingCount;

    public final int surveyCompletionDelay;

    public final Gson gson = new Gson();

    public Config(@NonNull Context context,
                  @NonNull ThreadsLib.PendingIntentCreator pendingIntentCreator,
                  @Nullable ThreadsLib.UnreadMessagesCountListener unreadMessagesCountListener,
                  boolean isDebugLoggingEnabled,
                  int historyLoadingCount,
                  int surveyCompletionDelay) {
        this.context = context.getApplicationContext();
        this.pendingIntentCreator = pendingIntentCreator;
        this.unreadMessagesCountListener = unreadMessagesCountListener;
        this.isDebugLoggingEnabled = isDebugLoggingEnabled;
        this.historyLoadingCount = historyLoadingCount;
        this.surveyCompletionDelay = surveyCompletionDelay;
        this.transport = getTransport(isDebugLoggingEnabled);
    }

    public void applyChatStyle(ChatStyle chatStyle) {
        this.chatStyle = chatStyle;
        PrefUtils.setIncomingStyle(chatStyle);
    }

    @NonNull
    public ChatStyle getChatStyle() {
        ChatStyle localInstance = chatStyle;
        if (localInstance == null) {
            synchronized (ChatStyle.class) {
                localInstance = chatStyle;
                if (localInstance == null) {
                    localInstance = PrefUtils.getIncomingStyle();
                    if (localInstance == null) {
                        localInstance = new ChatStyle();
                    }
                    chatStyle = localInstance;
                }
            }
        }
        return localInstance;
    }

    private Transport getTransport(boolean isDebugLoggingEnabled) {
        ConfigBuilder.TransportType transportType = ConfigBuilder.TransportType.MFMS_PUSH;
        String transportTypeValue = MetaDataUtils.getThreadsTransportType(this.context);
        if (!TextUtils.isEmpty(transportTypeValue)) {
            try {
                transportType = ConfigBuilder.TransportType.fromString(transportTypeValue);
            } catch (IllegalArgumentException e) {
                ThreadsLogger.e(TAG, "Transport type has incorrect value (correct values: MFMS_PUSH, THREADS_GATE). Default to MFMS_PUSH");
            }
        } else {
            ThreadsLogger.e(TAG, "Transport type value is not set (correct values: MFMS_PUSH, THREADS_GATE). Default to MFMS_PUSH");
        }
        if (ConfigBuilder.TransportType.THREADS_GATE == transportType) {
            String threadsGateUrl = MetaDataUtils.getThreadsGateUrl(this.context);
            if (TextUtils.isEmpty(threadsGateUrl)) {
                throw new MetaConfigurationException("Threads gate url is not set");
            }
            String threadsGateProviderUid = MetaDataUtils.getThreadsGateProviderUid(this.context);
            if (TextUtils.isEmpty(threadsGateProviderUid)) {
                throw new MetaConfigurationException("Threads gate provider uid is not set");
            }
            return new ThreadsGateTransport(threadsGateUrl, threadsGateProviderUid, isDebugLoggingEnabled);
        }
        return new MFMSPushTransport();
    }
}
