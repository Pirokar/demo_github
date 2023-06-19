package im.threads.business.database.table;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.Locale;

import im.threads.business.models.Quote;

public class QuotesTable extends Table {

    private static final String TABLE_QUOTE = "TABLE_QUOTE";
    private static final String COLUMN_QUOTE_UUID = "COLUMN_QUOTE_UUID";
    private static final String COLUMN_QUOTE_FROM = "COLUMN_QUOTE_HEADER";
    private static final String COLUMN_QUOTE_BODY = "COLUMN_QUOTE_BODY";
    private static final String COLUMN_QUOTE_TIMESTAMP = "COLUMN_QUOTE_TIMESTAMP";
    private static final String COLUMN_QUOTED_BY_MESSAGE_UUID_EXT = "COLUMN_QUOTED_BY_MESSAGE_UUID_EXT";

    private final FileDescriptionsTable fileDescriptionsTable;

    public QuotesTable(FileDescriptionsTable fileDescriptionsTable) {
        this.fileDescriptionsTable = fileDescriptionsTable;
    }

    @Override
    public void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_QUOTE + "("
                + COLUMN_QUOTE_UUID + " text,"
                + COLUMN_QUOTE_FROM + " text, "
                + COLUMN_QUOTE_BODY + " text, "
                + COLUMN_QUOTE_TIMESTAMP + " integer, "
                + COLUMN_QUOTED_BY_MESSAGE_UUID_EXT + " integer)" // message id
        );
    }

    @Override
    public void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUOTE);
    }

    @Override
    public void cleanTable(SQLiteOpenHelper sqlHelper) {
        sqlHelper.getWritableDatabase().execSQL("delete from " + TABLE_QUOTE);
    }

    public Quote getQuote(SQLiteOpenHelper sqlHelper, String quotedByMessageUuid) {
        if (TextUtils.isEmpty(quotedByMessageUuid)) {
            return null;
        }
        String query = String.format(Locale.US, "select * from %s where %s = ?", TABLE_QUOTE, COLUMN_QUOTED_BY_MESSAGE_UUID_EXT);
        try (Cursor c = sqlHelper.getWritableDatabase().rawQuery(query, new String[]{quotedByMessageUuid})) {
            if (c.moveToFirst()) {
                return new Quote(
                        cGetString(c, COLUMN_QUOTE_UUID),
                        cGetString(c, COLUMN_QUOTE_FROM),
                        cGetString(c, COLUMN_QUOTE_BODY),
                        fileDescriptionsTable.getFileDescription(sqlHelper, cGetString(c, COLUMN_QUOTE_UUID)),
                        cGetLong(c, COLUMN_QUOTE_TIMESTAMP)
                );
            }
        }
        return null;
    }
}
