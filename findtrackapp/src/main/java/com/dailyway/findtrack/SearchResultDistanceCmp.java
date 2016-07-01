package com.dailyway.findtrack;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: nkiselev
 * Date: 03/03/14
 * Time: 18:40
 * To change this template use File | Settings | File Templates.
 */
public class SearchResultDistanceCmp implements Comparator<SearchResult> {
    public int compare(SearchResult r1,SearchResult r2){
        if (r1.getMinDistance() > r2.getMinDistance()) return 1;
        if (r1.getMinDistance() < r2.getMinDistance()) return -1;
        return 0;
    }
}
