package com.dailyway.findtrack;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * Created by nkiselev on 09.09.2014.
 */
public class ProxyAuth extends Authenticator {
    private String sUserName;
    private String sPassword;

    //default constructor
    public ProxyAuth(){
        sUserName = "";
        sPassword = "";
    }

    /*constructor with parameters, should be used with configuration files
        psUserName - user name for proxy authentication
        psPassword - password for proxy
     */
    public ProxyAuth(String psUserName, String psPassword){
        sUserName = psUserName;
        sPassword = psPassword;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        String username = sUserName;
        String password = sPassword;
        // Return the information (a data holder that is used by Authenticator)
        return new PasswordAuthentication(username, password.toCharArray());
    }

}
