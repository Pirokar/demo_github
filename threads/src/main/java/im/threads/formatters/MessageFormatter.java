package im.threads.formatters;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.pushserver.android.model.PushMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import im.threads.BuildConfig;
import im.threads.R;
import im.threads.model.Attachment;
import im.threads.model.ChatItem;
import im.threads.model.ConsultConnectionMessage;
import im.threads.model.ConsultInfo;
import im.threads.model.ConsultPhrase;
import im.threads.model.EmptyChatItem;
import im.threads.model.FileDescription;
import im.threads.model.MessageState;
import im.threads.model.MessgeFromHistory;
import im.threads.model.Operator;
import im.threads.model.QuestionDTO;
import im.threads.model.Quote;
import im.threads.model.RequestResolveThread;
import im.threads.model.ScheduleInfo;
import im.threads.model.Survey;
import im.threads.model.UpcomingUserMessage;
import im.threads.model.UserPhrase;
import im.threads.utils.MessageMatcher;

/**
 * Created by yuri on 26.07.2016.
 * {
 * "result":"http://pushservertest.mfms.ru/push-test/file/download/eaydpjhiw2t124vxg4c2rb00p4yar7kn2nq7",
 * "optional":{
 * "type":"image/png",
 * "name":"jeb.png",
 * "size":23610,
 * "lastModified":1467358448772
 * },
 * "progress":null
 * }
 */
public class MessageFormatter {
    private static final String TAG = "MessageFormatter ";

    private static final String CLIENT_ID = "clientId";
    private static final String TYPE = "type";
    private static final String TEXT = "text";
    private static final String HIDE_AFTER = "hideAfter";
    private static final String QUOTES = "quotes";
    private static final String ATTACHMENTS = "attachments";
    private static final String ERROR_FORMATTING_JSON = "error formatting json";
    private static final String PROVIDER_ID = "providerId";
    private static final String BACKEND_ID = "backendId";

    // message types
    private static final String TYPE_CLIENT_INFO = "CLIENT_INFO";
    private static final String TYPE_SURVEY_QUESTION_ANSWER = "SURVEY_QUESTION_ANSWER";
    private static final String TYPE_SURVEY_PASSED = "SURVEY_PASSED";
    private static final String TYPE_CLOSE_THREAD = "CLOSE_THREAD";
    private static final String TYPE_REOPEN_THREAD = "REOPEN_THREAD";
    private static final String TYPE_OPERATOR_JOINED = "OPERATOR_JOINED";
    private static final String TYPE_OPERATOR_LEFT = "OPERATOR_LEFT";
    private static final String TYPE_CLIENT_OFFLINE = "CLIENT_OFFLINE";
    private static final String TYPE_TYPING = "TYPING";

    private MessageFormatter() {
    }

    public static String format(
            UpcomingUserMessage upcomingUserMessage
            , String quoteMfmsFilePath
            , String mfmsFilePath
            , String clientId) {
        if (upcomingUserMessage == null) return "";
        try {
            Quote quote = upcomingUserMessage.getQuote();
            FileDescription fileDescription = upcomingUserMessage.getFileDescription();
            JSONObject formattedMessage = new JSONObject();
            formattedMessage.put(CLIENT_ID, clientId);
            formattedMessage.put(TEXT, upcomingUserMessage.getText());
            if (quote != null) {
                JSONArray quotes = new JSONArray();
                formattedMessage.put(QUOTES, quotes);
                JSONObject quoteJson = new JSONObject();
                quotes.put(quoteJson);
                if (!TextUtils.isEmpty(quote.getText())) {
                    quoteJson.put(TEXT, quote.getText());

                }
                if (quote.getFileDescription() != null && quoteMfmsFilePath != null) {
                    quoteJson.put(ATTACHMENTS, attachmentsFromFileDescription(quote.getFileDescription(), quoteMfmsFilePath));
                }
            }
            if (fileDescription != null && mfmsFilePath != null) {
                formattedMessage.put(ATTACHMENTS, attachmentsFromFileDescription(fileDescription, mfmsFilePath));
            }
            //   return formattedMessage.toString().replaceAll("\\\\", "");
            return formattedMessage.toString();
        } catch (JSONException e) {
            Log.e(TAG, ERROR_FORMATTING_JSON);
            e.printStackTrace();

        }
        return "";
    }

    public static String format(
            UserPhrase upcomingUserMessage
            , ConsultInfo consultInfo
            , String quoteMfmsFilePath
            , String mfmsFilePath
            , String clientId) {
        try {
            Quote quote = upcomingUserMessage.getQuote();
            FileDescription fileDescription = upcomingUserMessage.getFileDescription();
            JSONObject formattedMessage = new JSONObject();
            formattedMessage.put(CLIENT_ID, clientId);
            formattedMessage.put(TEXT, upcomingUserMessage.getPhrase());

            if (quote != null) {
                JSONArray quotes = new JSONArray();

                formattedMessage.put(QUOTES, quotes);
                JSONObject quoteJson = new JSONObject();
                quotes.put(quoteJson);
                if (!TextUtils.isEmpty(quote.getText())) {
                    quoteJson.put(TEXT, quote.getText());
                }
                if (null != consultInfo) quoteJson.put("operator", consultInfo.toJson());
                if (quote.getFileDescription() != null && quoteMfmsFilePath != null) {
                    quoteJson.put(ATTACHMENTS, attachmentsFromFileDescription(quote.getFileDescription(), quoteMfmsFilePath));
                }
                if (upcomingUserMessage.getQuote().getMessageId() != null) {
                    quoteJson.put(PROVIDER_ID, upcomingUserMessage.getQuote().getMessageId());
                }
                if (upcomingUserMessage.getQuote().getBackendId() != null) {
                    quoteJson.put(BACKEND_ID, upcomingUserMessage.getQuote().getBackendId());
                }
            }
            if (fileDescription != null && mfmsFilePath != null) {
                formattedMessage.put(ATTACHMENTS, attachmentsFromFileDescription(fileDescription, mfmsFilePath));
            }
            return formattedMessage.toString();
        } catch (JSONException e) {
            Log.e(TAG, ERROR_FORMATTING_JSON);
            e.printStackTrace();

        }
        return "";
    }

    private static JSONObject getFullMessage(PushMessage pushMessage) {
        JSONObject fullMessage;

        try {
            fullMessage = new JSONObject(pushMessage.fullMessage);
        } catch (JSONException e) {
            e.printStackTrace();
            fullMessage = null;
        }

        return fullMessage;
    }

    private static String getType(JSONObject fullMessage) {
        String type;

        try {
            if (fullMessage.has(TYPE)) {
                type = fullMessage.getString(TYPE);
            } else {
                type = null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            type = null;
        }

        return type;
    }

    private static boolean isChatPush(JSONObject fullMessage) {
        String type;

        try {
            if (fullMessage.has("origin")) {
                type = fullMessage.getString("origin");
            } else {
                type = null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            type = null;
        }

        return type != null && type.equalsIgnoreCase("threads");
    }

//    private static Boolean getTypeOfSurvey(JSONObject fullMessage) {
//        Boolean simple;
//
//        try {
//            if (fullMessage.has("simple")) {
//                simple = fullMessage.getBoolean("simple");
//            } else {
//                simple = true;
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//            simple = true;
//        }
//
//        return simple;
//    }

    public static Long getHideAfter(JSONObject fullMessage) {
        try {
            return Long.parseLong(fullMessage.getString(HIDE_AFTER));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    private static String getMessage(JSONObject fullMessage, PushMessage pushMessage) {
        String message = null;
        try {
            message = fullMessage.getString(TEXT) == null
                    ? pushMessage.shortMessage : fullMessage.getString(TEXT);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return message;
    }

    private static ConsultPhrase getConsultPhraseFromPush(PushMessage pushMessage,
                                                          JSONObject fullMessage,
                                                          String message) {
        try {
            String messageId = pushMessage.messageId;
            String backendId;
            try {
                backendId = String.valueOf(fullMessage.getInt(BACKEND_ID));
            } catch (Exception e) {
                backendId = "0";
            }
            long timeStamp = pushMessage.sentAt;
            JSONObject operatorInfo = fullMessage.getJSONObject("operator");
            final String name = operatorInfo.getString("name");
            String photoUrl = operatorInfo.isNull("photoUrl") ? null : operatorInfo.getString("photoUrl");
            String status = operatorInfo.has("status") && !operatorInfo.isNull("status") ? operatorInfo.getString("status") : null;
            JSONArray attachmentsArray = fullMessage.has(ATTACHMENTS) ? fullMessage.getJSONArray(ATTACHMENTS) : null;
            FileDescription fileDescription = null;
            if (null != attachmentsArray)
                fileDescription = fileDescriptionFromJson(fullMessage.getJSONArray(ATTACHMENTS));
            if (fileDescription != null) {
                fileDescription.setFrom(name);
                fileDescription.setTimeStamp(timeStamp);
            }
            Quote quote = null;
            if (fullMessage.has(QUOTES))
                quote = quoteFromJson(fullMessage.getJSONArray(QUOTES));
            if (quote != null && quote.getFileDescription() != null) {
                quote.getFileDescription().setTimeStamp(timeStamp);
            }
            boolean gender = operatorInfo.isNull("gender") ? false : operatorInfo.getString("gender").equalsIgnoreCase("male");

            return new ConsultPhrase(
                    fileDescription,
                    quote,
                    name,
                    messageId,
                    message,
                    System.currentTimeMillis(),
                    String.valueOf(operatorInfo.getLong("id")),
                    photoUrl,
                    false,
                    status,
                    gender,
                    backendId
            );
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static UserPhrase getUserPhraseFromPush(PushMessage pushMessage,
                                                    JSONObject fullMessage,
                                                    String message) {
        try {
            String messageId = pushMessage.messageId;
            String backendId;
            try {
                backendId = String.valueOf(fullMessage.getInt(BACKEND_ID));
            } catch (Exception e) {
                backendId = "0";
            }
            long phraseTimeStamp = pushMessage.sentAt;
            JSONArray attachmentsArray = fullMessage.has(ATTACHMENTS) ? fullMessage.getJSONArray(ATTACHMENTS) : null;
            FileDescription fileDescription = null;
            if (null != attachmentsArray)
                fileDescription = fileDescriptionFromJson(fullMessage.getJSONArray(ATTACHMENTS));
            if (fileDescription != null) {
                fileDescription.setTimeStamp(phraseTimeStamp);
            }
            Quote mQuote = null;
            if (fullMessage.has(QUOTES))
                mQuote = quoteFromJson(fullMessage.getJSONArray(QUOTES));
            if (mQuote != null && mQuote.getFileDescription() != null) {
                mQuote.getFileDescription().setTimeStamp(phraseTimeStamp);
            }

            UserPhrase userPhrase = new UserPhrase(
                    messageId,
                    message,
                    mQuote,
                    phraseTimeStamp,
                    fileDescription,
                    backendId
            );
            userPhrase.setSentState(MessageState.STATE_SENT);
            return userPhrase;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return null, если не удалось распознать формат сообщения.
     */
    public static ChatItem format(PushMessage pushMessage) {
        JSONObject fullMessage = getFullMessage(pushMessage);

        // В пуше для чата должен быть fullMessage, и он должен соответствовать формату JSON.
        if (fullMessage == null) {
            return null;
        }

        String type = getType(fullMessage);

        // В пуше либо должен быть type известных чату типов,
        if (type != null) {
            if (type.equalsIgnoreCase(MessageMatcher.OPERATOR_JOINED) || type.equalsIgnoreCase(MessageMatcher.OPERATOR_LEFT)) {
                return getConsultConnectionFromPush(pushMessage);
            } else if (type.equalsIgnoreCase(MessageMatcher.SCHEDULE)) {
                return getScheduleInfoFromPush(pushMessage, fullMessage);
            } else if (type.equalsIgnoreCase(MessageMatcher.SURVEY)) {
                return getRatingFromPush(pushMessage, fullMessage);
            } else if (type.equalsIgnoreCase(MessageMatcher.MESSAGE)
                    || type.equalsIgnoreCase(MessageMatcher.ON_HOLD)) {
                // Либо в fullMessage должны содержаться ключи из списка:
                // "attachments", "text", "quotes"
                return checkMessageIsFull(pushMessage, fullMessage);
            } else if (type.equalsIgnoreCase(MessageMatcher.REQUEST_CLOSE_THREAD)) {
                return getCloseRequestFromPush(fullMessage);
            } else if (type.equalsIgnoreCase(MessageMatcher.NONE)
                    || type.equalsIgnoreCase(MessageMatcher.MESSAGES_READ)
                    || type.equalsIgnoreCase(MessageMatcher.OPERATOR_LOOKUP_STARTED)
                    || type.equalsIgnoreCase(MessageMatcher.CLIENT_BLOCKED)
                    || type.equalsIgnoreCase(MessageMatcher.THREAD_OPENED)
                    || type.equalsIgnoreCase(MessageMatcher.THREAD_CLOSED)
                    || type.equalsIgnoreCase(MessageMatcher.SCENARIO)
                    || isChatPush(fullMessage)) {
                return new EmptyChatItem(type);
            } else {
                return checkMessageIsFull(pushMessage, fullMessage);
            }
        }
        return null;
    }

    @Nullable
    private static ChatItem checkMessageIsFull(PushMessage pushMessage, JSONObject fullMessage) {
        String message = getMessage(fullMessage, pushMessage);
        if (message != null || fullMessage.has(ATTACHMENTS) || fullMessage.has(QUOTES)) {
            ChatItem item = getConsultPhraseFromPush(pushMessage, fullMessage, message);
            return item != null ? item : getUserPhraseFromPush(pushMessage, fullMessage, message);
        } else {
            return null;
        }
    }

    private static ScheduleInfo getScheduleInfoFromPush(PushMessage pushMessage, JSONObject fullMessage) {
        ScheduleInfo scheduleInfo = null;
        String text = getMessage(fullMessage, pushMessage);
        if (text != null) {
            try {
                scheduleInfo = new Gson().fromJson(text, ScheduleInfo.class);
                scheduleInfo.setDate(new Date().getTime());
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
        return scheduleInfo;
    }

    private static Survey getRatingFromPush(PushMessage pushMessage, JSONObject fullMessage) {
        Survey survey = null;
        String text = getMessage(fullMessage, pushMessage);
        if (text != null) {
            try {
                survey = new Gson().fromJson(text, Survey.class);
                long time = new Date().getTime();
                survey.setPhraseTimeStamp(time);
                survey.setSentState(MessageState.STATE_NOT_SENT);
                for (QuestionDTO questionDTO : survey.getQuestions()) {
                    questionDTO.setPhraseTimeStamp(time);
                }
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
        return survey;
    }

    // show close thread's request only if thread has time till close (hideAfter)
    private static RequestResolveThread getCloseRequestFromPush(JSONObject fullMessage) {
        Long hideAfter = getHideAfter(fullMessage);
        return hideAfter > 0 ? new RequestResolveThread(hideAfter, System.currentTimeMillis()) : null;
    }

    private static ConsultConnectionMessage getConsultConnectionFromPush(PushMessage pushMessage) {
        ConsultConnectionMessage chatItem = null;

        try {
            JSONObject fullMessage = new JSONObject(pushMessage.fullMessage);
            String messageId = pushMessage.messageId;
            long timeStamp = pushMessage.sentAt;
            JSONObject operator = fullMessage.getJSONObject("operator");
            long operatorId = operator.getLong("id");
            String name = operator.isNull("name") ? null : operator.getString("name");
            String status = operator.isNull("status") ? null : operator.getString("status");
            String type = fullMessage.getString(TYPE).equalsIgnoreCase(TYPE_OPERATOR_JOINED) ? ConsultConnectionMessage.TYPE_JOINED : ConsultConnectionMessage.TYPE_LEFT;
            boolean gender = operator.isNull("gender") ? false : operator.getString("gender").equalsIgnoreCase("male");
            String photourl = operator.isNull("photoUrl") ? null : operator.getString("photoUrl");
            String title = pushMessage.shortMessage == null ? null : pushMessage.shortMessage.split(" ")[0];
            boolean displayMessage = !fullMessage.has("display") || fullMessage.getBoolean("display");
            chatItem = new ConsultConnectionMessage(
                    String.valueOf(operatorId)
                    , type
                    , name
                    , gender
                    , System.currentTimeMillis()
                    , photourl
                    , status
                    , title
                    , pushMessage.messageId
                    , displayMessage);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return chatItem;
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
        optional.put(TYPE, type);
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
            optional.put(TYPE, type);
        }
        optional.put("name", fileDescription.getIncomingName());
        optional.put("size", fileDescription.getSize());
        optional.put("lastModified", System.currentTimeMillis());
        return attachments;
    }

    public static JSONArray attachmentsFromFileDescription(FileDescription fileDescription, String mfmsFilepath) throws JSONException {
        JSONArray attachments = null;
        if (fileDescription.getFilePath() != null && new File(fileDescription.getFilePath().replaceAll("file://", "")).exists()) {
            attachments = attachmentsFromFileDescription(new File(fileDescription.getFilePath().replaceAll("file://", "")));
        } else if (fileDescription.getDownloadPath() != null) {
            attachments = attachmentsFromMfmsPath(fileDescription);
        }
        attachments.getJSONObject(0).put("result", mfmsFilepath);
        return attachments;
    }

    public static boolean hasFile(UpcomingUserMessage message) {
        return (message.getFileDescription() != null || (message.getQuote() != null && message.getQuote().getFileDescription() != null));
    }

    public static boolean hasFile(UserPhrase message) {
        return (message.getFileDescription() != null || (message.getQuote() != null && message.getQuote().getFileDescription() != null));
    }

    public static Quote quoteFromJson(JSONArray quotes) throws JSONException {
        Quote quote = null;
        FileDescription quoteFileDescription = null;
        String quoteString = null;
        String consultName = "";
        if (quotes.length() > 0 && quotes.getJSONObject(0) != null && (quotes.getJSONObject(0).has(TEXT))) {
            quoteString = quotes.getJSONObject(0).getString(TEXT);
        }
        if (quotes.length() > 0
                && quotes.getJSONObject(0) != null
                && (quotes.getJSONObject(0).has(ATTACHMENTS))
                && (quotes.getJSONObject(0).getJSONArray(ATTACHMENTS).length() > 0
                && (quotes.getJSONObject(0).getJSONArray(ATTACHMENTS).getJSONObject(0).has("result")))) {
            String header = null;
            if (quotes.getJSONObject(0).getJSONArray(ATTACHMENTS).getJSONObject(0).has("optional")) {
                header = quotes.getJSONObject(0).getJSONArray(ATTACHMENTS).getJSONObject(0).getJSONObject("optional").getString("name");
            }
            quoteFileDescription = fileDescriptionFromJson(quotes.getJSONObject(0).getJSONArray(ATTACHMENTS));
        }
        if (quotes.length() > 0 && quotes.getJSONObject(0) != null && quotes.getJSONObject(0).has("operator")) {
            try {
                consultName = quotes.getJSONObject(0).getJSONObject("operator").getString("name");
            } catch (JSONException e) {
                Log.e(TAG, "" + quotes);
                e.printStackTrace();
            }
        }
        if (quoteString != null || quoteFileDescription != null) {
            quote = new Quote(consultName, quoteString, quoteFileDescription, System.currentTimeMillis());
        }
        if (quoteFileDescription != null) {
            quoteFileDescription.setFrom(consultName);
        }
        return quote;
    }

    public static Quote quoteFromList(List<MessgeFromHistory> quotes) {
        Quote quote = null;
        FileDescription quoteFileDescription = null;
        String quoteString = null;
        String consultName = "";
        if (quotes.size() > 0 && quotes.get(0) != null && (quotes.get(0).getText() != null)) {
            quoteString = quotes.get(0).getText();
        }
        if (quotes.size() > 0
                && quotes.get(0) != null
                && (quotes.get(0).getAttachments() != null)
                && (quotes.get(0).getAttachments().size() > 0
                && (quotes.get(0).getAttachments().get(0).getResult() != null))) {
            String header = null;
            if (quotes.get(0).getAttachments().get(0).getOptional() != null) {
                header = quotes.get(0).getAttachments().get(0).getOptional().getName();
            }
            quoteFileDescription = fileDescriptionFromList(quotes.get(0).getAttachments());
        }
        if (quotes.size() > 0 && quotes.get(0) != null && quotes.get(0).getOperator() != null) {
            consultName = quotes.get(0).getOperator().getName();
        }
        if (quoteString != null || quoteFileDescription != null) {
            quote = new Quote(consultName, quoteString, quoteFileDescription, System.currentTimeMillis());
        }
        if (quoteFileDescription != null) {
            quoteFileDescription.setFrom(consultName);
        }
        return quote;
    }

    public static FileDescription fileDescriptionFromJson(JSONArray jsonArray) throws JSONException {
        FileDescription fileDescription = null;
        if (jsonArray.length() > 0
                && jsonArray.getJSONObject(0) != null) {
            String header = null;
            if (jsonArray.getJSONObject(0).has("optional")) {
                header = jsonArray.getJSONObject(0).getJSONObject("optional").getString("name");
            }
            fileDescription = new FileDescription(
                    null
                    , null
                    , jsonArray.getJSONObject(0).getJSONObject("optional").getLong("size")
                    , 0);
            fileDescription.setDownloadPath(jsonArray.getJSONObject(0).getString("result"));
            fileDescription.setIncomingName(header);
        }
        return fileDescription;
    }

    public static FileDescription fileDescriptionFromList(List<Attachment> attachments) {
        FileDescription fileDescription = null;
        if (attachments.size() > 0 && attachments.get(0) != null) {
            String header = null;
            if (attachments.get(0).getOptional() != null) {
                header = attachments.get(0).getOptional().getName();
            }
            fileDescription = new FileDescription(
                    null
                    , null
                    , attachments.get(0).getOptional().getSize()
                    , 0);
            fileDescription.setDownloadPath(attachments.get(0).getResult());
            fileDescription.setIncomingName(header);
        }
        return fileDescription;
    }

    public static List<ChatItem> formatMessages(List<PushMessage> messages) {
        List<ChatItem> list = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            ChatItem chatItem = format(messages.get(i));
            if (chatItem != null) {
                list.add(chatItem);
            }
        }
        return list;
    }

    public static String createEnvironmentMessage(String clientName, String clientId, String data, Context ctx) {
        JSONObject object = new JSONObject();
        try {
            object.put("name", clientName);
            object.put(CLIENT_ID, clientId);
            object.put("data", data);
            object.put("platform", "Android");
            object.put("osVersion", getOsVersion());
            object.put("device", getDeviceName());
            object.put("appVersion", getAppVersion(ctx));
            object.put("libVersion", getLibVersion());
            object.put("clientLocale", getLocale(ctx));
            object.put(TYPE, TYPE_CLIENT_INFO);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object.toString().replaceAll("\\\\", "");
    }

//    public static String createRatingThumbsMessage(boolean rating, String messageId) {
//        JSONObject object = new JSONObject();
//        try {
//            if (rating) {
//                object.put("rating", "good");
//                object.put("message_id", messageId);
//            } else {
//                object.put("rating", "not_good");
//                object.put("message_id", messageId);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return object.toString().replaceAll("\\\\", "");
//    }
//
//    public static String createRatingStarsMessage(int rating, String messageId) {
//        JSONObject object = new JSONObject();
//        try {
//            object.put("rating", String.valueOf(rating));
//            object.put("message_id", messageId);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return object.toString().replaceAll("\\\\", "");
//    }

    public static String createRatingDoneMessage(long sendingId, long questionId, int rate, String clientId) {
        JSONObject object = new JSONObject();
        try {
            object.put(CLIENT_ID, clientId);
            object.put(TYPE, TYPE_SURVEY_QUESTION_ANSWER);
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
            object.put(CLIENT_ID, clientId);
            object.put(TYPE, TYPE_SURVEY_PASSED);
            object.put("sendingId", sendingId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString().replaceAll("\\\\", "");
    }

    public static String createResolveThreadMessage(String clientId) {
        JSONObject object = new JSONObject();
        try {
            object.put(CLIENT_ID, clientId);
            object.put(TYPE, TYPE_CLOSE_THREAD);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object.toString().replaceAll("\\\\", "");
    }

    public static String createReopenThreadMessage(String clientId) {
        JSONObject object = new JSONObject();
        try {
            object.put(CLIENT_ID, clientId);
            object.put(TYPE, TYPE_REOPEN_THREAD);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object.toString().replaceAll("\\\\", "");
    }

    private static String getAppName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        String appName;
        if (applicationInfo != null) {
            try {
                appName = applicationInfo.loadLabel(context.getPackageManager()).toString();
            } catch (Exception e) {
                e.printStackTrace();
                appName = "Unknown";
            }
        } else {
            appName = "Unknown";
        }
        return appName;
    }

    private static String getAppBundle() {
        return BuildConfig.APPLICATION_ID;
    }

    private static String getOsVersion() {
        return String.valueOf(Build.VERSION.SDK_INT);
    }

    private static String getDeviceName() {
        return Build.MANUFACTURER + " " + Build.MODEL;
    }

    private static String getAppVersion(Context ctx) {

        PackageInfo pInfo = null;
        try {
            pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pInfo != null ? pInfo.versionName + " (" + pInfo.versionCode + ")" : "";
    }

    private static String getLibVersion() {
        return BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")";
    }

    private static String getLocale(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return ctx.getResources().getConfiguration().getLocales().get(0).toLanguageTag();
        } else {
            //noinspection deprecation
            try {
                return ctx.getResources().getConfiguration().locale.toLanguageTag();
            } catch (NoSuchMethodError e) {
                return ctx.getResources().getConfiguration().locale.getLanguage()
                        + "-" + ctx.getResources().getConfiguration().locale.getCountry();
            }
        }
    }

    /*public static ArrayList<ChatItem> format(List<InOutMessage> messages) {
        ArrayList<ChatItem> out = new ArrayList<>();
        try {
            for (InOutMessage message : messages) {
                if (message == null || message.content == null || message.content.length() == 0)
                    continue;
                try {
                    JSONObject body = new JSONObject(message.content);
                    String messageId = String.valueOf(message.messageId);
//                    String messageId = body.getString("providerId");
                    String backendId = String.valueOf(body.getLong(BACKEND_ID));
                    long timeStamp = message.sentAt.millis;
                    JSONObject operatorInfo = body.has("operator") ? body.getJSONObject("operator") : null;
                    String name = null;
                    if (operatorInfo != null && operatorInfo.has("name") && !operatorInfo.isNull("name")) {
                        name = operatorInfo.getString("name");
                    }
                    String photoUrl = null;
                    if (operatorInfo != null && operatorInfo.has("photoUrl") && !operatorInfo.isNull("photoUrl")) {
                        photoUrl = operatorInfo.getString("photoUrl");
                    }
                    String operatorId = null;
                    if (operatorInfo != null && operatorInfo.has("id") && !operatorInfo.isNull("id")) {
                        operatorId = operatorInfo.getString("id");
                    }
                    String status = null;
                    if (operatorInfo != null && operatorInfo.has("status") && !operatorInfo.isNull("status")) {
                        status = operatorInfo.getString("status");
                    }
                    boolean gender = false;
                    if (operatorInfo != null && operatorInfo.has("gender") && !operatorInfo.isNull("gender")) {
                        gender = operatorInfo.getString("gender").equalsIgnoreCase("male");
                    }
                    boolean displayMessage = !body.has("display") || body.getBoolean("display");
                    if (body.has(TYPE) && (TYPE_OPERATOR_JOINED.equalsIgnoreCase(body.getString(TYPE)) || TYPE_OPERATOR_LEFT.equalsIgnoreCase(body.getString(TYPE)))) {
                        String type = body.getString(TYPE).equalsIgnoreCase(ConsultConnectionMessage.TYPE_JOINED) ? ConsultConnectionMessage.TYPE_JOINED : ConsultConnectionMessage.TYPE_LEFT;
                        out.add(new ConsultConnectionMessage(operatorId, type, name, gender, timeStamp, photoUrl, status, null, messageId, displayMessage));
                    } else {
                        String phraseText = body.has(TEXT) ? body.getString(TEXT) : null;
                        FileDescription fileDescription = body.has(ATTACHMENTS) ? fileDescriptionFromJson(body.getJSONArray(ATTACHMENTS)) : null;
                        if (fileDescription != null) {
                            fileDescription.setFrom(name);
                            fileDescription.setTimeStamp(timeStamp);
                        }
                        Quote quote = body.has(QUOTES) ? quoteFromJson(body.getJSONArray(QUOTES)) : null;
                        if (quote != null && quote.getFileDescription() != null)
                            quote.getFileDescription().setTimeStamp(timeStamp);
                        if (!message.incoming) {
                            out.add(new ConsultPhrase(fileDescription
                                    , quote
                                    , name
                                    , messageId
                                    , phraseText
                                    , timeStamp
                                    , operatorId
                                    , photoUrl
                                    , true
                                    , status
                                    , gender
                                    , backendId
                            ));
                        } else {
                            if (fileDescription != null) {
                                if (Locale.getDefault().getLanguage().equalsIgnoreCase("ru")) {
                                    fileDescription.setFrom("Я");
                                } else {
                                    fileDescription.setFrom("I");
                                }
                            }
                            out.add(new UserPhrase(messageId, phraseText, quote, timeStamp, fileDescription, MessageState.STATE_WAS_READ, backendId));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (e.getMessage() != null && e.getMessage().contains("annot be converted to")) {
                        String cont = message.content;
                        String type = cont.contains("присоедини") ? ConsultConnectionMessage.TYPE_JOINED : ConsultConnectionMessage.TYPE_LEFT;
                        String title = null;
                        String name = null;
                        try {
                            name = cont.split(" ")[1];
                            title = cont.split(" ")[0];
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        ConsultConnectionMessage ccm = new ConsultConnectionMessage(name, type, name, cont.contains("лся") ? true : false, message.sentAt.millis, null, null, title, String.valueOf(message.messageId), true);
                        out.add(ccm);
                    }
                    Log.e(TAG, "error parsing message" + message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "error while formatting");
            Log.e(TAG, "" + messages);
            e.printStackTrace();
        }
        return out;
    }
*/
    public static ArrayList<ChatItem> formatNew(List<MessgeFromHistory> messages) {
        ArrayList<ChatItem> out = new ArrayList<>();
        try {
            for (MessgeFromHistory message : messages) {
                if (message == null)
                    continue;
                String messageId = message.getProviderId();
                String backendId = String.valueOf(message.getId());
                long timeStamp = message.getTimeStamp();
                Operator operator = message.getOperator();
                String name = null;
                if (operator != null && operator.getName() != null && !operator.getName().isEmpty()) {
                    name = operator.getName();
                }
                String photoUrl = null;
                if (operator != null && operator.getPhotoUrl() != null && !operator.getPhotoUrl().isEmpty()) {
                    photoUrl = operator.getPhotoUrl();
                }
                String operatorId = null;
                if (operator != null && operator.getId() != null) {
                    operatorId = String.valueOf(operator.getId());
                }
//                    String status = null;
//                    if (operator != null && operatorInfo.has("status") && !operatorInfo.isNull("status")) {
//                        status = operatorInfo.getString("status");
//                    }
//                    boolean gender = false;
//                    if (operatorInfo != null && operatorInfo.has("gender") && !operatorInfo.isNull("gender")) {
//                        gender = operatorInfo.getString("gender").equalsIgnoreCase("male");
//                    }

                if (message.getType() != null && !message.getType().isEmpty() && (message.getType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_JOINED) || message.getType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_LEFT))) {
                    String type = message.getType().equalsIgnoreCase(ConsultConnectionMessage.TYPE_JOINED) ? ConsultConnectionMessage.TYPE_JOINED : ConsultConnectionMessage.TYPE_LEFT;
                    out.add(new ConsultConnectionMessage(operatorId, type, name, false, timeStamp, photoUrl, null, null, messageId, false));
                } else {
                    String phraseText = message.getText();
                    FileDescription fileDescription = message.getAttachments() != null ? fileDescriptionFromList(message.getAttachments()) : null;
                    if (fileDescription != null) {
                        fileDescription.setFrom(name);
                        fileDescription.setTimeStamp(timeStamp);
                    }
                    Quote quote = message.getQuotes() != null ? quoteFromList(message.getQuotes()) : null;
                    if (quote != null && quote.getFileDescription() != null)
                        quote.getFileDescription().setTimeStamp(timeStamp);
                    if (message.getOperator() != null) {
                        out.add(new ConsultPhrase(fileDescription
                                , quote
                                , name
                                , messageId
                                , phraseText
                                , timeStamp
                                , operatorId
                                , photoUrl
                                , true
                                , null
                                , false
                                , backendId
                        ));
                    } else {
                        if (fileDescription != null) {
                            if (Locale.getDefault().getLanguage().equalsIgnoreCase("ru")) {
                                fileDescription.setFrom("Я");
                            } else {
                                fileDescription.setFrom("I");
                            }
                        }
                        out.add(new UserPhrase(messageId, phraseText, quote, timeStamp, fileDescription, MessageState.STATE_WAS_READ, backendId));
                    }
                }
            }
            Collections.sort(out, new Comparator<ChatItem>() {
                @Override
                public int compare(ChatItem ci1, ChatItem ci2) {
                    return Long.valueOf(ci1.getTimeStamp()).compareTo(ci2.getTimeStamp());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "error while formatting");
            Log.e(TAG, "" + messages);
            e.printStackTrace();
        }
        return out;
    }

    /*private static ConsultConnectionMessage getConsultConnectionMessageFromInout(InOutMessage message) throws JSONException {
        JSONObject body = new JSONObject(message.content);
        String type = body.getString(TYPE).equalsIgnoreCase(TYPE_OPERATOR_JOINED) ? ConsultConnectionMessage.TYPE_JOINED : ConsultConnectionMessage.TYPE_LEFT;
        JSONObject operator = body.getJSONObject("operator");
        long operatorId = operator.getLong("id");
        String name = operator.isNull("name") ? null : operator.getString("name");
        String status = operator.isNull("status") ? null : operator.getString("status");
        boolean gender = operator.isNull("gender") ? false : operator.getString("gender").equalsIgnoreCase("male");
        String photourl = operator.isNull("photoUrl") ? null : operator.getString("photoUrl");
        String title = "";

        try {
            title = body.getString(TEXT).split(" ")[0];
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ConsultConnectionMessage c =
                new ConsultConnectionMessage(
                        String.valueOf(operatorId)
                        , type
                        , name
                        , gender
                        , message.sentAt.millis
                        , photourl
                        , status
                        , title
                        , String.valueOf(message.messageId)
                        , false);
        return c;
    }*/

    public static List<String> getReadIds(Bundle b) {
        ArrayList<String> ids = new ArrayList<>();
        try {
            if (b == null) return new ArrayList<>();
            Object o = b.get("readInMessageIds");

            if (o instanceof ArrayList) {
                Log.i(TAG, "getReadIds o instanceof ArrayList");
                ids.addAll((Collection<? extends String>) b.get("readInMessageIds"));

                Log.e(TAG, "getReadIds = ");
            }
            if (o instanceof String) {
                Log.i(TAG, "getReadIds o instanceof String " + o);
                String contents = (String) o;
                if (!contents.contains(",")) {
                    ids.add((String) o);
                } else {
                    String[] idsArray = contents.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
                    ids.addAll(Arrays.asList(idsArray));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids;
    }

    public static String getMessageTyping(String clientId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(CLIENT_ID, clientId);
            jsonObject.put(TYPE, TYPE_TYPING);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString().replace("\\\\", "");
    }

    public static String getMessageClientOffline(String clientId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(CLIENT_ID, clientId);
            jsonObject.put(TYPE, TYPE_CLIENT_OFFLINE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString().replace("\\\\", "");
    }

    /**
     * метод проверяет наличие поля clientId во входящем сообщении
     *
     * @return true если нет поля clientId или оно совпадает с текущим clientId
     */
    public static boolean checkId(PushMessage pushMessage, String clientID) {
        JSONObject fullMessage = getFullMessage(pushMessage);
        if (fullMessage == null) {
            return true;
        }
        if (clientID == null) {
            return false;
        }

        try {
            if (fullMessage.has(CLIENT_ID) && !clientID.equals(fullMessage.get(CLIENT_ID))) {
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static String getUserAgent(Context ctx) {
        return String.format(ctx.getResources().getString(R.string.user_agent), getOsVersion(),
                getAppVersion(ctx), getLibVersion(), getDeviceName());
    }
}
