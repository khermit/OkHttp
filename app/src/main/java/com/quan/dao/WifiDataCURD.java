package com.quan.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.quan.okhttp.MobileWifi;

/**
 * Created by quandk on 17-5-17.
 */

public class WifiDataCURD {
    private DBHelper dbHelper;
    private DatabaseContext dbContext;
    public WifiDataCURD(Context context){
        dbContext = new DatabaseContext(context, "1Wifi");
        dbHelper = new DBHelper(dbContext, "mobilewifi.db", null, 1);
    }
    public int insert(MobileWifi d){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", d.getId());

        values.put("ltime", "");
        values.put("mobiletime", d.getMobiletime());
        values.put("mac", d.getMac());
        values.put("ssid", d.getSsid());
        values.put("bssid", d.getBssid());
        values.put("rssi", d.getRssi());
        values.put("linkSpeed", d.getLinkspeed());
        values.put("Frequency", d.getFrequency());
        values.put("NetID", d.getNetid());
        values.put("score", d.getScore());
        values.put("netrxspeed", d.getNetrxspeed());
        values.put("nettxspeed", d.getNettxspeed());
        values.put("scanstr", d.getScanstr());

        long wifiId = db.insert("mobilewifi", null, values);
        db.close();
        return (int)wifiId;
    }
}
