package im.threads.business.transport;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import im.threads.R;
import im.threads.business.config.BaseConfig;
import im.threads.business.formatters.ChatItemType;
import im.threads.business.formatters.SpeechStatus;
import im.threads.business.logger.LoggerEdna;
import im.threads.business.models.Attachment;
import im.threads.business.models.ChatItem;
import im.threads.business.models.ConsultConnectionMessage;
import im.threads.business.models.ConsultPhrase;
import im.threads.business.models.FileDescription;
import im.threads.business.models.MessageFromHistory;
import im.threads.business.models.MessageState;
import im.threads.business.models.Operator;
import im.threads.business.models.Optional;
import im.threads.business.models.QuestionDTO;
import im.threads.business.models.Quote;
import im.threads.business.models.RequestResolveThread;
import im.threads.business.models.SimpleSystemMessage;
import im.threads.business.models.Survey;
import im.threads.business.models.UserPhrase;
import im.threads.business.rest.models.HistoryResponse;
import im.threads.business.utils.DateHelper;

public final class HistoryParser {
    private HistoryParser() {
    }

    @NonNull
    public static List<ChatItem> getChatItems(HistoryResponse response) {
        List<ChatItem> list = new ArrayList<>();
        if (response != null) {
            List<MessageFromHistory> responseList = response.getMessages();
            if (responseList != null) {
                list = getChatItems(responseList);
                HistoryLoader.INSTANCE.setupLastItemIdFromHistory(responseList);
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
                final long timeStamp = message.getTimeStamp();
                final Operator operator = message.getOperator();
                String name = null;
                String photoUrl = null;
                String operatorId = null;
                boolean sex = false;
                String orgUnit = null;
                String role = null;
                if (operator != null) {
                    name = operator.getAliasOrName();
                    photoUrl = !TextUtils.isEmpty(operator.getPhotoUrl()) ? operator.getPhotoUrl() : null;
                    operatorId = operator.getId() != null ? String.valueOf(operator.getId()) : null;
                    sex = Operator.Gender.MALE.equals(operator.getGender());
                    orgUnit = operator.getOrgUnit();
                    role = operator.getRole();
                }
                ChatItemType type = ChatItemType.fromString(message.getType());
                switch (type) {
                    case THREAD_ENQUEUED:
                    case AVERAGE_WAIT_TIME:
                    case PARTING_AFTER_SURVEY:
                    case THREAD_CLOSED:
                    case THREAD_WILL_BE_REASSIGNED:
                    case CLIENT_BLOCKED:
                    case THREAD_IN_PROGRESS:
                        out.add(getSystemMessageFromHistory(message));
                        break;
                    case OPERATOR_JOINED:
                    case OPERATOR_LEFT:
                        out.add(new ConsultConnectionMessage(uuid, operatorId,
                                message.getType(), name, sex, timeStamp, photoUrl,
                                null, null, orgUnit, role, message.isDisplay(),
                                message.getText(), message.getThreadId()));
                        break;
                    case SURVEY:
                        Survey survey = getSurveyFromJsonString(message.getText());
                        if (survey != null) {
                            survey.setRead(message.isRead());
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
                    case REQUEST_CLOSE_THREAD:
                        out.add(new RequestResolveThread(uuid, message.getHideAfter(), timeStamp, message.getThreadId(), message.isRead()));
                        break;
                    default:
                        String phraseText = "";
                        if (message.getText() != null) {
                            phraseText = message.getText();
                        } else if (message.getSpeechText() != null) {
                            phraseText = message.getSpeechText();
                        }
                        final FileDescription fileDescription = message.getAttachments() != null ? fileDescriptionFromList(message.getAttachments()) : null;
                        if (fileDescription != null) {
                            fileDescription.setFrom(name);
                            fileDescription.setTimeStamp(timeStamp);
                        }
                        final Quote quote = message.getQuotes() != null ? quoteFromList(message.getQuotes()) : null;
                        if (quote != null && quote.getFileDescription() != null)
                            quote.getFileDescription().setTimeStamp(timeStamp);
                        if (message.getOperator() != null) {
                            out.add(
                                    new ConsultPhrase(
                                            uuid,
                                            fileDescription,
                                            quote,
                                            name,
                                            phraseText,
                                            message.getFormattedText(),
                                            timeStamp,
                                            operatorId,
                                            photoUrl,
                                            message.isRead(),
                                            message.getOperator().getStatus(),
                                            false,
                                            message.getThreadId(),
                                            message.getQuickReplies(),
                                            message.getSettings() != null ? message.getSettings().isBlockInput() : null,
                                            SpeechStatus.Companion.fromString(message.getSpeechStatus())
                                    )
                            );
                        } else {
                            if (fileDescription != null) {
                                fileDescription.setFrom(BaseConfig.instance.context.getString(R.string.ecc_I));
                            }
                            MessageState sentState = message.isRead() ? MessageState.STATE_WAS_READ : MessageState.STATE_SENT;
                            out.add(new UserPhrase(uuid, phraseText, quote, timeStamp, fileDescription, sentState, message.getThreadId()));
                        }
                }
            }
            Collections.sort(out, (ci1, ci2) -> Long.compare(ci1.getTimeStamp(), ci2.getTimeStamp()));
        } catch (final Exception e) {
            LoggerEdna.error("error while formatting: " + messages, e);
        }
        return out;
    }

    private static Survey getSurveyFromJsonString(@NonNull String text) {
        try {
            Survey survey = BaseConfig.instance.gson.fromJson(text, Survey.class);
            final long time = new Date().getTime();
            survey.setPhraseTimeStamp(time);
            survey.setSentState(MessageState.STATE_NOT_SENT);
            survey.setDisplayMessage(true);
            for (final QuestionDTO questionDTO : survey.getQuestions()) {
                questionDTO.setPhraseTimeStamp(time);
            }
            return survey;
        } catch (final JsonSyntaxException e) {
            LoggerEdna.error("getSurveyFromJsonString", e);
            return null;
        }
    }

    private static Survey getCompletedSurveyFromHistory(MessageFromHistory message) {
        Survey survey = new Survey(message.getUuid(), message.getSendingId(), message.getTimeStamp(), MessageState.STATE_WAS_READ, message.isRead(), message.isDisplay());
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

    private static SimpleSystemMessage getSystemMessageFromHistory(MessageFromHistory message) {
        return new SimpleSystemMessage(message.getUuid(), message.getType(), message.getTimeStamp(), message.getText(), message.getThreadId());
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
                authorName = BaseConfig.instance.context.getString(R.string.ecc_I);
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
        if (attachments.size() > 0) {
            Attachment attachment = attachments.get(0);
            if (attachment != null) {
                String incomingName = attachment.getName();
                String mimeType = attachment.getType();
                long size = 0;
                Optional metaData = attachment.getOptional();
                if (metaData != null) {
                    size = metaData.getSize();
                }
                fileDescription = new FileDescription(
                        null,
                        null,
                        size,
                        0
                );
                fileDescription.setDownloadPath(attachment.getResult());
                fileDescription.setIncomingName(incomingName);
                fileDescription.setMimeType(mimeType);
                fileDescription.setState(attachment.getState());
                if (attachment.getErrorCode() != null) {
                    fileDescription.setErrorCode(attachment.getErrorCodeState());
                    fileDescription.setErrorMessage(attachment.getErrorMessage());
                }
            }
        }
        return fileDescription;
    }
}
