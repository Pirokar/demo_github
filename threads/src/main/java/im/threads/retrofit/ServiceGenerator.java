package im.threads.retrofit;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Admin on 28.03.2017.
 */

public class ServiceGenerator {

    private static String apiBaseUrl = "https://datastore.threads.im/";

    private static String userAgent;
    private static ThreadsApi sThreadsApi;

    private static final String USER_AGENT_HEADER = "User-Agent";


    public static ThreadsApi getThreadsApi() {
        if (sThreadsApi == null) {
            sThreadsApi = createService();
        }
        return sThreadsApi;
    }

    private static ThreadsApi createService() {
        Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(apiBaseUrl)
                        .addConverterFactory(GsonConverterFactory.create());

        HttpLoggingInterceptor logging =
                new HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY);

        Interceptor userAgentInterceptor = new Interceptor() {
            @Override
            public Response intercept(final Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder reqBuilder = original.newBuilder();
                reqBuilder.header(USER_AGENT_HEADER, userAgent);
                reqBuilder.method(original.method(), original.body()).build();
                return chain.proceed(reqBuilder.build());
            }
        };

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(userAgentInterceptor);
        httpClient.addInterceptor(logging);
        httpClient.connectTimeout(60, TimeUnit.SECONDS);
        builder.client(httpClient.build());
        Retrofit retrofit = builder.build();
        return retrofit.create(ThreadsApi.class);
    }

    public static void setUrl(String newApiBaseUrl) {
        apiBaseUrl = newApiBaseUrl;
    }

    public static void setUserAgent(final String userAgent) {
        ServiceGenerator.userAgent = userAgent;
    }
}
