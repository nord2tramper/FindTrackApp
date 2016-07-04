package com.dailyway.findtrack;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by nord_tramper on 02/07/16.
 */
public class GeoPointTest {

    @org.junit.Test
    public void testDistanceBetween() throws Exception {
        double  expectedDistance = 353.08;
        GeoPoint point1 = new GeoPoint(54.872578,32.029831,"base");
        GeoPoint point2 = new GeoPoint(55.713581,37.387812,"reference");

        double actualDistance = point1.distanceBetween(point2);
        assertEquals(expectedDistance,actualDistance,2);

    }

    @Test
    public void testParseStringCoord() throws Exception {
        String sLat = "N54 52.3547";
        String sLon = "E32 01.7899";
        double dLat = 54.872578;
        double dLon = 32.029831;

        GeoPoint gPoint = new GeoPoint(sLat,sLon,"base");

        assertEquals(gPoint.getLatitude(),dLat,0.000001);
        assertEquals(gPoint.getLongitude(),dLon,0.000001);
    }
}