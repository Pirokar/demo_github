package im.threads.internal.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class MetaDataUtils {

    private static final String TAG = MetaDataUtils.class.getSimpleName();

    private static final String DATASTORE_URL = "im.threads.getServerUrl";
    private static final String THREADS_GATE_URL = "im.threads.threadsGateUrl";
    private static final String THREADS_GATE_PROVIDER_UID = "im.threads.threadsGateProviderUid";
    private static final String THREADS_TRANSPORT_TYPE = "im.threads.threadsTransportType";

    @Nullable
    public static String getDatastoreUrl(@NonNull Context context) {
        return getMetaData(context, DATASTORE_URL);
    }

    @Nullable
    public static String getThreadsGateUrl(@NonNull Context context) {
        return getMetaData(context, THREADS_GATE_URL);
    }

    @Nullable
    public static String getThreadsGateProviderUid(@NonNull Context context) {
        return getMetaData(context, THREADS_GATE_PROVIDER_UID);
    }

    @Nullable
    public static String getThreadsTransportType(@NonNull Context context) {
        return getMetaData(context, THREADS_TRANSPORT_TYPE);
    }

    @Nullable
    private static String getMetaData(@NonNull Context context, String key) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            return bundle.getString(key);
        } catch (PackageManager.NameNotFoundException e) {
            ThreadsLogger.e(TAG, "Failed to load self applicationInfo - that's really weird. ", e);
            return null;
        }
    }
}
