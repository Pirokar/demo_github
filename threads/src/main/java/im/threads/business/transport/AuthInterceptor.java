package im.threads.business.transport;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.IOException;

import im.threads.business.utils.preferences.PrefUtilsBase;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        final Request request;
        if (!TextUtils.isEmpty(PrefUtilsBase.getAuthToken())) {
            request = chain.request()
                .newBuilder()
                .addHeader("Authorization", PrefUtilsBase.getAuthToken())
                .addHeader("X-Auth-Schema", PrefUtilsBase.getAuthSchema())
            .build();
        } else {
            request = chain.request();
        }
        return chain.proceed(request);
    }
}
