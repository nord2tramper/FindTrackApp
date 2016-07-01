package com.dailyway.findtrack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.FileNameMap;
import java.security.PrivateKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: nkiselev
 * Date: 28/02/14
 * Time: 17:15
 * To change this template use File | Settings | File Templates.
 * GeoFile - any file with points, tracks and routes
 * Now supported WPT,PLT,GPX,KML
 * GeoFile - container for GeoPoint + track attributes like name and path
 */
public class GeoFile {
    private String name;
    private String path;
    private BufferedReader reader;
    private int row;
    private String curFileLine;
    private int fileType;
    public ArrayList<GeoPoint> points;


    //File type Constants
    public int OZI_WPT = 1;
    public int OZI_PLT = 2;
    public int GPX = 3;
    public int KML = 4;

    //GPX tag name set
    private static String GPX_TAG_TRK = "trk";
    private static String GPX_TAG_NAME = "name";
    private static String GPX_TAG_TRKPOINT = "trkpt";
    private static String GPX_TAG_POINT = "wpt";
    private static String GPX_TAG_TRKSEQ = "trkseg";
    private static String GPX_TAG_ATTR_LAT = "lat";
    private static String GPX_TAG_ATTR_LON = "lon";

    //KML tag name set
    private static String KML_DOCUMENT = "Document";
    private static String KML_FOLDER_WAYPOINTS = "Waypoints";
    private static String KML_FOLDER_TRACKS = "Tracklogs";
    private static String KML_TAG_FOLDER = "Folder";
    private static String KML_TAG_NAME = "name";
    private static String KML_TAG_PLACEMARK = "Placemark";
    private static String KML_TAG_COORIDINATES = "coordinates";
    private static String KML_TAG_DESCR = "description";
    private static String HTML_A = "a";
    private static String HTML_IMG = "img";


    private void log(String s){
        System.out.println(s);
    }

    public GeoFile(){
        name = "";
        path = "";
        reader = null;
        row = 0;
        points = new ArrayList<GeoPoint>();
    }

    public GeoFile(String pName, String pPath) throws IOException {
        this.name = pName;
        this.path = pPath;
        this.reader = new BufferedReader(new FileReader(this.path));
        row = 0;
        points = new ArrayList<GeoPoint>();
        defineFileType();
        parse();
    }

    public double contains(GeoPoint p,double dist){
        double vDist;
        for (GeoPoint trackPoint:points){
            vDist = trackPoint.distanceBetween(p);
            if (vDist <= dist) return vDist;
        }
        return -1;//out of range of search
    }

    private void defineFileType() throws IOException{
        //read first lines and find out file type
        String fileLine = reader.readLine();
        row++;
        if (fileLine.toLowerCase().contains("oziexplorer track")) fileType = OZI_PLT;
        if (fileLine.toLowerCase().contains("oziexplorer waypoint")) fileType = OZI_WPT;
        if (fileLine.toLowerCase().contains("<?xml")){
            //this is XML-based file GPX or KML
            //read second line to determine type of file
            fileLine = reader.readLine();
            row++;
            if (fileLine.toLowerCase().contains("<gpx")) fileType = GPX;
            if (fileLine.toLowerCase().contains("<document") || fileLine.toLowerCase().contains("<kml")) fileType = KML;
        }
    }

    /*
        Find out file type and load all points into collection
    */
    private void parse() throws IOException{
        if (fileType == OZI_PLT || fileType == OZI_WPT){
            parseOZI();
        }
        if (fileType == GPX){
            parseGPX();
        }
        if (fileType == KML){
            parseKML();
        }
        System.out.println("File has been loaded. " + points.size() + " points loaded.");
        reader.close();
    }

    private void parseKML() throws IOException{
        //close file
        reader.close();

        //parse KML
        try{
            log("Start parsing KML file " + path);
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document kmlFile = docBuilder.parse (new File(path));
            kmlFile.getDocumentElement().normalize();

            //KML have two folders with geo-info Waypoints and Tracklogs
            //Look in Tracklogs only
            NodeList folders = kmlFile.getElementsByTagName(KML_TAG_FOLDER);
            for(int cFolder = 0; cFolder < folders.getLength(); cFolder++){
                Element folder = (Element)folders.item(cFolder);
                String fName = folder.getElementsByTagName(KML_TAG_NAME).item(0).getChildNodes().item(0).getNodeValue();
                if (fName.equals(KML_FOLDER_TRACKS))
                    parseKMLTrack(folder);
                else if (fName.equals(KML_FOLDER_WAYPOINTS))
                    parseKMLPoint(folder);
            }
            //if there is no Folders - assume we have only points under root Document
            if (folders.getLength() == 0){
                parseKMLPoint((Element)kmlFile.getElementsByTagName(KML_DOCUMENT).item(0));
            }
        }catch (SAXParseException err) {
            log("** Parsing error" + ", line "
                    + err.getLineNumber() + ", uri " + err.getSystemId());
            log(" " + err.getMessage());
        }catch (SAXException e) {
            Exception x = e.getException ();
            ((x == null) ? e : x).printStackTrace ();
        }catch (Throwable t) {
            t.printStackTrace ();
        }
    }

    private void parseKMLTrack(Element pTrack){
        NodeList nPlacemarks = pTrack.getElementsByTagName(KML_TAG_PLACEMARK);

        for (int cTracks=0; cTracks < nPlacemarks.getLength(); cTracks++){
            Element track = (Element)nPlacemarks.item(cTracks);
            String vTrackName;
            try{
                vTrackName = track.getElementsByTagName(KML_TAG_NAME).item(0).getChildNodes().item(0).getNodeValue();
            }catch (NullPointerException ne){
                vTrackName = "undefined";
            }

            String vTrackAllCoord = track.getElementsByTagName(KML_TAG_COORIDINATES).item(0).getChildNodes().item(0).getNodeValue();
            String[] aTrackCoord = vTrackAllCoord.split("\\s");
            //for (int cPoint=0; cPoint < aTrackCoord.length; cPoint++){
            int i = 0;
            for (String sPoint:aTrackCoord){
                points.add(new GeoPoint(new Float(sPoint.split(",")[1]),new Float(sPoint.split(",")[0]),vTrackName+":"+i));
                i++;
            }
        }
    }

    private void parseKMLPoint(Element pPoint){
        //get all <Placemark>
        NodeList lPoints = pPoint.getElementsByTagName(KML_TAG_PLACEMARK);
        for (int cPoints=0; cPoints < lPoints.getLength(); cPoints++){
            String vPointName = "";
            Element point = (Element)lPoints.item(cPoints);
            try{
                vPointName = point.getElementsByTagName(KML_TAG_NAME).item(0).getChildNodes().item(0).getNodeValue();
            }
            catch(NullPointerException npe){
                    vPointName = "Default";
            }
            String vPointCoord = point.getElementsByTagName(KML_TAG_COORIDINATES).item(0).getChildNodes().item(0).getNodeValue();
            String vDescription = point.getElementsByTagName(KML_TAG_DESCR).item(0).getChildNodes().item(0).getNodeValue();
            //try to get picture link from description
            String vPictureURL = "";
            if (vDescription.indexOf("<img src=\"http://mw2.google.com") > 0) {
                vPictureURL = vDescription.substring(vDescription.indexOf("<img src=\"http://mw2.google.com") + 10, vDescription.indexOf(".jpg") + 4).replace("medium", "small");
            }
            //Check each point
            points.add(new GeoPoint(new Float(vPointCoord.split(",")[1]),new Float(vPointCoord.split(",")[0]),vPointName,vPictureURL));
        }
    }


    private void parseGPX() throws IOException{
        //close file
        reader.close();

        //parse GPX
        try{
            log("Start parsing GPX file " + path);
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document gpxFile = docBuilder.parse (new File(path));
            gpxFile.getDocumentElement().normalize();

            NodeList lGPXTracks = gpxFile.getElementsByTagName(GPX_TAG_TRK);//get all tracks from GPX file
            log("There are "+lGPXTracks.getLength()+" tracks in the file.");

            for(int cTrackNo = 0; cTrackNo < lGPXTracks.getLength(); cTrackNo++){ //foreach track
                 Element track = (Element) lGPXTracks.item(cTrackNo);

                //get track name
                String vTrcName;
                try{
                    vTrcName = ((Element)track.getElementsByTagName(GPX_TAG_NAME).item(0)).getChildNodes().item(0).getNodeValue();
                }catch (NullPointerException ne){
                    vTrcName = "undefined";
                }
                log("Scan track :"+vTrcName);

                NodeList trcPoints = track.getElementsByTagName(GPX_TAG_TRKPOINT);
                for (int cPointNo=0; cPointNo< trcPoints.getLength(); cPointNo++){
                    Element tPoint = (Element)trcPoints.item(cPointNo);

                    Float vPointLat = new Float(tPoint.getAttribute(GPX_TAG_ATTR_LAT));
                    Float vPointLng = new Float(tPoint.getAttribute(GPX_TAG_ATTR_LON));

                    points.add(new GeoPoint(vPointLat,vPointLng,vTrcName+":"+cPointNo));
                }
            }

            //get all waypoints

            NodeList lGPXPoints = gpxFile.getElementsByTagName(GPX_TAG_POINT);//get all points from GPX file
            for(int cPointNo = 0; cPointNo < lGPXPoints.getLength(); cPointNo++){ //foreach track
                 Element point = (Element) lGPXPoints.item(cPointNo);

                //get point name
                String vPointName;
                try{
                    vPointName = ((Element)point.getElementsByTagName(GPX_TAG_NAME).item(0)).getChildNodes().item(0).getNodeValue();
                }catch (NullPointerException ne){
                    vPointName = "undefined";
                }

                Float vPointLat = new Float(point.getAttribute(GPX_TAG_ATTR_LAT));
                Float vPointLng = new Float(point.getAttribute(GPX_TAG_ATTR_LON));

                points.add(new GeoPoint(vPointLat,vPointLng,vPointName));
            }

        }catch (SAXParseException err) {
            log("** Parsing error" + ", line "
                    + err.getLineNumber() + ", uri " + err.getSystemId());
            log(" " + err.getMessage());
        }catch (SAXException e) {
            Exception x = e.getException ();
            ((x == null) ? e : x).printStackTrace ();
        }catch (Throwable t) {
            t.printStackTrace ();
        }

    }

    private void parseGPXTrack(){

    }

    private void parseGPXPoints(){

    }

    /*
        Load OZI file into collection
     */
    private void parseOZI() throws IOException{
        String[] items;
        String name = "";
        double lat  = 0;
        double lon  = 0;

        try{
            while ((curFileLine = reader.readLine()) != null){
                row++;
                items = curFileLine.split(",");
                if ((row >= 5 && fileType == OZI_WPT) || (row >= 7 && fileType == OZI_PLT)){
                    if (fileType == OZI_WPT){
                            name    = items[1].trim();
                            lon     = new Float(items[2].trim());
                            lat     = new Float(items[3].trim());
                    };
                    if (fileType == OZI_PLT){
                        name    = "";
                        lon     = new Float(items[0].trim());
                        lat     = new Float(items[1].trim());
                    };
                    points.add(new GeoPoint(lon,lat,name));
                }
            }
        }catch (IOException ie){
              reader.close();
        }
        reader.close();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

