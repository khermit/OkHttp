package com.quan.okhttp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ServiceConnection conn;

    private MyService.Binder binder;

    private EditText et_ssid;
    private EditText et_interval;

    private TextView tx_ssid;
    private TextView tx_interval;
    private TextView  tx_myservice;

    private Button bt_ssid;
    private Button bt_interval;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private String str_ssid;
    private String str_interval;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref.edit();

        et_ssid = (EditText) findViewById(R.id.et_ssid);
        et_interval = (EditText) findViewById(R.id.et_interval);

        tx_ssid = (TextView) findViewById(R.id.tx_ssid);
        tx_interval = (TextView) findViewById(R.id.tx_interval);
        tx_myservice = (TextView) findViewById(R.id.tx_myservice);

        bt_ssid = (Button) findViewById(R.id.bt_ssid);
        bt_interval = (Button) findViewById(R.id.bt_interval);

        String ssid = pref.getString("ssid", "first");

        if( !ssid.equals("first") ) { //如果不是第一次，则回显之前的数据
            str_ssid = pref.getString("ssid","no data");
            str_interval = pref.getString("interval","no data");

            et_ssid.setText(str_ssid);
            et_interval.setText(str_interval);

            tx_ssid.setText(str_ssid);
            tx_interval.setText(str_interval);
        }

        bt_ssid.setOnClickListener(this);
        bt_interval.setOnClickListener(this);

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
            super.handleMessage(msg);
            tx_myservice.setText(msg.getData().getString("data"));
        }
    };

    //在Activity死亡之前要解绑相关的Service
    @Override
    protected void onDestroy(){
        super.onDestroy();
        unbingMyService();//解绑服务
        stopMyService();//停止服务
    }

}
