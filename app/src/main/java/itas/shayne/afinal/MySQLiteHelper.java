package itas.shayne.afinal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Updated by croftd on 3/10/2021.
 * Note this is adapted from the Vogella Tutorial at:
 * http://www.vogella.com/tutorials/AndroidSQLite/article.html#sqliteoverview_sqlitedatabase
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_COMMENTS = "comments";
    public static final String COLUMN_ID = "_id";

    // EDIT HERE FOR NAME

    public static final String COLUMN_COMMENT = "comment";

    private static final String DATABASE_NAME = "commments.db";

    // EDIT HERE - you need to increment this number to force the database to be re-created
    private static final int DATABASE_VERSION = 2;

    // Database creation sql statement
    // EDIT HERE - You need to included COLUMN_NAME
    private static final String DATABASE_CREATE = "create table "
            + TABLE_COMMENTS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_COMMENT
            + " text not null);";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.d(MainActivity.DEBUG_TAG, "Creating db: " + DATABASE_CREATE);
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);
        onCreate(db);
    }
}


