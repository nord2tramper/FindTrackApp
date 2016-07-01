package com.dailyway.findtrack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: nkiselev
 * Date: 03/03/14
 * Time: 14:02
 * To change this template use File | Settings | File Templates.
 * Container for save all SearchResults - if we have found a lot of files
 */
public class SearchResults {
    ArrayList<SearchResult> results = new ArrayList<SearchResult>();

    public void addResult(String path,double dist){
        boolean found = false;
        SearchResult res;

        //try to search
        for(SearchResult r:results){
            if (r.getTrackPath().equals(path)){//if found - just update distance
                found = true;
                r.setMinDistance(dist);
            }

        }
        if(!found){//if not found - add new result
            results.add(new SearchResult(path,dist));
        }
    }

    public ArrayList<SearchResult> getResults(){
        return results;
    }

    /*sort results asc or desc by destination on path
        pType:  0 - by destination
                1 - by path
        pOrder: 0 - asc
                1 - desc
    */
    public void sort(int pType, int pOrder){
        switch (pType){
            case 0: switch (pOrder){
                        case 0: Collections.sort(results, new SearchResultDistanceCmp());break;
                        case 1: Collections.sort(results, Collections.reverseOrder(new SearchResultDistanceCmp()));break;
                    };break;
            case 1: switch (pOrder){
                        case 0:Collections.sort(results, new SearchResultPathCmp());break;
                        case 1:Collections.sort(results, Collections.reverseOrder(new SearchResultPathCmp()));break;
                    }
        }
    }
}
