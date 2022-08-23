package im.threads.internal.database;

import androidx.annotation.NonNull;

import java.util.List;

import im.threads.business.models.ChatItem;
import im.threads.internal.model.ConsultInfo;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.MessageState;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UserPhrase;

public interface DBHelper {

    void cleanDatabase();

    @NonNull
    List<ChatItem> getChatItems(int offset, int limit);

    ChatItem getChatItem(String messageUuid);

    void putChatItems(final List<ChatItem> items);

    boolean putChatItem(ChatItem chatItem);

    List<FileDescription> getAllFileDescriptions();

    void updateFileDescription(@NonNull FileDescription fileDescription);

    ConsultInfo getLastConsultInfo(@NonNull String id);

    List<UserPhrase> getUnsendUserPhrase(int count);

    void setUserPhraseStateByProviderId(String providerId, MessageState messageState);

    ConsultPhrase getLastConsultPhrase();

    int setAllConsultMessagesWereRead();

    void setMessageWasRead(String providerId);

    Survey getSurvey(long sendingId);

    int setNotSentSurveyDisplayMessageToFalse();

    int setOldRequestResolveThreadDisplayMessageToFalse();

    int getMessagesCount();

    int getUnreadMessagesCount();

    List<String> getUnreadMessagesUuid();

}
