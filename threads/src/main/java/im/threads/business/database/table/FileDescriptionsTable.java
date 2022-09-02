package im.threads.business.database.table;

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

import im.threads.business.models.FileDescription;
import im.threads.business.models.enums.AttachmentStateEnum;
import im.threads.business.models.enums.ErrorStateEnum;
import im.threads.business.utils.FileUtils;

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
    private static final String COLUMN_FD_MIME_TYPE = "COLUMN_FD_MIME_TYPE";
    private static final String COLUMN_FD_MESSAGE_UUID_EXT = "COLUMN_FD_MESSAGE_UUID_EXT";
    private static final String COLUMN_FD_ATTACHMENT_STATE = "ATTACHMENT_STATE";
    private static final String COLUMN_FD_ERROR_CODE = "ERROR_CODE";
    private static final String COLUMN_FD_ERROR_MESSAGE = "ERROR_MESSAGE";

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
                + COLUMN_FD_MIME_TYPE + " text,"
                + COLUMN_FD_DOWNLOAD_PROGRESS + " integer, "
                + COLUMN_FD_ATTACHMENT_STATE + " text, "
                + COLUMN_FD_ERROR_CODE + " text, "
                + COLUMN_FD_ERROR_MESSAGE + " text )"
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
                    FileUtils.safeParse(cGetString(c, COLUMN_FD_PATH)),
                    cGetLong(c, COLUMN_FD_SIZE),
                    cGetLong(c, COLUMN_FD_TIMESTAMP)
            );
            fd.setDownloadProgress(cGetInt(c, COLUMN_FD_DOWNLOAD_PROGRESS));
            fd.setDownloadPath(cGetString(c, COLUMN_FD_URL));
            fd.setIncomingName(cGetString(c, COLUMN_FD_FILENAME));
            fd.setMimeType(cGetString(c, COLUMN_FD_MIME_TYPE));
            fd.setState(AttachmentStateEnum.attachmentStateEnumFromString(cGetString(c, COLUMN_FD_ATTACHMENT_STATE)));
            fd.setErrorCode(ErrorStateEnum.errorStateEnumFromString(cGetString(c, COLUMN_FD_ERROR_CODE)));
            fd.setErrorMessage(cGetString(c, COLUMN_FD_ERROR_MESSAGE));
            fd.setMimeType(cGetString(c, COLUMN_FD_MIME_TYPE));
            return fd;
        }
    }

    public void putFileDescription(SQLiteOpenHelper sqlHelper, FileDescription fileDescription, String fdMessageUuid, boolean isFromQuote) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_FD_MESSAGE_UUID_EXT, fdMessageUuid);
        cv.put(COLUMN_FD_FROM, fileDescription.getFrom());
        if (fileDescription.getFileUri() != null) {
            cv.put(COLUMN_FD_PATH, fileDescription.getFileUri().toString());
        }
        cv.put(COLUMN_FD_URL, fileDescription.getDownloadPath());
        cv.put(COLUMN_FD_TIMESTAMP, fileDescription.getTimeStamp());
        cv.put(COLUMN_FD_SIZE, fileDescription.getSize());
        cv.put(COLUMN_FD_IS_FROM_QUOTE, isFromQuote);
        cv.put(COLUMN_FD_FILENAME, fileDescription.getIncomingName());
        cv.put(COLUMN_FD_MIME_TYPE, fileDescription.getMimeType());
        cv.put(COLUMN_FD_ATTACHMENT_STATE, fileDescription.getState().getState());
        cv.put(COLUMN_FD_ERROR_CODE, fileDescription.getErrorCode().getState());
        cv.put(COLUMN_FD_ERROR_MESSAGE, fileDescription.getErrorMessage());
        String sql = "select " + COLUMN_FD_MESSAGE_UUID_EXT + " and " + COLUMN_FD_PATH +
                " from " + TABLE_FILE_DESCRIPTION
                + " where " + COLUMN_FD_MESSAGE_UUID_EXT + " = ?";
        String[] selectionArgs = new String[]{fdMessageUuid};
        try (Cursor c = sqlHelper.getWritableDatabase().rawQuery(sql, selectionArgs)) {
            boolean existsInDb = c.getCount() > 0;
            if (existsInDb) {
                c.moveToFirst();
                String localPath = cGetString(c, COLUMN_FD_PATH);
                if (TextUtils.isEmpty(localPath)) {
                    cv.put(COLUMN_FD_DOWNLOAD_PROGRESS, fileDescription.getDownloadProgress());
                }
                sqlHelper.getWritableDatabase().update(TABLE_FILE_DESCRIPTION, cv,
                        COLUMN_FD_MESSAGE_UUID_EXT + " = ? ", new String[]{fdMessageUuid});
            } else {
                cv.put(COLUMN_FD_DOWNLOAD_PROGRESS, fileDescription.getDownloadProgress());
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
                        FileUtils.safeParse(cGetString(c, COLUMN_FD_PATH)),
                        cGetLong(c, COLUMN_FD_SIZE),
                        cGetLong(c, COLUMN_FD_TIMESTAMP)
                );
                fd.setDownloadProgress(cGetInt(c, COLUMN_FD_DOWNLOAD_PROGRESS));
                fd.setIncomingName(cGetString(c, COLUMN_FD_FILENAME));
                fd.setMimeType(cGetString(c, COLUMN_FD_MIME_TYPE));
                fd.setDownloadPath(cGetString(c, COLUMN_FD_URL));
                fd.setState(AttachmentStateEnum.attachmentStateEnumFromString(cGetString(c, COLUMN_FD_ATTACHMENT_STATE)));
                fd.setErrorCode(ErrorStateEnum.errorStateEnumFromString(cGetString(c, COLUMN_FD_ERROR_CODE)));
                fd.setErrorMessage(cGetString(c, COLUMN_FD_ERROR_MESSAGE));
                list.add(fd);
            }
            return list;
        }
    }

    public void updateFileDescription(SQLiteOpenHelper sqlHelper, @NonNull FileDescription fileDescription) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_FD_FROM, fileDescription.getFrom());
        cv.put(COLUMN_FD_PATH, fileDescription.getFileUri() != null ? fileDescription.getFileUri().toString() : null);
        cv.put(COLUMN_FD_URL, fileDescription.getDownloadPath());
        cv.put(COLUMN_FD_TIMESTAMP, fileDescription.getTimeStamp());
        cv.put(COLUMN_FD_SIZE, fileDescription.getSize());
        cv.put(COLUMN_FD_DOWNLOAD_PROGRESS, fileDescription.getDownloadProgress());
        cv.put(COLUMN_FD_FILENAME, fileDescription.getIncomingName());
        cv.put(COLUMN_FD_MIME_TYPE, fileDescription.getMimeType());
        cv.put(COLUMN_FD_ATTACHMENT_STATE, fileDescription.getState().getState());
        cv.put(COLUMN_FD_ERROR_CODE, fileDescription.getErrorCode().getState());
        cv.put(COLUMN_FD_ERROR_MESSAGE, fileDescription.getErrorMessage());

        sqlHelper.getWritableDatabase().update(TABLE_FILE_DESCRIPTION, cv,
                "" + COLUMN_FD_FILENAME
                        + " like ? and " + COLUMN_FD_URL + " like ?",
                new String[]{fileDescription.getIncomingName(), fileDescription.getDownloadPath()});
    }

}
