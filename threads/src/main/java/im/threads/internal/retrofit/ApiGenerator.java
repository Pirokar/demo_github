package im.threads.internal.retrofit;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.transport.AuthInterceptor;
import im.threads.internal.utils.AppInfoHelper;
import im.threads.internal.utils.DeviceInfoHelper;
import im.threads.internal.utils.MetaDataUtils;
import im.threads.internal.utils.PrefUtils;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiGenerator {

    private static final String USER_AGENT_HEADER = "User-Agent";
    private static ApiGenerator apiGenerator = null;

    private ThreadsApi threadsApi;

    public static ThreadsApi getThreadsApi() throws IOException {
        if (apiGenerator == null) {
            apiGenerator = new ApiGenerator();
        }
        return apiGenerator.threadsApi;
    }

    private ApiGenerator() throws IOException {
        String baseUrl = MetaDataUtils.getDatastoreUrl(Config.instance.context);
        if (baseUrl == null) {
            throw new IOException("Empty im.threads.getServerUrl meta variable");
        }
        threadsApi = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(createOkHttpClient())
                .build().create(ThreadsApi.class);
    }

    private OkHttpClient createOkHttpClient() {
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
                .connectTimeout(60, TimeUnit.SECONDS);
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
