package im.threads.internal;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import im.threads.ChatStyle;
import im.threads.ConfigBuilder;
import im.threads.ThreadsLib;
import im.threads.internal.exceptions.MetaConfigurationException;
import im.threads.internal.model.gson.UriDeserializer;
import im.threads.internal.model.gson.UriSerializer;
import im.threads.internal.transport.Transport;
import im.threads.internal.transport.threads_gate.ThreadsGateTransport;
import im.threads.internal.utils.MetaDataUtils;
import im.threads.internal.utils.PrefUtils;
import im.threads.internal.utils.ThreadsLogger;
import im.threads.styles.permissions.PermissionDescriptionDialogStyle;
import im.threads.styles.permissions.PermissionDescriptionType;

public final class Config {

    private static final String TAG = Config.class.getSimpleName();
    public static Config instance;

    @NonNull
    public final Context context;

    private volatile ChatStyle chatStyle = null;
    private volatile PermissionDescriptionDialogStyle
            storagePermissionDescriptionDialogStyle = null;
    private volatile PermissionDescriptionDialogStyle
            recordAudioPermissionDescriptionDialogStyle = null;
    private volatile PermissionDescriptionDialogStyle
            cameraPermissionDescriptionDialogStyle = null;

    @NonNull
    public final ThreadsLib.PendingIntentCreator pendingIntentCreator;
    @Nullable
    public final ThreadsLib.UnreadMessagesCountListener unreadMessagesCountListener;
    @NonNull
    public final Transport transport;
    @NonNull
    public final String serverBaseUrl;

    public final boolean isDebugLoggingEnabled;
    /**
     * set history loading count
     */
    public final int historyLoadingCount;

    public final int surveyCompletionDelay;
    public final boolean clientIdIgnoreEnabled;

    public final boolean newChatCenterApi;

    public final boolean attachmentEnabled;
    public final boolean filesAndMediaMenuItemEnabled;

    public final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Uri.class, new UriSerializer())
            .registerTypeAdapter(Uri.class, new UriDeserializer())
            .create();

    public Config(@NonNull Context context,
                  @Nullable String serverBaseUrl,
                  @Nullable ConfigBuilder.TransportType transportType,
                  @Nullable String threadsGateUrl,
                  @Nullable String threadsGateProviderUid,
                  @Nullable String threadsGateHCMProviderUid,
                  @NonNull ThreadsLib.PendingIntentCreator pendingIntentCreator,
                  @Nullable ThreadsLib.UnreadMessagesCountListener unreadMessagesCountListener,
                  boolean isDebugLoggingEnabled,
                  int historyLoadingCount,
                  int surveyCompletionDelay) {
        this.context = context.getApplicationContext();
        this.pendingIntentCreator = pendingIntentCreator;
        this.unreadMessagesCountListener = unreadMessagesCountListener;
        this.isDebugLoggingEnabled = isDebugLoggingEnabled;
        this.clientIdIgnoreEnabled = MetaDataUtils.getClientIdIgnoreEnabled(this.context);
        this.newChatCenterApi = MetaDataUtils.getNewChatCenterApi(this.context);
        this.attachmentEnabled = MetaDataUtils.getAttachmentEnabled(this.context);
        this.filesAndMediaMenuItemEnabled = MetaDataUtils.getFilesAndMeniaMenuItemEnabled(this.context);
        this.historyLoadingCount = historyLoadingCount;
        this.surveyCompletionDelay = surveyCompletionDelay;
        this.transport = getTransport(transportType, threadsGateUrl, threadsGateProviderUid,
                threadsGateHCMProviderUid);
        this.serverBaseUrl = getServerBaseUrl(serverBaseUrl);
    }

    public void applyChatStyle(ChatStyle chatStyle) {
        this.chatStyle = chatStyle;
        PrefUtils.setIncomingStyle(chatStyle);
    }

    public void applyStoragePermissionDescriptionDialogStyle(
            @NonNull PermissionDescriptionDialogStyle dialogStyle
    ) {
        this.storagePermissionDescriptionDialogStyle = dialogStyle;
        PrefUtils.setIncomingStyle(PermissionDescriptionType.STORAGE, dialogStyle);
    }

    public void applyRecordAudioPermissionDescriptionDialogStyle(
            @NonNull PermissionDescriptionDialogStyle dialogStyle
    ) {
        this.recordAudioPermissionDescriptionDialogStyle = dialogStyle;
        PrefUtils.setIncomingStyle(PermissionDescriptionType.RECORD_AUDIO, dialogStyle);
    }

    public void applyCameraPermissionDescriptionDialogStyle(
            @NonNull PermissionDescriptionDialogStyle dialogStyle
    ) {
        this.cameraPermissionDescriptionDialogStyle = dialogStyle;
        PrefUtils.setIncomingStyle(PermissionDescriptionType.CAMERA, dialogStyle);
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

    @NonNull
    public PermissionDescriptionDialogStyle getStoragePermissionDescriptionDialogStyle() {
        PermissionDescriptionDialogStyle localInstance = storagePermissionDescriptionDialogStyle;
        if (localInstance == null) {
            synchronized (PermissionDescriptionDialogStyle.class) {
                localInstance = storagePermissionDescriptionDialogStyle;
                if (localInstance == null) {
                    localInstance = PrefUtils.getIncomingStyle(PermissionDescriptionType.STORAGE);
                    if (localInstance == null) {
                        localInstance = PermissionDescriptionDialogStyle
                                .getDefaultDialogStyle(PermissionDescriptionType.STORAGE);
                    }
                    storagePermissionDescriptionDialogStyle = localInstance;
                }
            }
        }
        return localInstance;
    }

    @NonNull
    public PermissionDescriptionDialogStyle getRecordAudioPermissionDescriptionDialogStyle() {
        PermissionDescriptionDialogStyle localInstance = recordAudioPermissionDescriptionDialogStyle;
        if (localInstance == null) {
            synchronized (PermissionDescriptionDialogStyle.class) {
                localInstance = recordAudioPermissionDescriptionDialogStyle;
                if (localInstance == null) {
                    localInstance =
                            PrefUtils.getIncomingStyle(PermissionDescriptionType.RECORD_AUDIO);
                    if (localInstance == null) {
                        localInstance = PermissionDescriptionDialogStyle
                                .getDefaultDialogStyle(PermissionDescriptionType.RECORD_AUDIO);
                    }
                    recordAudioPermissionDescriptionDialogStyle = localInstance;
                }
            }
        }
        return localInstance;
    }

    @NonNull
    public PermissionDescriptionDialogStyle getCameraPermissionDescriptionDialogStyle() {
        PermissionDescriptionDialogStyle localInstance = cameraPermissionDescriptionDialogStyle;
        if (localInstance == null) {
            synchronized (PermissionDescriptionDialogStyle.class) {
                localInstance = cameraPermissionDescriptionDialogStyle;
                if (localInstance == null) {
                    localInstance = PrefUtils.getIncomingStyle(PermissionDescriptionType.CAMERA);
                    if (localInstance == null) {
                        localInstance = PermissionDescriptionDialogStyle
                                .getDefaultDialogStyle(PermissionDescriptionType.CAMERA);
                    }
                    cameraPermissionDescriptionDialogStyle = localInstance;
                }
            }
        }
        return localInstance;
    }

    private Transport getTransport(@Nullable ConfigBuilder.TransportType providedTransportType,
                                   @Nullable String providedThreadsGateUrl,
                                   @Nullable String providedThreadsGateProviderUid,
                                   @Nullable String providedThreadsGateHCMProviderUid) {
        ConfigBuilder.TransportType transportType;
        if (providedTransportType != null) {
            transportType = providedTransportType;
        } else {
            transportType = ConfigBuilder.TransportType.THREADS_GATE;
            String transportTypeValue = MetaDataUtils.getThreadsTransportType(this.context);
            if (!TextUtils.isEmpty(transportTypeValue)) {
                try {
                    transportType = ConfigBuilder.TransportType.fromString(transportTypeValue);
                } catch (IllegalArgumentException e) {
                    ThreadsLogger.e(TAG, "Transport type has incorrect value (correct value: THREADS_GATE). Default to THREADS_GATE");
                }
            } else {
                ThreadsLogger.e(TAG, "Transport type value is not set (correct value: THREADS_GATE). Default to THREADS_GATE");
            }
        }
        if (ConfigBuilder.TransportType.MFMS_PUSH == transportType) {
            throw new MetaConfigurationException("MFMS push transport is not supported anymore");
        }
        String threadsGateUrl = !TextUtils.isEmpty(providedThreadsGateUrl)
                ? providedThreadsGateUrl
                : MetaDataUtils.getThreadsGateUrl(this.context);
        if (TextUtils.isEmpty(threadsGateUrl)) {
            throw new MetaConfigurationException("Threads gate url is not set");
        }
        String threadsGateProviderUid = !TextUtils.isEmpty(providedThreadsGateProviderUid)
                ? providedThreadsGateProviderUid
                : MetaDataUtils.getThreadsGateProviderUid(this.context);
        String threadsGateHCMProviderUid = !TextUtils.isEmpty(providedThreadsGateHCMProviderUid)
                ? providedThreadsGateHCMProviderUid
                : MetaDataUtils.getThreadsGateHCMProviderUid(this.context);
        if (TextUtils.isEmpty(threadsGateProviderUid)) {
            throw new MetaConfigurationException("Threads gate provider uid is not set");
        }
        return new ThreadsGateTransport(threadsGateUrl, threadsGateProviderUid,
                threadsGateHCMProviderUid, isDebugLoggingEnabled);
    }

    @NonNull
    private String getServerBaseUrl(@Nullable String serverBaseUrl) {
        String baseUrl = TextUtils.isEmpty(serverBaseUrl) ? MetaDataUtils.getDatastoreUrl(this.context) : serverBaseUrl;
        if (baseUrl == null) {
            throw new MetaConfigurationException("Neither im.threads.getServerUrl meta variable, nor serverBaseUrl were provided");
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        return baseUrl;
    }
}
