package im.threads.formatters;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import im.threads.R;
import im.threads.model.ConsultInfo;
import im.threads.model.FileDescription;
import im.threads.model.Quote;
import im.threads.model.UserPhrase;
import im.threads.utils.AppInfoHelper;
import im.threads.utils.DeviceInfoHelper;

public class OutgoingMessageCreator {
    private static final String TAG = "MessageFormatter ";

    private static final String ERROR_FORMATTING_JSON = "error formatting json";

    private static String userAgent = "";

    private OutgoingMessageCreator() {
    }

    public static String createUserPhraseMessage(
            UserPhrase upcomingUserMessage
            , ConsultInfo consultInfo
            , String quoteMfmsFilePath
            , String mfmsFilePath
            , String clientId
            , Long threadId) {
        try {
            Quote quote = upcomingUserMessage.getQuote();
            FileDescription fileDescription = upcomingUserMessage.getFileDescription();
            JSONObject formattedMessage = new JSONObject();
            formattedMessage.put(PushMessageAttributes.CLIENT_ID, clientId);
            formattedMessage.put(PushMessageAttributes.TEXT, upcomingUserMessage.getPhrase());

            if (threadId != null && threadId != -1) {
                formattedMessage.put(PushMessageAttributes.THREAD_ID, String.valueOf(threadId));
            }

            if (quote != null) {
                JSONArray quotes = new JSONArray();

                formattedMessage.put(PushMessageAttributes.QUOTES, quotes);
                JSONObject quoteJson = new JSONObject();
                quotes.put(quoteJson);
                if (!TextUtils.isEmpty(quote.getText())) {
                    quoteJson.put(PushMessageAttributes.TEXT, quote.getText());
                }
                if (null != consultInfo) quoteJson.put("operator", consultInfo.toJson());
                if (quote.getFileDescription() != null && quoteMfmsFilePath != null) {
                    quoteJson.put(PushMessageAttributes.ATTACHMENTS, attachmentsFromFileDescription(quote.getFileDescription(), quoteMfmsFilePath));
                }
                if (upcomingUserMessage.getQuote().getMessageId() != null) {
                    quoteJson.put(PushMessageAttributes.PROVIDER_ID, upcomingUserMessage.getQuote().getMessageId());
                }
                if (upcomingUserMessage.getQuote().getBackendId() != null) {
                    quoteJson.put(PushMessageAttributes.BACKEND_ID, upcomingUserMessage.getQuote().getBackendId());
                }
            }
            if (fileDescription != null && mfmsFilePath != null) {
                formattedMessage.put(PushMessageAttributes.ATTACHMENTS, attachmentsFromFileDescription(fileDescription, mfmsFilePath));
            }
            return formattedMessage.toString();
        } catch (JSONException e) {
            Log.e(TAG, ERROR_FORMATTING_JSON);
            e.printStackTrace();

        }
        return "";
    }

    private static JSONArray attachmentsFromFileDescription(File file) throws JSONException {
        JSONArray attachments = new JSONArray();
        JSONObject attachment = new JSONObject();
        attachments.put(attachment);
        JSONObject optional = new JSONObject();
        attachment.put("optional", optional);
        String extension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".") + 1).toLowerCase();
        String type = null;
        if (extension.equals("jpg")) type = "image/jpg";
        if (extension.equals("png")) type = "image/png";
        if (extension.equals("pdf")) type = "text/pdf";
        optional.put(PushMessageAttributes.TYPE, type);
        optional.put("name", file.getName());
        optional.put("size", file.length());
        optional.put("lastModified", file.lastModified());
        return attachments;
    }

    private static JSONArray attachmentsFromMfmsPath(FileDescription fileDescription) throws JSONException {
        JSONArray attachments = new JSONArray();
        JSONObject attachment = new JSONObject();
        attachments.put(attachment);
        JSONObject optional = new JSONObject();
        attachment.put("optional", optional);
        if (fileDescription.getIncomingName() != null) {
            String extension = fileDescription.getIncomingName().substring(fileDescription.getIncomingName().lastIndexOf(".") + 1).toLowerCase();
            String type = null;
            if (extension.equals("jpg")) type = "image/jpg";
            if (extension.equals("png")) type = "image/png";
            if (extension.equals("pdf")) type = "text/pdf";
            optional.put(PushMessageAttributes.TYPE, type);
        }
        optional.put("name", fileDescription.getIncomingName());
        optional.put("size", fileDescription.getSize());
        optional.put("lastModified", System.currentTimeMillis());
        return attachments;
    }

    private static JSONArray attachmentsFromFileDescription(FileDescription fileDescription, String mfmsFilepath) throws JSONException {
        JSONArray attachments = null;
        if (fileDescription.getFilePath() != null && new File(fileDescription.getFilePath().replaceAll("file://", "")).exists()) {
            attachments = attachmentsFromFileDescription(new File(fileDescription.getFilePath().replaceAll("file://", "")));
        } else if (fileDescription.getDownloadPath() != null) {
            attachments = attachmentsFromMfmsPath(fileDescription);
        }

        if (attachments != null) {
            attachments.getJSONObject(0).put("result", mfmsFilepath);
        }
        return attachments;
    }

    public static String createEnvironmentMessage(String clientName, String clientId, String data, Context ctx) {
        JSONObject object = new JSONObject();
        try {
            object.put("name", clientName);
            object.put(PushMessageAttributes.CLIENT_ID, clientId);
            object.put("data", data);
            object.put("platform", "Android");
            object.put("osVersion", DeviceInfoHelper.getOsVersion());
            object.put("device", DeviceInfoHelper.getDeviceName());
            object.put("ip", DeviceInfoHelper.getIpAddress());
            object.put("appVersion", AppInfoHelper.getAppVersion(ctx));
            object.put("appName", AppInfoHelper.getAppName(ctx));
            object.put("appBundle", AppInfoHelper.getAppId(ctx));
            object.put("libVersion", AppInfoHelper.getLibVersion());
            object.put("clientLocale", DeviceInfoHelper.getLocale(ctx));
            object.put(PushMessageAttributes.TYPE, PushMessageTypes.CLIENT_INFO.name());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object.toString().replaceAll("\\\\", "");
    }

    public static String createRatingDoneMessage(long sendingId, long questionId, int rate, String clientId) {
        JSONObject object = new JSONObject();
        try {
            object.put(PushMessageAttributes.CLIENT_ID, clientId);
            object.put(PushMessageAttributes.TYPE, PushMessageTypes.SURVEY_QUESTION_ANSWER.name());
            object.put("sendingId", sendingId);
            object.put("questionId", questionId);
            object.put("rate", rate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString().replaceAll("\\\\", "");
    }

    public static String createRatingRecievedMessage(long sendingId, String clientId) {
        JSONObject object = new JSONObject();
        try {
            object.put(PushMessageAttributes.CLIENT_ID, clientId);
            object.put(PushMessageAttributes.TYPE, PushMessageTypes.SURVEY_PASSED.name());
            object.put("sendingId", sendingId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString().replaceAll("\\\\", "");
    }

    public static String createResolveThreadMessage(String clientId) {
        JSONObject object = new JSONObject();
        try {
            object.put(PushMessageAttributes.CLIENT_ID, clientId);
            object.put(PushMessageAttributes.TYPE, PushMessageTypes.CLOSE_THREAD.name());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object.toString().replaceAll("\\\\", "");
    }

    public static String createReopenThreadMessage(String clientId) {
        JSONObject object = new JSONObject();
        try {
            object.put(PushMessageAttributes.CLIENT_ID, clientId);
            object.put(PushMessageAttributes.TYPE, PushMessageTypes.REOPEN_THREAD.name());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object.toString().replaceAll("\\\\", "");
    }

    public static String createMessageTyping(String clientId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PushMessageAttributes.CLIENT_ID, clientId);
            jsonObject.put(PushMessageAttributes.TYPE, PushMessageTypes.TYPING.name());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString().replace("\\\\", "");
    }

    public static String createMessageClientOffline(String clientId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PushMessageAttributes.CLIENT_ID, clientId);
            jsonObject.put(PushMessageAttributes.TYPE, PushMessageTypes.CLIENT_OFFLINE.name());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString().replace("\\\\", "");
    }

    public static String getUserAgent(Context ctx) {
        if (TextUtils.isEmpty(userAgent)) {
            userAgent = String.format(ctx.getResources().getString(R.string.threads_user_agent),
                    DeviceInfoHelper.getOsVersion(),
                    DeviceInfoHelper.getDeviceName(),
                    DeviceInfoHelper.getIpAddress(),
                    AppInfoHelper.getAppVersion(ctx),
                    AppInfoHelper.getAppId(ctx),
                    AppInfoHelper.getLibVersion());
        }
        return userAgent;
    }

    public static String createInitChatMessage(String clientId) {
        JSONObject object = new JSONObject();
        try {
            object.put(PushMessageAttributes.CLIENT_ID, clientId);
            object.put(PushMessageAttributes.TYPE, PushMessageTypes.INIT_CHAT.name());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString().replaceAll("\\\\", "");
    }
}
