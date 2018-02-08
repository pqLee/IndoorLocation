package com.intel.indoorlocation.DataManager;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import java.io.File;
import java.io.IOException;

public class DBContext extends ContextWrapper {
    public static String LOG_TAG = "DBContext";
    public DBContext(Context base){
        super(base);
    }
    @Override
    public File getDatabasePath(String name){
        boolean sdExist = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        if (!sdExist){
            Log.e(LOG_TAG,"SD card not exists!");
            return null;
        }
        else {
            String dbDir = Environment.getExternalStorageDirectory().toString();
            dbDir += "/intel";
            String dbPath = dbDir + "/" +name;
            Log.d(LOG_TAG,"dbPath is " +dbPath);
            File dirFile = new File(dbDir);
            if(!dirFile.exists()){
                dirFile.mkdirs();
            }
            boolean isFileCreateSuccess = false;
            File dbFile = new File(dbPath);

            if (!dbFile.exists()){
                try{
                    isFileCreateSuccess = dbFile.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                    Log.e(LOG_TAG,"create file failed");
                }
            }else {
                isFileCreateSuccess = true;
            }
            Log.d(LOG_TAG,"after create dbPath is " +dbPath);

            if (isFileCreateSuccess){
                return dbFile;
            }else {
                return null;
            }
        }
    }
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory){
        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
        return result;
    }
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory,
                                               DatabaseErrorHandler errorHandler) {
        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
        return result;
    }
}
