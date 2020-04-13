package im.threads.internal.database.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import im.threads.internal.model.QuickReply;
import im.threads.internal.utils.ThreadsLogger;

public class QuickRepliesTable extends Table {

    private static final String TAG = QuickRepliesTable.class.getCanonicalName();

    private static final String TABLE_QUICK_REPLIES = "TABLE_QUICK_REPLIES";
    private static final String COLUMN_QUICK_REPLIES_ID = "COLUMN_ID";
    private static final String COLUMN_QUICK_REPLIES_SERVER_ID = "COLUMN_SERVER_ID";
    private static final String COLUMN_QUICK_REPLIES_MESSAGE_UUID = "COLUMN_MESSAGE_UUID";
    private static final String COLUMN_QUICK_REPLIES_TYPE = "COLUMN_TYPE";
    private static final String COLUMN_QUICK_REPLIES_TEXT = "COLUMN_TEXT";
    private static final String COLUMN_QUICK_REPLIES_IMAGE_URL = "COLUMN_IMAGE_URL";
    private static final String COLUMN_QUICK_REPLIES_URL = "COLUMN_URL";

    @Override
    public void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_QUICK_REPLIES + "("
                + COLUMN_QUICK_REPLIES_ID + " integer primary key autoincrement, "
                + COLUMN_QUICK_REPLIES_SERVER_ID + " integer, "
                + COLUMN_QUICK_REPLIES_MESSAGE_UUID + " string, "
                + COLUMN_QUICK_REPLIES_TYPE + " text, "
                + COLUMN_QUICK_REPLIES_TEXT + " text, "
                + COLUMN_QUICK_REPLIES_IMAGE_URL + " text, "
                + COLUMN_QUICK_REPLIES_URL + " text "
                + ")");
    }

    @Override
    public void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUICK_REPLIES);
    }

    @Override
    public void cleanTable(SQLiteOpenHelper sqlHelper) {
        sqlHelper.getWritableDatabase().execSQL("delete from " + TABLE_QUICK_REPLIES);
    }

    public List<QuickReply> getQuickReplies(SQLiteOpenHelper sqlHelper, String messageUUID) {
        List<QuickReply> items = new ArrayList<>();
        String query = "select * from " + TABLE_QUICK_REPLIES + " where " + COLUMN_QUICK_REPLIES_MESSAGE_UUID + " = ?";
        try (Cursor c = sqlHelper.getWritableDatabase().rawQuery(query, new String[]{String.valueOf(messageUUID)})) {
            if (c.getCount() == 0) {
                return items;
            }
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                QuickReply quickReply = new QuickReply();
                quickReply.setId(cGetInt(c, COLUMN_QUICK_REPLIES_SERVER_ID));
                quickReply.setType(cGetString(c, COLUMN_QUICK_REPLIES_TYPE));
                quickReply.setText(cGetString(c, COLUMN_QUICK_REPLIES_TEXT));
                quickReply.setImageUrl(cGetString(c, COLUMN_QUICK_REPLIES_IMAGE_URL));
                quickReply.setUrl(cGetString(c, COLUMN_QUICK_REPLIES_URL));
            }
            return items;
        }
    }

    public void putQuickReplies(SQLiteOpenHelper sqlHelper, String messageUUID, List<QuickReply> quickReplies) {
        try {
            sqlHelper.getWritableDatabase().beginTransaction();
            String deleteQuickRepliesSql = "delete from " + TABLE_QUICK_REPLIES
                    + " where " + COLUMN_QUICK_REPLIES_MESSAGE_UUID + " = ? ";
            sqlHelper.getWritableDatabase().execSQL(deleteQuickRepliesSql, new String[]{String.valueOf(messageUUID)});
            for (QuickReply item : quickReplies) {
                putQuickReply(sqlHelper, messageUUID, item);
            }
            sqlHelper.getWritableDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            ThreadsLogger.e(TAG, "putQuickReplies", e);
        } finally {
            sqlHelper.getWritableDatabase().endTransaction();
        }
    }

    private void putQuickReply(SQLiteOpenHelper sqlHelper, String messageUUID, QuickReply quickReply) {
        ContentValues quickReplyCv = new ContentValues();
        quickReplyCv.put(COLUMN_QUICK_REPLIES_SERVER_ID, quickReply.getId());
        quickReplyCv.put(COLUMN_QUICK_REPLIES_SERVER_ID, messageUUID);
        quickReplyCv.put(COLUMN_QUICK_REPLIES_TYPE, quickReply.getType());
        quickReplyCv.put(COLUMN_QUICK_REPLIES_TEXT, quickReply.getText());
        quickReplyCv.put(COLUMN_QUICK_REPLIES_IMAGE_URL, quickReply.getImageUrl());
        quickReplyCv.put(COLUMN_QUICK_REPLIES_URL, quickReply.getUrl());
        sqlHelper.getWritableDatabase().insert(TABLE_QUICK_REPLIES, null, quickReplyCv);
    }

}
