package com.dailyway.findtrack;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by nord_tramper on 30/06/16.
 * Get settings from file, save it back and store it during run
 */
public class FindTrackSettings {
    private String      sIniFileName = "findtrack.ini";// config file name
    FileInputStream     fisSettings;
    Properties          pProp;
    //FileOutputStream    fosSettings;

    //parameters
    private boolean bUseProxy;
    private String  sProxyHost;
    private String  sProxyPort;
    private String  sProxyUser;
    private String  sProxyPassword;

    public FindTrackSettings(){
        try {
            fisSettings = new FileInputStream(sIniFileName);
            pProp = new Properties();
            pProp.load(fisSettings);

            bUseProxy       = Boolean.valueOf(pProp.getProperty("proxy.use"));
            sProxyHost      = pProp.getProperty("proxy.host");
            sProxyPort      = pProp.getProperty("proxy.port");
            sProxyUser      = pProp.getProperty("proxy.user");
            sProxyPassword  = pProp.getProperty("proxy.password");

        }
        catch (IOException e){
            System.err.println("No config file found. Apply defaults! "+e.getMessage());
        }
    }

    public String getsProxyHost() {
        return sProxyHost;
    }

    public void setsProxyHost(String sProxyHost) {
        this.sProxyHost = sProxyHost;
    }

    public String getsProxyPort() {
        return sProxyPort;
    }

    public void setsProxyPort(String sProxyPort) {
        this.sProxyPort = sProxyPort;
    }

    public String getsProxyUser() {
        return sProxyUser;
    }

    public void setsProxyUser(String sProxyUser) {
        this.sProxyUser = sProxyUser;
    }

    public String getsProxyPassword() {
        return sProxyPassword;
    }

    public void setsProxyPassword(String sProxyPassword) {
        this.sProxyPassword = sProxyPassword;
    }

    public boolean isUseProxy() {
        return bUseProxy;
    }

    public void setUseProxy(boolean bUseProxy) {
        this.bUseProxy = bUseProxy;
    }

}
