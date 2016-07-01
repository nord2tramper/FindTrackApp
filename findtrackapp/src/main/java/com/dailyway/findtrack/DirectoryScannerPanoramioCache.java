package com.dailyway.findtrack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by nkiselev on 08.09.2014.
 * Scanner for panoramio KML cache
 */
public class DirectoryScannerPanoramioCache extends DirectoryScanner {
    public DirectoryScannerPanoramioCache(String path) throws IOException {
        scanPath   = path;
    }

    protected void scanDir(String pDir) throws IOException{
        GeoFile curTrack;
        double dist;
        //Get file list from current directory
        try{
            String[] files = new File(pDir).list();
            for(int i=0; i < files.length; i++){
                if (!(new File(pDir+"\\"+files[i])).isDirectory()){//file
                    if (files[i].indexOf(".kml") > 0){
                        log("Start to scan track "+files[i]);
                        curTrack = new GeoFile("Temp",pDir + "\\" + files[i]);
                        //save all points into reference points
                        for (GeoPoint p:curTrack.points){
                            lPoints.add(p);
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
            log("Founded points count:" + lPoints.size());
        }
        catch(NullPointerException e){
            log("Directory "+pDir+" not found!");
            e.printStackTrace();
        }
    }
    public ArrayList<GeoPoint> getResult(){
        return lPoints;
    }
}
