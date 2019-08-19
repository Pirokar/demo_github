package im.threads.internal.opengraph;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import im.threads.internal.Config;
import im.threads.internal.utils.Callback;
import im.threads.internal.utils.ThreadsLogger;
import im.threads.internal.retrofit.ServiceGenerator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;

public class OGDataProvider {
    private static final String TAG = OGDataProvider.class.getSimpleName();

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
        if (Config.instance.isDebugLoggingEnabled) {
            clientBuilder.addInterceptor(new HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BASIC));
        }
        //Preventing redirects to mobile versions without OpenGraph
        Interceptor userAgentInterceptor = chain -> {
            Request original = chain.request();
            Request.Builder reqBuilder = original.newBuilder();
            reqBuilder.header(ServiceGenerator.USER_AGENT_HEADER,
                    "Chrome/68.0.3440.106");
            reqBuilder.method(original.method(), original.body()).build();
            return chain.proceed(reqBuilder.build());
        };
        clientBuilder.addInterceptor(userAgentInterceptor);
        clientBuilder.connectTimeout(60, TimeUnit.SECONDS);
        client = clientBuilder.build();
    }

    public static void getOGDataProxy(String url, final Callback<OGData, Throwable> callback) {
        //Workaround - proxy doesn't understand scheme-less urls
        if (!url.startsWith("http")) {
            url = "http://" + url;
        }
        ServiceGenerator.getThreadsApi().getOGDataProxy(url).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                try {
                    OGData ogData = OGParser.parse(response.body().byteStream());
                    callback.onSuccess(ogData);
                } catch (Exception e) {
                    ThreadsLogger.e(TAG, "getOGDataProxy", e);
                    callback.onError(e);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onError(t);
            }
        });
    }


    public static void getOGData(String url, final retrofit2.Callback<OGData> callback) {
        //Workaround - retrofit will add baseUrl for scheme-less urls
        if (!url.startsWith("http")) {
            url = "http://" + url;
        }
        ServiceGenerator.getThreadsApi().getOGData(url).enqueue(callback);
    }

    public static void getOGData(final String url, final Callback<OGData, Throwable> callback) {
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
                try {
                    Request request = new Request.Builder().url(finalUrl).build();
                    Response response = getInstance().client.newCall(request).execute();
                    callback.onSuccess(OGParser.parse(response.body().byteStream()));
                } catch (Exception e) {
                    callback.onError(e);
                }
            }
        });
    }
}
