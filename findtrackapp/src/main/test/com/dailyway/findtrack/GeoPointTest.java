package com.dailyway.findtrack;

import static org.junit.Assert.*;

/**
 * Created by nord_tramper on 02/07/16.
 */
public class GeoPointTest {

    @org.junit.Test
    public void testDistanceBetween() throws Exception {
        double  expectedDistance = 100;
        GeoPoint point1 = new GeoPoint(1,2,"base");
        GeoPoint point2 = new GeoPoint(2,2,"reference");

        double actualDistance = point1.distanceBetween(point2);
        assertEquals(expectedDistance,actualDistance,0);

    }
}