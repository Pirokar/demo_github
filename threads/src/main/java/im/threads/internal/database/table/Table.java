package im.threads.internal.database.table;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public abstract class Table {

    public abstract void createTable(SQLiteDatabase db);

    public abstract void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion);

    public abstract void cleanTable(SQLiteOpenHelper sqlHelper);

    static boolean cIsNull(Cursor c, String columnName) {
        return c.isNull(c.getColumnIndex(columnName));
    }

    static boolean cGetBool(Cursor c, String columnName) {
        return cGetInt(c, columnName) == 1;
    }

    @Nullable
    static String cGetString(Cursor c, String columnName) {
        return cIsNull(c, columnName) ? null : c.getString(c.getColumnIndex(columnName));
    }

    static long cGetLong(Cursor c, String columnName) {
        return c.getLong(c.getColumnIndex(columnName));
    }

    static int cGetInt(Cursor c, String columnName) {
        return cIsNull(c, columnName) ? 0 : c.getInt(c.getColumnIndex(columnName));
    }

}
