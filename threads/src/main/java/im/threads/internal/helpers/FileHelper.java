package im.threads.internal.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.io.File;

import im.threads.internal.Config;
import im.threads.internal.chat_updates.ChatUpdateProcessor;
import im.threads.internal.transport.models.AttachmentSettings;
import im.threads.internal.utils.ThreadsLogger;

public enum  FileHelper {

    INSTANCE;

    private static final double MEGABYTE = 1024 * 1024;
    private static final String PREF_ATTACHMENT_SETTINGS = "PREF_ATTACHMENT_SETTINGS";
    private static String TAG = FileHelper.class.getSimpleName();

    FileHelper() {
        ChatUpdateProcessor.getInstance().getAttachmentSettingsProcessor()
                .subscribe(attachmentSettings -> saveAttachmentSettings(attachmentSettings.getContent()));
    }

    public static File createImageFile(Context context) {
        String filename = "thr" + System.currentTimeMillis() + ".jpg";
        File output = new File(context.getFilesDir(), filename);
        ThreadsLogger.d(TAG, "File genereated into filesDir : " + output.getAbsolutePath());
        return output;
    }

    public boolean isAllowedFileSize(long fileSize) {
        return fileSize / MEGABYTE <= getMaxAllowedFileSize();
    }

    public boolean isAllowedFileExtension(String fileExtension) {
        AttachmentSettings.Content attachmentSettings = getAttachmentSettings();
        for (String allowedExt : attachmentSettings.getFileExtensions()) {
            if (allowedExt.equalsIgnoreCase(fileExtension)) {
                return true;
            }
        }
        return false;
    }

    public long getMaxAllowedFileSize() {
        return getAttachmentSettings().getMaxSize();
    }

    private void saveAttachmentSettings(AttachmentSettings.Content attachmentSettingsContent) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(Config.instance.context).edit();
        editor.putString(PREF_ATTACHMENT_SETTINGS, Config.instance.gson.toJson(attachmentSettingsContent));
        editor.commit();
    }

    private AttachmentSettings.Content getAttachmentSettings() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Config.instance.context);
        String settingsStr = sharedPreferences.getString(PREF_ATTACHMENT_SETTINGS, null);
        if (settingsStr == null) {
            return getDefaultAttachmentSettings();
        } else {
            AttachmentSettings.Content attachmentSettingsContent = Config.instance.gson.fromJson(settingsStr, AttachmentSettings.Content.class);
            if (attachmentSettingsContent == null) {
                return getDefaultAttachmentSettings();
            } else {
                return attachmentSettingsContent;
            }
        }
    }

    private AttachmentSettings.Content getDefaultAttachmentSettings() {
        return new AttachmentSettings.Content(30, new String[] {"jpeg", "jpg", "png", "pdf", "doc", "docx", "rtf"});
    }
}
