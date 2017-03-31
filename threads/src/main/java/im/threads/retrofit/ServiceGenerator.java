package im.threads.retrofit;

import java.io.IOException;

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

    private static RetrofitService retrofitService;

    public static RetrofitService getRetrofitService() {
        if(retrofitService == null) {
            retrofitService = createService();
        }
        return retrofitService;
    }

    private static RetrofitService createService() {
        Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(apiBaseUrl)
                        .addConverterFactory(GsonConverterFactory.create());

//        Interceptor interceptor = new Interceptor() {
//            @Override
//            public Response intercept(Chain chain) throws IOException {
//                Request original = chain.request();
//
//                Request request = original.newBuilder()
//                        .header("X-Client-Token", token)
//                        .method(original.method(), original.body())
//                        .build();
//
//                return chain.proceed(request);
//            }
//        };

        HttpLoggingInterceptor logging =
                new HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
//        httpClient.addInterceptor(interceptor);
        builder.client(httpClient.build());

        Retrofit retrofit = builder.build();

        return retrofit.create(RetrofitService.class);
    }
}
