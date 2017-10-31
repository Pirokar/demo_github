package im.threads.formatters;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import im.threads.model.Attachment;
import im.threads.model.ChatItem;
import im.threads.model.ConsultConnectionMessage;
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
import im.threads.model.UserPhrase;

public class IncomingMessageParser {
    private static final String TAG = "MessageFormatter ";

    private IncomingMessageParser() {
    }

    private static JSONObject getFullMessage(final PushMessage pushMessage) {
        JSONObject fullMessage = null;

        try {
            if (pushMessage.fullMessage != null) {
                fullMessage = new JSONObject(pushMessage.fullMessage);
            }
        } catch (final JSONException e) {
            e.printStackTrace();
        }

        return fullMessage;
    }

    private static String getType(final JSONObject fullMessage) {
        String type = "";

        try {
            type = fullMessage.getString(PushMessageAttributes.TYPE);
        } catch (final JSONException e) {
            e.printStackTrace();
        }

        return type;
    }

    private static String getClientId(final JSONObject fullMessage) {
        String type = "";

        try {
            type = fullMessage.getString(PushMessageAttributes.CLIENT_ID);
        } catch (final JSONException e) {
            e.printStackTrace();
        }

        return type;
    }

    private static boolean isChatPush(final JSONObject fullMessage) {
        String type;

        try {
            if (fullMessage.has("origin")) {
                type = fullMessage.getString("origin");
            } else {
                type = null;
            }
        } catch (final JSONException e) {
            e.printStackTrace();
            type = null;
        }

        return type != null && type.equalsIgnoreCase("threads");
    }

    public static Long getHideAfter(final JSONObject fullMessage) {
        try {
            return Long.parseLong(fullMessage.getString(PushMessageAttributes.HIDE_AFTER));
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    private static String getMessage(final JSONObject fullMessage, final PushMessage pushMessage) {
        String message = null;
        try {
            message = fullMessage.getString(PushMessageAttributes.TEXT) == null
                    ? pushMessage.shortMessage : fullMessage.getString(PushMessageAttributes.TEXT);
        } catch (final JSONException e) {
            e.printStackTrace();
        }

        return message;
    }

    private static ConsultPhrase getConsultPhraseFromPush(final PushMessage pushMessage,
                                                          final JSONObject fullMessage,
                                                          final String message) {
        try {
            final String messageId = pushMessage.messageId;
            String backendId;
            try {
                backendId = String.valueOf(fullMessage.getInt(PushMessageAttributes.BACKEND_ID));
            } catch (final Exception e) {
                backendId = "0";
            }
            final long timeStamp = pushMessage.sentAt;
            final JSONObject operatorInfo = fullMessage.getJSONObject("operator");
            final String name = operatorInfo.getString("name");
            final String photoUrl = operatorInfo.isNull("photoUrl") ? null : operatorInfo.getString("photoUrl");
            final String status = operatorInfo.has("status") && !operatorInfo.isNull("status") ? operatorInfo.getString("status") : null;
            final JSONArray attachmentsArray = fullMessage.has(PushMessageAttributes.ATTACHMENTS) ? fullMessage.getJSONArray(PushMessageAttributes.ATTACHMENTS) : null;
            FileDescription fileDescription = null;
            if (null != attachmentsArray)
                fileDescription = fileDescriptionFromJson(fullMessage.getJSONArray(PushMessageAttributes.ATTACHMENTS));
            if (fileDescription != null) {
                fileDescription.setFrom(name);
                fileDescription.setTimeStamp(timeStamp);
            }
            Quote quote = null;
            if (fullMessage.has(PushMessageAttributes.QUOTES))
                quote = quoteFromJson(fullMessage.getJSONArray(PushMessageAttributes.QUOTES));
            if (quote != null && quote.getFileDescription() != null) {
                quote.getFileDescription().setTimeStamp(timeStamp);
            }
            final boolean gender = operatorInfo.isNull("gender") ? false : operatorInfo.getString("gender").equalsIgnoreCase("male");

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
        } catch (final JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static UserPhrase getUserPhraseFromPush(final PushMessage pushMessage,
                                                    final JSONObject fullMessage,
                                                    final String message) {
        try {
            final String messageId = pushMessage.messageId;
            String backendId;
            try {
                backendId = String.valueOf(fullMessage.getInt(PushMessageAttributes.BACKEND_ID));
            } catch (final Exception e) {
                backendId = "0";
            }
            final long phraseTimeStamp = pushMessage.sentAt;
            final JSONArray attachmentsArray = fullMessage.has(PushMessageAttributes.ATTACHMENTS) ? fullMessage.getJSONArray(PushMessageAttributes.ATTACHMENTS) : null;
            FileDescription fileDescription = null;
            if (null != attachmentsArray)
                fileDescription = fileDescriptionFromJson(fullMessage.getJSONArray(PushMessageAttributes.ATTACHMENTS));
            if (fileDescription != null) {
                fileDescription.setTimeStamp(phraseTimeStamp);
            }
            Quote mQuote = null;
            if (fullMessage.has(PushMessageAttributes.QUOTES))
                mQuote = quoteFromJson(fullMessage.getJSONArray(PushMessageAttributes.QUOTES));
            if (mQuote != null && mQuote.getFileDescription() != null) {
                mQuote.getFileDescription().setTimeStamp(phraseTimeStamp);
            }

            final UserPhrase userPhrase = new UserPhrase(
                    messageId,
                    message,
                    mQuote,
                    phraseTimeStamp,
                    fileDescription,
                    backendId
            );
            userPhrase.setSentState(MessageState.STATE_SENT);
            return userPhrase;
        } catch (final JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return null, если не удалось распознать формат сообщения.
     */
    public static ChatItem format(final PushMessage pushMessage) {
        final JSONObject fullMessage = getFullMessage(pushMessage);

        // В пуше для чата должен быть fullMessage, и он должен соответствовать формату JSON.
        if (fullMessage == null) {
            return null;
        }

        final String typeStr = getType(fullMessage);

        if (TextUtils.isEmpty(typeStr)) {
            return null;
        }

        try {
            final PushMessageTypes type = PushMessageTypes.valueOf(typeStr);
            switch (type) {
                case OPERATOR_JOINED:
                case OPERATOR_LEFT:
                    return getConsultConnectionFromPush(pushMessage);
                case SCHEDULE:
                    return getScheduleInfoFromPush(pushMessage, fullMessage);
                case SURVEY:
                    return getRatingFromPush(pushMessage, fullMessage);
                case REQUEST_CLOSE_THREAD:
                    return getCloseRequestFromPush(fullMessage);
                case MESSAGE:
                case ON_HOLD:
                    // Либо в fullMessage должны содержаться ключи из списка:
                    // "attachments", "text", "quotes"
                    return checkMessageIsFull(pushMessage, fullMessage);
                case NONE:
                case MESSAGES_READ:
                case OPERATOR_LOOKUP_STARTED:
                case CLIENT_BLOCKED:
                case SCENARIO:
                    return new EmptyChatItem(type.name());
                default:
                    return checkMessageIsFull(pushMessage, fullMessage);
            }
        } catch (final IllegalArgumentException ex) {
            // pass
        }

        return null;
    }

    @Nullable
    private static ChatItem checkMessageIsFull(final PushMessage pushMessage, final JSONObject fullMessage) {
        final String message = getMessage(fullMessage, pushMessage);
        if (message != null || fullMessage.has(PushMessageAttributes.ATTACHMENTS) || fullMessage.has(PushMessageAttributes.QUOTES)) {
            final ChatItem item = getConsultPhraseFromPush(pushMessage, fullMessage, message);
            return item != null ? item : getUserPhraseFromPush(pushMessage, fullMessage, message);
        } else {
            return null;
        }
    }

    private static Long getThreadId(final JSONObject fullMessage) {
        try {
            return Long.parseLong(fullMessage.getString(PushMessageAttributes.THREAD_ID));
        } catch (final JSONException e) {
            e.printStackTrace();
        }
        return -1L;
    }

    private static ScheduleInfo getScheduleInfoFromPush(final PushMessage pushMessage, final JSONObject fullMessage) {
        ScheduleInfo scheduleInfo = null;
        final String text = getMessage(fullMessage, pushMessage);
        if (text != null) {
            try {
                scheduleInfo = new Gson().fromJson(text, ScheduleInfo.class);
                scheduleInfo.setDate(new Date().getTime());
            } catch (final JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
        return scheduleInfo;
    }

    private static Survey getRatingFromPush(final PushMessage pushMessage, final JSONObject fullMessage) {
        Survey survey = null;
        final String text = getMessage(fullMessage, pushMessage);
        if (text != null) {
            try {
                survey = new Gson().fromJson(text, Survey.class);
                final long time = new Date().getTime();
                survey.setPhraseTimeStamp(time);
                survey.setSentState(MessageState.STATE_NOT_SENT);
                for (final QuestionDTO questionDTO : survey.getQuestions()) {
                    questionDTO.setPhraseTimeStamp(time);
                }
            } catch (final JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
        return survey;
    }

    // show close thread's request only if thread has time till close (hideAfter)
    private static RequestResolveThread getCloseRequestFromPush(final JSONObject fullMessage) {
        final Long hideAfter = getHideAfter(fullMessage);
        return hideAfter > 0 ? new RequestResolveThread(hideAfter, System.currentTimeMillis()) : null;
    }

    private static ConsultConnectionMessage getConsultConnectionFromPush(final PushMessage pushMessage) {
        ConsultConnectionMessage chatItem = null;

        try {
            final JSONObject fullMessage = new JSONObject(pushMessage.fullMessage);
            final String messageId = pushMessage.messageId;
            final long timeStamp = pushMessage.sentAt;
            final JSONObject operator = fullMessage.getJSONObject("operator");
            final long operatorId = operator.getLong("id");
            final String name = operator.isNull("name") ? null : operator.getString("name");
            final String status = operator.isNull("status") ? null : operator.getString("status");
            final String type = fullMessage.getString(PushMessageAttributes.TYPE);
            final boolean gender = operator.isNull("gender") ? false : operator.getString("gender").equalsIgnoreCase("male");
            final String photourl = operator.isNull("photoUrl") ? null : operator.getString("photoUrl");
            final String title = pushMessage.shortMessage == null ? null : pushMessage.shortMessage.split(" ")[0];
            final boolean displayMessage = !fullMessage.has("display") || fullMessage.getBoolean("display");
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

        } catch (final JSONException e) {
            e.printStackTrace();
        }

        return chatItem;
    }

    public static Quote quoteFromJson(final JSONArray quotes) throws JSONException {
        Quote quote = null;
        FileDescription quoteFileDescription = null;
        String quoteString = null;
        String consultName = "";
        if (quotes.length() > 0 && quotes.getJSONObject(0) != null && (quotes.getJSONObject(0).has(PushMessageAttributes.TEXT))) {
            quoteString = quotes.getJSONObject(0).getString(PushMessageAttributes.TEXT);
        }
        if (quotes.length() > 0
                && quotes.getJSONObject(0) != null
                && (quotes.getJSONObject(0).has(PushMessageAttributes.ATTACHMENTS))
                && (quotes.getJSONObject(0).getJSONArray(PushMessageAttributes.ATTACHMENTS).length() > 0
                && (quotes.getJSONObject(0).getJSONArray(PushMessageAttributes.ATTACHMENTS).getJSONObject(0).has("result")))) {
            String header = null;
            if (quotes.getJSONObject(0).getJSONArray(PushMessageAttributes.ATTACHMENTS).getJSONObject(0).has("optional")) {
                header = quotes.getJSONObject(0).getJSONArray(PushMessageAttributes.ATTACHMENTS).getJSONObject(0).getJSONObject("optional").getString("name");
            }
            quoteFileDescription = fileDescriptionFromJson(quotes.getJSONObject(0).getJSONArray(PushMessageAttributes.ATTACHMENTS));
        }
        if (quotes.length() > 0 && quotes.getJSONObject(0) != null && quotes.getJSONObject(0).has("operator")) {
            try {
                consultName = quotes.getJSONObject(0).getJSONObject("operator").getString("name");
            } catch (final JSONException e) {
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

    public static Quote quoteFromList(final List<MessgeFromHistory> quotes) {
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

    public static FileDescription fileDescriptionFromJson(final JSONArray jsonArray) throws JSONException {
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

    public static FileDescription fileDescriptionFromList(final List<Attachment> attachments) {
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

    public static List<ChatItem> formatMessages(final List<PushMessage> messages) {
        final List<ChatItem> list = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            final ChatItem chatItem = format(messages.get(i));
            if (chatItem != null) {
                list.add(chatItem);
            }
        }
        return list;
    }

    public static ArrayList<ChatItem> formatNew(final List<MessgeFromHistory> messages) {
        final ArrayList<ChatItem> out = new ArrayList<>();
        try {
            for (final MessgeFromHistory message : messages) {
                if (message == null)
                    continue;
                final String messageId = message.getProviderId();
                final String backendId = String.valueOf(message.getId());
                final long timeStamp = message.getTimeStamp();
                final Operator operator = message.getOperator();
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

                if (message.getType() != null && !message.getType().isEmpty() &&
                        (message.getType().equalsIgnoreCase(PushMessageTypes.OPERATOR_JOINED.name()) ||
                        message.getType().equalsIgnoreCase(PushMessageTypes.OPERATOR_LEFT.name()))) {
                    final String type = message.getType();
                    out.add(new ConsultConnectionMessage(operatorId, type, name, false, timeStamp, photoUrl, null, null, messageId, false));
                } else {
                    final String phraseText = message.getText();
                    final FileDescription fileDescription = message.getAttachments() != null ? fileDescriptionFromList(message.getAttachments()) : null;
                    if (fileDescription != null) {
                        fileDescription.setFrom(name);
                        fileDescription.setTimeStamp(timeStamp);
                    }
                    final Quote quote = message.getQuotes() != null ? quoteFromList(message.getQuotes()) : null;
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
                public int compare(final ChatItem ci1, final ChatItem ci2) {
                    return Long.valueOf(ci1.getTimeStamp()).compareTo(ci2.getTimeStamp());
                }
            });
        } catch (final Exception e) {
            e.printStackTrace();
            Log.e(TAG, "error while formatting");
            Log.e(TAG, "" + messages);
            e.printStackTrace();
        }
        return out;
    }

    public static List<String> getReadIds(final Bundle b) {
        final ArrayList<String> ids = new ArrayList<>();
        try {
            if (b == null) return new ArrayList<>();
            final Object o = b.get("readInMessageIds");

            if (o instanceof ArrayList) {
                Log.i(TAG, "getReadIds o instanceof ArrayList");
                final Collection<? extends String> readInMessageIds = (Collection<? extends String>) b.get("readInMessageIds");
                if (readInMessageIds != null) {
                    ids.addAll(readInMessageIds);
                }

                Log.e(TAG, "getReadIds = ");
            }
            if (o instanceof String) {
                Log.i(TAG, "getReadIds o instanceof String " + o);
                final String contents = (String) o;
                if (!contents.contains(",")) {
                    ids.add((String) o);
                } else {
                    final String[] idsArray = contents.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
                    ids.addAll(Arrays.asList(idsArray));
                }

            }

        } catch (final Exception e) {
            e.printStackTrace();
        }
        return ids;
    }

    /**
     * метод проверяет наличие поля clientId во входящем сообщении
     *
     * @return true если нет поля clientId или оно совпадает с текущим clientId
     */
    public static boolean checkId(final PushMessage pushMessage, final String clientID) {
        final JSONObject fullMessage = getFullMessage(pushMessage);
        if (fullMessage == null) {
            return true;
        }
        if (clientID == null) {
            return false;
        }

        try {
            if (fullMessage.has(PushMessageAttributes.CLIENT_ID) && !clientID.equals(fullMessage.get(PushMessageAttributes.CLIENT_ID))) {
                return false;
            }
        } catch (final JSONException e) {
            e.printStackTrace();
        }
        return true;
    }
}
