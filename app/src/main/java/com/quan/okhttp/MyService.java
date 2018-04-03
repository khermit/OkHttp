package com.quan.okhttp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
//import android.os.Binder;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.quan.wifilibrary.WiFiManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Request;

import static java.lang.Thread.sleep;

//import com.zhy.http.okhttp.OkHttpUtils;

/**
 * Created by quandk on 17-12-14.
 * 自定义本地服务
 */

/**
 *一、
 * 1. startService()
 *  第一次调用：构造方法--> onCreate() --> onStartCommand()
 *  后面再次调用： --> onStartCommand()
 * 2. stopService()：--> onDestroy()
 *
 *
 *二、
 * 3. bindService(intent, serviceConnection)
 *  第一次调用：构造方法--> onCreate() --> onBind() --> onServiceConnected()
 *4.unbindService(): （只有当前Activity与Service连接）--> onUnbind() --> onDestroy()
 *
 */

public class MyService extends Service {
//    public MobileWifi mw = new MobileWifi();
    public static final String TAG = "MyService";
    public static boolean ftpUploadAddr = true; //okhttpaddr
    private Context mContext;
    private WifiAdmin wifiAdmin ;
    private String ssid = "OW12_5G";
    private int interval = 0;
    private WiFiManager mWiFiManager;
    private String ftpAddr = "202.117.10.67";
    private String ftpUsername = "ylab";
    private String ftpPasword = "ylab";
    private String okhttpaddr = "192.168.0.120";

    public MyService() {
        Log.e("TAG", "MyService()");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("TAG", "onBind()");
        return new Binder();
    }

    public class Binder extends android.os.Binder{
        public void setSsid(String ssid){
            MyService.this.ssid = ssid;
            //Toast.makeText(MyService.this, "ssid set succeed by Binder!", Toast.LENGTH_LONG).show();
        }
        public void setInterval(String str_interval){
            MyService.this.interval = Integer.valueOf(str_interval);
            //Toast.makeText(MyService.this, "interval set succeed by Binder!", Toast.LENGTH_LONG).show();
        }
        public void setFtpAddr(String str_ftpAddr){
            MyService.this.ftpAddr = str_ftpAddr;
            //Toast.makeText(MyService.this, "interval set succeed by Binder!", Toast.LENGTH_LONG).show();
        }
        public void setFtpUser(String str_ftpUser){
            ftpUsername = str_ftpUser;
        }
        public void setFtpPswd(String str_ftpPswd){
            ftpPasword = str_ftpPswd;
        }
        public void setOkhttpaddr(String str_okhttpaddr){
            okhttpaddr = str_okhttpaddr;
        }

        public void startFtpUpload(String ssid){
            startUpload(ssid);
        }

        public MyService getService(){
            return MyService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent){
        Log.e("TAG", "onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        mContext = this;
        wifiAdmin = new WifiAdmin(this);
        Log.e("TAG", "MyService onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.i(TAG, "MyService onStartCommand()");


        //新建线程 将数据传送给activity
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("TAG", "Thread run: 更新时间数据");
                SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
                while(true){
                    try {
                        sleep(1000);//每隔1秒发送一次数据
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(null != callback){
                        callback.onDataChange(df.format(new Date()));
                    }
                }
            }
        }).start();

        //新建线程，执行测网速
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("TAG", "Thread run: 网速");
                NetworkSpeedUtils networkSpeedUtils = new NetworkSpeedUtils();
                networkSpeedUtils.startShowNetSpeed();
            }
        }).start();

        //新建线程，上传数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("TAG", "Thread run: 上传数据");
                UploadWifiDataTask uploadWifiDataTask = new UploadWifiDataTask();
                uploadWifiDataTask.startUpload(mContext, okhttpaddr);
            }
        }).start();

//        for(int i=0;i<20;i++){
//            startMyDownload("LDA.mp4");
//        }
        new DownloadThread(1, "LDA.mp4").start();
        new DownloadThread(2, "LDA.mp4").start();
        new DownloadThread(3, "LDA.mp4").start();



        //新建线程，监测wifi的情况，如果断开，则连接。
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("TAG", "Thread run： WIFI");
                if(null == mWiFiManager){
                    mWiFiManager = WiFiManager.getInstance(getApplicationContext());
                }
                while (true){
                    mWiFiManager.openWiFi();
                    if(mWiFiManager.isWifiEnabled()){
                        Log.i(TAG, "Wwifi已经打开");
                        if(mWiFiManager.isWifiConnected()){
                            Log.i(TAG, "Wwifi已经连接");
                            WifiInfo connectInfo = mWiFiManager.getConnectionInfo();
                            String SSID = connectInfo.getSSID();
                            if(SSID.equals("\"" + ssid + "\"")){
                                Log.i(TAG, "已正确连接：" + SSID + " 状态良好");
                                try {
                                    sleep(1000*60*1);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Log.i(TAG, "已错误连接：" + SSID + " 即将断开");
                                boolean res = mWiFiManager.disconnectCurrentWifi();
                                Log.i(TAG, "断开状态：" + String.valueOf(res));
                            }
                        } else {
                            Log.i(TAG, "当前未连接任何wifi，即将开始连接已设置的ssid:" + ssid );
                            boolean res = mWiFiManager.connectBeforeNetwork(ssid);
                            Log.i(TAG, "连接"+ssid+"状态:" + String.valueOf(res));
                        }

                    } else {
                        Log.i(TAG, "WIFI为关闭状态");
                    }
                    try {
                        sleep(1000*10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.e("TAG", "MyService onDestroy()");
    }

    private Callback  callback = null;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public Callback getCallback() {
        return callback;
    }

    public static interface Callback{
        void onDataChange(String data);
        void onFtpUploadDataChange(String data);
    }


    private void startUpload(String ssid){
        //新建线程，执行上传操作
        final String mySsid = ssid;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Thread run：上传");
                Log.i(TAG,"connecting to ftp server...");
                FTPManager ftpManager = new FTPManager();
                Log.i(TAG,ftpManager.rootPath);

                String uploadFtpAddr = ftpAddr;
                String uploadFtpUsername = ftpUsername;
                String uploadFtpPasword = ftpPasword;

                if(MyService.ftpUploadAddr){ //okhttpaddr
                    uploadFtpAddr = okhttpaddr;
                    uploadFtpUsername = "quandk";
                    uploadFtpPasword  = "quandkq";
                }

                String localPath = "/storage/emulated/0/1Wifi/mobilewifi.db";
                Date day = new Date();
                SimpleDateFormat df  = new SimpleDateFormat("yyyyMMdd");
                String currentDay = df.format(day);
                String serverPath = currentDay + "/" + mySsid + "/";

                try {
                    if(ftpManager.connect(uploadFtpAddr, uploadFtpUsername, uploadFtpPasword)){
                        Log.i(TAG, "FTP连接成功，即将开始上传...");
                        if(null != callback){
                            callback.onFtpUploadDataChange("ready!");
                        }
                        if(ftpManager.uploadFile(localPath,serverPath)){
                            Log.i(TAG, "FTP上传成功！");
                            ftpManager.closeFTP();
                            if(null != callback){
                                callback.onFtpUploadDataChange("success!");
                            }
                        }
                    }else{
                        Log.i(TAG, "FTP连接失败！");
                        if(null != callback){
                            callback.onFtpUploadDataChange("fail!");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private class DownloadThread extends Thread{
        private String fileName;
        private int index;
        public DownloadThread(int index, String fileName){
            this.fileName = fileName;
            this.index = index;
        }

        @Override
        public void run() {
//            super.run();
            Log.i(TAG, "Thread " + index + " run：下载");
            Log.i(TAG,index + " connecting to ftp server...");
            FTPManager ftpManager = new FTPManager();
            Log.i(TAG,ftpManager.rootPath);
            long m = ( (long)(Math.random()*50) + 10 )*1000*60;
            //m = 0;
            while(true){
                Log.i(TAG, "开始sleep：" + String.valueOf(m));
                //m = ( (long)(Math.random()*50) + 10 )*1000*60;
                if ( interval > 0 && interval < 60){
                    try {
                        Log.i(TAG, "ftp sleep interval: " + interval + " min");
                        Thread.sleep(1000*60*interval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if( 0 == interval){
                    Log.i(TAG, "ftp sleep 0 min");
                } else {
                    try {
                        m = (int)(Math.random()*50 + 1);
                        Log.i(TAG, "ftp sleep random: " + m + " min");
                        Thread.sleep(1000*60*m);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    if(ftpManager.connect(ftpAddr, ftpUsername, ftpPasword)){
                        Log.i(TAG, index + " FTP连接成功，即将开始下载...");
                        if(ftpManager.downloadFile(ftpManager.rootPath,fileName)){
                            ftpManager.closeFTP();
                        }
                    }else{
                        Log.i(TAG, index + " FTP连接失败！");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

    }

}
