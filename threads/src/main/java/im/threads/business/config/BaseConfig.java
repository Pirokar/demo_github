package im.threads.business.config;

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

import im.threads.business.core.UnreadMessagesCountListener;
import im.threads.business.exceptions.MetaConfigurationException;
import im.threads.business.imageLoading.ImageLoaderOkHttpProvider;
import im.threads.business.logger.core.LoggerConfig;
import im.threads.business.models.SslSocketFactoryConfig;
import im.threads.business.rest.config.RequestConfig;
import im.threads.business.rest.config.SocketClientSettings;
import im.threads.business.transport.Transport;
import im.threads.business.transport.threadsGate.ThreadsGateTransport;
import im.threads.business.utils.MetadataBusiness;
import im.threads.business.utils.TlsConfigurationUtils;
import im.threads.business.utils.gson.UriDeserializer;
import im.threads.business.utils.gson.UriSerializer;
import okhttp3.Interceptor;

public class BaseConfig {

    private static final String TAG = BaseConfig.class.getSimpleName();
    public static BaseConfig instance;

    @NonNull
    public final Context context;

    public final RequestConfig requestConfig;
    public final SslSocketFactoryConfig sslSocketFactoryConfig;

    @Nullable
    public final Interceptor networkInterceptor;
    @Nullable
    public final UnreadMessagesCountListener unreadMessagesCountListener;
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

    public final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Uri.class, new UriSerializer())
            .registerTypeAdapter(Uri.class, new UriDeserializer())
            .create();

    public BaseConfig(@NonNull Context context,
                      @Nullable String serverBaseUrl,
                      @Nullable String datastoreUrl,
                      @Nullable String threadsGateUrl,
                      @Nullable String threadsGateProviderUid,
                      @Nullable String threadsGateHCMProviderUid,
                      @Nullable Boolean isNewChatCenterApi,
                      @Nullable LoggerConfig loggerConfig,
                      @Nullable UnreadMessagesCountListener unreadMessagesCountListener,
                      @Nullable Interceptor networkInterceptor,
                      boolean isDebugLoggingEnabled,
                      int historyLoadingCount,
                      int surveyCompletionDelay,
                      @NonNull RequestConfig requestConfig,
                      List<Integer> certificateRawResIds) {
        this.context = context.getApplicationContext();
        this.unreadMessagesCountListener = unreadMessagesCountListener;
        this.networkInterceptor = networkInterceptor;
        this.isDebugLoggingEnabled = isDebugLoggingEnabled;
        this.newChatCenterApi = getIsNewChatCenterApi(isNewChatCenterApi);
        this.loggerConfig = loggerConfig;
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

    private Transport getTransport(@Nullable String providedThreadsGateUrl,
                                   @Nullable String providedThreadsGateProviderUid,
                                   @Nullable String providedThreadsGateHCMProviderUid,
                                   SocketClientSettings socketClientSettings) {
        String threadsGateProviderUid = !TextUtils.isEmpty(providedThreadsGateProviderUid)
                ? providedThreadsGateProviderUid
                : MetadataBusiness.getThreadsGateProviderUid(this.context);
        String threadsGateHCMProviderUid = !TextUtils.isEmpty(providedThreadsGateHCMProviderUid)
                ? providedThreadsGateHCMProviderUid
                : MetadataBusiness.getThreadsGateHCMProviderUid(this.context);
        String threadsGateUrl = !TextUtils.isEmpty(providedThreadsGateUrl)
                ? providedThreadsGateUrl
                : MetadataBusiness.getThreadsGateUrl(this.context);
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
        String baseUrl = TextUtils.isEmpty(serverBaseUrl) ? MetadataBusiness.getServerBaseUrl(this.context) : serverBaseUrl;
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
        String datastoreUrl = TextUtils.isEmpty(dataStoreUrl) ? MetadataBusiness.getDatastoreUrl(this.context) : dataStoreUrl;
        if (datastoreUrl == null) {
            throw new MetaConfigurationException("Neither im.threads.getDatastoreUrl meta variable, nor datastoreUrl were provided");
        }
        if (!datastoreUrl.endsWith("/")) {
            datastoreUrl = datastoreUrl + "/";
        }
        return datastoreUrl;
    }

    private boolean getIsNewChatCenterApi(@Nullable Boolean isNewChatCenterApi) {
        return isNewChatCenterApi == null ? MetadataBusiness.getNewChatCenterApi(this.context) : isNewChatCenterApi;
    }
}
