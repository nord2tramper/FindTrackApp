package com.dailyway.findtrack;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: nkiselev
 * Date: 03/03/14
 * Time: 11:28
 * To change this template use File | Settings | File Templates.
 */
public class SearchResult {

    private String trackPath;
    private double minDistance;

    public SearchResult(String path, double distance){
        this.trackPath = path;
        this.minDistance = distance;
    }

    public void setMinDistance(double distance){
        if (this.minDistance > distance) this.minDistance = distance;
    }

    public double getMinDistance(){
        return this.minDistance;
    }

    public String getTrackPath() {
        return trackPath;
    }

}
