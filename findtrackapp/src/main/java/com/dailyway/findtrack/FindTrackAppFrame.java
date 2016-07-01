package com.dailyway.findtrack;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by IntelliJ IDEA.
 * User: nkiselev
 * Date: 26/11/13
 * Time: 17:18
 * To change this template use File | Settings | File Templates.
 */
public class FindTrackAppFrame extends JFrame implements ActionListener {
   public FindTrackAppFrame() { // constructor
    super("FindYourTracks"); // define frame title
    // define Menubar
    MenuBar mb = new MenuBar();
    setMenuBar(mb);
    // Define File menu and with Exit menu item
    Menu fileMenu = new Menu("File");
    mb.add(fileMenu);
    MenuItem exitMenuItem = new MenuItem("Exit");
    fileMenu.add(exitMenuItem);
    exitMenuItem.addActionListener (this);
    // define the applet and add to the frame
    //FindTrack myApplet = new FindTrack();
    FindTrackForm myApplet = new FindTrackForm();
    add(myApplet, BorderLayout.CENTER);
       //todo try to create rubber design
       addComponentListener ( getBoundsListener () );
    // call applet's init method (since it is not
    // automatically called in a Java application)
    myApplet.init();
  } // end constructor

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

    private ComponentListener getBoundsListener(){
        return new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                //super.componentResized(e);
                // get component
                final Component component = e.getComponent ();
                final Rectangle bounds = component.getBounds ();


            }
        };
    }
}
