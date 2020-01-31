package im.threads.internal.transport;

import android.text.TextUtils;

import com.google.gson.JsonObject;

import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import im.threads.internal.Config;
import im.threads.internal.formatters.ChatItemType;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ConsultConnectionMessage;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.MessageState;
import im.threads.internal.model.QuestionDTO;
import im.threads.internal.model.Quote;
import im.threads.internal.model.RequestResolveThread;
import im.threads.internal.model.ScheduleInfo;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.transport.models.Attachment;
import im.threads.internal.transport.models.MessageContent;
import im.threads.internal.transport.models.Operator;
import im.threads.internal.transport.models.OperatorJoinedContent;
import im.threads.internal.transport.models.RequestResolveThreadContent;
import im.threads.internal.transport.models.TextContent;

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
            case OPERATOR_JOINED:
            case OPERATOR_LEFT:
                return getConsultConnection(messageId, sentAt, shortMessage, fullMessage);
            case SCHEDULE:
                return getScheduleInfo(fullMessage);
            case SURVEY:
                return getRating(sentAt, fullMessage);
            case REQUEST_CLOSE_THREAD:
                return getRequestResolveThread(fullMessage);
            case NONE:
            case MESSAGES_READ:
            case OPERATOR_LOOKUP_STARTED:
            case CLIENT_BLOCKED:
            case SCENARIO:
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
        return !TextUtils.isEmpty(currentClientId)
                && fullMessage != null
                && fullMessage.has(MessageAttributes.CLIENT_ID)
                && currentClientId.equalsIgnoreCase(fullMessage.get(MessageAttributes.CLIENT_ID).getAsString());
    }

    private static ConsultConnectionMessage getConsultConnection(final String messageId, final long sentAt, final String shortMessage, final JsonObject fullMessage) {
        OperatorJoinedContent content = Config.instance.gson.fromJson(fullMessage, OperatorJoinedContent.class);
        Operator operator = content.getOperator();
        return new ConsultConnectionMessage(
                content.getUuid(),
                messageId,
                String.valueOf(operator.getId()),
                content.getType(),
                operator.getAliasOrName(),
                "male".equalsIgnoreCase(operator.getGender()),
                sentAt,
                operator.getPhotoUrl(),
                operator.getStatus(),
                shortMessage == null ? null : shortMessage.split(" ")[0],
                operator.getOrganizationUnit(),
                content.isDisplay()
        );
    }

    private static ScheduleInfo getScheduleInfo(final JsonObject fullMessage) {
        TextContent content = Config.instance.gson.fromJson(fullMessage, TextContent.class);
        ScheduleInfo scheduleInfo = Config.instance.gson.fromJson(content.getText(), ScheduleInfo.class);
        scheduleInfo.setDate(new Date().getTime());
        return scheduleInfo;
    }

    private static Survey getRating(final long sentAt, final JsonObject fullMessage) {
        TextContent content = Config.instance.gson.fromJson(fullMessage, TextContent.class);
        Survey survey = Config.instance.gson.fromJson(content.getText(), Survey.class);
        survey.setPhraseTimeStamp(sentAt);
        survey.setSentState(MessageState.STATE_NOT_SENT);
        for (final QuestionDTO questionDTO : survey.getQuestions()) {
            questionDTO.setPhraseTimeStamp(sentAt);
        }
        return survey;
    }

    private static RequestResolveThread getRequestResolveThread(final JsonObject fullMessage) {
        RequestResolveThreadContent content = Config.instance.gson.fromJson(fullMessage, RequestResolveThreadContent.class);
        return content.getHideAfter() > 0 ? new RequestResolveThread(content.getHideAfter(), System.currentTimeMillis()) : null;
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
                    fileDescription,
                    quote,
                    name,
                    phrase,
                    sentAt,
                    operatorId,
                    photoUrl,
                    false,
                    status,
                    gender
            );
        } else {
            FileDescription fileDescription = null;
            if (content.getAttachments() != null) {
                fileDescription = getFileDescription(content.getAttachments(), null, sentAt);
            }
            final UserPhrase userPhrase = new UserPhrase(content.getUuid(), messageId, phrase, quote, sentAt, fileDescription);
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
