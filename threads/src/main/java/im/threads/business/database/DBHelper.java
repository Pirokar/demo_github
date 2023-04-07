package im.threads.business.database;

import androidx.annotation.NonNull;

import java.util.List;

import im.threads.business.models.ChatItem;
import im.threads.business.models.FileDescription;

public interface DBHelper {

    void cleanDatabase();

    @NonNull
    List<ChatItem> getChatItems(int offset, int limit);

    List<FileDescription> getAllFileDescriptions();
}
