package com.quan.okhttp;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.quan.wifilibrary.WiFiManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.MediaType;


/**
 * Created by quandk on 18-1-29.
 */

public class UploadWifiDataTask {
    private static final String TAG = "UploadWifiDataTask";
    public MobileWifi mw = new MobileWifi();
    private WifiAdmin wifiAdmin ;
    private Context mContext;
    private String url = "http://202.117.49.160:8080/mobile/send";
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            uploadWifiDataFunc();
        }
    };
    public void startUpload(Context context){
        mContext = context;
        wifiAdmin = new WifiAdmin(mContext);
        new Timer().schedule(task, 1000, 1000);//2s后启动任务, 每1s执行一次
    }
    public void uploadWifiDataFunc(){
        Log.i(TAG, "uploadWifiDataFunc");
        mw.setMobiletime(df.format(new Date()));
        mw.setNetrxspeed(NetworkSpeedUtils.RxSpeed);
        mw.setNettxspeed(NetworkSpeedUtils.TxSpeed);
        if(wifiAdmin.getWifiInfo(mw, mContext)){
            OkHttpUtils.postString().url(url).content(new Gson().toJson(mw))
                    .mediaType(MediaType.parse("application/json; charset=utf-8"))
                    .build().execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    //Log.e("onError", e.getMessage());
                }
                @Override
                public void onResponse(String response, int id) {
                    Log.e("PostResponse:",response);
                }
            });
            Log.i(TAG,"成功上传json数据");
        }else{
            Log.i(TAG,"Wifi 未连接");
        }
    }
}
