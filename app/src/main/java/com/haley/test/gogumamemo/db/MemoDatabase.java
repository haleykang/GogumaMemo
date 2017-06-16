package com.haley.test.gogumamemo.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.haley.test.gogumamemo.BasicInfo;

/**
 * Created by 202-18 on 2017-06-14.
 */

public class MemoDatabase {
    public static final String TAG = "MemoDatabase";
    private static MemoDatabase database;
    public static String TABLE_MEMO = "MEMO";
    public static String TABLE_PHOTO = "PHOTO";
    public static int DATABASE_VERSION = 1;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private Context context;

    private MemoDatabase(Context context) {
        this.context = context;
    }

    public static MemoDatabase getInstance(Context context) {
        if(database == null) {
            database = new MemoDatabase(context);
        }
        return database;
    } // end of getInstance();

    public boolean open() {
        println("opening database [" + BasicInfo.DATABASE_NAME + "].");
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        return true;

    } // end of open()

    public void close() {
        println("closing database [" + BasicInfo.DATABASE_NAME + "].");
        db.close();
        database = null;
    } // end of close()

    public Cursor rawQuery(String SQL) {

        println("\nexecuteQuery called.\n");
        Cursor c1 = null;
        try {
            c1 = db.rawQuery(SQL, null);
            println("cursor count : " + c1.getCount());
        } catch(Exception e) {
            Log.e(TAG, "Exception in executeQuery", e);
        }
        return c1;
    } // end of rawQuery()

    public boolean execSQL(String SQL) {

        println("\nexecute called.\n");
        try {
            println("SQL : " + SQL);
            db.execSQL(SQL);

        } catch(Exception e) {
            Log.e(TAG, "Exception in executeQuery", e);
        }
        return false;
    } // end of execSQL()

    // 헬퍼 클래스
    private class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, BasicInfo.DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            println("creating database [" + BasicInfo.DATABASE_NAME + "].");
            println("creating table [" + TABLE_MEMO + "].");
            String DROP_SQL = "drop table if exists " + TABLE_MEMO;
            // drop table
            try {
                db.execSQL(DROP_SQL);
            } catch(Exception e) {
                Log.e(TAG, "Exception in DROP_SQL", e);
            }
            // create table
            String CREATE_SQL = "create table " + TABLE_MEMO + "("
                    + "  _id INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + "  INPUT_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "  CONTENT_TEXT TEXT DEFAULT '', "
                    + "  ID_PHOTO INTEGER, "
                    + "  CREATE_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP "
                    + ")";
            try {
                db.execSQL(CREATE_SQL);
            } catch(Exception e) {
                Log.e(TAG, "Exception int CREATE_SQL", e);
            }

            // TABLE_PHOTO
            println("creating table [" + TABLE_PHOTO + "].");
            // drop existing table
            DROP_SQL = "drop table if exists " + TABLE_PHOTO;
            try {
                db.execSQL(DROP_SQL);
            } catch(Exception e) {
                Log.e(TAG, "Exception in DROP_SQL", e);
            }

            CREATE_SQL = "create table " + TABLE_PHOTO + "("
                    + "  _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + "  URI TEXT, "
                    + "  CREATE_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP "
                    + ")";

            try {
                db.execSQL(CREATE_SQL);
            } catch(Exception e) {
                Log.e(TAG, "Exception int CREATE_SQL", e);
            }

            // create index
            String CREATE_INDEX_SQL = "create index " + TABLE_PHOTO + "_IDX ON " + TABLE_PHOTO + "("
                    + "URI"
                    + ")";
            try {
                db.execSQL(CREATE_INDEX_SQL);
            } catch(Exception e) {
                Log.e(TAG, "Exception int CREATE_INDEX_SQL", e);
            }

        } // end of onCreat()

        @Override
        public void onOpen(SQLiteDatabase db) {
            // super.onOpen(db);
            println("opened database [" + BasicInfo.DATABASE_NAME + "].");

        } // onOpen()

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            println("Upgrading database from version " + oldVersion + " to " + newVersion + ".");
        } // onUpgrade()
    } // end of DatabaseHelper 클래스

    private void println(String msg) {
        Log.d(TAG, msg);
    }

}
