package com.dailyway.findtrack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by IntelliJ IDEA.
 * User: nkiselev
 * Date: 28/02/14
 * Time: 16:59
 * To change this template use File | Settings | File Templates.
 * Scan directory to find out all files on the Distance from the reference point or points in file
 */
/*
    Class get directory path and
 */
public class DirectoryScanner {
    protected String scanPath;
    private String srcPath;
    private double distance;
    private GeoFile poiFile;
    protected ArrayList<GeoPoint> lPoints = new ArrayList<GeoPoint>();
    private SearchResults results = new SearchResults();

    protected void log(String s){
        System.out.println(s);
    }

    public DirectoryScanner(){
        this.scanPath   = "";
        this.srcPath    = "";
        this.distance = 0;
    }

    public DirectoryScanner(String src, String path, String distance) throws IOException{
        this.scanPath   = path;
        this.srcPath    = src;
        this.distance = new Float(distance);

        //load all points
        poiFile = new GeoFile("Refernce POI",this.srcPath);
        lPoints.addAll(poiFile.points); //all points are in our object
    }

    public DirectoryScanner(GeoPoint searchPoint, String path, String distance){
        this.scanPath = path;
        this.distance = new Float(distance);
        lPoints.add(searchPoint);//set only one point for search
    }

    public SearchResults getResults() {
        return results;
    }

    public void scan() throws IOException{
        scanDir(scanPath);
    }

    protected void scanDir(String pDir) throws IOException{
        GeoFile curTrack;
        double dist;
        //Get file list from current directory
        try{
            String[] files = new File(pDir).list();
            for(int i=0; i < files.length; i++){
                if (!(new File(pDir+"\\"+files[i])).isDirectory()){//file
                    if (files[i].indexOf(".plt") > 0 || files[i].indexOf(".kml") > 0 || files[i].indexOf(".gpx") > 0){
                        log("Start to scan track "+files[i]);
                        curTrack = new GeoFile("Temp",pDir + "\\" + files[i]);
                        for (GeoPoint p:lPoints){
                            dist = curTrack.contains(p,distance);
                            if (dist != -1){
                                   results.addResult(pDir + "\\" + files[i],dist);
                            }
                        }
                    }else{
                        log(files[i]+" was skipped.");
                    }
                }
                else{//directory
                    scanDir(pDir + "\\" + files[i]);
                }
            }
            //Show result
            log("Founded tracks count:" + results.getResults().size());
        }
        catch(NullPointerException e){
            log("Directory "+pDir+" not found!");
            e.printStackTrace();
        }
    }

    public void sort(int pType, int pOrder){
        results.sort(pType,pOrder);
    }

}
