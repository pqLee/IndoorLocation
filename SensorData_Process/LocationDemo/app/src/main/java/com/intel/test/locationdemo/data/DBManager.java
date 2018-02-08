package com.intel.test.locationdemo.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by qixing on 17-8-4.
 */

public class DBManager {
    private static final String LOG_TAG = "DBManager";
    private DBHelper dbHelper;
    private SQLiteDatabase db;
   // private Context mContext;
   // private final String dbPath =android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/myTest2.db";

    public DBManager(Context context){
        DBContext dbContext = new DBContext(context);
       // mContext = dbContext;
        dbHelper = DBHelper.instance(dbContext);
        db = dbHelper.getWritableDatabase();
    }


    public void createTable(String tableName){
        Log.d(LOG_TAG, "tableName: " + tableName);
        //dbHelper = new DBHelper(mContext);
        dbHelper.createTable(tableName);

    }
//    public SQLiteDatabase openDataBase(){
//        SQLiteDatabase openDB = null;
//        try{
//            String dbPath = "/data/data/com.intel.test.locationdemo/databases/myTest.db";
//            openDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
//            Log.d(LOG_TAG, "openDB: " + openDB);
//            return openDB;
//        }catch (SQLiteException e){
//            e.printStackTrace();
//        }
//        return null;
//    }

//    public void addOneData(String tableName, SampleData sampleData){
//        db.beginTransaction();
//        try{
//            Log.d(LOG_TAG,"add one data");
//            db.execSQL("INSERT INTO " + tableName + " VALUES(?,?,?,?,?)",
//                    new Object[]{null,sampleData.mTag,sampleData.mX,sampleData.mY,sampleData.mZ});
//            Log.d(LOG_TAG,"add sucessful");
//            db.setTransactionSuccessful();
//        }finally {
//            db.endTransaction();
//            Log.d(LOG_TAG,"add failed");
//        }
//    }

    public void add(String tableName, Queue<SampleData> mQueue){
        db.beginTransaction();
        Log.d(LOG_TAG,"length is " + mQueue.size());
        try {
            for(SampleData sd : mQueue){
                Log.d(LOG_TAG,"add a series datas");
                db.execSQL("INSERT INTO " + tableName +" VALUES(?,?,?,?,?)",
                        new Object[]{null,sd.mTag,sd.mX,sd.mY,sd.mZ});
                Log.d(LOG_TAG,"add sucessful");
            }
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
            Log.d(LOG_TAG,"add failed");
        }
    }

    public void updateData(String tableName, String tag, Queue<SampleData> mQueue){
        db.beginTransaction();
        try {
            ArrayList<Integer> idlist = new ArrayList<Integer>();
            idlist = queryIDs(tableName,tag);
            int i = 0;
            for(SampleData sd : mQueue){
                Log.d(LOG_TAG,"update a series datas");
                ContentValues contentValues = new ContentValues();
                contentValues.put("X",sd.getmX());
                contentValues.put("Y",sd.getmY());
                contentValues.put("Z",sd.getmZ());
                db.update(tableName,contentValues,"_id=?",new String[]{String.valueOf(idlist.get(i))});
                Log.d(LOG_TAG,"update sucessful");
                i++;
            }
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
            Log.d(LOG_TAG,"update failed");
        }
    }


//    public void updateOneData(String tableName, SampleData sd){
//        db.beginTransaction();
//        Log.d(LOG_TAG,"updateOneData");
//        try{
//            ContentValues contentValues = new ContentValues();
//            contentValues.put("X",sd.getmX());
//            contentValues.put("Y",sd.getmY());
//            contentValues.put("Z",sd.getmZ());
//            db.update(tableName,contentValues,"Tag=?",new String[]{sd.getmTag()});
//            Log.d(LOG_TAG,"add sucessful");
//            db.setTransactionSuccessful();
//        }finally {
//            db.endTransaction();
//            Log.d(LOG_TAG,"update failed");
//        }
//
//    }

//    public void deleteOneData(String tableName, SampleData sd){
//        db.delete(tableName, "Tag=?", new String[]{sd.getmTag()});
//    }

    public void deleteData(String tableName, String tag){
        db.beginTransaction();
        Log.d(LOG_TAG,"deleteData");
        try{
            String sql = "DELETE FROM " + tableName + " WHERE Tag = '" + tag  + "'";
            db.execSQL(sql);
            Log.d(LOG_TAG,"delete sucessful");
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
            Log.d(LOG_TAG,"delete failed");
        }
    }

    public ArrayList<Integer>queryIDs(String tableName, String tag){
        Log.d(LOG_TAG,"queryIDs");
        ArrayList<Integer> idlist = new ArrayList<Integer>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE Tag = '" + tag + "'",null);
        Log.d(LOG_TAG,"cursor size is " + cursor.getCount());
        int i = 0;
        while (cursor.moveToNext()){
            idlist.add(cursor.getInt(cursor.getColumnIndex("_id")));
            Log.d(LOG_TAG,"query id"+idlist.get(i));
            i++;
        }
        cursor.close();
        return idlist;
    }

    public Queue<SampleData>queryTagData(String tableName, String tag){
        Log.d(LOG_TAG,"queryTagData");
        Queue<SampleData>mQueue = new LinkedList<SampleData>();
        Log.d(LOG_TAG,"tableName is " + tableName);
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE Tag = '" + tag + "'",null);
        Log.d(LOG_TAG,"cursor size is " + cursor.getCount());
        while (cursor.moveToNext()){
            Log.d(LOG_TAG,"now data is");
            SampleData sampleData = new SampleData();
            sampleData.setmTag(cursor.getString(cursor.getColumnIndex("Tag")));
            sampleData.setmX(cursor.getFloat(cursor.getColumnIndex("X")));
            sampleData.setmY(cursor.getFloat(cursor.getColumnIndex("Y")));
            sampleData.setmZ(cursor.getFloat(cursor.getColumnIndex("Z")));
            Log.d(LOG_TAG,"query data: "
                    + sampleData.mTag + ","
                    + sampleData.mX + "," +
                    sampleData.mY + ","
                    +sampleData.mZ);
            mQueue.add(sampleData);
        }
        cursor.close();
        return mQueue;
    }

    public List<SampleData>queryTable(String tableName){
        Log.d(LOG_TAG,"queryTable");
        Log.d(LOG_TAG,"tableName is " + tableName);
        List<SampleData> mList = new ArrayList<SampleData>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName,null);
        Log.d(LOG_TAG,"cursor size is " + cursor.getColumnCount());
        while (cursor.moveToNext()){
            Log.d(LOG_TAG,"now data is");
            SampleData sampleData = new SampleData();
            sampleData.setmTag(cursor.getString(cursor.getColumnIndex("Tag")));
            sampleData.setmX(cursor.getFloat(cursor.getColumnIndex("X")));
            sampleData.setmY(cursor.getFloat(cursor.getColumnIndex("Y")));
            sampleData.setmZ(cursor.getFloat(cursor.getColumnIndex("Z")));
            Log.d(LOG_TAG,"query data: "
                    + sampleData.mTag + ","
                    + sampleData.mX + "," +
                    sampleData.mY + ","
                    +sampleData.mZ);
            mList.add(sampleData);
        }
        cursor.close();
        return mList;
    }

    public Queue<SampleData>queryTotalData(String tableName){
        Log.d(LOG_TAG,"query");
        Queue<SampleData>mQueue = new LinkedList<SampleData>();
        Log.d(LOG_TAG,"tableName is " + tableName);
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName,null);
        Log.d(LOG_TAG,"cursor size is " + cursor.getColumnCount());
        while (cursor.moveToNext()){
            Log.d(LOG_TAG,"now data is");
            SampleData sampleData = new SampleData();
            sampleData.setmTag(cursor.getString(cursor.getColumnIndex("Tag")));

            sampleData.setmX(cursor.getFloat(cursor.getColumnIndex("X")));
            sampleData.setmY(cursor.getFloat(cursor.getColumnIndex("Y")));
            sampleData.setmZ(cursor.getFloat(cursor.getColumnIndex("Z")));
            Log.d(LOG_TAG,"query data: "
                    + sampleData.mTag + ","
                    + sampleData.mX + "," +
                    sampleData.mY + ","
                    +sampleData.mZ);
            mQueue.add(sampleData);
        }
        cursor.close();
        return mQueue;
    }

    public List<String> queryTableNameList(){
        ArrayList<String> tableNameList = new ArrayList<String>();
        if (db != null) {
            Cursor c = db.rawQuery("select name from sqlite_master where type='table' order by name", null);
            while (c.moveToNext()) {
                Log.d(LOG_TAG, "tableNameList: " + c.getString(0));
                tableNameList.add(c.getString(0));
            }
            c.close();
        }
        return tableNameList;
    }

    public void deleteTable(String tableName){
        db.execSQL("DROP TABLE "+tableName);
    }
    public void closeDatabase(){
        db.close();
    }

}
