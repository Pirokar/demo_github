package im.threads.internal.opengraph;

import im.threads.internal.retrofit.ApiGenerator;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public final class OGDataProvider {

    private static volatile OGDataProvider instance;

    private OGDataProvider() {
    }

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

    public Maybe<OGData> getOGData(final String url) {
        return Single
                .fromCallable(() -> {
                    if (!url.toLowerCase().startsWith("http")) {
                        OGResponse ogResponse = ApiGenerator.getThreadsApi().openGraph("http://" + url).execute().body();
                        if (ogResponse == null || ogResponse.getOgdata() == null) {
                            ogResponse = ApiGenerator.getThreadsApi().openGraph("https://" + url).execute().body();
                        }
                        return ogResponse;
                    } else {
                       return ApiGenerator.getThreadsApi().openGraph(url).execute().body();
                    }
                })
                .filter(ogResponse -> ogResponse.getOgdata() != null)
                .map(OGResponse::getOgdata)
                .subscribeOn(Schedulers.io());
    }
}
