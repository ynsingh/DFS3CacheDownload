package simulateGC.init;

import dfsUfsCore.dfsMgr.Dfs3Download;
import dfsUfsCore.dfsMgr.ListFiles;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.security.GeneralSecurityException;

import dfsUfsCore.dfsMgr.DFS3Upload;

public class DFSUI extends JFrame {
    private final JMenuBar menu;
    private final JMenu m1;
    private final JMenuItem dfsUpload;
    private final JMenuItem dfsDownload;
    private final JMenuItem ufsUpload;
    private final JMenuItem ufsDownload;
    private final JButton btnDfsUpload;
    private final JButton btnDfsDownload;
    private final JButton btnUfsUpload;
    private final JButton btnUfsDownload;
    private final JPanel pMain;
    private final JPanel pCenter;
    private JTextArea tac;
    private final JLabel lbllogo;

    public DFSUI() {
        //menu bar and menu item initialization
        menu = new JMenuBar();
        m1 = new JMenu("Options");

        dfsUpload = new JMenuItem("DFS Upload");
        dfsDownload = new JMenuItem("DFS Download");
        ufsUpload = new JMenuItem("UFS Upload");
        ufsDownload = new JMenuItem("UFS Download");

        //button intialization
        // The first line initialises the button with text as displayed against
        // each. the second line shows the tip related to the button

        btnDfsUpload = new JButton("DFS Upload");
        btnDfsUpload.setToolTipText("Upload to DFS cloud");

        btnDfsDownload = new JButton("DFS Download");
        btnDfsDownload.setToolTipText("Download from DFS cloud");

        btnUfsUpload = new JButton("UFS Upload");
        btnUfsUpload.setToolTipText("Upload to UFS cloud");

        btnUfsDownload = new JButton("UFS Download");
        btnUfsDownload.setToolTipText("Download from UFS cloud");

        //initialization of panel
        // change these to add or remove any of the panel
        pMain = new JPanel();
        pCenter = new JPanel();
        // lbllogo change here to display a logo
        lbllogo = new JLabel(new ImageIcon("//G:/MFCfinish.gif"), JLabel.CENTER);
        //add menu items to Options menu
        m1.add(dfsUpload);
        m1.add(dfsDownload);
        m1.add(ufsUpload);
        m1.add(ufsDownload);
        // add the menu items to menu bar
        menu.add(m1);
        // add buttons to the panel
        pMain.add(btnDfsUpload);
        pMain.add(btnDfsDownload);
        pMain.add(btnUfsUpload);
        pMain.add(btnUfsDownload);
        // set layout for the panel main change here for background, layout text displayed etc
        pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));
        pMain.setBorder(BorderFactory.createTitledBorder("OPTIONS"));
        pMain.setLayout(new GridLayout(4, 1));
        pMain.setBackground(Color.white);
        // set layout for the panel center
        pCenter.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));
        pCenter.setLayout(new GridLayout(1, 1));
        pCenter.add(lbllogo);
        // position of the panel is controlled from here
        this.getContentPane().add(pMain, "East");
        this.getContentPane().add(pCenter, "Center");
        // size of the window containing everything above is specified here
        this.setSize(500, 400);
        this.setResizable(false);
        this.setLocation(450, 200);
        this.setTitle("B4 DFS-UFS");
        this.show();
// The following sections list all the action listeners in order
// change here to modify what a button does
///////////////////////////////////////////////////////////////////////////////////////////////////////
        btnDfsUpload.addActionListener(e -> {
            //call the Upload class once user clicks on Upload
            try {
                boolean flag = dfsUfsCore.dfsMgr.DFS3Upload.start(true);//reference to Upload.start()
                JFrame frame;
                frame = new JFrame();
                if(flag)
                    JOptionPane.showMessageDialog(frame,"Upload successful!","Brihaspati-4 DFS/UFS",JOptionPane.PLAIN_MESSAGE);
                else
                    JOptionPane.showMessageDialog(frame,"Upload failed!","Brihaspati-4 DFS/UFS",JOptionPane.PLAIN_MESSAGE);

            } catch (IOException | GeneralSecurityException ex) {
                ex.printStackTrace();
            }
        });
/////////////////////////////////////////////////////////////////////////////////////////////////////////
        btnDfsDownload.addActionListener(e -> {
            //call the Upload class once user clicks on Upload
            try {
                ListFiles.start(true);//reference to Upload.start()
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
/////////////////////////////////////////////////////////////////////////////////////////////////////////
        btnUfsUpload.addActionListener(e -> {
            //call the Upload class once user clicks on Upload
            try {
                boolean flag = dfsUfsCore.dfsMgr.DFS3Upload.start(false);//reference to Upload.start()
                JFrame frame;
                frame = new JFrame();
                if(flag)
                    JOptionPane.showMessageDialog(frame,"Upload successful!","Brihaspati-4 DFS/UFS",JOptionPane.PLAIN_MESSAGE);
                else
                    JOptionPane.showMessageDialog(frame,"Upload failed!","Brihaspati-4 DFS/UFS",JOptionPane.PLAIN_MESSAGE);

            } catch (IOException | GeneralSecurityException ex) {
                ex.printStackTrace();
            }
        });
/////////////////////////////////////////////////////////////////////////////////////////////////////////
        btnUfsDownload.addActionListener(e -> {
            //call the Upload class once user clicks on Upload
            //ListFiles.start(false);//reference to Upload.start()
            ufsOptions();
        });
    }
    public static void ufsOptions() {

        String[] ufsOptions = { "Hash of File", "File Name with Time Stamp", "Email ID of Owner"};
        String options = (String) JOptionPane.showInputDialog(null, "Which option do you want to Download the file with?", "UFS Download Options",
                JOptionPane.PLAIN_MESSAGE, null, ufsOptions, ufsOptions[0]);
        switch (options) {
            case "Hash of File":
                String hashOfFile = JOptionPane.showInputDialog("Enter the Hash of the File");
                try {
                    Dfs3Download.start(hashOfFile, false);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case "File Name with Time Stamp":
                String fileName = JOptionPane.showInputDialog("Enter the File Name in Filename@@TimeStamp format");
                try {
                    Dfs3Download.start(fileName, false);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case "Email ID of Owner":
                String mailID = JOptionPane.showInputDialog("Enter the Email ID of the file owner");
                String rootDir=mailID+"/"+"UFSuploaded.csv";
                try {
                    Dfs3Download.start(rootDir, false);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }

    }
}
