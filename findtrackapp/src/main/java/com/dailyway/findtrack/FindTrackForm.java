package com.dailyway.findtrack;

import javax.swing.*;
import javax.swing.table.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: nkiselev
 * Date: 27/02/14
 * Time: 16:46
 * To change this template use File | Settings | File Templates.
 */
//public class FindTrackForm extends Applet{
public class FindTrackForm extends JFrame implements ActionListener{
    private JPanel pnMain;
    private JTabbedPane tpPointType;
    private JTextField fLat;
    private JTextField fLng;
    private JTextField fDir;
    private JTextField fDistance;
    private JTextField fCopyDir;
    private JButton btnSelectSource;
    private JButton btnSelectDest;
    private JButton btnFind;
    private JButton btnCopy;
    private JTextArea taLog;
    private JPanel pnSinglePoint;
    private JPanel pnFailPoint;
    private JLabel lbOnePoint;
    private JLabel lbLatitude;
    private JLabel lbLatitudeTempl;
    private JLabel lbLongitude;
    private JLabel lbLongitudeTmpl;
    private JLabel lbSourceDir;
    private JLabel lbDistance;
    private JLabel lbCopyTo;
    private JLabel lbPointsFromFile;
    private JButton btnSelectSrc;
    private JLabel lbSrcFileName;
    private JTextField fSrcFile;
    private JTable tbResults;
    private JButton btnGetPicts;
    private JPanel pnPanoramio;
    private JLabel lblPanoMsg1;

    /*
        My variables
    */
    SearchResults   foundedTracks;
    private DefaultTableModel tbResultsModel;
    public FindTrackSettings settings;

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() instanceof MenuItem) {
            String menuLabel = ((MenuItem)evt.getSource()).getLabel();
            if(menuLabel.equals("Exit")) {
                // close application, when exit is selected
                dispose();
                System.exit(0);
            } // end if
        } // end if
    } // end ActionPerformed


    /*
        Applet initialisation
    */
    public void init(){
        add(pnMain, java.awt.BorderLayout.CENTER);
    }

    /*===============================================================================
        Internal functions
    =================================================================================*/
    /*
        Check input
     */
    private boolean checkParameters(){
        boolean ret = true;
        if (fLat.getText().length() == 0){
            taLog.append("Latitude is empty. Please use N/Sdd mm.mmm format"+"\n");
            ret = false;
        }
        if (fLng.getText().length() == 0){
            taLog.append("Longitude is empty. Please use W/Edd mm.mmmm format"+"\n");
            ret = false;
        }
        if (fDistance.getText().length() == 0){
            taLog.append("Distance is empty. Set to default value 1 km"+"\n");
            fDistance.setText("1");
        }
        if (fDir.getText().length() == 0){
            taLog.append("Directory is empty. Set o default c:\\maps"+"\n");
            fDir.setText("c:\\Maps");
        }
        //Check data format
        try{
            String vLatDeg = fLat.getText().substring(0,fLat.getText().indexOf(" "));
            String vLatMin = fLat.getText().substring(fLat.getText().indexOf(" ")+1);
        }
        catch (Exception e){
            taLog.append("Latitude is incorrect. Please use N/Sdd mm.mmmm format"+"\n");
            ret = false;
        }
        try{
            String vLngDeg = fLng.getText().substring(0,fLng.getText().indexOf(" "));
            String vLngMin = fLng.getText().substring(fLng.getText().indexOf(" ")+1);
        }
        catch (Exception e){
            taLog.append("Longitude is incorrect. Please use W/Edd mm.mmmm format"+"\n");
            ret = false;
        }
        try{
            Float dist = new Float(fDistance.getText());
        }
        catch (Exception e){
            taLog.append("Distance should be number"+"\n");
            ret = false;
        }

        return ret;
    }


    private boolean checkParametersWpt(){
        boolean ret = true;
        if (fDistance.getText().length() == 0){
            taLog.append("Distance is empty. Set to default value 1 km"+"\n");
            fDistance.setText("1");
        }
        if (fDir.getText().length() == 0){
            taLog.append("Directory is empty. Set to default c:\\maps"+"\n");
            fDir.setText("c:\\Maps");
        }
        if (fSrcFile.getText().length() == 0){
            taLog.append("Waypoint file should be selected!\n");
            ret = false;
        }
        //Check data format
        try{
            Float dist = new Float(fDistance.getText());
        }
        catch (Exception e){
            taLog.append("Distance should be number"+"\n");
            ret = false;
        }

        return ret;
    }

    /*
        Copy founded tracks to specific folder
     */
    private void copyResults(SearchResults ts){ //todo change
        //get directory path
        String vDirCopy = fCopyDir.getText();
        if (vDirCopy.substring(vDirCopy.length()-1).equals("\\"))
            vDirCopy = vDirCopy.substring(0,vDirCopy.length()-1);
        //go through all results
        String vTrack;
        String vCopy;
        taLog.append("Copy founded tracks to new location:"+vDirCopy+"\n");
        try{
            for (SearchResult res:ts.getResults()){
                vTrack = res.getTrackPath();
                //get only path without any additional information
                vTrack = vTrack.split(": ")[0];
                File srcFile = new File(vTrack);
                vCopy = vDirCopy+"\\"+srcFile.getName();
                taLog.append(vCopy+"\n");
                //make copy
                FileInputStream src     = new FileInputStream(vTrack);
                FileOutputStream dst     = new FileOutputStream(vCopy);
                byte[] buf = new byte[1024];//1Kb buffer
                int len;
                while ((len = src.read(buf)) > 0){
                  dst.write(buf, 0, len);
                }
                src.close();
                dst.close();
            }
        }
        catch(IOException e){
            taLog.append("Error while copy tracks!\n");
            taLog.append(e.getMessage());
        }
        taLog.append("Copy complete successful!");
    }

    /*
        Open File dialog and return selected path
     */
    private String getPath(){
        JFileChooser ch = new JFileChooser();
        String vPath = "";
        ch.setCurrentDirectory(new File("."));
        ch.setDialogTitle("Select folder with tracks");
        ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        ch.setAcceptAllFileFilterUsed(false);


        if (ch.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            vPath = ch.getSelectedFile().toString();
        }
        return vPath;
    }

    private String getFile(){
        JFileChooser ch = new JFileChooser();
        String vPath = "";
        ch.setCurrentDirectory(new File("."));
        ch.setDialogTitle("Select folder with tracks");
        ch.setFileSelectionMode(JFileChooser.FILES_ONLY);
        ch.setAcceptAllFileFilterUsed(false);


        if (ch.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            vPath = ch.getSelectedFile().toString();
        }
        return vPath;
    }

    /*===============================================================================
        Function for GUI
    =================================================================================*/
    public FindTrackForm() {

        //add menu
        super("FindYourTracks"); // define frame title
        this.setBounds(1,1,570,500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // define Menubar
        MenuBar mb = new MenuBar();
        setMenuBar(mb);
        // Define File menu and with Exit menu item
        Menu fileMenu = new Menu("File");
        mb.add(fileMenu);
        MenuItem exitMenuItem = new MenuItem("Exit");
        fileMenu.add(exitMenuItem);
        exitMenuItem.addActionListener (this);

        //add header to table

        tbResults.setAutoCreateRowSorter(true);
        tbResultsModel = (DefaultTableModel) tbResults.getModel();
        TableColumn c;

        c = new TableColumn(1);
        c.setHeaderValue("File");
        tbResults.getColumnModel().addColumn(c);
        tbResultsModel.addColumn("File");

        c = new TableColumn(2);
        c.setHeaderValue("Distance");
        tbResults.getColumnModel().addColumn(c);
        tbResultsModel.addColumn("Distance");

        tbResults.repaint();




        btnSelectSource.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fDir.setText(getPath());
            }
        });

        btnFind.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TrackSearch ts = null;
                DirectoryScanner ds = null;
                Boolean searchAllowed = true;

                taLog.append("==================\n");
                taLog.append("Check parameters..."+"\n");
                if (tpPointType.getSelectedIndex() == 0 && checkParameters()){//First tab
                    if (checkParameters()){
                        taLog.append("Parameters accepted..."+"\n");
                        GeoPoint searchPoint = new GeoPoint(fLat.getText(),fLng.getText(),"Search point");
                        ds = new DirectoryScanner(searchPoint,fDir.getText(),fDistance.getText());
                    }
                }
                    else if (tpPointType.getSelectedIndex() == 1 && checkParametersWpt()){
                        taLog.append("Parameters accepted..."+"\n");
                        try{
                            ds = new DirectoryScanner(fSrcFile.getText(),fDir.getText(),fDistance.getText());
                        }
                        catch (IOException ie){
                            searchAllowed = false;
                            taLog.append("Can't open waypoint file!");
                        }
                    }
                    else{
                        searchAllowed = false;
                        taLog.append("Stop working.\n");
                        taLog.append("==================\n\n");
                    }
                if (searchAllowed){
                    taLog.append("Search process started..."+"\n");
                    try{
                        ds.scan();
                    }catch (IOException ie){
                    }

                    taLog.append("Results:\n");
                    ds.sort(0,0);
                    foundedTracks = ds.getResults();
                    clearResultTable();
                    for (SearchResult res:foundedTracks.getResults()){
                        taLog.append(res.getTrackPath()+": "+Math.round(res.getMinDistance())+"km\n");
                        //fill the result table
                        Object[] newResult = {res.getTrackPath(),res.getMinDistance()};
                        tbResultsModel.addRow(newResult);
                        tbResultsModel.fireTableDataChanged();
                    }
                    if (foundedTracks.getResults().size() == 0)  taLog.append("No tracks found"+"\n");
                }
            }
        });

        btnSelectDest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fCopyDir.setText(getPath());
            }
        });

        btnCopy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copyResults(foundedTracks);
            }
        });
        btnSelectSrc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fSrcFile.setText(getFile());
            }
        });
        btnGetPicts.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //create instance of GeoFile
                try {
                    taLog.append("Start scanning PanoramioKML cache..."+"\n");
                    DirectoryScannerPanoramioCache ds = new DirectoryScannerPanoramioCache(fDir.getText());
                    ds.scan();
                    taLog.append(ds.lPoints.size()+" points loaded. Start to download pictures..."+"\n");
                    PanoramioDownloader pd = new PanoramioDownloader(ds.getResult(),fCopyDir.getText());
                    pd.download(settings);
                    taLog.append("Download complete!"+"\n");
                }
                catch (IOException ex){
                    taLog.append("Download failed!"+"\n");
                    taLog.append(ex.getMessage());
                }
            }
        });
        add(pnMain, java.awt.BorderLayout.CENTER);
        //load configuration
        settings = new FindTrackSettings();
        taLog.append("Settings loaded from file.");
    }

    private void clearResultTable(){
        tbResultsModel.setRowCount(0);
    }
}