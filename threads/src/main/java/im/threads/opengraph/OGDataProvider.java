package im.threads.opengraph;

import im.threads.retrofit.ServiceGenerator;
import retrofit2.Callback;

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
        ServiceGenerator.getThreadsApi().getOGData(url).enqueue(callback);
    }

}
