package im.threads.internal.opengraph;

import com.annimon.stream.Optional;

import java.io.IOException;
import java.util.concurrent.Callable;

import im.threads.internal.retrofit.BackendApiGenerator;
import im.threads.internal.utils.ThreadsLogger;
import io.reactivex.Maybe;
import io.reactivex.Single;

public final class OGDataProvider {

    private static final String TAG = "OGDataProvider";
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
                .fromCallable((Callable<Optional<OGResponse>>) () -> {
                    try {
                        if (!url.toLowerCase().startsWith("http")) {
                            OGResponse ogResponse = BackendApiGenerator.getApi().openGraph("http://" + url).execute().body();
                            if (ogResponse == null || ogResponse.getOgdata() == null) {
                                ogResponse = BackendApiGenerator.getApi().openGraph("https://" + url).execute().body();
                            }
                            return Optional.ofNullable(ogResponse);
                        } else {
                            return Optional.ofNullable(BackendApiGenerator.getApi().openGraph(url).execute().body());
                        }
                    } catch (IOException e) {
                        ThreadsLogger.e(TAG, "getOGData failed: ", e);
                    }
                    return Optional.empty();
                })
                .filter(ogOptional -> ogOptional.isPresent() && ogOptional.get().getOgdata() != null)
                .map(ogResponseOptional -> ogResponseOptional.get().getOgdata());
    }
}
