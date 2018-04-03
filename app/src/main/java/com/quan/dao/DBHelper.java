package com.quan.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by quandk on 17-5-17.
 */

public class DBHelper extends SQLiteOpenHelper {
    //Database version
    private static final int DDTABASE_VERSION = 3;
    private static final String DATABASE_NAME = "mobilewifi.db";

    public DBHelper(Context context, String databasename, SQLiteDatabase.CursorFactory factory, int version){
        super(context, databasename, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        String CREATE_TABLE_STUDENT="CREATE TABLE IF NOT EXISTS mobilewifi ( " +
                "id" + " INTEGER PRIMARY KEY AUTOINCREMENT ," +

                "ltime" + " TEXT ," +
                "mobiletime" + " TEXT ," +
                "mac" + " TEXT ," +
                "ssid" + " TEXT ," +
                "bssid" + " TEXT ," +
                "rssi" + " INTEGER ," +
                "linkSpeed" + " INTEGER ," +
                "Frequency" + " INTEGER ," +
                "NetID" + " INTEGER ," +
                "score" + " INTEGER ," +
                "netrxspeed" + " INTEGER ," +
                "nettxspeed" + " INTEGER ," +
                "scanstr" + " TEXT)";

        db.execSQL(CREATE_TABLE_STUDENT);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //如果旧表存在，删除，所以数据将会消失
        db.execSQL("DROP TABLE IF EXISTS mobilewifi");
        onCreate(db);
     }
}
