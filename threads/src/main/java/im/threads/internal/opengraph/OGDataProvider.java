package im.threads.internal.opengraph;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import im.threads.internal.Config;
import im.threads.internal.utils.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;

public final class OGDataProvider {

    private static final String USER_AGENT_HEADER = "User-Agent";

    private static volatile OGDataProvider instance;
    private final OkHttpClient client;
    private final OGParser ogParser;
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
        client = createOkHttpClient();
        ogParser = new OGParser();
    }

    private OkHttpClient createOkHttpClient() {
        //Preventing redirects to mobile versions without OpenGraph
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .addInterceptor(chain -> chain.proceed(chain.request().newBuilder().header(USER_AGENT_HEADER, "Chrome/68.0.3440.106").build()))
                .connectTimeout(60, TimeUnit.SECONDS);
        if (Config.instance.isDebugLoggingEnabled) {
            clientBuilder.addInterceptor(new HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BASIC));
        }
        return clientBuilder.build();
    }

    public void getOGData(final String url, final Callback<OGData, Throwable> callback) {
        mExecutor.execute(() -> {
            final String finalUrl;
            //Adding http for urls without scheme
            if (!url.toLowerCase().startsWith("http")) {
                finalUrl = "http://" + url;
            } else {
                finalUrl = url;
            }
            try {
                ResponseBody responseBody = client.newCall(new Request.Builder().url(finalUrl).build()).execute().body();
                if (responseBody == null) {
                    callback.onError(new IOException("null response body"));
                    return;
                }
                callback.onSuccess(ogParser.parse(responseBody.byteStream()));
            } catch (IOException e) {
                callback.onError(e);
            }
        });
    }
}
