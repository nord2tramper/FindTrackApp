package com.dailyway.findtrack;

import java.io.*;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.IImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.imaging.util.IoUtils;


/**
 * Created by nkiselev on 08.09.2014.
 * Downloader for Panoramio cash medium quality using small quality file name in SASPlanet cash
 */
public class PanoramioDownloader {
    private Collection<GeoPoint> lPoints;
    private String vDest;

    private void log(String s){
        System.out.println(s);
    }
    public PanoramioDownloader(){
        lPoints = new ArrayList<GeoPoint>();
        vDest = ".\\";
    }

    public PanoramioDownloader(ArrayList<GeoPoint> plPoints,String pDest){
        lPoints = plPoints;
        vDest = pDest;
    }

    public void download(FindTrackSettings pSettings){
        URL pictURL;
        String pictFile;
        String fileName;
        //PROXY!!!!
        if (pSettings.isUseProxy()) {
            System.setProperty("http.proxyHost", pSettings.getsProxyHost());
            System.setProperty("http.proxyPort", pSettings.getsProxyPort());
            Authenticator.setDefault(new ProxyAuth(pSettings.getsProxyUser(),pSettings.getsProxyPassword()));
        }
        //=========*/

        //open directory
       //for each point
       for (GeoPoint p:lPoints){
           //download picture
           try {
               pictURL  = new URL(p.getPictURL());
               pictFile = pictURL.getFile();
               //fileName = vDest+"\\"+pictFile.substring(pictFile.lastIndexOf("/"));
               fileName = vDest+"\\_"+p.getName().replace(",","").trim().replace(" ", "_")+".jpg";//file with no exif
               pictFile = vDest+"\\" +p.getName().replace("small","medium").replace(",","").trim().replace(" ", "_")+".jpg";//file with no exif

               InputStream  is = pictURL.openStream();
               OutputStream os = new FileOutputStream(fileName);
               byte [] b = new byte[2048];//read by 2Kb
               int len;

               while ((len = is.read(b)) != -1){
                   os.write(b,0,len);
               }

               is.close();
               os.close();
               log("File "+fileName+" created.");
               //add GeoTag using exiftool.exe - external programm
               writeGeoData(new File(fileName),new File(fileName.replace("\\_","\\")),p.getLongitude(),p.getLatitude());
               new File(fileName).delete();
           }
           catch (MalformedURLException mu){
               log(p.getPictURL()+" - invalid URL");
           }
           catch (ImageReadException ire){
               log("Error while reading EXIF!");
               log(ire.getMessage());
           }
           catch (ImageWriteException iwe){
               log("Error while writing EXIF!");
               log(iwe.getMessage());
           }
           catch (IOException mu){
               log(p.getPictURL()+" error while reading\\writing!");
           }
       }
    }

    private static void writeGeoData(File jpegImageFile, File destImageFile,double pLon, double pLat) throws IOException, ImageReadException, ImageWriteException
    {
        OutputStream os = null;
        boolean canThrow = false;
        try {
            TiffOutputSet outputSet = null;

            // note that metadata might be null if no metadata is found.
            IImageMetadata metadata = Imaging.getMetadata(jpegImageFile);
            JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if (null != jpegMetadata) {
                // note that exif might be null if no Exif metadata is found.
                TiffImageMetadata exif = jpegMetadata.getExif();

                if (null != exif) {
                    outputSet = exif.getOutputSet();
                }
            }

            // if file does not contain any exif metadata, we create an empty
            // set of exif metadata. Otherwise, we keep all of the other
            // existing tags.
            if (null == outputSet) {
                outputSet = new TiffOutputSet();
            }

            {
                outputSet.setGPSInDegrees(pLon, pLat);
            }

            os = new FileOutputStream(destImageFile);
            os = new BufferedOutputStream(os);

            new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os, outputSet);
            canThrow = true;
        } finally {
            IoUtils.closeQuietly(canThrow, os);
        }
    }
}
