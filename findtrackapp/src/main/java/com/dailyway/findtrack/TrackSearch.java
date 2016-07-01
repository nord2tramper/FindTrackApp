package com.dailyway.findtrack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.Node;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by IntelliJ IDEA.
 * User: nkiselev
 * Date: 2/3/12
 * Time: 11:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class TrackSearch {
    private ArrayList foundedTracks;
    private Float pLat;
    private Float pLng;
    private float pDist;
    private String pDir;
    private int trackStringNum;
    private int trackType;
    private String pSrcFile;//file with refernce points
    private ArrayList<GeoPoint> lWpt;//array with points

    //Constants
    private static boolean cLog = true;//Debug messages on|off
    private static int cMeters = 0;//0 - distance in Kilometers 1 - distance in meters
    private static double eR = 6371;//Earth radius in Kilometers
    private static double eRm = 6372795;//Earth radius in meters
    public static double pi = 3.14159265358979;//Pi
    private static int PLT_TRACK = 0;
    private static int GPX_TRACK = 1;
    private static int KML_TRACK = 2;
    private static int WPT_FILE = 3;
    private static String GPX_TAG_TRK = "trk";
    private static String GPX_TAG_NAME = "name";
    private static String GPX_TAG_TRKSEQ = "trkseg";
    private static String GPX_TAG_ATTR_LAT = "lat";
    private static String GPX_TAG_ATTR_LON = "lon";
    private static String KML_FOLDER_WAYPOINTS = "Waypoints";
    private static String KML_FOLDER_TRACKS = "Tracklogs";
    private static String KML_TAG_FOLDER = "Folder";
    private static String KML_TAG_NAME = "name";
    private static String KML_TAG_PLACEMARK = "Placemark";
    private static String KML_TAG_COORIDINATES = "coordinates";

    //Patterns for point in text-base files (PLT,WPT)
    private Pattern wptPointPattern = Pattern.compile("\\d+\\s*,(.+),\\s*(\\d+.\\d+)\\s*,\\s*(\\d+.\\d+)\\s*,.+");
    private Pattern pltPointPattern = Pattern.compile("\\s*(\\d+\\d+)\\s*,\\s*(\\d+.\\d+)\\s*,.+");

    //logging functionality
    private void log(String s){
        log(s,false);
    }

    private void log(String s, boolean _log){
        if (cLog || _log) System.out.println(s);
    }
    //------------------------------------------------------

    //Return array with all founded tracks
    public ArrayList getResult(){
        return foundedTracks;
    }

    private void banner(){
        log("===========================================================",true);
        log("Track finder",true);
        log("Written by nord_tramper (nord-tramper@expeditionlife.ru)",true);
        log("ExpeditionLife.Ru",true);
        log("===========================================================",true);
    }
    public TrackSearch(){
        foundedTracks = new ArrayList(0);
        pLat = new Float(0);
        pLng = new Float(0);
        pDir = ".";
        pDist = 1;
        trackStringNum = 0;
        pSrcFile = "";

    }

    public TrackSearch(String pLatDeg, String pLatMin, String pLngDeg, String pLngMin, String pDist){
        preInit(pLatDeg, pLatMin, pLngDeg, pLngMin, pDist, ".");
    }
    public TrackSearch(String pLatDeg, String pLatMin, String pLngDeg, String pLngMin, String pDist, String pDir){
        preInit(pLatDeg, pLatMin, pLngDeg, pLngMin, pDist, pDir);
    }

    public TrackSearch(String pSearchFile, String pDist, String pDir) throws IOException{
        lWpt = new ArrayList();
        this.pSrcFile = pSearchFile;
        parseSrcFile();
        //set others
        preInit("0", "0", "0", "0", pDist, pDir);
    }


    public TrackSearch(double pLat, double pLng, float pDist){
        init(pLat,pLng,pDist,".");
    }

    public TrackSearch(double pLat, double pLng, float pDist, String pDir){
        init(pLat,pLng,pDist,pDir);
    }

    //Initialize all parameters (I hope all)
    private void preInit(String pLatDeg, String pLatMin, String pLngDeg, String pLngMin, String pDist, String pDir){
        double vLat = ((new Float(pLatDeg.substring(1))) + ((new Float(pLatMin)))/60);
        double vLng = ((new Float(pLngDeg.substring(1))) + ((new Float(pLngMin)))/60);
        if (pLatDeg.substring(0,1) == "S"){vLat = -vLat;}
        if (pLngDeg.substring(0,1) == "W"){vLng = -vLng;}

        init(vLat,vLng,new Float(pDist).floatValue(),pDir);
    }

    private void init(double pLat, double pLng, float pDist, String pDir){
        trackStringNum = 0;
        foundedTracks = new ArrayList(0);

        this.pLat = new Float(pLat*pi/180);
        this.pLng = new Float(pLng*pi/180);
        this.pDist = pDist;
        if (cMeters == 1) this.pDist = 1000*this.pDist;
        if (pDir.substring(pDir.length()-1).equals("\\"))
            this.pDir = pDir.substring(0,pDir.length()-1);
        else
            this.pDir = pDir;
        log("Look into:"+this.pDir);

    }

    //Main - using for standalone run from command line
    //N54 27.0364 E36 45.8540 1 C:\work\java\FindeTrackApp\tracks\
    public static void main(String[] args){

        //Get and print parameters
        if (args.length < 5){
            System.out.println("Not enough parameters!");
            System.out.println("Please use: N/SLatDeg LatMin E/WLngDeg LngMin Distance [Folder] ");
            System.exit(0);
        }
        System.out.println("Parameters count:"+args.length);
        System.out.println("Run search tracks for point:" + args[0] + " " + args[1] + " " + args[2] + " " + args[3] + " using proximity distance " + args[4] + "km");
        TrackSearch ts = null;
        if (args.length == 6)
            ts = new TrackSearch(args[0],args[1],args[2],args[3],args[4],args[5]);
        else
            ts = new TrackSearch(args[0],args[1],args[2],args[3],args[4]);
//        ts.banner();
        ts.scan();
        ts.printFoundedTracks();
//        ts.banner();
    }

    //Scan directory for tracks
    public void scan(){
        log("scan()-"+pDir);
        scanDir(pDir);
    }

    public void scan(String pDir){
        log("scan(pDir)-"+pDir);
        scanDir(pDir);
    }

    private void scanDir(String pDir){
        //Get file list from current directory
        try{
            String[] files = new File(pDir).list();
            for(int i=0; i < files.length; i++){
                if (!(new File(pDir+"\\"+files[i])).isDirectory()){//file
                    if (files[i].indexOf(".plt") > 0 || files[i].indexOf(".kml") > 0 || files[i].indexOf(".gpx") > 0){
                        log("Start to scan track "+files[i]);
                        scanTrack(pDir+"\\"+files[i]);
                    }else{
                        log(files[i]+" was skipped.");
                    }

                }
                else{//directory
                    scanDir(pDir+"\\"+files[i]);
                }
            }
            //Show result
            log("Founded tracks count:" + foundedTracks.size());
        }
        catch(NullPointerException e){
            log("Directory "+pDir+" not found!");
            e.printStackTrace();
        }
    }

    private void scanTrack(String pTrack){
        try{
            trackStringNum = 0;
            BufferedReader bfTrack = new BufferedReader( new FileReader(pTrack));
            trackType = getFileType(bfTrack);
            if (trackType == PLT_TRACK)         parsePLT(bfTrack,pTrack);
            else if (trackType == KML_TRACK)    parseKML(bfTrack,pTrack);
            else if (trackType == GPX_TRACK)    parseGPX(bfTrack, pTrack);
            else log("Track type undefined!",true);
        }
        catch (FileNotFoundException fne){
            log("Error while opening file "+pTrack+"! Skip it from search.");
            fne.printStackTrace();
        }
        catch(IOException ie){
            log("Error while reading file "+pTrack+"! Skip it from search.");
            ie.printStackTrace();
        }
        catch (NullPointerException e){
            log("File has incorrect format! Skip it from search");
        }
    }

    /*
        Indentify type of given file: OziExplorer, GPX, KML
     */
    private int getFileType(BufferedReader br) throws IOException, NullPointerException{
        String fileLine;
        int fileType = 0;
        //read first line
        fileLine = br.readLine();
        trackStringNum++;
        if (fileLine.toLowerCase().contains("oziexplorer track")) fileType = PLT_TRACK;
        if (fileLine.toLowerCase().contains("oziexplorer waypoint")) fileType = WPT_FILE;
        if (fileLine.toLowerCase().contains("<?xml")){
            //this is XML-based file GPX or KML
            //read second line to determine type of file
            fileLine = br.readLine();
            trackStringNum++;
            if (fileLine.toLowerCase().contains("<gpx")) fileType = GPX_TRACK;
            if (fileLine.toLowerCase().contains("<document") || fileLine.toLowerCase().contains("<kml")) fileType = KML_TRACK;
        }
        return fileType;
    }

    private void parsePLT(BufferedReader plt, String trackName) throws IOException{
        String trackLine;
        while ((trackLine = plt.readLine()) != null){
            trackStringNum++;
            if (trackStringNum > 6 && parsePLTTrackLine(trackLine)){
                foundedTracks.add(trackName);
                plt.close();
                break;//Right point found, skip other from scanning
            };
        }
    }

    private void parseKML(BufferedReader kml, String trackName) throws IOException{
        log("=====Start of KML parser=====");
        log("Close and open "+trackName);
        kml.close();
        //=======================================================
        try{
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse (new File(trackName));
            doc.getDocumentElement().normalize();
            log("Root element is " + doc.getDocumentElement().getNodeName());

            //Go through all folders inside KML (waypoints, tracks)
            NodeList folders = doc.getElementsByTagName(KML_TAG_FOLDER);
            for (int cFolder=0; cFolder < folders.getLength(); cFolder++){
                Element folder = (Element)folders.item(cFolder);
                String fName = folder.getElementsByTagName(KML_TAG_NAME).item(0).getChildNodes().item(0).getNodeValue();
                log(fName);
                if (fName.equals(KML_FOLDER_WAYPOINTS)) kmlScanPoints(folder,trackName);
                else if (fName.equals(KML_FOLDER_TRACKS)) kmlScanTracks(folder,trackName);
            }
        }
        catch (SAXParseException err) {
            log("** Parsing error" + ", line "
                    + err.getLineNumber() + ", uri " + err.getSystemId(), true);
            log(" " + err.getMessage(), true);
        }catch (SAXException e) {
            Exception x = e.getException ();
            ((x == null) ? e : x).printStackTrace ();
        }catch (Throwable t) {
            t.printStackTrace ();
        }

        //=======================================================
        log("=====End of KML parser=====");
    }

    //Check one coordinate unit <lng,lat,alt>
    private boolean kmlCheckPoint(String pCoord){
        //get latitude and longitude from coordinate unit
        String[] units = pCoord.split(",");
        double vLat = new Float(units[1]).doubleValue();
        double vLng = new Float(units[0]).doubleValue();
        return isSuitable(vLat,vLng);
    }

    private void kmlScanPoints(Element waypoints, String trackName){
        //get all <Placemark>
        NodeList points = waypoints.getElementsByTagName(KML_TAG_PLACEMARK);
        for (int cPoints=0; cPoints < points.getLength(); cPoints++){
            String vPointName = "";
            Element point = (Element)points.item(cPoints);
            try{
                vPointName = point.getElementsByTagName(KML_TAG_NAME).item(0).getChildNodes().item(0).getNodeValue();
            }
            catch(NullPointerException npe){
                    vPointName = "Default";
            }
            String vPointCoord = point.getElementsByTagName(KML_TAG_COORIDINATES).item(0).getChildNodes().item(0).getNodeValue();
            //Check each point
            if (kmlCheckPoint(vPointCoord)){
                //Point found! Save file and point name
                foundedTracks.add(trackName+": Point: "+vPointName);
                break;//Skip further scan
            }
        }
    }

    private void kmlScanTracks(Element tracks, String trackName){
        //get all <Placemark> (tracks int this folder)
        NodeList points = tracks.getElementsByTagName(KML_TAG_PLACEMARK);
        for (int cTracks=0; cTracks < points.getLength(); cTracks++){
            Element track = (Element)points.item(cTracks);
            String vTrackName = track.getElementsByTagName(KML_TAG_NAME).item(0).getChildNodes().item(0).getNodeValue();
            String vTrackAllCoord = track.getElementsByTagName(KML_TAG_COORIDINATES).item(0).getChildNodes().item(0).getNodeValue();
            String[] aTrackCoord = vTrackAllCoord.split("\\s");
            for (int cPoint=0; cPoint < aTrackCoord.length; cPoint++){
                if (kmlCheckPoint(aTrackCoord[cPoint])){
                    //Point found! Save file and point name
                    foundedTracks.add(trackName+": Track: "+vTrackName);
                    break;//Skip further scan
                }
            }
        }
    }

    private void parseGPX(BufferedReader gpx, String trackName) throws IOException{
        log("=====Start of GPX parser=====");
        log("Close and open "+trackName);
        gpx.close();
        //=======================================================
        try{
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse (new File(trackName));
            doc.getDocumentElement().normalize();
            log("Root element is " + doc.getDocumentElement().getNodeName());
            //get all <trk> (GPX may contain more than one track inside)
            NodeList tracks = doc.getElementsByTagName(GPX_TAG_TRK);
            log("Tracks count in the file:" + tracks.getLength());
            for (int cTrackNo = 0; cTrackNo < tracks.getLength(); cTrackNo++){
                Element track = (Element) tracks.item(cTrackNo);

                //Get track name from <name> element
                Element trcName = (Element)track.getElementsByTagName(GPX_TAG_NAME).item(0);
                String vTrcName = trcName.getChildNodes().item(0).getNodeValue();
                log("Scan track :"+vTrcName);

                //Get all points <trkpt> from <trkseg> element of <trk>
                NodeList trcPoints = track.getElementsByTagName(GPX_TAG_TRKSEQ).item(0).getChildNodes();
                for (int cPointNo=0; cPointNo< trcPoints.getLength(); cPointNo++){
                    Element point = (Element)trcPoints.item(cPointNo);
                    Float vPointLat = new Float(point.getAttribute(GPX_TAG_ATTR_LAT));
                    Float vPointLng = new Float(point.getAttribute(GPX_TAG_ATTR_LON));
                    //Check each point
                    if (isSuitable(vPointLat,vPointLng)){
                        //Track found! Save track file: track name
                        foundedTracks.add(trackName+": Track: "+vTrcName);
                        break;//Skip further scan this track
                    }
                }
            }
        }
        catch (SAXParseException err) {
            log("** Parsing error" + ", line "
                    + err.getLineNumber() + ", uri " + err.getSystemId(), true);
            log(" " + err.getMessage(), true);
        }catch (SAXException e) {
            Exception x = e.getException ();
            ((x == null) ? e : x).printStackTrace ();
        }catch (Throwable t) {
            t.printStackTrace ();
        }

        //=======================================================
        log("====End of GPX parser====");
    }

    //Parse PLT track line and make decision is it in proximity distance or not?
    private boolean parsePLTTrackLine(String line){
        Float lat = null;
        Float lng = null;
        //get latitude and longitude in radians
        lat = new Float(line.substring(0, line.indexOf(",") - 1));
        lng = new Float(line.substring(line.indexOf(",") + 1, line.indexOf(",", line.indexOf(",") + 1)));
        return isSuitable(lat,lng);
    }

    //Check point distance and proximity distance. Convert track point into radians!!!!
    private boolean isSuitable(double pLat, double pLng){
        return (getPointDistance(this.pLat, this.pLng, new Float(pLat*pi/180), new Float(pLng*pi/180)) <= this.pDist);
    }

    private double getPointDistance(Float p1Lat, Float p1Lng, Float p2Lat, Float p2Lng){
        if (cMeters == 1)
            return Math.acos(Math.sin(p1Lat)*Math.sin(p2Lat) + Math.cos(p1Lat)*Math.cos(p2Lat)*Math.cos(p1Lng - p2Lng))*eRm;
        else
            return Math.acos(Math.sin(p1Lat)*Math.sin(p2Lat) + Math.cos(p1Lat)*Math.cos(p2Lat)*Math.cos(p1Lng - p2Lng))*eR;
    }

    public void printFoundedTracks(){
        for (int i = 0; i < foundedTracks.size(); i++){
            log(foundedTracks.get(i).toString());
        }
    }

    /* Functions related to scan points from reference file*/
    private void parseSrcFile() throws IOException{
        int fileType;
        //Get file type
        //open file
        BufferedReader br = new BufferedReader(new FileReader(pSrcFile));
        fileType = getFileType(br);
        //parse file and save all points from file
        if (fileType == PLT_TRACK){//ozi
            scanOziPLT(br);
        } if(fileType == WPT_FILE){
            scanOziWPT(br);
        }else if (fileType == GPX_TRACK){
            scanGPX(br);
        }else if (fileType == KML_TRACK){
            scanKML(br);
        }
    }

    private void scanOziPLT(BufferedReader br) throws IOException{

    }

    private void scanOziWPT(BufferedReader br) throws IOException{
        String ln;
        while ((ln = br.readLine()) != null){
            Matcher m = wptPointPattern.matcher(ln);
            if (m.find()){
                //parse and add new point in array
                lWpt.add(new GeoPoint(m.group(2),m.group(3),m.group(1)));
            }
        }
    }

    private void scanGPX(BufferedReader br) throws IOException{

    }

    private void scanKML(BufferedReader br) throws IOException{

    }



}
