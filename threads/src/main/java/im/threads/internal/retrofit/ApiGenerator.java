package im.threads.internal.retrofit;

import java.util.concurrent.TimeUnit;

import im.threads.R;
import im.threads.config.HttpClientSettings;
import im.threads.internal.Config;
import im.threads.internal.transport.AuthInterceptor;
import im.threads.internal.utils.AppInfoHelper;
import im.threads.internal.utils.DeviceInfoHelper;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiGenerator {

    private static final String USER_AGENT_HEADER = "User-Agent";
    private static ApiGenerator apiGenerator = null;

    private final ThreadsApi threadsApi;

    public static ThreadsApi getThreadsApi() {
        if (apiGenerator == null) {
            apiGenerator = new ApiGenerator();
        }
        return apiGenerator.threadsApi;
    }

    private ApiGenerator() {
        Retrofit build = new Retrofit.Builder()
                .baseUrl(Config.instance.serverBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(createOkHttpClient())
                .build();
        threadsApi = new ThreadsApi(
                build.create(OldThreadsApi.class),
                build.create(NewThreadsApi.class)
        );
    }

    private OkHttpClient createOkHttpClient() {
        HttpClientSettings httpSettings = Config.instance.requestConfig
                .getThreadsApiHttpClientSettings();
        OkHttpClient.Builder httpClient = new OkHttpClient
                .Builder()
                .addInterceptor(chain -> chain.proceed(
                        chain.request()
                                .newBuilder()
                                .header(USER_AGENT_HEADER, getUserAgent())
                                .build()
                        )
                )
                .addInterceptor(new AuthInterceptor())
                .connectTimeout(httpSettings.getConnectTimeoutMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(httpSettings.getReadTimeoutMillis(), TimeUnit.MILLISECONDS)
                .writeTimeout(httpSettings.getWriteTimeoutMillis(), TimeUnit.MILLISECONDS);
        if (Config.instance.isDebugLoggingEnabled) {
            httpClient.addInterceptor(new HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY));
        }
        return httpClient.build();
    }

    private String getUserAgent() {
        return String.format(
                Config.instance.context.getResources().getString(R.string.threads_user_agent),
                DeviceInfoHelper.getOsVersion(),
                DeviceInfoHelper.getDeviceName(),
                DeviceInfoHelper.getIpAddress(),
                AppInfoHelper.getAppVersion(),
                AppInfoHelper.getAppId(),
                AppInfoHelper.getLibVersion()
        );
    }

}
