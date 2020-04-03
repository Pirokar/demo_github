package im.threads.internal.transport;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.formatters.ChatItemType;
import im.threads.internal.model.Attachment;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ConsultConnectionMessage;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.HistoryResponse;
import im.threads.internal.model.MessageFromHistory;
import im.threads.internal.model.MessageState;
import im.threads.internal.model.Operator;
import im.threads.internal.model.QuestionDTO;
import im.threads.internal.model.Quote;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.utils.DateHelper;
import im.threads.internal.utils.ThreadsLogger;

public final class HistoryParser {
    private static final String TAG = "HistoryParser ";

    private HistoryParser() {
    }

    @NonNull
    public static List<ChatItem> getChatItems(HistoryResponse response) {
        List<ChatItem> list = new ArrayList<>();
        if (response != null) {
            List<MessageFromHistory> responseList = response.getMessages();
            if (responseList != null) {
                list = getChatItems(responseList);
                HistoryLoader.setupLastItemIdFromHistory(responseList);
            }
        }
        return list;
    }

    private static List<ChatItem> getChatItems(final List<MessageFromHistory> messages) {
        final List<ChatItem> out = new ArrayList<>();
        try {
            for (final MessageFromHistory message : messages) {
                if (message == null) {
                    continue;
                }
                final String uuid = message.getUuid();
                final String providerId = message.getProviderId();
                final List<String> providerIds = message.getProviderIds();
                final long timeStamp = message.getTimeStamp();
                final Operator operator = message.getOperator();
                String name = null;
                String photoUrl = null;
                String operatorId = null;
                boolean sex = false;
                String orgUnit = null;
                if (operator != null) {
                    name = operator.getAliasOrName();
                    photoUrl = !TextUtils.isEmpty(operator.getPhotoUrl()) ? operator.getPhotoUrl() : null;
                    operatorId = operator.getId() != null ? String.valueOf(operator.getId()) : null;
                    sex = Operator.Gender.MALE.equals(operator.getGender());
                    orgUnit = operator.getOrgUnit();
                }
                ChatItemType type = ChatItemType.fromString(message.getType());
                switch (type) {
                    case OPERATOR_JOINED:
                    case OPERATOR_LEFT:
                        out.add(new ConsultConnectionMessage(uuid, providerId, providerIds, operatorId, message.getType(), name, sex, timeStamp, photoUrl, null, null, orgUnit, message.isDisplay()));
                        break;
                    case SURVEY:
                        Survey survey = getSurveyFromJsonString(message.getText());
                        if (survey != null) {
                            survey.setPhraseTimeStamp(message.getTimeStamp());
                            for (final QuestionDTO questionDTO : survey.getQuestions()) {
                                questionDTO.setPhraseTimeStamp(message.getTimeStamp());
                            }
                            out.add(survey);
                        }
                        break;
                    case SURVEY_QUESTION_ANSWER:
                        out.add(getCompletedSurveyFromHistory(message));
                        break;
                    default:
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
                            out.add(new ConsultPhrase(uuid, providerId, providerIds, fileDescription, quote, name, phraseText, timeStamp,
                                    operatorId, photoUrl, message.isRead(), null, false));
                        } else {
                            if (fileDescription != null) {
                                fileDescription.setFrom(Config.instance.context.getString(R.string.threads_I));
                            }
                            MessageState sentState = message.isRead() ? MessageState.STATE_WAS_READ : MessageState.STATE_SENT;
                            out.add(new UserPhrase(uuid, providerId, providerIds, phraseText, quote, timeStamp, fileDescription, sentState));
                        }
                }
            }
            Collections.sort(out, (ci1, ci2) -> Long.compare(ci1.getTimeStamp(), ci2.getTimeStamp()));
        } catch (final Exception e) {
            ThreadsLogger.e(TAG, "error while formatting: " + messages, e);
        }
        return out;
    }

    private static Survey getSurveyFromJsonString(@NonNull String text) {
        try {
            Survey survey = Config.instance.gson.fromJson(text, Survey.class);
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
                authorName = quoteFromHistory.getOperator().getAliasOrName();
            } else {
                authorName = Config.instance.context.getString(R.string.threads_I);
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

    private static FileDescription fileDescriptionFromList(final List<Attachment> attachments) {
        FileDescription fileDescription = null;
        if (attachments.size() > 0 && attachments.get(0) != null) {
            String header = null;
            if (attachments.get(0).getOptional() != null) {
                header = attachments.get(0).getOptional().getName();
            }
            fileDescription = new FileDescription(
                    null,
                    null,
                    attachments.get(0).getOptional().getSize(),
                    0
            );
            fileDescription.setDownloadPath(attachments.get(0).getResult());
            fileDescription.setIncomingName(header);
            fileDescription.setSelfie(attachments.get(0).isSelfie());
        }
        return fileDescription;
    }
}
