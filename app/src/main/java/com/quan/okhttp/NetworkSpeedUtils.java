package com.quan.okhttp;

import android.content.Context;
import android.net.TrafficStats;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import static android.net.TrafficStats.getTotalRxBytes;
import static android.net.TrafficStats.getTotalTxBytes;

/**
 * Created by quandk on 17-12-20.
 */

public class NetworkSpeedUtils {
    private static final String TAG = "NetworkSpeedUtils";
    private long lastTotalRxBytes = 0;
    private long lastTotalTxBytes = 0;
    private long newTotalRxBytes = 0;
    private long newTotalTxBytes = 0;
    private long lastTimeStamp = 0;
    private long nowTimeStamp = 0;
    public static double RxSpeed = 0;
    public static double TxSpeed = 0;
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            computeNetSpeed();
        }
    };
    public void startShowNetSpeed(){
        lastTotalRxBytes = getTotalRxBytes();
        lastTotalTxBytes = getTotalTxBytes();
        lastTimeStamp = System.currentTimeMillis();
        new Timer().schedule(task, 1000, 1000);//1s后启动任务, 每1s执行一次
    }
    public void computeNetSpeed(){
        newTotalRxBytes = getTotalRxBytes();
        nowTimeStamp = System.currentTimeMillis();
//        newTotalTxBytes = getTotalTxBytes();
        RxSpeed = (newTotalRxBytes - lastTotalRxBytes)*1000/((nowTimeStamp - lastTimeStamp)*1024);
//        TxSpeed = (newTotalTxBytes - lastTotalTxBytes)/2;
        lastTotalRxBytes = newTotalRxBytes;
        lastTimeStamp = nowTimeStamp;
//        lastTotalTxBytes = newTotalTxBytes;
        Log.e(TAG, RxSpeed + "b/s ");
    }
}
