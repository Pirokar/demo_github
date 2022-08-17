package im.threads.internal;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.KeyStore;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import im.threads.ChatStyle;
import im.threads.ThreadsLib;
import im.threads.config.RequestConfig;
import im.threads.config.SocketClientSettings;
import im.threads.internal.domain.logger.LoggerConfig;
import im.threads.internal.exceptions.MetaConfigurationException;
import im.threads.internal.imageLoading.ImageLoaderOkHttpProvider;
import im.threads.internal.model.SslSocketFactoryConfig;
import im.threads.internal.model.gson.UriDeserializer;
import im.threads.internal.model.gson.UriSerializer;
import im.threads.internal.transport.Transport;
import im.threads.internal.transport.threads_gate.ThreadsGateTransport;
import im.threads.internal.utils.MetaDataUtils;
import im.threads.internal.utils.PrefUtils;
import im.threads.internal.utils.TlsConfigurationUtils;
import im.threads.styles.permissions.PermissionDescriptionDialogStyle;
import im.threads.styles.permissions.PermissionDescriptionType;
import okhttp3.Interceptor;

public final class Config {

    private static final String TAG = Config.class.getSimpleName();
    public static Config instance;

    @NonNull
    public final Context context;

    public final RequestConfig requestConfig;
    public final SslSocketFactoryConfig sslSocketFactoryConfig;
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
    public final Interceptor networkInterceptor;
    @Nullable
    public final ThreadsLib.UnreadMessagesCountListener unreadMessagesCountListener;
    @NonNull
    public final Transport transport;
    @NonNull
    public final String serverBaseUrl;
    @NonNull
    public final String datastoreUrl;

    public final boolean isDebugLoggingEnabled;
    /**
     * set history loading count
     */
    public final int historyLoadingCount;

    public final int surveyCompletionDelay;

    public final boolean newChatCenterApi;

    @Nullable
    public final LoggerConfig loggerConfig;

    public final boolean attachmentEnabled;
    public final boolean filesAndMediaMenuItemEnabled;

    public final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Uri.class, new UriSerializer())
            .registerTypeAdapter(Uri.class, new UriDeserializer())
            .create();

    public Config(@NonNull Context context,
                  @Nullable String serverBaseUrl,
                  @Nullable String datastoreUrl,
                  @Nullable String threadsGateUrl,
                  @Nullable String threadsGateProviderUid,
                  @Nullable String threadsGateHCMProviderUid,
                  @Nullable Boolean isNewChatCenterApi,
                  @Nullable LoggerConfig loggerConfig,
                  @NonNull ThreadsLib.PendingIntentCreator pendingIntentCreator,
                  @Nullable ThreadsLib.UnreadMessagesCountListener unreadMessagesCountListener,
                  @Nullable Interceptor networkInterceptor,
                  boolean isDebugLoggingEnabled,
                  int historyLoadingCount,
                  int surveyCompletionDelay,
                  @NonNull RequestConfig requestConfig,
                  List<Integer> certificateRawResIds) {
        this.context = context.getApplicationContext();
        this.pendingIntentCreator = pendingIntentCreator;
        this.unreadMessagesCountListener = unreadMessagesCountListener;
        this.networkInterceptor = networkInterceptor;
        this.isDebugLoggingEnabled = isDebugLoggingEnabled;
        this.newChatCenterApi = getIsNewChatCenterApi(isNewChatCenterApi);
        this.loggerConfig = loggerConfig;
        this.attachmentEnabled = MetaDataUtils.getAttachmentEnabled(this.context);
        this.filesAndMediaMenuItemEnabled = MetaDataUtils.getFilesAndMeniaMenuItemEnabled(this.context);
        this.historyLoadingCount = historyLoadingCount;
        this.surveyCompletionDelay = surveyCompletionDelay;
        this.sslSocketFactoryConfig = getSslSocketFactoryConfig(certificateRawResIds);
        this.transport = getTransport(threadsGateUrl, threadsGateProviderUid,
                threadsGateHCMProviderUid, requestConfig.getSocketClientSettings());
        this.serverBaseUrl = getServerBaseUrl(serverBaseUrl);
        this.datastoreUrl = getDatastoreUrl(datastoreUrl);
        this.requestConfig = requestConfig;
        ImageLoaderOkHttpProvider.INSTANCE.createOkHttpClient(
                requestConfig.getPicassoHttpClientSettings(),
                sslSocketFactoryConfig
        );
    }

    private SslSocketFactoryConfig getSslSocketFactoryConfig(List<Integer> certificateRawResIds) {
        if (certificateRawResIds == null || certificateRawResIds.isEmpty())
            return null;

        KeyStore keyStore = TlsConfigurationUtils.createTlsPinningKeyStore(
                context.getResources(),
                certificateRawResIds
        );
        TrustManager[] trustManagers = TlsConfigurationUtils.getTrustManagers(keyStore);
        X509TrustManager trustManager = TlsConfigurationUtils.getX509TrustManager(trustManagers);
        SSLSocketFactory sslSocketFactory =
                TlsConfigurationUtils.createTlsPinningSocketFactory(trustManagers);
        return new SslSocketFactoryConfig(sslSocketFactory, trustManager);
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

    private Transport getTransport(@Nullable String providedThreadsGateUrl,
                                   @Nullable String providedThreadsGateProviderUid,
                                   @Nullable String providedThreadsGateHCMProviderUid,
                                   SocketClientSettings socketClientSettings) {
        String threadsGateProviderUid = !TextUtils.isEmpty(providedThreadsGateProviderUid)
                ? providedThreadsGateProviderUid
                : MetaDataUtils.getThreadsGateProviderUid(this.context);
        String threadsGateHCMProviderUid = !TextUtils.isEmpty(providedThreadsGateHCMProviderUid)
                ? providedThreadsGateHCMProviderUid
                : MetaDataUtils.getThreadsGateHCMProviderUid(this.context);
        String threadsGateUrl = !TextUtils.isEmpty(providedThreadsGateUrl)
                ? providedThreadsGateUrl
                : MetaDataUtils.getThreadsGateUrl(this.context);
        if (TextUtils.isEmpty(threadsGateUrl)) {
            throw new MetaConfigurationException("Threads gate url is not set");
        }
        if (TextUtils.isEmpty(threadsGateProviderUid)) {
            throw new MetaConfigurationException("Threads gate provider uid is not set");
        }
        return new ThreadsGateTransport(
                threadsGateUrl,
                threadsGateProviderUid,
                threadsGateHCMProviderUid,
                isDebugLoggingEnabled,
                socketClientSettings,
                sslSocketFactoryConfig,
                networkInterceptor
        );
    }

    @NonNull
    private String getServerBaseUrl(@Nullable String serverBaseUrl) {
        String baseUrl = TextUtils.isEmpty(serverBaseUrl) ? MetaDataUtils.getServerBaseUrl(this.context) : serverBaseUrl;
        if (baseUrl == null) {
            throw new MetaConfigurationException("Neither im.threads.getServerUrl meta variable, nor serverBaseUrl were provided");
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        return baseUrl;
    }

    @NonNull
    private String getDatastoreUrl(@Nullable String dataStoreUrl) {
        String datastoreUrl = TextUtils.isEmpty(dataStoreUrl) ? MetaDataUtils.getDatastoreUrl(this.context) : dataStoreUrl;
        if (datastoreUrl == null) {
            throw new MetaConfigurationException("Neither im.threads.getDatastoreUrl meta variable, nor datastoreUrl were provided");
        }
        if (!datastoreUrl.endsWith("/")) {
            datastoreUrl = datastoreUrl + "/";
        }
        return datastoreUrl;
    }

    private boolean getIsNewChatCenterApi(@Nullable Boolean isNewChatCenterApi) {
        return isNewChatCenterApi == null ? MetaDataUtils.getNewChatCenterApi(this.context) : isNewChatCenterApi;
    }
}
