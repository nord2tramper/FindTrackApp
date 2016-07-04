package com.dailyway.findtrack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: nkiselev
 * Date: 28/02/14
 * Time: 14:52
 * To change this template use File | Settings | File Templates.
 * Class represent one piont with coordinates, name and some usefull constants
 */
public class GeoPoint {
    private static double eR = 6371;//Earth radius in kilometers
    private static double eRm = 6372795;//Earth radius in meters
    public static double pi = 3.14159265358979;//Pi
    private double latitude;
    private double longitude;
    private String name;
    private String pictURL;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }




    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public GeoPoint(){
        latitude = 0;
        longitude = 0;
        name = "";
    }

    public GeoPoint(double pLa, double pLo, String pName){
        latitude = pLa;
        longitude = pLo;
        name = pName;
    }

    public GeoPoint(double pLa, double pLo, String pName, String pPictURL){
        latitude = pLa;
        longitude = pLo;
        name = pName;
        pictURL = pPictURL;
    }

    public GeoPoint(String pLa,String pLo,String pName){
        name = pName;
        latitude = parseStringCoord(pLa);
        longitude = parseStringCoord(pLo);
    }

    //parse one coordinate
    protected double parseStringCoord(String pVal){
        String vSign = "1";
        char   cSign;
        String vDeg = "0";
        String vMin = "0";
        String vSec = "0";
        //clear input string
        String vVal = pVal.trim().replaceAll("\\s+"," ");//get rid of spaces around the string and more then 1 space inside
        //Patterns
        //                                 N         dd    .     dddd ^
        Pattern pDeg = Pattern.compile("(^\\w?)\\s?(\\d+[\\.,]\\d*.).?$");//Ndd.ddd
        //                                 N         dd ^      mm      .    mmm '
        Pattern pMin = Pattern.compile("(^\\w?)\\s?(\\d+).?\\s(\\d+[\\.,]\\d*).?$");//Ndd^ mm.mmm'
        //                                 N         dd ^      mm  '       ss     ,    sss "
        Pattern pSec = Pattern.compile("(^\\w?)\\s?(\\d+).?\\s(\\d+).?\\s(\\d+[\\.,]\\d*).{1,2}$");

        Matcher mDeg = pDeg.matcher(vVal);
        Matcher mMin = pMin.matcher(vVal);
        Matcher mSec = pSec.matcher(vVal);

        int coordSign;

        if (mDeg.find()){
            vSign = mDeg.group(1);
            vDeg  = mDeg.group(2);
        }else if (mMin.find()){
            vSign = mMin.group(1);
            vDeg  = mMin.group(2);
            vMin  = mMin.group(3);
        }else if (mSec.find()){
            vSign = mSec.group(1);
            vDeg  = mSec.group(2);
            vMin  = mSec.group(3);
            vSec  = mSec.group(4);
        }

        //get coordinate sign
        cSign = vSign.substring(0,1).toCharArray()[0];
        switch (cSign){
            case 'N':case 'n':case 'E':case 'e': coordSign = 1;break;
            case 'S':case 's':case 'W':case 'w': coordSign = -1;break;
            default: coordSign = 1;break;
        }

        //get double value from string

        return coordSign*(new Float(vDeg) + new Float(vMin)/60 + new Float(vSec)/360);
    }

    public double distanceBetween(GeoPoint pPoint){
        return distanceBetweenPrecise(pPoint);
    }

    public double distanceBetweenPrecise(GeoPoint pPoint){
        double lat1 = this.latitude*pi/180;
        double lon1 = this.longitude*pi/180;
        double lat2 = pPoint.getLatitude()*pi/180;
        double lon2 = pPoint.getLongitude()*pi/180;

        return Math.atan(Math.sqrt(Math.pow(Math.cos(lat2)*Math.sin(lon1-lon2),2) + Math.pow((Math.cos(lat1)*Math.sin(lat2) - Math.sin(lat1)*Math.cos(lat2)*Math.cos(lon1-lon2)),2))/(Math.sin(lat1)*Math.sin(lat2) + Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon1-lon2)))*eR;
    }

    public double distanceBetweenSimple(GeoPoint pPoint){
        double lat1 = this.latitude*pi/180;
        double lon1 = this.longitude*pi/180;
        double lat2 = pPoint.getLatitude()*pi/180;
        double lon2 = pPoint.getLongitude()*pi/180;

        return Math.acos(Math.sin(lat1)*Math.sin(lat2) + Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon1 - lon2))*eR;
    }

    public String getPictURL() {
        return pictURL;
    }

    public void setPictURL(String pictURL) {
        this.pictURL = pictURL;
    }
}
