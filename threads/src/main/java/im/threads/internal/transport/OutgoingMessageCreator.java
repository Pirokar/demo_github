package im.threads.internal.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;

import im.threads.internal.formatters.ChatItemType;
import im.threads.internal.model.ConsultInfo;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.Quote;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.utils.AppInfoHelper;
import im.threads.internal.utils.DeviceInfoHelper;
import im.threads.internal.utils.PrefUtils;

public final class OutgoingMessageCreator {

    private OutgoingMessageCreator() {
    }

    public static JsonObject createInitChatMessage(String clientId, String data) {
        JsonObject object = new JsonObject();
        object.addProperty(MessageAttributes.CLIENT_ID, clientId);
        object.addProperty(MessageAttributes.TYPE, ChatItemType.INIT_CHAT.name());
        object.addProperty(MessageAttributes.DATA, data);
        object.addProperty(MessageAttributes.APP_MARKER_KEY, PrefUtils.getAppMarker());
        return object;
    }

    public static JsonObject createEnvironmentMessage(String clientName, String clientId, boolean clientIdEncrypted, String data, Context ctx) {
        JsonObject object = new JsonObject();
        object.addProperty("name", clientName);
        object.addProperty(MessageAttributes.CLIENT_ID, clientId);
        object.addProperty(MessageAttributes.CLIENT_ID_ENCRYPTED, clientIdEncrypted);
        object.addProperty(MessageAttributes.DATA, data);
        object.addProperty("platform", "Android");
        object.addProperty("osVersion", DeviceInfoHelper.getOsVersion());
        object.addProperty("device", DeviceInfoHelper.getDeviceName());
        object.addProperty("ip", DeviceInfoHelper.getIpAddress());
        object.addProperty("appVersion", AppInfoHelper.getAppVersion());
        object.addProperty("appName", AppInfoHelper.getAppName());
        object.addProperty(MessageAttributes.APP_BUNDLE_KEY, AppInfoHelper.getAppId());
        object.addProperty(MessageAttributes.APP_MARKER_KEY, PrefUtils.getAppMarker());
        object.addProperty("libVersion", AppInfoHelper.getLibVersion());
        object.addProperty("clientLocale", DeviceInfoHelper.getLocale(ctx));
        object.addProperty(MessageAttributes.TYPE, ChatItemType.CLIENT_INFO.name());
        return object;
    }

    public static JsonObject createMessageTyping(String clientId, String input) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(MessageAttributes.CLIENT_ID, clientId);
        jsonObject.addProperty(MessageAttributes.TYPE, ChatItemType.TYPING.name());
        jsonObject.addProperty(MessageAttributes.TYPING_DRAFT, input);
        jsonObject.addProperty(MessageAttributes.APP_MARKER_KEY, PrefUtils.getAppMarker());
        return jsonObject;
    }

    public static JsonObject createRatingDoneMessage(Survey survey, String clientId, String appMarker) {
        JsonObject object = new JsonObject();
        object.addProperty(MessageAttributes.CLIENT_ID, clientId);
        object.addProperty(MessageAttributes.TYPE, ChatItemType.SURVEY_QUESTION_ANSWER.name());
        object.addProperty("sendingId", survey.getSendingId());
        object.addProperty("questionId", survey.getQuestions().get(0).getId());
        object.addProperty("rate", survey.getQuestions().get(0).getRate());
        object.addProperty("text", survey.getQuestions().get(0).getText());
        object.addProperty(MessageAttributes.APP_MARKER_KEY, appMarker);
        return object;
    }

    public static JsonObject createResolveThreadMessage(String clientId) {
        JsonObject object = new JsonObject();
        object.addProperty(MessageAttributes.CLIENT_ID, clientId);
        object.addProperty(MessageAttributes.TYPE, ChatItemType.CLOSE_THREAD.name());
        object.addProperty(MessageAttributes.APP_MARKER_KEY, PrefUtils.getAppMarker());
        return object;
    }

    public static JsonObject createReopenThreadMessage(String clientId) {
        JsonObject object = new JsonObject();
        object.addProperty(MessageAttributes.CLIENT_ID, clientId);
        object.addProperty(MessageAttributes.TYPE, ChatItemType.REOPEN_THREAD.name());
        object.addProperty(MessageAttributes.APP_MARKER_KEY, PrefUtils.getAppMarker());
        return object;
    }

    public static JsonObject createRatingReceivedMessage(long sendingId, String clientId) {
        JsonObject object = new JsonObject();
        object.addProperty(MessageAttributes.CLIENT_ID, clientId);
        object.addProperty(MessageAttributes.TYPE, ChatItemType.SURVEY_PASSED.name());
        object.addProperty(MessageAttributes.APP_MARKER_KEY, PrefUtils.getAppMarker());
        object.addProperty("sendingId", sendingId);
        return object;
    }

    public static JsonObject createMessageClientOffline(String clientId) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(MessageAttributes.CLIENT_ID, clientId);
        jsonObject.addProperty(MessageAttributes.TYPE, ChatItemType.CLIENT_OFFLINE.name());
        jsonObject.addProperty(MessageAttributes.APP_MARKER_KEY, PrefUtils.getAppMarker());
        return jsonObject;
    }

    public static JsonObject createUserPhraseMessage(@NonNull UserPhrase upcomingUserMessage,
                                                     @Nullable ConsultInfo consultInfo,
                                                     @Nullable String quoteMfmsFilePath,
                                                     @Nullable String mfmsFilePath,
                                                     @Nullable String clientId,
                                                     @Nullable Long threadId) {
        Quote quote = upcomingUserMessage.getQuote();
        FileDescription fileDescription = upcomingUserMessage.getFileDescription();
        JsonObject formattedMessage = new JsonObject();
        formattedMessage.addProperty(MessageAttributes.UUID, upcomingUserMessage.getUuid());
        formattedMessage.addProperty(MessageAttributes.CLIENT_ID, clientId);
        final String phrase = upcomingUserMessage.getPhrase();
        formattedMessage.addProperty(MessageAttributes.TEXT, phrase == null ? "" : phrase);
        if (threadId != null && threadId != -1) {
            formattedMessage.addProperty(MessageAttributes.THREAD_ID, String.valueOf(threadId));
        }
        formattedMessage.addProperty(MessageAttributes.APP_MARKER_KEY, PrefUtils.getAppMarker());
        if (quote != null) {
            JsonArray quotes = new JsonArray();
            formattedMessage.add(MessageAttributes.QUOTES, quotes);
            JsonObject quoteJson = new JsonObject();
            quotes.add(quoteJson);
            if (!TextUtils.isEmpty(quote.getText())) {
                quoteJson.addProperty(MessageAttributes.TEXT, quote.getText());
            }
            if (consultInfo != null) {
                quoteJson.add("operator", consultInfo.toJson());//TODO #THREADS-5270 What is it for?
            }
            if (quote.getFileDescription() != null && quoteMfmsFilePath != null) {
                quoteJson.add(MessageAttributes.ATTACHMENTS, attachmentsFromFileDescription(quote.getFileDescription(), quoteMfmsFilePath));
            }
            if (upcomingUserMessage.getQuote().getUuid() != null) {
                quoteJson.addProperty(MessageAttributes.UUID, upcomingUserMessage.getQuote().getUuid());
            }
        }
        if (fileDescription != null && mfmsFilePath != null) {
            formattedMessage.add(MessageAttributes.ATTACHMENTS, attachmentsFromFileDescription(fileDescription, mfmsFilePath));
        }
        return formattedMessage;
    }

    private static JsonArray attachmentsFromFileDescription(File file, boolean isSelfie) {
        JsonArray attachments = new JsonArray();
        JsonObject attachment = new JsonObject();
        attachments.add(attachment);
        JsonObject optional = new JsonObject();
        attachment.addProperty("isSelfie", isSelfie);
        attachment.add("optional", optional);
        String extension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".") + 1).toLowerCase();
        String type = null;
        if (extension.equals("jpg")) {
            type = "image/jpg";
        }
        if (extension.equals("png")) {
            type = "image/png";
        }
        if (extension.equals("pdf")) {
            type = "text/pdf";
        }
        optional.addProperty(MessageAttributes.TYPE, type);
        optional.addProperty("name", file.getName());
        optional.addProperty("size", file.length());
        optional.addProperty("lastModified", file.lastModified());
        return attachments;
    }

    private static JsonArray attachmentsFromMfmsPath(FileDescription fileDescription) {
        JsonArray attachments = new JsonArray();
        JsonObject attachment = new JsonObject();
        attachments.add(attachment);
        JsonObject optional = new JsonObject();
        attachment.addProperty("isSelfie", fileDescription.isSelfie());
        attachment.add("optional", optional);
        if (fileDescription.getIncomingName() != null) {
            String extension = fileDescription.getIncomingName().substring(fileDescription.getIncomingName().lastIndexOf(".") + 1).toLowerCase();
            String type = null;
            if (extension.equals("jpg")) {
                type = "image/jpg";
            }
            if (extension.equals("png")) {
                type = "image/png";
            }
            if (extension.equals("pdf")) {
                type = "text/pdf";
            }
            optional.addProperty(MessageAttributes.TYPE, type);
        }
        optional.addProperty("name", fileDescription.getIncomingName());
        optional.addProperty("size", fileDescription.getSize());
        optional.addProperty("lastModified", System.currentTimeMillis());
        return attachments;
    }

    private static JsonArray attachmentsFromFileDescription(FileDescription fileDescription, String mfmsFilepath) {
        JsonArray attachments = null;
        if (fileDescription.getFilePath() != null && new File(fileDescription.getFilePath()).exists()) {
            attachments = attachmentsFromFileDescription(new File(fileDescription.getFilePath()), fileDescription.isSelfie());
        } else if (fileDescription.getDownloadPath() != null) {
            attachments = attachmentsFromMfmsPath(fileDescription);
        }
        if (attachments != null) {
            ((JsonObject) attachments.get(0)).addProperty("result", mfmsFilepath);
        }
        return attachments;
    }
}
