package com.quan.okhttp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quan.wifilibrary.WiFiManager;
import com.quan.wifilibrary.listener.OnWifiConnectListener;
import com.quan.wifilibrary.listener.OnWifiEnabledListener;
import com.quan.wifilibrary.listener.OnWifiScanResultsListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnWifiConnectListener, OnWifiEnabledListener, OnWifiScanResultsListener {
    private static final String TAG = "MainActivity";

    private ServiceConnection conn;

    private MyService.Binder binder;

    private EditText et_ssid;
    private EditText et_interval;
    private EditText et_password;

    private TextView tx_ssid;
    private TextView tx_interval;
    private TextView tx_myservice;
    private TextView tx_wifi;

    private Button bt_ssid;
    private Button bt_interval;
    private Button bt_password;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private String str_ssid;
    private String str_interval;
    private String str_password = null;

    private WiFiManager mWiFiManager;

    public static final int UPDATE_WIFI = 1;
    public static final int UPDATE_MYSERVICE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref.edit();

        et_ssid = (EditText) findViewById(R.id.et_ssid);
        et_interval = (EditText) findViewById(R.id.et_interval);
        et_password = (EditText) findViewById(R.id.et_password);


        tx_ssid = (TextView) findViewById(R.id.tx_ssid);
        tx_interval = (TextView) findViewById(R.id.tx_interval);
        tx_myservice = (TextView) findViewById(R.id.tx_myservice);
        tx_wifi = (TextView) findViewById(R.id.tx_wifi);

        bt_ssid = (Button) findViewById(R.id.bt_ssid);
        bt_interval = (Button) findViewById(R.id.bt_interval);
        bt_password = (Button) findViewById(R.id.bt_password);

        mWiFiManager = WiFiManager.getInstance(getApplicationContext());
        mWiFiManager.openWiFi();
        mWiFiManager.setOnWifiEnabledListener(this);//添加wifi状态的监听
        mWiFiManager.setOnWifiConnectListener(this);//添加wifi连接的监听
        mWiFiManager.setOnWifiScanResultsListener(this);//添加wifi扫面结果的监听

        String ssid = pref.getString("ssid", "first");

        if( !ssid.equals("first") ) { //如果不是第一次，则回显之前的数据
            str_ssid = pref.getString("ssid","no data");
            str_interval = pref.getString("interval","no data");
            str_password = pref.getString("password", "no data");

            et_ssid.setText(str_ssid);
            et_interval.setText(str_interval);
            et_password.setText(str_password);

            tx_ssid.setText(str_ssid);
            tx_interval.setText(str_interval);
        }

        bt_ssid.setOnClickListener(this);
        bt_interval.setOnClickListener(this);
        bt_password.setOnClickListener(this);

        startMyService();//启动服务
        bindMyService();//绑定服务
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_ssid:
                str_ssid = et_ssid.getText().toString();
                editor.putString("ssid",str_ssid);
                editor.apply();
                et_ssid.setText(str_ssid);
                tx_ssid.setText("ssid: " + str_ssid);
                if (null != binder){
                    binder.setSsid(str_ssid);
                }
                Toast.makeText(MainActivity.this, "ssid set succeed!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.bt_interval:
                str_interval = et_interval.getText().toString();
                editor.putString("interval",str_interval);
                editor.apply();
                et_interval.setText(str_interval);
                tx_interval.setText("interval: " + str_interval);
                if (null != binder){
                    binder.setInterval(str_interval);
                }
                Toast.makeText(MainActivity.this, "interval set succeed!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.bt_password:
                str_password = et_password.getText().toString();
                editor.putString("password",str_password);
                editor.apply();
                et_password.setText(str_password);
                Toast.makeText(MainActivity.this, "password set succeed!", Toast.LENGTH_SHORT).show();
        }

    }

    public void startMyService(){
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
    }

    public void stopMyService(){
        Intent intent = new Intent(this, MyService.class);
        stopService(intent);
    }

    //Activity 与 Service 绑定
    public void bindMyService(){
        Intent intent = new Intent(this, MyService.class);
        //创建连接对象
        if(null == conn){
            conn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) { //此处的iBinder访问到的是MyService中onBind()的返回值。
                    //服务绑定成功时执行
                    Log.e("TAG", "OnServiceConnected");
                    binder = (MyService.Binder) iBinder;
                    binder.getService().setCallback(new MyService.Callback() {
                        @Override
                        public void onDataChange(String data) {
                            Message msg = new Message();
                            Bundle b = new Bundle();
                            b.putString("data", data);
                            msg.what = UPDATE_MYSERVICE;
                            msg.setData(b);
                            handler.sendMessage(msg);
                        }
                    });
                }
                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    //程序崩溃或被杀掉时执行
                    Log.e("TAG", "OnServiceDisconnected");
                }
            };
            //绑定服务
            bindService(intent, conn, Context.BIND_AUTO_CREATE);
        } else {
            Log.e("TAG", "already bind");
        }
    }

    public void unbingMyService(){
        if(null != conn){
            unbindService(conn);
            conn = null;
            Log.e("TAG", "unbind MyService succeed!");
        } else {
            Log.e("TAG", "no service need to unbind!");
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_WIFI:
                    tx_wifi.setText(msg.getData().getString("wifi"));
                    break;
                case UPDATE_MYSERVICE:
                    tx_myservice.setText(msg.getData().getString("data"));
                    break;
                default:
                    break;
            }
        }
    };

    //在Activity死亡之前要解绑相关的Service
    @Override
    protected void onDestroy(){
        super.onDestroy();
        unbingMyService();//解绑服务
        stopMyService();//停止服务
    }

    /**WIFI连接信息的回调
     * @param log log
     */
    @Override
    public void onWiFiConnectLog(String log) {
        Log.i(TAG, "onWiFiConnectLog listener:" + log);

    }
    /**WIFI连接成功的回调
     * @param SSID 热点名
     */
    @Override
    public void onWiFiConnectSuccess(String SSID) {
        Log.i(TAG, "onWiFiConnectSuccess listener:" + SSID);
        Message msg = new Message();
        Bundle b = new Bundle();
        msg.what = UPDATE_WIFI;
        b.putString("wifi", SSID + " connection succeess!");
        msg.setData(b);
        handler.sendMessage(msg);
    }
    /** WIFI连接失败的回调
     * @param SSID 热点名
     */
    @Override
    public void onWiFiConnectFailure(String SSID) {
        Log.i(TAG, "onWiFiConnectFailure listener:" + SSID);
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("wifi", SSID + " connection failed! ready to reconnect");
        msg.what = UPDATE_WIFI;
        msg.setData(b);
        handler.sendMessage(msg);

//        new Thread() {
//            @Override
//            public void run() {
//                mWiFiManager.connectWPA2Network(str_ssid, str_password);
//            }
//        }.start();

        b.putString("wifi", SSID + "reconnectting... " );
        msg.setData(b);
        handler.sendMessage(msg);

    }
    /** WIFI开关的回调
     * @param enabled true 可用 false 不可用
     */
    @Override
    public void onWifiEnabled(boolean enabled) {
        Log.i(TAG, "onWifiEnabled listener:" + enabled);
        Message msg = Message.obtain();
        msg.what = UPDATE_WIFI;
        Bundle b = new Bundle();
        if (enabled){
            b.putString("wifi", "wifi opened!");
            msg.setData(b);
            handler.sendMessage(msg);

//            b.putString("wifi", "ready to connect " + str_ssid);
//            msg.setData(b);
//            handler.sendMessage(msg);

            new Thread() {
                @Override
                public void run() {
                    Log.i(TAG, "onWifiEnabled listener:" + "ready to connect:" + str_ssid);
                    boolean rre = mWiFiManager.connectBeforeNetwork(str_ssid);
                    Log.i(TAG, "onWifiEnabled listener:" + "connect res: " + rre);
                }
            }.start();

            msg = Message.obtain();
            msg.what = UPDATE_WIFI;
            Bundle bb = new Bundle();
            bb.putString("wifi", str_ssid + " connectting..." );
            msg.setData(bb);
            handler.sendMessage(msg);


        }else{
            b.putString("wifi", "wifi closed!");
            msg.setData(b);
            handler.sendMessage(msg);
        }

    }

    /** 扫描结果的回调
     * @param scanResults 扫描结果
     */
    @Override
    public void onScanResults(List<ScanResult> scanResults) {
//        Log.i(TAG, "onScanResults listener: size:" + scanResults.size());
//        StringBuilder stringBuilder = new StringBuilder();
//        for (int i = 0; i < scanResults.size(); i++) {
//            stringBuilder
//                    .append("Index_" + new Integer(i + 1).toString() + ":");
//            // 将ScanResult信息转换成一个字符串包
//            // 其中把包括：BSSID、SSID、capabilities、frequency、level
//            stringBuilder.append((scanResults.get(i)).toString());
//            stringBuilder.append("/n");
//        }
//        Message msg = new Message();
//        Bundle b = new Bundle();
//        b.putString("wifi", "scanResult: " + stringBuilder.toString());
//        msg.what = UPDATE_WIFI;
//        msg.setData(b);
//        handler.sendMessage(msg);
//        Log.i(TAG, "onScanResults listener:" + stringBuilder.toString());

    }
}
