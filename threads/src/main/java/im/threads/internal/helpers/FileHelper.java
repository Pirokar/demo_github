package im.threads.internal.helpers;

import android.content.Context;

import java.io.File;

import im.threads.internal.Config;
import im.threads.internal.chat_updates.ChatUpdateProcessor;
import im.threads.internal.domain.logger.LoggerEdna;
import im.threads.internal.transport.models.AttachmentSettings;
import im.threads.internal.utils.PrefUtils;

public enum FileHelper {

    INSTANCE;

    private static final double MEGABYTE = 1024 * 1024;

    FileHelper() {
        ChatUpdateProcessor.getInstance().getAttachmentSettingsProcessor()
                .subscribe(attachmentSettings -> saveAttachmentSettings(attachmentSettings.getContent()),
                        LoggerEdna::e
                );
    }

    public static File createImageFile(Context context) {
        String filename = "thr" + System.currentTimeMillis() + ".jpg";
        File output = new File(context.getFilesDir(), filename);
        LoggerEdna.d("File genereated into filesDir : " + output.getAbsolutePath());
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
        PrefUtils.setAttachmentSettings(Config.instance.gson.toJson(attachmentSettingsContent));
    }

    private AttachmentSettings.Content getAttachmentSettings() {
        String settingsStr = PrefUtils.getAttachmentSettings();
        if (settingsStr.isEmpty()) {
            return getDefaultAttachmentSettings();
        } else {
            AttachmentSettings.Content attachmentSettingsContent
                    = Config.instance.gson.fromJson(settingsStr, AttachmentSettings.Content.class);
            if (attachmentSettingsContent == null) {
                return getDefaultAttachmentSettings();
            } else {
                return attachmentSettingsContent;
            }
        }
    }

    private AttachmentSettings.Content getDefaultAttachmentSettings() {
        return new AttachmentSettings.Content(30, new String[]{"jpeg", "jpg", "png", "pdf", "doc", "docx", "rtf"});
    }
}
