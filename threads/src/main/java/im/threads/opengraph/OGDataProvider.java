package im.threads.opengraph;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import im.threads.model.ChatStyle;
import im.threads.retrofit.ServiceGenerator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class OGDataProvider {

    private static volatile OGDataProvider instance;
    private final OkHttpClient client;
    private Executor mExecutor = Executors.newCachedThreadPool();

    public static OGDataProvider getInstance() {

        if (instance == null) {
            synchronized (OGDataProvider.class) {
                if (instance == null) {
                    instance = new OGDataProvider();
                }
            }
        }
        return instance;
    }

    private OGDataProvider() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (ChatStyle.getInstance().isDebugLoggingEnabled) {
            clientBuilder.addInterceptor(new HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY));
        }

        //Preventing redirects to mobile versions without OpenGraph
        Interceptor userAgentInterceptor = new Interceptor() {
            @Override
            public Response intercept(final Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder reqBuilder = original.newBuilder();
                reqBuilder.header(ServiceGenerator.USER_AGENT_HEADER,
                        "Chrome/68.0.3440.106");
                reqBuilder.method(original.method(), original.body()).build();
                return chain.proceed(reqBuilder.build());
            }
        };

        clientBuilder.addInterceptor(userAgentInterceptor);
        clientBuilder.connectTimeout(60, TimeUnit.SECONDS);

        client = clientBuilder.build();
    }


    public static void getOGData(String url, final retrofit2.Callback<OGData> callback) {

        //Workaround - retrofit will add baseUrl for scheme-less urls
        if (!url.startsWith("http")) {
            url = "http://" + url;
        }

        ServiceGenerator.getThreadsApi().getOGData(url).enqueue(callback);
    }

    public static void getOGData(final String url, final Callback<OGData> callback) {

        getInstance().mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final String finalUrl;

                //Adding http for urls without scheme
                if (!url.startsWith("http")) {
                    finalUrl = "http://" + url;
                } else {
                    finalUrl = url;
                }

                Request request = new Request.Builder().url(finalUrl).build();

                try {
                    Response response = getInstance().client.newCall(request).execute();
                    callback.onSuccess(OGParser.parse(response.body().byteStream()));
                } catch (IOException e) {
                    callback.onError(e);
                }
            }
        });
    }

    public interface Callback<T> {
        void onSuccess(T data);

        void onError(Exception error);
    }

}
