package com.quan.okhttp;

import android.content.Context;

import com.quan.wifilibrary.WiFiManager;
import com.quan.wifilibrary.listener.OnWifiEnabledListener;

/**
 * Created by quandk on 18-1-26.
 */

public class MainWifi {
    private String ssid;
    private WiFiManager mWiFiManager;
    public MainWifi(String ssid, Context context){
        this.ssid = ssid;
        this.mWiFiManager = WiFiManager.getInstance(context);
    }

    public void start(){
        mWiFiManager.openWiFi();
        mWiFiManager.closeWiFi();

        mWiFiManager.setOnWifiEnabledListener(new OnWifiEnabledListener() {
            /**
             * WIFI开关状态的回调
             *
             * @param enabled true 打开 false 关闭
             */
            @Override
            public void onWifiEnabled(boolean enabled) {
                // TODO
            }
        });
    }


}
