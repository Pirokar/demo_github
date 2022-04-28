package im.threads.internal.transport;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import im.threads.internal.Config;
import im.threads.internal.chat_updates.ChatUpdateProcessor;
import im.threads.internal.formatters.ChatItemType;
import im.threads.internal.formatters.SpeechStatus;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ConsultConnectionMessage;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.MessageRead;
import im.threads.internal.model.MessageState;
import im.threads.internal.model.QuestionDTO;
import im.threads.internal.model.Quote;
import im.threads.internal.model.RequestResolveThread;
import im.threads.internal.model.ScheduleInfo;
import im.threads.internal.model.SearchingConsult;
import im.threads.internal.model.SimpleSystemMessage;
import im.threads.internal.model.SpeechMessageUpdate;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.transport.models.Attachment;
import im.threads.internal.transport.models.AttachmentSettings;
import im.threads.internal.transport.models.MessageContent;
import im.threads.internal.transport.models.Operator;
import im.threads.internal.transport.models.OperatorJoinedContent;
import im.threads.internal.transport.models.RequestResolveThreadContent;
import im.threads.internal.transport.models.SpeechMessageUpdatedContent;
import im.threads.internal.transport.models.SurveyContent;
import im.threads.internal.transport.models.SystemMessageContent;
import im.threads.internal.transport.models.TextContent;
import im.threads.internal.utils.ThreadsLogger;

public final class MessageParser {

    private static final String TAG = MessageParser.class.getSimpleName();

    private MessageParser() {
    }

    /**
     * @return null, если не удалось распознать формат сообщения.
     */
    @Nullable
    public static ChatItem format(String messageId, long sentAt, String shortMessage, JsonObject fullMessage) {
        final ChatItemType type = ChatItemType.fromString(getType(fullMessage));
        switch (type) {
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
            case CLIENT_BLOCKED:
            case SCENARIO:
                return null;
            case ATTACHMENT_SETTINGS:
                AttachmentSettings attachmentSettings = Config.instance.gson.fromJson(fullMessage, AttachmentSettings.class);
                if (attachmentSettings.getClientId() != null) {
                    ChatUpdateProcessor.getInstance().postAttachmentSettings(attachmentSettings);
                }
                return null;
            case SPEECH_MESSAGE_UPDATED:
                SpeechMessageUpdatedContent content = Config.instance.gson.fromJson(fullMessage, SpeechMessageUpdatedContent.class);
                if (content.getUuid() != null && content.getAttachments() != null) {
                    return new SpeechMessageUpdate(
                            content.getUuid(),
                            SpeechStatus.Companion.fromString(content.getSpeechStatus()),
                            getFileDescription(content.getAttachments(), null, sentAt)
                    );
                } else {
                    ThreadsLogger.e(TAG, "SPEECH_MESSAGE_UPDATED with invalid params");
                }
                return null;
            case MESSAGE:
            case ON_HOLD:
            default:
                return getPhrase(messageId, sentAt, shortMessage, fullMessage);
        }
    }

    public static String getType(final JsonObject fullMessage) {
        return fullMessage.get(MessageAttributes.TYPE).getAsString();
    }

    /**
     * @return true если в fullMessage есть поле clientId и оно совпадает с currentClientId
     */
    public static boolean checkId(final JsonObject fullMessage, final String currentClientId) {
        return Config.instance.clientIdIgnoreEnabled ||
                (!TextUtils.isEmpty(currentClientId)
                        && fullMessage != null
                        && fullMessage.has(MessageAttributes.CLIENT_ID)
                        && currentClientId.equalsIgnoreCase(fullMessage.get(MessageAttributes.CLIENT_ID).getAsString())
                );
    }

    private static MessageRead getMessageRead(final JsonObject fullMessage) {
        return new MessageRead(Arrays.asList(fullMessage.get(MessageAttributes.READ_MESSAGE_ID).getAsString().split(",")));
    }

    private static ConsultConnectionMessage getConsultConnection(final String messageId, final long sentAt, final String shortMessage, final JsonObject fullMessage) {
        OperatorJoinedContent content = Config.instance.gson.fromJson(fullMessage, OperatorJoinedContent.class);
        Operator operator = content.getOperator();
        return new ConsultConnectionMessage(
                content.getUuid(),
                messageId,
                content.getProviderIds(),
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
        SystemMessageContent content = Config.instance.gson.fromJson(fullMessage, SystemMessageContent.class);
        return new SimpleSystemMessage(content.getUuid(), content.getType(), sentAt, content.getText(), content.getThreadId());
    }

    private static ScheduleInfo getScheduleInfo(final JsonObject fullMessage) {
        TextContent content = Config.instance.gson.fromJson(fullMessage, TextContent.class);
        ScheduleInfo scheduleInfo = Config.instance.gson.fromJson(content.getText(), ScheduleInfo.class);
        scheduleInfo.setDate(new Date().getTime());
        return scheduleInfo;
    }

    private static Survey getSurvey(final long sentAt, final JsonObject fullMessage) {
        SurveyContent content = Config.instance.gson.fromJson(fullMessage, SurveyContent.class);
        Survey survey = Config.instance.gson.fromJson(content.getText(), Survey.class);
        survey.setUuid(content.getUuid());
        survey.setPhraseTimeStamp(sentAt);
        survey.setSentState(MessageState.STATE_NOT_SENT);
        survey.setDisplayMessage(true);
        survey.setRead(false);
        for (final QuestionDTO questionDTO : survey.getQuestions()) {
            questionDTO.setPhraseTimeStamp(sentAt);
        }
        return survey;
    }

    private static RequestResolveThread getRequestResolveThread(final long sentAt, final JsonObject fullMessage) {
        RequestResolveThreadContent content = Config.instance.gson.fromJson(fullMessage, RequestResolveThreadContent.class);
        return new RequestResolveThread(content.getUuid(), content.getHideAfter(), sentAt, content.getThreadId(), false);
    }

    @Nullable
    private static ChatItem getPhrase(final String messageId, final long sentAt, final String shortMessage, final JsonObject fullMessage) {
        MessageContent content = Config.instance.gson.fromJson(fullMessage, MessageContent.class);
        if (content.getText() == null && content.getAttachments() == null && content.getQuotes() == null) {
            return null;
        }
        String phrase = content.getText() != null ? content.getText() : shortMessage;
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
                    messageId,
                    content.getProviderIds(),
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
                    content.getSettings() != null ? content.getSettings().isBlockInput() : !Config.instance.getChatStyle().inputEnabledDuringQuickReplies,
                    SpeechStatus.Companion.fromString(content.getSpeechStatus())
            );
        } else {
            FileDescription fileDescription = null;
            if (content.getAttachments() != null) {
                fileDescription = getFileDescription(content.getAttachments(), null, sentAt);
            }
            final UserPhrase userPhrase = new UserPhrase(content.getUuid(), messageId, phrase, quote, sentAt, fileDescription, content.getThreadId());
            userPhrase.setSentState(MessageState.STATE_SENT);
            return userPhrase;
        }
    }

    private static FileDescription getFileDescription(final List<Attachment> attachments, String from, long timeStamp) {
        FileDescription fileDescription = null;
        if (!attachments.isEmpty() && attachments.get(0) != null) {
            Attachment attachment = attachments.get(0);
            fileDescription = new FileDescription(
                    from,
                    null,
                    attachment.getSize(),
                    timeStamp
            );
            fileDescription.setDownloadPath(attachment.getResult());
            fileDescription.setIncomingName(attachment.getName());
            fileDescription.setSelfie(attachment.isSelfie());
            fileDescription.setMimeType(attachment.getType());
            fileDescription.setState(attachment.getState());
            if (attachment.getErrorCode() != null) {
                fileDescription.setErrorCode(attachment.getErrorCode());
                fileDescription.setErrorMessage(attachment.getErrorMessage());
            }
        }
        return fileDescription;
    }

    private static Quote getQuote(final List<im.threads.internal.transport.models.Quote> quotes) {
        if (!quotes.isEmpty() && quotes.get(0) != null) {
            im.threads.internal.transport.models.Quote quote = quotes.get(0);
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
