package im.threads.internal.database.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import im.threads.internal.model.FileDescription;

public class FileDescriptionsTable extends Table {

    private static final String TABLE_FILE_DESCRIPTION = "TABLE_FILE_DESCRIPTION";
    private static final String COLUMN_FD_FROM = "COLUMN_FD_FROM";
    private static final String COLUMN_FD_PATH = "COLUMN_FD_PATH";
    private static final String COLUMN_FD_URL = "COLUMN_URL";
    private static final String COLUMN_FD_DOWNLOAD_PROGRESS = "COLUMN_FD_DOWNLOAD_PROGRESS";
    private static final String COLUMN_FD_TIMESTAMP = "COLUMN_FD_TIMESTAMP";
    private static final String COLUMN_FD_SIZE = "COLUMN_FD_SIZE";
    private static final String COLUMN_FD_IS_FROM_QUOTE = "COLUMN_FD_IS_FROM_QUOTE";
    private static final String COLUMN_FD_FILENAME = "COLUMN_FD_FILENAME";
    private static final String COLUMN_FD_MESSAGE_UUID_EXT = "COLUMN_FD_MESSAGE_UUID_EXT";
    private static final String COLUMN_FD_SELFIE = "COLUMN_FD_SELFIE";

    @Override
    public void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_FILE_DESCRIPTION + " ( "
                + COLUMN_FD_FROM + " text, "
                + COLUMN_FD_PATH + " text, "
                + COLUMN_FD_TIMESTAMP + " integer, "
                + COLUMN_FD_MESSAGE_UUID_EXT + " integer, "
                + COLUMN_FD_URL + " text, "
                + COLUMN_FD_SIZE + " integer, "
                + COLUMN_FD_IS_FROM_QUOTE + " integer, "
                + COLUMN_FD_FILENAME + " text,"
                + COLUMN_FD_DOWNLOAD_PROGRESS + " integer, "
                + COLUMN_FD_SELFIE + " integer)"
        );
    }

    @Override
    public void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILE_DESCRIPTION);
    }

    @Override
    public void cleanTable(SQLiteOpenHelper sqlHelper) {
        sqlHelper.getWritableDatabase().execSQL("delete from " + TABLE_FILE_DESCRIPTION);
    }

    @Nullable
    public FileDescription getFileDescription(SQLiteOpenHelper sqlHelper, String messageUuid) {
        if (TextUtils.isEmpty(messageUuid)) {
            return null;
        }
        String query = String.format(Locale.US, "select * from %s where %s = ?", TABLE_FILE_DESCRIPTION, COLUMN_FD_MESSAGE_UUID_EXT);
        try (Cursor c = sqlHelper.getWritableDatabase().rawQuery(query, new String[]{messageUuid})) {
            if (!c.moveToFirst()) {
                return null;
            }
            FileDescription fd = new FileDescription(
                    cGetString(c, COLUMN_FD_FROM),
                    cGetString(c, COLUMN_FD_PATH),
                    cGetLong(c, COLUMN_FD_SIZE),
                    cGetLong(c, COLUMN_FD_TIMESTAMP)
            );
            fd.setDownloadProgress(cGetInt(c, COLUMN_FD_DOWNLOAD_PROGRESS));
            fd.setDownloadPath(cGetString(c, COLUMN_FD_URL));
            fd.setSelfie(cGetBool(c, COLUMN_FD_SELFIE));
            fd.setIncomingName(cGetString(c, COLUMN_FD_FILENAME));
            return fd;
        }
    }

    public void putFileDescription(SQLiteOpenHelper sqlHelper, FileDescription fileDescription, String fdMessageUuid, boolean isFromQuote) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_FD_MESSAGE_UUID_EXT, fdMessageUuid);
        cv.put(COLUMN_FD_FROM, fileDescription.getFrom());
        if (!TextUtils.isEmpty(fileDescription.getFilePath())) {
            cv.put(COLUMN_FD_PATH, fileDescription.getFilePath());
        }
        cv.put(COLUMN_FD_URL, fileDescription.getDownloadPath());
        cv.put(COLUMN_FD_TIMESTAMP, fileDescription.getTimeStamp());
        cv.put(COLUMN_FD_SIZE, fileDescription.getSize());
        cv.put(COLUMN_FD_DOWNLOAD_PROGRESS, fileDescription.getDownloadProgress());
        cv.put(COLUMN_FD_IS_FROM_QUOTE, isFromQuote);
        cv.put(COLUMN_FD_FILENAME, fileDescription.getIncomingName());
        cv.put(COLUMN_FD_SELFIE, fileDescription.isSelfie());
        String sql = "select " + COLUMN_FD_MESSAGE_UUID_EXT +
                " from " + TABLE_FILE_DESCRIPTION
                + " where " + COLUMN_FD_MESSAGE_UUID_EXT + " = ?";
        String[] selectionArgs = new String[]{fdMessageUuid};
        try (Cursor c = sqlHelper.getWritableDatabase().rawQuery(sql, selectionArgs)) {
            boolean existsInDb = c.getCount() > 0;
            if (existsInDb) {
                sqlHelper.getWritableDatabase().update(TABLE_FILE_DESCRIPTION, cv,
                        COLUMN_FD_MESSAGE_UUID_EXT + " = ? ", new String[]{fdMessageUuid});
            } else {
                sqlHelper.getWritableDatabase().insert(TABLE_FILE_DESCRIPTION, null, cv);
            }
        }
    }

    public List<FileDescription> getAllFileDescriptions(SQLiteOpenHelper sqlHelper) {
        String query = String.format(Locale.US, "select * from %s order by %s desc", TABLE_FILE_DESCRIPTION, COLUMN_FD_TIMESTAMP);
        List<FileDescription> list = new ArrayList<>();
        try (Cursor c = sqlHelper.getWritableDatabase().rawQuery(query, new String[]{})) {
            if (!c.moveToFirst()) {
                return list;
            }
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                FileDescription fd = new FileDescription(
                        cGetString(c, COLUMN_FD_FROM),
                        cGetString(c, COLUMN_FD_PATH),
                        cGetLong(c, COLUMN_FD_SIZE),
                        cGetLong(c, COLUMN_FD_TIMESTAMP)
                );
                fd.setDownloadProgress(cGetInt(c, COLUMN_FD_DOWNLOAD_PROGRESS));
                fd.setIncomingName(cGetString(c, COLUMN_FD_FILENAME));
                fd.setDownloadPath(cGetString(c, COLUMN_FD_URL));
                fd.setSelfie(cGetBool(c, COLUMN_FD_SELFIE));
                list.add(fd);
            }
            return list;
        }
    }

    public void updateFileDescription(SQLiteOpenHelper sqlHelper, @NonNull FileDescription fileDescription) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_FD_FROM, fileDescription.getFrom());
        cv.put(COLUMN_FD_PATH, fileDescription.getFilePath());
        cv.put(COLUMN_FD_URL, fileDescription.getDownloadPath());
        cv.put(COLUMN_FD_TIMESTAMP, fileDescription.getTimeStamp());
        cv.put(COLUMN_FD_SIZE, fileDescription.getSize());
        cv.put(COLUMN_FD_DOWNLOAD_PROGRESS, fileDescription.getDownloadProgress());
        cv.put(COLUMN_FD_FILENAME, fileDescription.getIncomingName());
        cv.put(COLUMN_FD_SELFIE, fileDescription.isSelfie());
        sqlHelper.getWritableDatabase().update(TABLE_FILE_DESCRIPTION, cv,
                "" + COLUMN_FD_FILENAME
                        + " like ? and " + COLUMN_FD_URL + " like ?",
                new String[]{fileDescription.getIncomingName(), fileDescription.getDownloadPath()});
    }

}
