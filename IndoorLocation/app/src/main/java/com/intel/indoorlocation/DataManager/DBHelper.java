package com.intel.indoorlocation.DataManager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by qixing on 17-8-4.
 *
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "myTest.db";

    private static DBHelper mMe;

    private DBHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    public static void init(Context context) {
        mMe = new DBHelper(context);
    }

    public static void destroy() {
        mMe = null;
    }

    public static DBHelper instance(Context context) {
        if (mMe == null) {
            mMe.init(context);
            //throw new RuntimeException("NOT INIT");
        }
        return mMe;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    }

    public void createTable(String name) {
        String sql = "create table if not exists " + name + " (_id integer primary key autoincrement, Tag text,  X real, Y real, Z real)";
        getWritableDatabase().execSQL(sql);
    }
}
