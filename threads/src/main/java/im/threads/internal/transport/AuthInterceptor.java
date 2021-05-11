package im.threads.internal.transport;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.IOException;

import im.threads.internal.utils.PrefUtils;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        final Request request;
        if (!TextUtils.isEmpty(PrefUtils.getAuthToken())) {
            request = chain.request()
                .newBuilder()
                .addHeader("Authorization", PrefUtils.getAuthToken())
                .addHeader("X-Auth-Schema", PrefUtils.getAuthSchema())
            .build();
        } else {
            request = chain.request();
        }
        return chain.proceed(request);
    }
}
