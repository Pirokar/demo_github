package im.threads.opengraph;

import java.io.IOException;

import im.threads.retrofit.ServiceGenerator;
import im.threads.retrofit.ThreadsApi;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class OGDataProvider {

    private static volatile OGDataProvider instance;

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


    public static void getOGData(String url, final Callback<OGData> callback) {

        ThreadsApi api = ServiceGenerator.getThreadsApi();

        api.getUrlResponseBody(url).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                OGData data = null;

                try {
                    data = OGParser.parse(response.body().byteStream());
                } catch (IOException e) {
                    callback.onError(e);
                }

                callback.onSuccess(data);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onError(t);
            }
        });

    }

    public interface Callback<T> {
        void onSuccess(T object);

        void onError(Throwable error);
    }

}
