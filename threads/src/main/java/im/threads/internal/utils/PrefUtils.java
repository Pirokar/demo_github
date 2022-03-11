package im.threads.internal.utils;

import static im.threads.ConfigBuilder.TransportType.THREADS_GATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.util.ObjectsCompat;
import androidx.preference.PreferenceManager;

import com.google.firebase.installations.FirebaseInstallations;
import com.google.gson.JsonSyntaxException;

import java.io.Serializable;
import java.util.UUID;

import im.threads.ChatStyle;
import im.threads.internal.Config;
import im.threads.internal.model.CampaignMessage;
import im.threads.internal.model.ClientNotificationDisplayType;
import im.threads.internal.model.FileDescription;
import im.threads.internal.transport.CloudMessagingType;
import im.threads.styles.permissions.PermissionDescriptionDialogStyle;
import im.threads.styles.permissions.PermissionDescriptionType;

public final class PrefUtils {
    private static final String TAG = "PrefUtils ";

    //styles
    private static final String APP_STYLE = "APP_STYLE";
    private static final String STORAGE_PERMISSION_DESCRIPTION_DIALOG_STYLE =
            "STORAGE_PERMISSION_DESCRIPTION_DIALOG_STYLE";
    private static final String RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_STYLE =
            "RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_STYLE";
    private static final String CAMERA_PERMISSION_DESCRIPTION_DIALOG_STYLE =
            "CAMERA_PERMISSION_DESCRIPTION_DIALOG_STYLE";

    private static final String TAG_CLIENT_ID = "TAG_CLIENT_ID";
    private static final String TAG_CLIENT_ID_ENCRYPTED = "TAG_CLIENT_ID_ENCRYPTED";
    private static final String CLIENT_ID_SIGNATURE_KEY = "CLIENT_ID_SIGNATURE";
    private static final String TAG_NEW_CLIENT_ID = "TAG_NEW_CLIENT_ID";
    @Deprecated
    private static final String IS_CLIENT_ID_SET_TAG = "IS_CLIENT_ID_SET_TAG";
    private static final String CLIENT_NAME = "DEFAULT_CLIENT_NAMETITLE_TAG";
    private static final String EXTRA_DATA = "EXTRA_DATE";
    private static final String LAST_COPY_TEXT = "LAST_COPY_TEXT";
    @Deprecated
    private static final String TAG_THREAD_ID = "THREAD_ID";
    private static final String APP_MARKER_KEY = "APP_MARKER";
    private static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private static final String FCM_TOKEN = "FCM_TOKEN";
    private static final String HCM_TOKEN = "HCM_TOKEN";
    private static final String CLOUD_MESSAGING_TYPE = "CLOUD_MESSAGING_TYPE";
    @Deprecated
    private static final String TRANSPORT_TYPE = "TRANSPORT_TYPE";
    private static final String DEVICE_UID = "DEVICE_UID";
    private static final String AUTH_TOKEN = "AUTH_TOKEN";
    private static final String AUTH_SCHEMA = "AUTH_SCHEMA";
    private static final String MIGRATED = "MIGRATED";
    private static final String CLIENT_NOTIFICATION_DISPLAY_TYPE = "CLIENT_NOTIFICATION_DISPLAY_TYPE";
    private static final String THREAD_ID = "THREAD_ID";
    private static final String FILE_DESCRIPTION_DRAFT = "FILE_DESCRIPTION_DRAFT";
    private static final String CAMPAIGN_MESSAGE = "CAMPAIGN_MESSAGE";

    private static final String UNREAD_PUSH_COUNT = "UNREAD_PUSH_COUNT";

    private static final String STORE_NAME = "im.threads.internal.utils.PrefStore";

    private PrefUtils() {
    }

    public static String getLastCopyText() {
        return getDefaultSharedPreferences().getString(LAST_COPY_TEXT, null);
    }

    public static void setLastCopyText(String text) {
        getDefaultSharedPreferences()
                .edit()
                .putString(LAST_COPY_TEXT, text)
                .commit();
    }

    public static String getUserName() {
        return getDefaultSharedPreferences().getString(CLIENT_NAME, "");
    }

    public static void setUserName(String clientName) {
        getDefaultSharedPreferences()
                .edit()
                .putString(CLIENT_NAME, clientName)
                .commit();
    }

    public static String getData() {
        return getDefaultSharedPreferences().getString(EXTRA_DATA, "");
    }

    public static void setData(String data) {
        getDefaultSharedPreferences()
                .edit()
                .putString(EXTRA_DATA, data)
                .commit();
    }

    public static void setNewClientId(@NonNull String clientId) {
        getDefaultSharedPreferences()
                .edit()
                .putString(TAG_NEW_CLIENT_ID, clientId)
                .commit();
    }

    public static String getNewClientID() {
        return getDefaultSharedPreferences().getString(TAG_NEW_CLIENT_ID, null);
    }

    public static void setClientId(@NonNull String clientId) {
        getDefaultSharedPreferences()
                .edit()
                .putString(TAG_CLIENT_ID, clientId)
                .commit();
    }

    public static String getClientID() {
        return getDefaultSharedPreferences().getString(TAG_CLIENT_ID, "");
    }

    public static void setClientIdEncrypted(boolean clientIdEncrypted) {
        getDefaultSharedPreferences()
                .edit()
                .putBoolean(TAG_CLIENT_ID_ENCRYPTED, clientIdEncrypted)
                .commit();
    }

    public static boolean getClientIDEncrypted() {
        return getDefaultSharedPreferences().getBoolean(TAG_CLIENT_ID_ENCRYPTED, false);
    }

    public static String getClientIdSignature() {
        return getDefaultSharedPreferences().getString(CLIENT_ID_SIGNATURE_KEY, "");
    }

    public static void setClientIdSignature(String clientIdSignature) {
        getDefaultSharedPreferences()
                .edit()
                .putString(CLIENT_ID_SIGNATURE_KEY, clientIdSignature)
                .commit();
    }

    public static ClientNotificationDisplayType getClientNotificationDisplayType() {
        return ClientNotificationDisplayType.fromString(
                getDefaultSharedPreferences().getString(CLIENT_NOTIFICATION_DISPLAY_TYPE, "")
        );
    }

    public static void setClientNotificationDisplayType(@NonNull ClientNotificationDisplayType type) {
        getDefaultSharedPreferences()
                .edit()
                .putString(CLIENT_NOTIFICATION_DISPLAY_TYPE, type.name())
                .commit();
    }

    public static long getThreadId() {
        return getDefaultSharedPreferences().getLong(THREAD_ID, -1);
    }

    public static void setThreadId(long threadId) {
        getDefaultSharedPreferences()
                .edit()
                .putLong(THREAD_ID, threadId)
                .commit();
    }

    @Nullable
    public static FileDescription getFileDescriptionDraft() {
        String value = getDefaultSharedPreferences().getString(FILE_DESCRIPTION_DRAFT, "");
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        return Config.instance.gson.fromJson(value, FileDescription.class);
    }

    public static void setFileDescriptionDraft(@Nullable FileDescription fileDescriptionDraft) {
        String value = fileDescriptionDraft != null ? Config.instance.gson.toJson(fileDescriptionDraft) : "";
        getDefaultSharedPreferences()
                .edit()
                .putString(FILE_DESCRIPTION_DRAFT, value)
                .commit();
    }

    @Nullable
    public static CampaignMessage getCampaignMessage() {
        String value = getDefaultSharedPreferences().getString(CAMPAIGN_MESSAGE, "");
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        return Config.instance.gson.fromJson(value, CampaignMessage.class);
    }

    public static void setCampaignMessage(@Nullable CampaignMessage campaignMessage) {
        getDefaultSharedPreferences()
                .edit()
                .putString(CAMPAIGN_MESSAGE, campaignMessage != null ? Config.instance.gson.toJson(campaignMessage) : null)
                .commit();
    }

    public static boolean isClientIdEmpty() {
        return getClientID().isEmpty() && !Config.instance.clientIdIgnoreEnabled;
    }

    @Nullable
    public static ChatStyle getIncomingStyle() {
        return getIncomingStyle(APP_STYLE, ChatStyle.class);
    }

    @Nullable
    public static PermissionDescriptionDialogStyle getIncomingStyle(
            @NonNull PermissionDescriptionType type) {
        PermissionDescriptionDialogStyle style = null;
        switch (type) {
            case STORAGE:
                style = getIncomingStyle(STORAGE_PERMISSION_DESCRIPTION_DIALOG_STYLE,
                        PermissionDescriptionDialogStyle.class);
                break;
            case RECORD_AUDIO:
                style = getIncomingStyle(RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_STYLE,
                        PermissionDescriptionDialogStyle.class);
                break;
            case CAMERA:
                style = getIncomingStyle(CAMERA_PERMISSION_DESCRIPTION_DIALOG_STYLE,
                        PermissionDescriptionDialogStyle.class);
                break;
            default:
                break;
        }
        return style;
    }

    @Nullable
    private static <T extends Serializable> T getIncomingStyle(@NonNull String key,
                                                               @NonNull Class<T> styleClass) {
        T style = null;
        try {
            SharedPreferences sharedPreferences = getDefaultSharedPreferences();
            if (sharedPreferences.getString(key, null) != null) {
                String sharedPreferencesString = sharedPreferences.getString(key, null);
                style = Config.instance.gson.fromJson(sharedPreferencesString, styleClass);
            }
        } catch (IllegalStateException | JsonSyntaxException ex) {
            ThreadsLogger.w(TAG, "getIncomingStyle " + styleClass.getCanonicalName()
                    + " failed: ", ex);
        }
        return style;
    }

    public static void setIncomingStyle(@NonNull ChatStyle style) {
        setIncomingStyle(APP_STYLE, style);
    }

    public static void setIncomingStyle(@NonNull PermissionDescriptionType type,
                                        @NonNull PermissionDescriptionDialogStyle style) {
        switch (type) {
            case STORAGE:
                setIncomingStyle(STORAGE_PERMISSION_DESCRIPTION_DIALOG_STYLE, style);
                break;
            case RECORD_AUDIO:
                setIncomingStyle(RECORD_AUDIO_PERMISSION_DESCRIPTION_DIALOG_STYLE, style);
                break;
            case CAMERA:
                setIncomingStyle(CAMERA_PERMISSION_DESCRIPTION_DIALOG_STYLE, style);
                break;
            default:
                break;
        }
    }

    private static <T extends Serializable> void setIncomingStyle(@NonNull String key,
                                                                  @NonNull T style) {
        getDefaultSharedPreferences()
                .edit()
                .putString(key, Config.instance.gson.toJson(style))
                .commit();
    }

    public static String getAppMarker() {
        String appMarker = getDefaultSharedPreferences().getString(APP_MARKER_KEY, "");
        return appMarker.length() > 0 ? appMarker : null;
    }

    public static void setAppMarker(String appMarker) {
        getDefaultSharedPreferences()
                .edit()
                .putString(APP_MARKER_KEY, appMarker)
                .commit();
    }

    public static String getFcmToken() {
        String fcmToken = getDefaultSharedPreferences().getString(FCM_TOKEN, "");
        return fcmToken.length() > 0 ? fcmToken : null;
    }

    public static void setFcmToken(String fcmToken) {
        String cloudMessagingType = PrefUtils.getCloudMessagingType();
        if (cloudMessagingType == null) {
            PrefUtils.setCloudMessagingType(CloudMessagingType.FCM.toString());
        }
        getDefaultSharedPreferences()
                .edit()
                .putString(FCM_TOKEN, fcmToken)
                .commit();
    }

    public static String getHcmToken() {
        String hcmToken = getDefaultSharedPreferences().getString(HCM_TOKEN, "");
        return hcmToken.length() > 0 ? hcmToken : null;
    }

    public static void setHcmToken(String hcmToken) {
        String cloudMessagingType = PrefUtils.getCloudMessagingType();
        if (cloudMessagingType == null) {
            PrefUtils.setCloudMessagingType(CloudMessagingType.HCM.toString());
        }
        getDefaultSharedPreferences()
                .edit()
                .putString(HCM_TOKEN, hcmToken)
                .commit();
    }

    @Nullable
    public static String getCloudMessagingType() {
        String cloudMessagingType = getDefaultSharedPreferences().getString(CLOUD_MESSAGING_TYPE, "");
        return !TextUtils.isEmpty(cloudMessagingType) ? cloudMessagingType : null;
    }

    public static void setCloudMessagingType(String cloudMessagingType) {
        getDefaultSharedPreferences()
                .edit()
                .putString(CLOUD_MESSAGING_TYPE, cloudMessagingType)
                .commit();
    }

    public static String getDeviceAddress() {
        String deviceAddress = getDefaultSharedPreferences().getString(DEVICE_ADDRESS, "");
        return deviceAddress.length() > 0 ? deviceAddress : null;
    }

    public static void setDeviceAddress(String deviceAddress) {
        getDefaultSharedPreferences()
                .edit()
                .putString(DEVICE_ADDRESS, deviceAddress)
                .commit();
    }

    public static synchronized String getDeviceUid() {
        String deviceUid = getDefaultSharedPreferences().getString(DEVICE_UID, "");
        if (deviceUid.length() <= 0) {
            deviceUid = UUID.randomUUID().toString();
            getDefaultSharedPreferences()
                    .edit()
                    .putString(DEVICE_UID, deviceUid)
                    .commit();
        }
        return deviceUid;
    }

    public static String getAuthToken() {
        return getDefaultSharedPreferences().getString(AUTH_TOKEN, "");
    }

    public static void setAuthToken(String authToken) {
        getDefaultSharedPreferences()
                .edit()
                .putString(AUTH_TOKEN, authToken)
                .commit();
    }

    public static String getAuthSchema() {
        return getDefaultSharedPreferences().getString(AUTH_SCHEMA, "");
    }

    public static void setAuthSchema(String authSchema) {
        getDefaultSharedPreferences()
                .edit()
                .putString(AUTH_SCHEMA, authSchema)
                .commit();
    }

    public static void migrateToSeparateStorageIfNeeded() {
        SharedPreferences newSharedPreferences = getDefaultSharedPreferences();
        if (!newSharedPreferences.getBoolean(MIGRATED, false)) {
            SharedPreferences oldSharedPreferences = PreferenceManager.getDefaultSharedPreferences(Config.instance.context);
            newSharedPreferences
                    .edit()
                    .putString(TAG_CLIENT_ID, oldSharedPreferences.getString(PrefUtils.class + TAG_CLIENT_ID, ""))
                    .putBoolean(TAG_CLIENT_ID_ENCRYPTED, oldSharedPreferences.getBoolean(PrefUtils.class + TAG_CLIENT_ID_ENCRYPTED, false))
                    .putString(CLIENT_ID_SIGNATURE_KEY, oldSharedPreferences.getString(PrefUtils.class + CLIENT_ID_SIGNATURE_KEY, ""))
                    .putString(TAG_NEW_CLIENT_ID, oldSharedPreferences.getString(PrefUtils.class + TAG_NEW_CLIENT_ID, null))
                    .putBoolean(IS_CLIENT_ID_SET_TAG, oldSharedPreferences.getBoolean(PrefUtils.class + IS_CLIENT_ID_SET_TAG, false))
                    .putString(CLIENT_NAME, oldSharedPreferences.getString(PrefUtils.class + CLIENT_NAME, ""))
                    .putString(EXTRA_DATA, oldSharedPreferences.getString(PrefUtils.class + EXTRA_DATA, ""))
                    .putString(LAST_COPY_TEXT, oldSharedPreferences.getString(PrefUtils.class + LAST_COPY_TEXT, null))
                    .putLong(TAG_THREAD_ID, oldSharedPreferences.getLong(PrefUtils.class + TAG_THREAD_ID, -1L))
                    .putString(APP_MARKER_KEY, oldSharedPreferences.getString(PrefUtils.class + APP_MARKER_KEY, ""))
                    .putString(DEVICE_ADDRESS, oldSharedPreferences.getString(PrefUtils.class + DEVICE_ADDRESS, ""))
                    .putString(DEVICE_UID, oldSharedPreferences.getString(PrefUtils.class + DEVICE_UID, ""))
                    .putBoolean(MIGRATED, true)
                    .commit();
        }
    }

    @WorkerThread
    public static void migrateTransportIfNeeded() {
        String transportType = getTransportType();
        if (!TextUtils.isEmpty(transportType)) {
            if (!ObjectsCompat.equals(transportType, Config.instance.transport.getType().toString())) {
                resetPushToken();
            }
        }
        setTransportType(THREADS_GATE.toString());
    }

    public static int getUnreadPushCount() {
        return getDefaultSharedPreferences().getInt(UNREAD_PUSH_COUNT, 0);
    }

    public static void setUnreadPushCount(int unreadPushCount) {
        getDefaultSharedPreferences().edit().putInt(UNREAD_PUSH_COUNT, unreadPushCount).commit();
    }

    private static void resetPushToken() {
        FirebaseInstallations.getInstance().delete()
                .addOnCompleteListener(
                        task -> FirebaseInstallations.getInstance().getId()
                );
    }

    private static String getTransportType() {
        String transportType = getDefaultSharedPreferences().getString(TRANSPORT_TYPE, "");
        return !TextUtils.isEmpty(transportType) ? transportType : null;
    }

    private static void setTransportType(String transportType) {
        getDefaultSharedPreferences()
                .edit()
                .putString(TRANSPORT_TYPE, transportType)
                .commit();
    }

    private static SharedPreferences getDefaultSharedPreferences() {
        return Config.instance.context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE);
    }
}
