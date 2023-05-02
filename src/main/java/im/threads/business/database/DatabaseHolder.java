package im.threads.business.database;

import androidx.annotation.NonNull;

import java.util.List;

import im.threads.business.config.BaseConfig;
import im.threads.business.models.ChatItem;
import im.threads.business.models.FileDescription;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public final class DatabaseHolder {

    private static DatabaseHolder instance;
    private final ThreadsDbHelper mMyOpenHelper;

    private DatabaseHolder() {
        mMyOpenHelper = new ThreadsDbHelper(BaseConfig.instance.context);
    }

    @NonNull
    public static DatabaseHolder getInstance() {
        if (instance == null) {
            instance = new DatabaseHolder();
        }
        return instance;
    }

    @NonNull
    public List<ChatItem> getChatItems(int offset, int limit) {
        return mMyOpenHelper.getChatItems(offset, limit);
    }

    public Single<List<FileDescription>> getAllFileDescriptions() {
        return Single.fromCallable(mMyOpenHelper::getAllFileDescriptions)
                .subscribeOn(Schedulers.io());
    }
}
