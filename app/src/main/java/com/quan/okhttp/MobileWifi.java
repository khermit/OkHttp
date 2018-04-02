package com.quan.okhttp;

import java.util.Date;

public class MobileWifi {
    private Integer id;

    private Date ltime;

    private String mobiletime;

    private String mac;

    private String ssid;

    private String bssid;

    private Integer rssi;

    private Integer linkspeed;

    private Integer frequency;

    private Integer netid;

    private Integer score;



    private Double netrxspeed;

    private Double nettxspeed;

    private String scanstr;


    public Double getNetrxspeed() {
        return netrxspeed;
    }

    public void setNetrxspeed(Double netrxspeed) {
        this.netrxspeed = netrxspeed;
    }

    public Double getNettxspeed() {
        return nettxspeed;
    }

    public void setNettxspeed(Double nettxspeed) {
        this.nettxspeed = nettxspeed;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getLtime() {
        return ltime;
    }

    public void setLtime(Date ltime) {
        this.ltime = ltime;
    }

    public String getMobiletime() {
        return mobiletime;
    }

    public void setMobiletime(String mobiletime) {
        this.mobiletime = mobiletime == null ? null : mobiletime.trim();
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac == null ? null : mac.trim();
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid == null ? null : ssid.trim();
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid == null ? null : bssid.trim();
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    public Integer getLinkspeed() {
        return linkspeed;
    }

    public void setLinkspeed(Integer linkspeed) {
        this.linkspeed = linkspeed;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public Integer getNetid() {
        return netid;
    }

    public void setNetid(Integer netid) {
        this.netid = netid;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getScanstr() {
        return scanstr;
    }

    public void setScanstr(String scanstr) {
        this.scanstr = scanstr == null ? null : scanstr.trim();
    }
}