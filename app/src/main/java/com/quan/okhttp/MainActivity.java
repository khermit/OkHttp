package com.quan.okhttp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startMyService();
        //stopMyService();
    }
    public void startMyService(){
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
    }

    public void stopMyService(){
        Intent intent = new Intent(this, MyService.class);
        stopService(intent);
    }
    private ServiceConnection conn;

    //Activity 与 Service 绑定
    public void bindMyService(View v){
        Intent intent = new Intent(this, MyService.class);
        //创建连接对象
        if(null == conn){
            conn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    Log.e("TAG", "OnServiceConnected");
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    Log.e("TAG", "OnServiceDisconnected");
                }
            };
            //绑定服务
            bindService(intent, conn, Context.BIND_AUTO_CREATE);
        } else {
            Log.e("TAG", "already bind");
        }


    }

    public void unbingMyService(View v){
        if(null != conn){
            unbindService(conn);
            conn = null;
            Log.e("TAG", "unbind MyService succeed!");
        } else {
            Log.e("TAG", "no service need to unbind!");
        }

    }

    //在Activity死亡之前要解绑相关的Service
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(null != conn){
            unbindService(conn);
            conn = null;
            Log.e("TAG", "unbind MyService succeed!");
        }
        stopMyService();
    }
}
