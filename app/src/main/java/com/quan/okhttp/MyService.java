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
    private Context mContext;
    private WifiAdmin wifiAdmin ;
    private String ssid = "OW12_5G";
    private int interval = 0;
    private WiFiManager mWiFiManager;
    private String ftpAddr = "202.117.10.67";
    private String ftpUsername = "ylab";
    private String ftpPasword = "ylab";

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
            if (str_ftpAddr.equals("202.117.10.67")) {
                ftpUsername = "ylab";
                ftpPasword = "ylab";
            } else {
                ftpUsername = "quandk";
                ftpPasword = "quandkq";
            }
            //Toast.makeText(MyService.this, "interval set succeed by Binder!", Toast.LENGTH_LONG).show();
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
                Log.i("TAG", "Thread run: 上传数据");
                String url = "http://202.117.49.160:8080/mobile/send";
                SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
                while(true){
                    try {
                        sleep(1000);//每隔1秒发送一次数据
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    boolean res = mWiFiManager.hasNetwork();
//                    Log.i(TAG, "当前网络状态： " + String.valueOf(res));
//                    mw.setMobiletime(df.format(new Date()));
//                    mw.setNetrxspeed(NetworkSpeedUtils.RxSpeed);
//                    mw.setNettxspeed(NetworkSpeedUtils.TxSpeed);
//                    if(wifiAdmin.getWifiInfo(mw, mContext)){
//                        OkHttpUtils.postString().url(url).content(new Gson().toJson(mw))
//                                .mediaType(MediaType.parse("application/json; charset=utf-8"))
//                                .build().execute(new StringCallback() {
//                                    @Override
//                                    public void onError(Call call, Exception e, int id) {
//                                        //Log.e("onError", e.getMessage());
//                                    }
//                                    @Override
//                                    public void onResponse(String response, int id) {
//                                        Log.e("PostResponse:",response);
//                                    }
//                                });
//                        Log.i(TAG,"成功上传json数据");
//                    }else{
//                        Log.i(TAG,"Wifi 未连接");
//                    }
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
                uploadWifiDataTask.startUpload(mContext);
            }
        }).start();


        //新建线程三，执行下载操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Thread run：下载");
                Log.i(TAG,"connecting to ftp server...");
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
                            Log.i(TAG, "FTP连接成功，即将开始下载...");
                            if(ftpManager.downloadFile(ftpManager.rootPath,"LDA.mp4")){
                                ftpManager.closeFTP();
                            }
                        }else{
                            Log.i(TAG, "FTP连接失败！");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        //新建线程四，执行下载操作
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {

                String url;
                //while(true){
                {
                    //下载文件（支持大文件）(邓紫棋-泡沫)
                    url = "http://he.yinyuetai.com/uploads/videos/common/DD5201536EDB9652BC457D277E27123B.flv?sc\\u003d489edccd0ea78ac6\\u0026br\\u003d3154\\u0026vid\\u003d528428\\u0026aid\\u003d273\\u0026area\\u003dHT\\u0026vst\\u003d0";
                    OkHttpUtils.get().url(url).build()//下载文件放的路径-并重命名
                            .execute(new FileCallBack(Environment.getExternalStorageDirectory().getAbsolutePath(),"1_okhttp_Utils.mp4") {
                                @Override
                                public void inProgress(float progress, long total, int id) {
                                    //super.inProgress(progress, total, id);
                                    Log.e("fileDownload:", " " + (int)(100*progress));//获取下载进度
                                }
                                @Override
                                public void onError(Call call, Exception e, int id) {
                                    Log.e("fileDownload_onError:",e.getMessage());
                                }
                                @Override
                                public void onResponse(File response, int id) {
                                    Log.e("fileDownload:",response.getAbsolutePath());//获取下载成功的路径
                                    File file = new File(response.getAbsolutePath());
                                    if(file.isFile()&&file.exists()){
                                        file.delete();
                                    }
                                }
                            });
                    url = "http://r4---sn-5hne6nlk.googlevideo.com/videoplayback?sparams=dur,ei,expire,id,ip,ipbits,ipbypass,itag,lmt,mime,mip,mm,mn,ms,mv,pl,ratebypass,source&ip=149.13.117.213&itag=22&ei=w9E5WqM7goGEB5Ddr9AJ&expire=1513760291&ipbits=0&lmt=1509034180527244&ratebypass=yes&source=youtube&mime=video%2Fmp4&dur=10818.409&key=cms1&pl=24&id=o-ADUYZHF6XTy9AyaTYPsyXCr8LLjmZLiId5b8jspdS15t&signature=072E8A17ADFF517EE3A009E229461CDFFFC58DE3.60F872BC82953035F7C07884E47F8BE9DA34421A&video_id=uu8BD1rGXWQ&title=3+Hour+Healing+Tibetan+Bowl+Music-+Meditation+Music%2C+Relaxing+Music%2C+Soothing+Music%2C+Calming+%E2%98%AF2204&rm=sn-aigees7s,sn-5hnele7s&req_id=4f32a328d70ea3ee&ipbypass=yes&mip=185.214.71.130&redirect_counter=2&cms_redirect=yes&mm=34&mn=sn-5hne6nlk&ms=ltu&mt=1513738639&mv=m";
                    OkHttpUtils.get().url(url).build()//下载文件放的路径-并重命名
                            .execute(new FileCallBack(Environment.getExternalStorageDirectory().getAbsolutePath(),"youtube.mp4") {
                                @Override
                                public void inProgress(float progress, long total, int id) {
                                    //super.inProgress(progress, total, id);
                                    Log.e("fileDownload:", " " + (int)(100*progress));//获取下载进度
                                }
                                @Override
                                public void onError(Call call, Exception e, int id) {
                                    Log.e("fileDownload_onError:",e.getMessage());
                                }
                                @Override
                                public void onResponse(File response, int id) {
                                    Log.e("fileDownload:",response.getAbsolutePath());//获取下载成功的路径
                                    File file = new File(response.getAbsolutePath());
                                    if(file.isFile()&&file.exists()){
                                        file.delete();
                                    }
                                }
                            });
                }
            }
        }).start();*/

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
    }

}
