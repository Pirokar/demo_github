package im.threads.business.transport;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import im.threads.business.chat_updates.ChatUpdateProcessorJavaGetter;
import im.threads.business.config.BaseConfig;
import im.threads.business.formatters.ChatItemType;
import im.threads.business.formatters.SpeechStatus;
import im.threads.business.logger.LoggerEdna;
import im.threads.business.models.ChatItem;
import im.threads.business.models.ConsultConnectionMessage;
import im.threads.business.models.ConsultPhrase;
import im.threads.business.models.FileDescription;
import im.threads.business.models.MessageRead;
import im.threads.business.models.MessageStatus;
import im.threads.business.models.QuestionDTO;
import im.threads.business.models.Quote;
import im.threads.business.models.RequestResolveThread;
import im.threads.business.models.ScheduleInfo;
import im.threads.business.models.SearchingConsult;
import im.threads.business.models.SimpleSystemMessage;
import im.threads.business.models.SpeechMessageUpdate;
import im.threads.business.models.Survey;
import im.threads.business.models.UserPhrase;
import im.threads.business.transport.models.Attachment;
import im.threads.business.transport.models.AttachmentSettings;
import im.threads.business.transport.models.MessageContent;
import im.threads.business.transport.models.Operator;
import im.threads.business.transport.models.OperatorJoinedContent;
import im.threads.business.transport.models.RequestResolveThreadContent;
import im.threads.business.transport.models.SpeechMessageUpdatedContent;
import im.threads.business.transport.models.SurveyContent;
import im.threads.business.transport.models.SystemMessageContent;
import im.threads.business.transport.models.TextContent;

public final class MessageParser {
    private MessageParser() {
    }

    /**
     * @return null, если не удалось распознать формат сообщения.
     */
    @Nullable
    public static ChatItem format(String messageId, long sentAt, String shortMessage, JsonObject fullMessage) {
        final ChatItemType type = ChatItemType.fromString(getType(fullMessage));
        switch (type) {
            case CLIENT_BLOCKED:
            case THREAD_ENQUEUED:
            case AVERAGE_WAIT_TIME:
            case PARTING_AFTER_SURVEY:
            case THREAD_CLOSED:
            case THREAD_WILL_BE_REASSIGNED:
            case THREAD_IN_PROGRESS:
                return getSystemMessage(sentAt, fullMessage);
            case OPERATOR_JOINED:
            case OPERATOR_LEFT:
                return getConsultConnection(messageId, sentAt, shortMessage, fullMessage);
            case SCHEDULE:
                return getScheduleInfo(fullMessage);
            case SURVEY:
                return getSurvey(sentAt, fullMessage);
            case REQUEST_CLOSE_THREAD:
                return getRequestResolveThread(sentAt, fullMessage);
            case OPERATOR_LOOKUP_STARTED:
                return new SearchingConsult();
            case NONE:
            case MESSAGES_READ:
                return getMessageRead(fullMessage);
            case SCENARIO:
                return null;
            case ATTACHMENT_SETTINGS:
                AttachmentSettings attachmentSettings = BaseConfig.instance.gson.fromJson(fullMessage, AttachmentSettings.class);
                if (attachmentSettings.getClientId() != null) {
                    new ChatUpdateProcessorJavaGetter().getProcessor().postAttachmentSettings(attachmentSettings);
                }
                return null;
            case SPEECH_MESSAGE_UPDATED:
                SpeechMessageUpdatedContent content = BaseConfig.instance.gson.fromJson(fullMessage, SpeechMessageUpdatedContent.class);
                if (content.getUuid() != null && content.getAttachments() != null) {
                    return new SpeechMessageUpdate(
                            content.getUuid(),
                            SpeechStatus.Companion.fromString(content.getSpeechStatus()),
                            getFileDescription(content.getAttachments(), null, sentAt)
                    );
                } else {
                    LoggerEdna.error("SPEECH_MESSAGE_UPDATED with invalid params");
                }
                return null;
            case MESSAGE:
            case ON_HOLD:
            default:
                return getPhrase(sentAt, shortMessage, fullMessage);
        }
    }

    public static String getType(final JsonObject fullMessage) {
        return fullMessage.get(MessageAttributes.TYPE).getAsString();
    }

    private static MessageRead getMessageRead(final JsonObject fullMessage) {
        return new MessageRead(Arrays.asList(fullMessage.get(MessageAttributes.READ_MESSAGE_ID).getAsString().split(",")));
    }

    private static ConsultConnectionMessage getConsultConnection(final String messageId, final long sentAt, final String shortMessage, final JsonObject fullMessage) {
        OperatorJoinedContent content = BaseConfig.instance.gson.fromJson(fullMessage, OperatorJoinedContent.class);
        Operator operator = content.getOperator();
        return new ConsultConnectionMessage(
                content.getUuid(),
//                messageId,
                String.valueOf(operator.getId()),
                content.getType(),
                operator.getAliasOrName(),
                "male".equalsIgnoreCase(operator.getGender()),
                sentAt,
                operator.getPhotoUrl(),
                operator.getStatus(),
                shortMessage == null ? null : shortMessage.split(" ")[0],
                operator.getOrganizationUnit(),
                operator.getRole(),
                content.isDisplay(),
                content.getText(),
                content.getThreadId()
        );
    }

    private static SimpleSystemMessage getSystemMessage(final long sentAt, final JsonObject fullMessage) {
        SystemMessageContent content = BaseConfig.instance.gson.fromJson(fullMessage, SystemMessageContent.class);
        return new SimpleSystemMessage(content.getUuid(), content.getType(), sentAt, content.getText(), content.getThreadId());
    }

    private static ScheduleInfo getScheduleInfo(final JsonObject fullMessage) {
        TextContent content = BaseConfig.instance.gson.fromJson(fullMessage, TextContent.class);
        ScheduleInfo scheduleInfo = BaseConfig.instance.gson.fromJson(content.getText(), ScheduleInfo.class);
        scheduleInfo.setDate(new Date().getTime());
        return scheduleInfo;
    }

    private static Survey getSurvey(final long sentAt, final JsonObject fullMessage) {
        SurveyContent content = BaseConfig.instance.gson.fromJson(fullMessage, SurveyContent.class);
        Survey survey = BaseConfig.instance.gson.fromJson(content.getText(), Survey.class);
        survey.setUuid(content.getUuid());
        survey.setPhraseTimeStamp(sentAt);
        survey.setSentState(MessageStatus.FAILED);
        survey.setDisplayMessage(true);
        survey.setRead(false);
        for (final QuestionDTO questionDTO : survey.getQuestions()) {
            questionDTO.setPhraseTimeStamp(sentAt);
        }
        return survey;
    }

    private static RequestResolveThread getRequestResolveThread(final long sentAt, final JsonObject fullMessage) {
        RequestResolveThreadContent content = BaseConfig.instance.gson.fromJson(fullMessage, RequestResolveThreadContent.class);
        return new RequestResolveThread(content.getUuid(), content.getHideAfter(), sentAt, content.getThreadId(), false);
    }

    @Nullable
    private static ChatItem getPhrase(final long sentAt, final String shortMessage, final JsonObject fullMessage) {
        MessageContent content = BaseConfig.instance.gson.fromJson(fullMessage, MessageContent.class);
        if (content.getText() == null && content.getAttachments() == null && content.getQuotes() == null) {
            return null;
        }
        Boolean isMessageRead = false;
        if (content.getRead() != null) {
            isMessageRead = content.getRead();
        }
        String phrase;
        if (content.getText() != null) {
            phrase = content.getText();
        } else if (content.getSpeechText() != null) {
            phrase = content.getSpeechText();
        } else {
            phrase = shortMessage;
        }
        Quote quote = null;
        if (content.getQuotes() != null) {
            quote = getQuote(content.getQuotes());
        }
        Operator operator = content.getOperator();
        if (operator != null) {
            String operatorId = String.valueOf(operator.getId());
            String name = operator.getAliasOrName();
            String photoUrl = operator.getPhotoUrl();
            String status = operator.getStatus();
            boolean gender = operator.getGender() == null || "male".equalsIgnoreCase(operator.getGender());
            FileDescription fileDescription = null;
            if (content.getAttachments() != null) {
                fileDescription = getFileDescription(content.getAttachments(), name, sentAt);
            }
            return new ConsultPhrase(
                    content.getUuid(),
                    fileDescription,
                    quote,
                    name,
                    phrase,
                    content.getFormattedText(),
                    sentAt,
                    operatorId,
                    photoUrl,
                    false,
                    status,
                    gender,
                    content.getThreadId(),
                    content.getQuickReplies(),
                    content.getSettings() != null ? content.getSettings().isBlockInput() : null,
                    SpeechStatus.Companion.fromString(content.getSpeechStatus())
            );
        } else {
            FileDescription fileDescription = null;
            if (content.getAttachments() != null) {
                fileDescription = getFileDescription(content.getAttachments(), null, sentAt);
            }
            final UserPhrase userPhrase = new UserPhrase(content.getUuid(), phrase, quote, sentAt, fileDescription, content.getThreadId());
            userPhrase.setSentState(MessageStatus.SENT);
            userPhrase.setRead(isMessageRead);
            return userPhrase;
        }
    }

    private static FileDescription getFileDescription(final List<Attachment> attachments, String from, long timeStamp) {
        FileDescription fileDescription = null;
        if (attachments != null && !attachments.isEmpty() && attachments.get(0) != null) {
            Attachment attachment = attachments.get(0);
            fileDescription = new FileDescription(
                    from,
                    null,
                    attachment.getSize(),
                    timeStamp
            );
            fileDescription.setDownloadPath(attachment.getResult());
            fileDescription.setOriginalPath(attachment.getOriginalUrl());
            fileDescription.setIncomingName(attachment.getName());
            fileDescription.setMimeType(attachment.getType());
            fileDescription.setState(attachment.getState());
            if (attachment.getErrorCode() != null) {
                fileDescription.setErrorCode(attachment.getErrorCodeState());
                fileDescription.setErrorMessage(attachment.getErrorMessage());
            }
        }
        return fileDescription;
    }

    private static Quote getQuote(final List<im.threads.business.transport.models.Quote> quotes) {
        if (quotes != null && !quotes.isEmpty() && quotes.get(0) != null) {
            im.threads.business.transport.models.Quote quote = quotes.get(0);
            String authorName = quote.getOperator() != null ? quote.getOperator().getAliasOrName() : null;
            long timestamp = quote.getReceivedDate() != null ? quote.getReceivedDate().getTime() : System.currentTimeMillis();
            FileDescription fileDescription = null;
            if (quote.getAttachments() != null) {
                fileDescription = getFileDescription(quote.getAttachments(), authorName, timestamp);
            }
            if (quote.getUuid() != null && (quote.getText() != null || fileDescription != null)) {
                return new Quote(quote.getUuid(), authorName, quote.getText(), fileDescription, timestamp);
            }
        }
        return null;
    }
}
