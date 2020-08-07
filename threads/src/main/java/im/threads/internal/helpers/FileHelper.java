package im.threads.internal.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;

import im.threads.internal.Config;
import im.threads.internal.chat_updates.ChatUpdateProcessor;
import im.threads.internal.transport.models.AttachmentSettings;
import im.threads.internal.utils.ThreadsLogger;

public enum FileHelper {

    INSTANCE;

    private static String TAG = FileHelper.class.getSimpleName();

    private static final String IMAGE_FILE_PREFIX = "thr";
    private static final String IMAGE_FILE_EXTENSION = ".jpg";
    private static final long MEGABYTE = 1024 * 1024;
    private static final String PREF_ATTACHMENT_SETTINGS = "PREF_ATTACHMENT_SETTINGS";

    FileHelper(){
        ChatUpdateProcessor.getInstance().getAttachmentSettingsProcessor()
                .subscribe(attachmentSettings -> saveAttachmentSettings(attachmentSettings.getContent()));
    }

    public boolean canAttachFile(File file) {
        AttachmentSettings.Content attachmentSettings = getAttachmentSettings();
        if (file.length() / MEGABYTE > attachmentSettings.getMaxSize()) {
            return false;
        }
        String path = file.getAbsolutePath();
        if (path.contains(".")) {
            String extension = path.substring(path.lastIndexOf(".") + 1);
            for (String allowedExt : attachmentSettings.getFileExtensions()) {
                if (allowedExt.equalsIgnoreCase(extension)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void saveAttachmentSettings(AttachmentSettings.Content attachmentSettingsContent) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(Config.instance.context).edit();
        editor.putString(PREF_ATTACHMENT_SETTINGS, Config.instance.gson.toJson(attachmentSettingsContent));
        editor.commit();
    }

    private AttachmentSettings.Content getAttachmentSettings() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Config.instance.context);
        String settingsStr = sharedPreferences.getString(PREF_ATTACHMENT_SETTINGS, null);
        AttachmentSettings.Content content;
        if (settingsStr == null) {
            return getDefaultAttachmentSettings();
        } else {
            AttachmentSettings attachmentSettings = Config.instance.gson.fromJson(settingsStr, AttachmentSettings.class);
            if (attachmentSettings == null) {
                return getDefaultAttachmentSettings();
            } else {
                return attachmentSettings.getContent();
            }
        }
    }

    private AttachmentSettings.Content getDefaultAttachmentSettings() {
        return new AttachmentSettings.Content(30, new String[] {"jpeg", "jpg", "png", "pdf", "doc", "docx", "rtf"});
    }

    public File createImageFile(Context context) {
        String filename = IMAGE_FILE_PREFIX + System.currentTimeMillis() + IMAGE_FILE_EXTENSION;
        File output = null;
        try {
            output = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), filename);
            ThreadsLogger.d(TAG, "File genereated into ExternalStoragePublicDirectory, DCIM : " + output.getAbsolutePath());
        } catch (Exception e) {
            ThreadsLogger.w(TAG, "Could not create file in public storage");
            ThreadsLogger.d(TAG, "", e);
        }
        if (output == null) {
            output = new File(context.getFilesDir(), filename);
            ThreadsLogger.d(TAG, "File genereated into filesDir : " + output.getAbsolutePath());
        }
        return output;
    }

    public boolean isThreadsImage(File file) {
        return file.getName().startsWith(IMAGE_FILE_PREFIX) && file.getName().endsWith(IMAGE_FILE_EXTENSION);
    }
}
