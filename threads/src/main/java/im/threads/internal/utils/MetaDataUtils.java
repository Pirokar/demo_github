package im.threads.internal.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MetaDataUtils {

    private static final String TAG = MetaDataUtils.class.getSimpleName();

    private static final String DATASTORE_URL = "im.threads.getDatastoreUrl";
    private static final String SERVER_BASE_URL = "im.threads.getServerUrl";
    private static final String THREADS_GATE_URL = "im.threads.threadsGateUrl";
    private static final String THREADS_GATE_PROVIDER_UID = "im.threads.threadsGateProviderUid";
    private static final String THREADS_GATE_HCM_PROVIDER_UID = "im.threads.threadsGateHCMProviderUid";
    @Deprecated
    private static final String THREADS_TRANSPORT_TYPE = "im.threads.threadsTransportType";
    private static final String CLIENT_ID_IGNORE_ENABLED = "im.threads.clientIdIgnoreEnabled";
    private static final String NEW_CHAT_CENTER_API = "im.threads.newChatCenterApi";
    private static final String ATTACHMENT_ENABLED = "im.threads.attachmentEnabled";
    private static final String FILES_AND_MEDIA_MENU_ITEM_ENABLED = "im.threads.filesAndMediaMenuItemEnabled";

    @Nullable
    public static String getDatastoreUrl(@NonNull Context context) {
        Bundle metaData = getMetaData(context);
        if (metaData != null && metaData.containsKey(DATASTORE_URL)) {
            return metaData.getString(DATASTORE_URL);
        }
        return null;
    }

    @Nullable
    public static String getServerBaseUrl(@NonNull Context context) {
        Bundle metaData = getMetaData(context);
        if (metaData != null && metaData.containsKey(SERVER_BASE_URL)) {
            return metaData.getString(SERVER_BASE_URL);
        }
        return null;
    }

    @Nullable
    public static String getThreadsGateUrl(@NonNull Context context) {
        Bundle metaData = getMetaData(context);
        if (metaData != null && metaData.containsKey(THREADS_GATE_URL)) {
            return metaData.getString(THREADS_GATE_URL);
        }
        return null;
    }

    @Nullable
    public static String getThreadsGateProviderUid(@NonNull Context context) {
        Bundle metaData = getMetaData(context);
        if (metaData != null && metaData.containsKey(THREADS_GATE_PROVIDER_UID)) {
            return metaData.getString(THREADS_GATE_PROVIDER_UID);
        }
        return null;
    }

    @Nullable
    public static String getThreadsGateHCMProviderUid(@NonNull Context context) {
        Bundle metaData = getMetaData(context);
        if (metaData != null && metaData.containsKey(THREADS_GATE_HCM_PROVIDER_UID)) {
            return metaData.getString(THREADS_GATE_HCM_PROVIDER_UID);
        }
        return null;
    }

    @Nullable
    public static String getThreadsTransportType(@NonNull Context context) {
        Bundle metaData = getMetaData(context);
        if (metaData != null && metaData.containsKey(THREADS_TRANSPORT_TYPE)) {
            return metaData.getString(THREADS_TRANSPORT_TYPE);
        }
        return null;
    }

    public static boolean getClientIdIgnoreEnabled(@NonNull Context context) {
        Bundle metaData = getMetaData(context);
        if (metaData != null && metaData.containsKey(CLIENT_ID_IGNORE_ENABLED)) {
            return metaData.getBoolean(CLIENT_ID_IGNORE_ENABLED);
        }
        return false;
    }

    public static boolean getNewChatCenterApi(@NonNull Context context) {
        Bundle metaData = getMetaData(context);
        if (metaData != null && metaData.containsKey(NEW_CHAT_CENTER_API)) {
            return metaData.getBoolean(NEW_CHAT_CENTER_API);
        }
        return false;
    }

    public static boolean getAttachmentEnabled(@NonNull Context context) {
        Bundle metaData = getMetaData(context);
        if (metaData != null && metaData.containsKey(ATTACHMENT_ENABLED)) {
            return metaData.getBoolean(ATTACHMENT_ENABLED);
        }
        return true;
    }

    public static boolean getFilesAndMeniaMenuItemEnabled(@NonNull Context context) {
        Bundle metaData = getMetaData(context);
        if (metaData != null && metaData.containsKey(FILES_AND_MEDIA_MENU_ITEM_ENABLED)) {
            return metaData.getBoolean(FILES_AND_MEDIA_MENU_ITEM_ENABLED);
        }
        return true;
    }

    @Nullable
    public static Bundle getMetaData(@NonNull Context context) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return ai.metaData;
        } catch (PackageManager.NameNotFoundException e) {
            ThreadsLogger.e(TAG, "Failed to load self applicationInfo - that's really weird. ", e);
            return null;
        }
    }
}
