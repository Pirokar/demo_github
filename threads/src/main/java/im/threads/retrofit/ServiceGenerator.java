package im.threads.retrofit;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Admin on 28.03.2017.
 */

public class ServiceGenerator {

    private static String apiBaseUrl = "https://datastore.threads.im/";

    private static ThreadsApi sThreadsApi;

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

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        httpClient.connectTimeout(60, TimeUnit.SECONDS);
        builder.client(httpClient.build());
        Retrofit retrofit = builder.build();
        return retrofit.create(ThreadsApi.class);
    }

    public static void setUrl(String newApiBaseUrl) {
        apiBaseUrl = newApiBaseUrl;
    }
}
