package im.threads.internal.retrofit;

import java.util.concurrent.TimeUnit;

import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.opengraph.OGDataConverterFactory;
import im.threads.internal.utils.AppInfoHelper;
import im.threads.internal.utils.DeviceInfoHelper;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ServiceGenerator {

    private static String apiBaseUrl = "https://datastore.threads.im/";

    private static String userAgent;
    private static ThreadsApi sThreadsApi;

    public static final String USER_AGENT_HEADER = "User-Agent";

    public static ThreadsApi getThreadsApi() {
        if (sThreadsApi == null) {
            sThreadsApi = createService();
        }
        return sThreadsApi;
    }

    private static ThreadsApi createService() {
        userAgent = String.format(
                Config.instance.context.getResources().getString(R.string.threads_user_agent),
                DeviceInfoHelper.getOsVersion(),
                DeviceInfoHelper.getDeviceName(),
                DeviceInfoHelper.getIpAddress(),
                AppInfoHelper.getAppVersion(),
                AppInfoHelper.getAppId(),
                AppInfoHelper.getLibVersion()
        );
        Retrofit.Builder builder =
                new Retrofit.Builder()
                                .baseUrl(apiBaseUrl)
                        .addConverterFactory(new OGDataConverterFactory())
                        .addConverterFactory(GsonConverterFactory.create());

        Interceptor userAgentInterceptor = chain -> {
            Request original = chain.request();
            Request.Builder reqBuilder = original.newBuilder();
            reqBuilder.header(USER_AGENT_HEADER, userAgent);
            reqBuilder.method(original.method(), original.body()).build();
            return chain.proceed(reqBuilder.build());
        };

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(userAgentInterceptor);

        if (Config.instance.isDebugLoggingEnabled) {
            httpClient.addInterceptor(new HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY));
        }

        httpClient.connectTimeout(60, TimeUnit.SECONDS);
        builder.client(httpClient.build());
        Retrofit retrofit = builder.build();
        return retrofit.create(ThreadsApi.class);
    }

    public static void setUrl(String newApiBaseUrl) {
        apiBaseUrl = newApiBaseUrl;
    }
}
