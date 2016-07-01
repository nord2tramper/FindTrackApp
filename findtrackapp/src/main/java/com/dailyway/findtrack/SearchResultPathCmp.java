package com.dailyway.findtrack;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: nkiselev
 * Date: 03/03/14
 * Time: 18:43
 * To change this template use File | Settings | File Templates.
 */
public class SearchResultPathCmp implements Comparator<SearchResult>{

    public int compare(SearchResult r1, SearchResult r2){
        return r1.getTrackPath().compareTo(r2.getTrackPath());
    }
}
