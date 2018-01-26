package com.quan.okhttp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

/**
 * Created by quandk on 17-5-16.
 */

public class WifiAdmin {
    private WifiManager mWifiManager;
    private WifiInfo mWifiInfo;
    private List<ScanResult> mWifiList;
    private List<ScanResult> mWifiListInfo;
    private List<WifiConfiguration> mWifiConfigurations;
    private WifiConfiguration xjtuConfiguration = null;
    private int netIdIndex = 1;
    private int oldid = -5;
    private int num_xjtu1x = 0;
    StringBuffer sb = new StringBuffer();//StringBuffer是线程安全的
    StringBuilder sbuilder = new StringBuilder();//单线程下，用StringBuilder快.
    private String[] score;
    public static String ScanBSSID = "ScanBSSID";
    public static String GetBSSID = "GetBSSID";
    WifiManager.WifiLock mWifiLock;
    public WifiAdmin(Context context){
        Log.i("WifiAdminActivity", "__________WifiAdmin_________");
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();
    }
    public void openWifi(){
        Log.i("WifiAdminActivity", "__________openWifi_________");
        if(!mWifiManager.isWifiEnabled()){
            mWifiManager.setWifiEnabled(true);
        }
    }
    public void closeWifi(){
        Log.i("WifiAdminActivity", "__________closeWifi_________");
        if(!mWifiManager.isWifiEnabled()){
            mWifiManager.setWifiEnabled(false);
        }
    }
    public int checkState(){
        Log.i("WifiAdminActivity", "__________checkState_________");
        return mWifiManager.getWifiState();
    }
    public void acquireWifiLock(){
        mWifiLock.acquire();
    }
    public void releaseWifiLock(){
        if(mWifiLock.isHeld())
            mWifiLock.acquire();
    }
    public void createWifiLock(){
        mWifiLock = mWifiManager.createWifiLock("test");
    }
    public List<WifiConfiguration> getConfiguration(){
        return mWifiConfigurations;
    }
    public void connectionConfiguration(int index){
        if(index>mWifiConfigurations.size()){
            return ;
        }
        mWifiManager.enableNetwork(mWifiConfigurations.get(index).networkId, true);
    }

    public void startScan(){
        Log.i("WifiAdminActivity", "__________startScan_________");
        mWifiManager.startScan();
        mWifiList = mWifiManager.getScanResults();
        mWifiListInfo = mWifiList;
        mWifiConfigurations = mWifiManager.getConfiguredNetworks();//return a list of all networks configured for the current foreground user. Upon failure to fetch or when Wifi if turn off ,it can be null
    }
    public List<ScanResult> getmWifiList(){
        Log.i("WifiAdminActivity", "__________getmWifiList_________");
        return mWifiList;
    }
    public StringBuffer lookUpScan(){
        StringBuffer sb = new StringBuffer();
        for(int i=0; i<mWifiList.size(); ++i){
            sb.append("Index_" + new Integer(i + 1).toString() + ":");
            sb.append((mWifiList.get(i)).toString()).append("\n");
        }
        return sb;
    }
    public String getMacAddress(){
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }
    public String getBSSID(){
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }
    public int getIpAddress(){
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }
    public int getNetWorkId(){
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    public void addNetWork(WifiConfiguration configuration){
        int wcgId = mWifiManager.addNetwork(configuration);
        mWifiManager.enableNetwork(wcgId, true);
    }
    public void disConnectionWifi(int netId){
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }
    public boolean changeRssi(Context context){
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        mWifiManager.startScan();
        mWifiList = mWifiManager.getScanResults();

        for(int i=0; i<mWifiList.size(); i++){
            Log.i("WifiAdmin", "____ConSize___________mWifilist----:"  + mWifiList.get(i));
        }

        for(;num_xjtu1x<mWifiList.size();){
            if("xjtu1x".equals(mWifiList.get(num_xjtu1x).SSID)){
                mWifiList.get(num_xjtu1x).level = -20;
                num_xjtu1x++;
                break;
            }
            else{
                num_xjtu1x++;
                if(num_xjtu1x>=mWifiList.size())
                    num_xjtu1x = 0;
            }

        }
        for(int i=0; i<mWifiList.size(); i++){
            Log.i("WifiAdmin", "____ConSize___________mWifilist2222----:"  + mWifiList.get(i));
        }

        boolean isDisc = mWifiManager.disconnect();
        Log.i("WifiAdmin", "____isDisc:"+isDisc);
        //boolean isEnable = mWifiManager.enableNetwork(mWifiConfigurations.get(netIdIndex).networkId, true);
        //boolean isEnable = mWifiManager.enableNetwork(newid, true);
        //Log.i("WifiAdmin", "____isEnable:"+isEnable);
        ++netIdIndex;

        boolean isConn = mWifiManager.reconnect();
        Log.i("WifiAdmin", "____isConn:"+isConn);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return isConn;
    }
    public boolean connectWifi(Context context)
    {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        mWifiManager.startScan();
        mWifiList = mWifiManager.getScanResults();

        for(int i=0; i<mWifiList.size(); i++){
            Log.i("WifiAdmin", "____ConSize___________mWifilist----:"  + mWifiList.get(i));
        }

        mWifiConfigurations = mWifiManager.getConfiguredNetworks();
        WifiConfiguration conf;
        if(xjtuConfiguration == null){
            for(int i=0; i<mWifiConfigurations.size(); i++){
                if("xjtu1x".equals(mWifiConfigurations.get(i).SSID.replaceAll("\"",""))){
                    xjtuConfiguration = mWifiConfigurations.get(i);

                    Log.i("WifiAdmin", "____ConSize___________mWifiConfigurations----:"  + mWifiConfigurations.get(i));

                }
            }
        }
        conf = xjtuConfiguration;
        mWifiManager.startScan();
        mWifiList = mWifiManager.getScanResults();
        if(num_xjtu1x>=mWifiList.size())
            num_xjtu1x = 0;
        for(;num_xjtu1x<mWifiList.size();){
            if("xjtu1x".equals(mWifiList.get(num_xjtu1x).SSID)){
                conf.BSSID = mWifiList.get(num_xjtu1x).BSSID;
                ScanBSSID = conf.BSSID;
                num_xjtu1x++;
                break;
            }
            else{
                num_xjtu1x++;
                if(num_xjtu1x>=mWifiList.size())
                    num_xjtu1x = 0;
            }

        }

        //conf.BSSID = "38:91:d5:c9:81:c1";
        Log.i("WifiAdmin", "____conf___________conf---:"  + num_xjtu1x + "  " + conf);

        if(oldid>-1)
            ;//conf.BSSID = "38:91:d5:c9:81:c1";;//mWifiManager.removeNetwork(oldid);
        //conf.BSSID = "38:91:d5:c6:98:f1";
        //conf.BSSID = "38:91:d5:c9:81:c1";
        int newid = mWifiManager.updateNetwork(conf);
        oldid = newid;
        mWifiManager.startScan();
        mWifiListInfo = mWifiManager.getScanResults();


        boolean isDisc = mWifiManager.disconnect();
        Log.i("WifiAdmin", "____isDisc:"+isDisc);
        //boolean isEnable = mWifiManager.enableNetwork(mWifiConfigurations.get(netIdIndex).networkId, true);
        //boolean isEnable = mWifiManager.enableNetwork(newid, true);
        //Log.i("WifiAdmin", "____isEnable:"+isEnable);
        ++netIdIndex;

        boolean isConn = mWifiManager.reconnect();
        Log.i("WifiAdmin", "____isConn:"+isConn);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        GetBSSID = mWifiManager.getConnectionInfo().getBSSID();
        return isConn;

    }
    public boolean reconnectWifi(Context context)
    {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //mWifiConfigurations = mWifiManager.getConfiguredNetworks();
        //WifiConfiguration config = new WifiConfiguration();
        //WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        /*
        if( -5 == oldid ) {
            if (xjtuConfiguration == null) {
                for (int i = 0; i < mWifiConfigurations.size(); i++) {
                    if ("xjtu1x".equals(mWifiConfigurations.get(i).SSID.replaceAll("\"", ""))) {
                        xjtuConfiguration = mWifiConfigurations.get(i);
                        Log.i("WifiAdmin", "____ConSize___________mWifiConfigurations----:" + mWifiConfigurations.get(i));
                        Log.i("WifiAdmin", "____ConSize___________enterpriseConfig----:" +xjtuConfiguration.enterpriseConfig);
                    }
                }
            }
        }*/
        //enterpriseConfig = xjtuConfiguration.enterpriseConfig;
        //Log.i("WifiAdmin", "____ConSize___________enterpriseConfig----:" + enterpriseConfig);
        //enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PEAP);
        //enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.NONE);
        //enterpriseConfig.setCaCertificate(null);
        //enterpriseConfig.setClientKeyEntry(null,null);
        //enterpriseConfig.setIdentity(Identity);
        //enterpriseConfig.setAnonymousIdentity(null);
        //enterpriseConfig.setPassword(Password);
        /*
        config.enterpriseConfig = enterpriseConfig;
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
        //config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
        config.allowedAuthAlgorithms.set(WifiEnterpriseConfig.Eap.PEAP);
        config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        //config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.NONE);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        */
        /*
        config = xjtuConfiguration;
        config.SSID = "\"xjtu1x\"";
        config.status = WifiConfiguration.Status.ENABLED;
        */
        //移除原来的网络配置文件
        //mWifiManager.disconnect();
        /*
        if( -5 != oldid ){
            boolean isDisc = mWifiManager.disconnect();
            Log.i("WifiAdmin", "____isDisc:"+isDisc);
            mWifiManager.removeNetwork(oldid);
        }*/
        /*
        mWifiManager.startScan();
        mWifiList = mWifiManager.getScanResults();

        for(int i=0; i<mWifiList.size(); i++){
            Log.i("WifiAdmin", "____ConSize___________mWifilist----:"  + mWifiList.get(i));
        }*/
        /*
        mWifiConfigurations = mWifiManager.getConfiguredNetworks();
        WifiConfiguration conf;
        if(xjtuConfiguration == null){
            for(int i=0; i<mWifiConfigurations.size(); i++){
                if("xjtu1x".equals(mWifiConfigurations.get(i).SSID.replaceAll("\"",""))){
                    xjtuConfiguration = mWifiConfigurations.get(i);

                    Log.i("WifiAdmin", "____ConSize___________mWifiConfigurations----:"  + mWifiConfigurations.get(i));

                }
            }
        }*/
        //conf = xjtuConfiguration;
        mWifiManager.startScan();
        //mWifiList = mWifiManager.getScanResults();
        mWifiListInfo = mWifiManager.getScanResults();
        //if(num_xjtu1x>=mWifiList.size())
         //   num_xjtu1x = 0;
        /*
        for(;num_xjtu1x<mWifiList.size();){
            if(SSID.equals(mWifiList.get(num_xjtu1x).SSID)){
                config.BSSID = mWifiList.get(num_xjtu1x).BSSID;
                ScanBSSID = config.BSSID;
                num_xjtu1x++;
                break;
            }
            else{
                num_xjtu1x++;
                if(num_xjtu1x>=mWifiList.size())
                    num_xjtu1x = 0;
            }

        }*/
        //conf.BSSID = "38:91:d5:c9:81:c1";
        //Log.i("WifiAdmin", "____conf___________conf---:"  + num_xjtu1x + "  " + config);

        //conf.BSSID = "38:91:d5:c6:98:f1";
        //conf.BSSID = "38:91:d5:c9:81:c1";
        //WifiConfiguration wc = config;
        //mWifiManager.removeNetwork(mWifiManager.getConnectionInfo().getNetworkId());

        //config.networkId = -1;
        //int newid = mWifiManager.addNetwork(config);
        //mWifiManager.enableNetwork(newid,true);
        //oldid = newid;

        //boolean isEnable = mWifiManager.enableNetwork(mWifiConfigurations.get(netIdIndex).networkId, true);
        //boolean isEnable = mWifiManager.enableNetwork(config.networkId, true);
        //Log.i("WifiAdmin", "____isEnable:"+isEnable);
        //++netIdIndex;
        boolean disConn = mWifiManager.disconnect();
        Log.i("WifiAdmin", "____disConn:"+disConn);

        boolean isConn = mWifiManager.reconnect();
        Log.i("WifiAdmin", "____isConn:"+isConn);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /*
        for (int i = 0; i < mWifiConfigurations.size(); i++) {
            if ("xjtu1x".equals(mWifiConfigurations.get(i).SSID.replaceAll("\"", ""))) {
                mWifiConf = mWifiConfigurations.get(i).toString();
                Log.i("WifiAdmin", "________________________________________________________:"+mWifiConfigurations.get(i));
            }
        }*/
        GetBSSID = mWifiManager.getConnectionInfo().getBSSID();
        return isConn;
    }

    public boolean changeWifi(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiConfigurations = mWifiManager.getConfiguredNetworks();//return a list of all networks configured for the current foreground user. Upon failure to fetch or when Wifi if turn off ,it can be null
        int newid = 0;

        WifiConfiguration conf = new WifiConfiguration();


        if(xjtuConfiguration == null){
            for(int i=0; i<mWifiConfigurations.size(); i++){
                if("xjtu1x".equals(mWifiConfigurations.get(i).SSID.replaceAll("\"",""))){
                    xjtuConfiguration = mWifiConfigurations.get(i);
                    conf = xjtuConfiguration;
                    Log.i("WifiAdmin", "____ConSize___________mWifiConfigurations----:"  + mWifiConfigurations.get(i).preSharedKey);
                    Log.i("WifiAdmin", "____ConSize___________mWifiConfigurations--xjtu--:"  + xjtuConfiguration);
                    Log.i("WifiAdmin", "____ConSize___________mWifiConfigurations--psk--:"  + xjtuConfiguration.preSharedKey.toString());
                }
            }
        }

        mWifiManager.startScan();
        mWifiList = mWifiManager.getScanResults();
        for(int i=0; i<mWifiList.size(); i++){
            Log.i("WifiAdmin", "____ConSize___________mWifilist----:"  + mWifiList.get(i));
        }


            if (netIdIndex >= mWifiConfigurations.size())
                netIdIndex = 0;

            //判断当前的配置是否在扫描的结果中，得到一个配置的netIdIndex
            boolean flag_wifi = true;
            //while(flag_wifi){
                //mWifiManager.startScan();
                //mWifiList = mWifiManager.getScanResults();

                //String ssid = mWifiConfigurations.get(netIdIndex++).SSID.replaceAll("\"","");//得到配置文件中的ssid   需要判断 循环扫描列表，看xjtu1的BSSID（mac）是否在 配置文件中
                //if (netIdIndex >= mWifiConfigurations.size())
                //   netIdIndex = 0;
                //for(int i=0; i<mWifiList.size(); i++){//循环扫描列表
                if(num_xjtu1x >= mWifiList.size())
                    num_xjtu1x=0;
                while(flag_wifi){
                    //if(ssid.equals(mWifiList.get(i).SSID)){
                    //   flag_wifi = false;
                    //}
                    if("xjtu1x".equals(mWifiList.get(num_xjtu1x).SSID)){
                        flag_wifi = false;
                        xjtuConfiguration.BSSID = mWifiList.get(num_xjtu1x).BSSID;
                        if(oldid>-1)
                            mWifiManager.removeNetwork(oldid);
                        //newid = mWifiManager.addNetwork(xjtuConfiguration);
                        //oldid = newid;
                        newid = mWifiManager.updateNetwork(xjtuConfiguration);
                        Log.i("WifiAdmin", "____ConSize___________newNetworkId:" + newid);
                        Log.i("WifiAdmin", "____ConSize___________num_xjtu1x:" + num_xjtu1x);
                        Log.i("WifiAdmin", "____ConSize___________xjtuConfiguration:" + xjtuConfiguration);
                        num_xjtu1x++;
                        /*for(int j=0; j<mWifiConfigurations.size();j++){
                            Log.i("WifiAdmin", "___________Con____:" + j + ":" + mWifiConfigurations.get(j));
                            Log.i("WifiAdmin", "___________Lis____:" + num_xjtu1x + ":" + mWifiList.get(num_xjtu1x));
                            Log.i("WifiAdmin", "___________null____:" + "null".equals(mWifiConfigurations.get(j).BSSID));
                            if ( ("null".equals(mWifiConfigurations.get(j).BSSID) || "any".equals(mWifiConfigurations.get(j).BSSID) ) && mWifiConfigurations.get(j).BSSID.equals(mWifiList.get(i).BSSID));
                            else{
                                xjtuConfiguration.BSSID = mWifiList.get(i).BSSID;
                                int newid = mWifiManager.addNetwork(xjtuConfiguration);
                                Log.i("WifiAdmin", "____ConSize___________newNetworkId:" + newid);
                            }

                        }*/

                    }
                    num_xjtu1x++;
                    if(num_xjtu1x >= mWifiList.size())
                        num_xjtu1x=0;
                    //Log.i("WifiAdmin", "____ConSize___________scanresult----:" + ssid+ "," + mWifiList.get(i).SSID.toString() + ',' + ssid.equals(mWifiList.get(i).SSID));
                }
            //}
        /*
        for(int i=0; i<mWifiList.size(); i++)
            Log.i("WifiAdmin", "____ConSize___________scanresult----:" + mWifiList.get(i).toString());
        for(int i=0; i<mWifiConfigurations.size(); i++)
            Log.i("WifiAdmin", "____ConSize___________mWifiConfigurations----:" + mWifiConfigurations.get(i).toString());

            if(netIdIndex == 0)
                netIdIndex = mWifiConfigurations.size()-1;
            else
                netIdIndex--;
        */
            /*Log.i("WifiAdmin", "____ConSize_____networkId:" + mWifiConfigurations.get(netIdIndex).networkId + "  " + mWifiConfigurations.get(netIdIndex).SSID);
            if (mWifiConfigurations.get(netIdIndex).SSID.toString().indexOf(mWifiManager.getScanResults().toString()) != -1)
                Log.i("WifiAdmin", "____ConSize_________:OKOKOKOKOKOKOKOK");
            Log.i("WifiAdmin", "____ConSize__________________________________________:" + mWifiConfigurations.toString());
            Log.i("WifiAdmin", "____ConSize:" + mWifiConfigurations.size() + "  netIdIndex:" + netIdIndex);
            for (netIdIndex = 0; netIdIndex < mWifiConfigurations.size(); netIdIndex++)
                Log.i("WifiAdmin", "____NetworkId:" + mWifiConfigurations.get(netIdIndex).networkId + "  " + mWifiConfigurations.get(netIdIndex).SSID);
            //mWifiManager.enableNetwork(mWifiConfigurations.get(netIdIndex).networkId, true);
            */
            boolean isDisc = mWifiManager.disconnect();
            Log.i("WifiAdmin", "____isDisc:"+isDisc);
            //boolean isEnable = mWifiManager.enableNetwork(mWifiConfigurations.get(netIdIndex).networkId, true);
            boolean isEnable = mWifiManager.enableNetwork(newid, true);
            Log.i("WifiAdmin", "____isEnable:"+isEnable);
            ++netIdIndex;

            boolean isConn = mWifiManager.reconnect();
            Log.i("WifiAdmin", "____isConn:"+isConn);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return isConn;
    }

    public boolean isWifiConnected(Context context){
        if(context != null){
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            //NetworkInfo mWifiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo ni = mConnectivityManager.getActiveNetworkInfo();
            if(ni != null && ni.getType() == ConnectivityManager.TYPE_WIFI){
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                //String state = wifiInfo.getSupplicantState().toString();
                //Log.i("WifiAdmin", "____getSupplicantState:"+ wifiInfo.getSupplicantState().toString().equals("COMPLETED"));
                return wifiInfo.getSupplicantState().toString().equals("COMPLETED");
            }
            else
                return false;
        }
        return false;
    }

    public boolean getWifiInfo(MobileWifi mw, Context context){
        sb.setLength(0);
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiManager.startScan();
        mWifiList = mWifiManager.getScanResults();
        mWifiInfo = mWifiManager.getConnectionInfo();// return dynamic information about the current Wi-Fi connection, if any is active
        if(null == mWifiInfo){
            Log.i("WifiAdmin", "____getConnectionInfo()__null:"+ mWifiInfo.toString());
            return false;//"No connection";
        }
        else {
            sb = sb.append(
                    //"ConnectionInfo:   " + mWifiInfo.toString() + "   ----  ;").append(
                    /*";" + mWifiInfo.getLinkSpeed()).append(
                    ";" + mWifiInfo.getNetworkId()).append(
                    ";" + mWifiInfo.describeContents()).append(
                    ";" + mWifiInfo.getBSSID()).append(
                    ";" + mWifiInfo.getMacAddress()).append(
                    ";" + mWifiInfo.getSSID()).append(
                    ";" + mWifiInfo.getFrequency()).append(
                    ";" + mWifiInfo.getHiddenSSID()).append(
                    ";" + mWifiInfo.getIpAddress()).append(
                    ";" + mWifiInfo.getRssi()).append(
                    ";" + mWifiInfo.getMacAddress()).append(*/
                    ";" + mWifiInfo.getSupplicantState());

            mw.setLinkspeed(mWifiInfo.getLinkSpeed());
            mw.setSsid(mWifiInfo.getSSID());
            mw.setMac(mWifiInfo.getMacAddress());
            mw.setRssi(mWifiInfo.getRssi());
            mw.setFrequency(mWifiInfo.getFrequency());
            mw.setNetid(mWifiInfo.getNetworkId());
            mw.setBssid(mWifiInfo.getBSSID());

            score = mWifiInfo.toString().split(" ");
            mw.setScore(Integer.parseInt(score[score.length-1]));

            sbuilder.delete(0,sbuilder.length());//delete快于setLength(0)快于新建sbuilder
            for (int i=0; i< mWifiList.size(); i++){
                sbuilder.append(mWifiList.get(i).level + " ");
                sbuilder.append(mWifiList.get(i).frequency + " ");
            }
            mw.setScanstr(sbuilder.toString());
            Log.i("WifiAdmin", "____getSupplicantState:"+ mWifiInfo.toString());
        }
        return true;
    }
}


