package im.threads.internal.formatters;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mfms.android.push_lite.repo.push.remote.model.PushMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import im.threads.internal.model.Attachment;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ConsultConnectionMessage;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.EmptyChatItem;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.MessageFromHistory;
import im.threads.internal.model.MessageState;
import im.threads.internal.model.Operator;
import im.threads.internal.model.QuestionDTO;
import im.threads.internal.model.Quote;
import im.threads.internal.model.RequestResolveThread;
import im.threads.internal.model.ScheduleInfo;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.utils.DateHelper;
import im.threads.internal.utils.ThreadsLogger;

public class IncomingMessageParser {
    private static final String TAG = "MessageFormatter ";

    private IncomingMessageParser() {
    }

    public static boolean isThreadsOriginPush(PushMessage pushMessage) {
        final JSONObject fullMessage = getFullMessage(pushMessage);
        return fullMessage != null && isThreadsOriginPush(fullMessage);
    }

    public static boolean isThreadsOriginPush(final Bundle bundle) {
        return bundle != null && PushMessageAttributes.THREADS.equalsIgnoreCase(bundle.getString(PushMessageAttributes.ORIGIN));
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

    public static ArrayList<ChatItem> formatNew(final List<MessageFromHistory> messages) {
        final ArrayList<ChatItem> out = new ArrayList<>();
        try {
            for (final MessageFromHistory message : messages) {
                if (message == null)
                    continue;
                final String uuid = message.getUuid();
                final String providerId = message.getProviderId();
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
                boolean sex = false;
                if (operator != null && operator.getGender() != null) {
                    sex = operator.getGender() == Operator.Gender.MALE;
                }

                String orgUnit = null;
                if (operator != null) {
                    orgUnit = operator.getOrgUnit();
                }

                if (!TextUtils.isEmpty(message.getType()) &&
                        (message.getType().equalsIgnoreCase(PushMessageType.OPERATOR_JOINED.name()) ||
                                message.getType().equalsIgnoreCase(PushMessageType.OPERATOR_LEFT.name()))) {
                    final String type = message.getType();
                    out.add(new ConsultConnectionMessage(uuid, providerId, operatorId, type, name, sex, timeStamp, photoUrl, null, null, orgUnit, message.isDisplay()));

                } else if (!TextUtils.isEmpty(message.getType())
                        && message.getType().equalsIgnoreCase(PushMessageType.SURVEY.name())) {

                    Survey survey = getSurveyFromJsonString(message.getText());

                    if (survey != null) {
                        survey.setPhraseTimeStamp(message.getTimeStamp());
                        for (final QuestionDTO questionDTO : survey.getQuestions()) {
                            questionDTO.setPhraseTimeStamp(message.getTimeStamp());
                        }
                        out.add(survey);
                    }

                } else if (!TextUtils.isEmpty(message.getType())
                        && message.getType().equalsIgnoreCase(PushMessageType.SURVEY_QUESTION_ANSWER.name())) {

                    Survey completedSurvey = getCompletedSurveyFromHistory(message);
                    out.add(completedSurvey);

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
                        out.add(new ConsultPhrase(uuid, providerId, fileDescription, quote, name, phraseText, timeStamp,
                                operatorId, photoUrl, true, null, false));
                    } else {
                        if (fileDescription != null) {
                            if (Locale.getDefault().getLanguage().equalsIgnoreCase("ru")) {
                                fileDescription.setFrom("Я");
                            } else {
                                fileDescription.setFrom("I");
                            }
                        }
                        out.add(new UserPhrase(uuid, providerId, phraseText, quote, timeStamp, fileDescription, MessageState.STATE_WAS_READ));
                    }
                }
            }
            Collections.sort(out, (ci1, ci2) -> Long.valueOf(ci1.getTimeStamp()).compareTo(ci2.getTimeStamp()));
        } catch (final Exception e) {
            ThreadsLogger.e(TAG, "error while formatting: " + messages, e);
        }
        return out;
    }

    public static List<String> getReadIds(final Bundle b) {
        final ArrayList<String> ids = new ArrayList<>();
        try {
            if (b == null) return new ArrayList<>();
            final Object readIds = b.get(PushMessageAttributes.READ_PROVIDER_IDS);

            if (readIds instanceof ArrayList) {
                ThreadsLogger.i(TAG, "getReadIds instanceof ArrayList");
                final Collection<? extends String> readInMessageIds = (Collection<? extends String>) b.get(PushMessageAttributes.READ_PROVIDER_IDS);
                if (readInMessageIds != null) {
                    ids.addAll(readInMessageIds);
                }

                ThreadsLogger.e(TAG, "getReadIds = ");
            }
            if (readIds instanceof String) {
                ThreadsLogger.i(TAG, "getReadIds instanceof String " + readIds);
                final String contents = (String) readIds;
                if (!contents.contains(",")) {
                    ids.add((String) readIds);
                } else {
                    final String[] idsArray = contents.replaceAll("\\[", "").replaceAll("]", "").split(",");
                    ids.addAll(Arrays.asList(idsArray));
                }
            }
        } catch (final Exception e) {
            ThreadsLogger.e(TAG, "getReadIds", e);
        }
        return ids;
    }

    /**
     * метод проверяет наличие поля clientId во входящем сообщении
     *
     * @return true если нет поля clientId или оно совпадает с текущим clientId
     */
    public static boolean checkId(final PushMessage pushMessage, final String currentClientId) {

        final JSONObject fullMessage = getFullMessage(pushMessage);

        boolean isCurrentClientId = false;

        try {
            isCurrentClientId = !TextUtils.isEmpty(currentClientId)
                    && fullMessage != null
                    && fullMessage.has(PushMessageAttributes.CLIENT_ID)
                    && currentClientId.equalsIgnoreCase(fullMessage.getString(PushMessageAttributes.CLIENT_ID));
        } catch (JSONException e) {
            ThreadsLogger.e(TAG, "checkId", e);
        }

        return isCurrentClientId;
    }

    public static boolean checkId(Bundle pushBundle, String currentClientId) {
        return !TextUtils.isEmpty(currentClientId)
                && pushBundle.containsKey(PushMessageAttributes.CLIENT_ID)
                && currentClientId.equalsIgnoreCase(pushBundle.getString(PushMessageAttributes.CLIENT_ID));
    }

    public static String getAppMarker(PushMessage pushMessage) {
        String appMarker = null;
        final JSONObject fullMessage = getFullMessage(pushMessage);

        if (fullMessage != null && fullMessage.has(PushMessageAttributes.APP_MARKER_KEY)) {
            try {
                appMarker = fullMessage.getString(PushMessageAttributes.APP_MARKER_KEY);
            } catch (JSONException e) {
                ThreadsLogger.e(TAG, "getAppMarker", e);
            }
        }
        return appMarker;
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
            final PushMessageType type = PushMessageType.fromString(typeStr);
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

    private static JSONObject getFullMessage(final PushMessage pushMessage) {
        JSONObject fullMessage = null;
        try {
            if (pushMessage.fullMessage != null) {
                fullMessage = new JSONObject(pushMessage.fullMessage);
            }
        } catch (final JSONException e) {
            ThreadsLogger.e(TAG, "getFullMessage", e);
        }
        return fullMessage;
    }

    private static String getType(final JSONObject fullMessage) {
        String type = "";
        try {
            type = fullMessage.getString(PushMessageAttributes.TYPE);
        } catch (final JSONException e) {
            ThreadsLogger.e(TAG, "getType", e);
        }
        return type;
    }

    private static boolean isThreadsOriginPush(final JSONObject fullMessage) {
        String origin = "";
        try {
            origin = fullMessage.getString(PushMessageAttributes.ORIGIN);
        } catch (final JSONException e) {
            ThreadsLogger.e(TAG, "isThreadsOriginPush", e);
        }
        return PushMessageAttributes.THREADS.equalsIgnoreCase(origin);
    }

    private static String getMessage(final JSONObject fullMessage, final PushMessage pushMessage) {
        String message = null;
        try {
            message = fullMessage.getString(PushMessageAttributes.TEXT) == null
                    ? pushMessage.shortMessage : fullMessage.getString(PushMessageAttributes.TEXT);
        } catch (final JSONException e) {
            ThreadsLogger.e(TAG, "getMessage", e);
        }
        return message;
    }

    private static ConsultPhrase getConsultPhraseFromPush(final PushMessage pushMessage,
                                                          final JSONObject fullMessage,
                                                          final String message) {
        try {
            String uuid;
            try {
                uuid = fullMessage.getString(PushMessageAttributes.UUID);
            } catch (final Exception e) {
                uuid = UUID.randomUUID().toString();
            }
            String providerId = pushMessage.messageId;
            String receivedDateString = fullMessage.getString(PushMessageAttributes.RECEIVED_DATE);
            long timeStamp = receivedDateString == null || receivedDateString.isEmpty() ? System.currentTimeMillis() : DateHelper.getMessageTimestampFromDateString(receivedDateString);
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
            final boolean gender = !operatorInfo.isNull("gender") && operatorInfo.getString("gender").equalsIgnoreCase("male");
            String operatorId = String.valueOf(operatorInfo.getLong("id"));
            return new ConsultPhrase(uuid, providerId, fileDescription, quote, name, message, timeStamp,
                    operatorId, photoUrl, false, status, gender);
        } catch (final JSONException e) {
            ThreadsLogger.e(TAG, "getConsultPhraseFromPush", e);
            return null;
        }
    }

    private static UserPhrase getUserPhraseFromPush(final PushMessage pushMessage,
                                                    final JSONObject fullMessage,
                                                    final String message) {
        try {
            String uuid;
            try {
                uuid = fullMessage.getString(PushMessageAttributes.UUID);
            } catch (final Exception e) {
                uuid = UUID.randomUUID().toString();
            }
            String providerId = pushMessage.messageId;
            String receivedDateString = fullMessage.getString(PushMessageAttributes.RECEIVED_DATE);
            long phraseTimeStamp = receivedDateString == null || receivedDateString.isEmpty() ? System.currentTimeMillis() : DateHelper.getMessageTimestampFromDateString(receivedDateString);
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
            final UserPhrase userPhrase = new UserPhrase(uuid, providerId, message, mQuote, phraseTimeStamp, fileDescription);
            userPhrase.setSentState(MessageState.STATE_SENT);
            return userPhrase;
        } catch (final JSONException e) {
            ThreadsLogger.e(TAG, "getUserPhraseFromPush", e);
            return null;
        }
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

    private static ScheduleInfo getScheduleInfoFromPush(final PushMessage pushMessage, final JSONObject fullMessage) {
        ScheduleInfo scheduleInfo = null;
        final String text = getMessage(fullMessage, pushMessage);
        if (text != null) {
            try {
                scheduleInfo = new Gson().fromJson(text, ScheduleInfo.class);
                scheduleInfo.setDate(new Date().getTime());
            } catch (final JsonSyntaxException e) {
                ThreadsLogger.e(TAG, "getScheduleInfoFromPush", e);
            }
        }
        return scheduleInfo;
    }

    private static Survey getRatingFromPush(final PushMessage pushMessage, final JSONObject fullMessage) {
        final String text = getMessage(fullMessage, pushMessage);
        if (!TextUtils.isEmpty(text)) {
            Survey survey = getSurveyFromJsonString(text);
            if (survey != null) {
                survey.setPhraseTimeStamp(pushMessage.sentAt);
                for (final QuestionDTO questionDTO : survey.getQuestions()) {
                    questionDTO.setPhraseTimeStamp(pushMessage.sentAt);
                }
            }
            return survey;
        } else {
            return null;
        }
    }

    private static Survey getSurveyFromJsonString(@NonNull String text) {
        try {
            Survey survey = new Gson().fromJson(text, Survey.class);
            final long time = new Date().getTime();
            survey.setPhraseTimeStamp(time);
            survey.setSentState(MessageState.STATE_NOT_SENT);
            for (final QuestionDTO questionDTO : survey.getQuestions()) {
                questionDTO.setPhraseTimeStamp(time);
            }
            return survey;
        } catch (final JsonSyntaxException e) {
            ThreadsLogger.e(TAG, "getSurveyFromJsonString", e);
            return null;
        }
    }

    private static Survey getCompletedSurveyFromHistory(MessageFromHistory message) {
        Survey survey = new Survey(message.getSendingId(), message.getTimeStamp(), MessageState.STATE_WAS_READ);
        QuestionDTO question = new QuestionDTO();
        question.setId(message.getQuestionId());
        question.setPhraseTimeStamp(message.getTimeStamp());
        question.setText(message.getText());
        question.setRate(message.getRate());
        question.setScale(message.getScale());
        question.setSendingId(message.getSendingId());
        question.setSimple(message.isSimple());
        survey.setQuestions(Collections.singletonList(question));
        return survey;
    }

    // show close thread's request only if thread has time till close (hideAfter)
    private static RequestResolveThread getCloseRequestFromPush(final JSONObject fullMessage) {
        final Long hideAfter = getHideAfter(fullMessage);
        return hideAfter > 0 ? new RequestResolveThread(hideAfter, System.currentTimeMillis()) : null;
    }

    private static Long getHideAfter(final JSONObject fullMessage) {
        try {
            return Long.parseLong(fullMessage.getString(PushMessageAttributes.HIDE_AFTER));
        } catch (final Exception e) {
            ThreadsLogger.e(TAG, "getHideAfter", e);
        }
        return 0L;
    }

    private static ConsultConnectionMessage getConsultConnectionFromPush(final PushMessage pushMessage) {
        ConsultConnectionMessage chatItem = null;
        try {
            final JSONObject fullMessage = new JSONObject(pushMessage.fullMessage);
            final String providerId = pushMessage.messageId;
            String uuid = fullMessage.getString(PushMessageAttributes.UUID);
            final long timeStamp = pushMessage.sentAt;
            final JSONObject operator = fullMessage.getJSONObject("operator");
            final long operatorId = operator.getLong("id");
            final String name = operator.isNull("name") ? null : operator.getString("name");
            final String status = operator.isNull("status") ? null : operator.getString("status");
            final String type = fullMessage.getString(PushMessageAttributes.TYPE);
            final boolean gender = !operator.isNull("gender") && operator.getString("gender").equalsIgnoreCase("male");
            final String photourl = operator.isNull("photoUrl") ? null : operator.getString("photoUrl");
            final String title = pushMessage.shortMessage == null ? null : pushMessage.shortMessage.split(" ")[0];
            final boolean displayMessage = !fullMessage.has("display") || fullMessage.getBoolean("display");
            String orgUnit = operator.isNull(PushMessageAttributes.OPERATOR_ORG_UNIT) ? null : operator.getString(PushMessageAttributes.OPERATOR_ORG_UNIT);
            chatItem = new ConsultConnectionMessage(uuid, providerId, String.valueOf(operatorId), type, name, gender,
                    timeStamp, photourl, status, title, orgUnit, displayMessage);
        } catch (final JSONException e) {
            ThreadsLogger.e(TAG, "getConsultConnectionFromPush", e);
        }
        return chatItem;
    }

    private static Quote quoteFromJson(final JSONArray quotes) throws JSONException {
        Quote quote = null;
        if (quotes.length() > 0 && quotes.getJSONObject(0) != null) {
            JSONObject quoteJson = quotes.getJSONObject(0);
            FileDescription quoteFileDescription = null;
            String quoteUuid = null;
            String quoteString = null;
            String authorName = "";
            String receivedDateString = quoteJson.getString(PushMessageAttributes.RECEIVED_DATE);
            long timestamp = receivedDateString == null || receivedDateString.isEmpty() ? System.currentTimeMillis() : DateHelper.getMessageTimestampFromDateString(receivedDateString);
            if (quoteJson.has(PushMessageAttributes.UUID)) {
                quoteUuid = quoteJson.getString(PushMessageAttributes.UUID);
            }
            if ((quoteJson.has(PushMessageAttributes.TEXT))) {
                quoteString = quoteJson.getString(PushMessageAttributes.TEXT);
            }
            if ((quoteJson.has(PushMessageAttributes.ATTACHMENTS))
                    && (quoteJson.getJSONArray(PushMessageAttributes.ATTACHMENTS).length() > 0
                    && (quoteJson.getJSONArray(PushMessageAttributes.ATTACHMENTS).getJSONObject(0).has("result")))) {
                quoteFileDescription = fileDescriptionFromJson(quoteJson.getJSONArray(PushMessageAttributes.ATTACHMENTS));
            }
            if (quoteJson.has("operator")) {
                try {
                    authorName = quoteJson.getJSONObject("operator").getString("name");
                } catch (final JSONException e) {
                    ThreadsLogger.e(TAG, "" + quotes, e);
                }
            } else {
                if (Locale.getDefault().getLanguage().equalsIgnoreCase("ru")) {
                    authorName = "Я";
                } else {
                    authorName = "I";
                }
            }
            if (quoteUuid != null && (quoteString != null || quoteFileDescription != null)) {
                quote = new Quote(quoteUuid, authorName, quoteString, quoteFileDescription, timestamp);
            }
            if (quoteFileDescription != null) {
                quoteFileDescription.setFrom(authorName);
            }
        }
        return quote;
    }

    private static Quote quoteFromList(final List<MessageFromHistory> quotes) {
        Quote quote = null;
        if (quotes.size() > 0 && quotes.get(0) != null) {
            MessageFromHistory quoteFromHistory = quotes.get(0);
            FileDescription quoteFileDescription = null;
            String quoteString = null;
            String authorName;
            String receivedDateString = quoteFromHistory.getReceivedDate();
            long timestamp = receivedDateString == null || receivedDateString.isEmpty() ? System.currentTimeMillis() : DateHelper.getMessageTimestampFromDateString(receivedDateString);
            if (quoteFromHistory.getText() != null) {
                quoteString = quoteFromHistory.getText();
            }
            if ((quoteFromHistory.getAttachments() != null)
                    && (quoteFromHistory.getAttachments().size() > 0
                    && (quoteFromHistory.getAttachments().get(0).getResult() != null))) {
                quoteFileDescription = fileDescriptionFromList(quoteFromHistory.getAttachments());
            }
            if (quoteFromHistory.getOperator() != null) {
                authorName = quoteFromHistory.getOperator().getName();
            } else {
                if (Locale.getDefault().getLanguage().equalsIgnoreCase("ru")) {
                    authorName = "Я";
                } else {
                    authorName = "I";
                }
            }
            if (quoteString != null || quoteFileDescription != null) {
                quote = new Quote(quoteFromHistory.getUuid(), authorName, quoteString, quoteFileDescription, timestamp);
            }
            if (quoteFileDescription != null) {
                quoteFileDescription.setFrom(authorName);
            }
        }
        return quote;
    }

    private static FileDescription fileDescriptionFromJson(final JSONArray jsonArray) throws JSONException {
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

    private static FileDescription fileDescriptionFromList(final List<Attachment> attachments) {
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
}
