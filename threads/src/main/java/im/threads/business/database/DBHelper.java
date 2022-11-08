package im.threads.business.database;

import androidx.annotation.NonNull;

import java.util.List;

import im.threads.business.models.ChatItem;
import im.threads.business.models.ConsultInfo;
import im.threads.business.models.ConsultPhrase;
import im.threads.business.models.FileDescription;
import im.threads.business.models.MessageState;
import im.threads.business.models.Survey;
import im.threads.business.models.UserPhrase;

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

    void setMessageWasRead(String uuid);

    Survey getSurvey(long sendingId);

    int setNotSentSurveyDisplayMessageToFalse();

    int setOldRequestResolveThreadDisplayMessageToFalse();

    int getMessagesCount();

    int getUnreadMessagesCount();

    List<String> getUnreadMessagesUuid();

}
