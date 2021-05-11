package init;

import dfsMgr.Dfs3Download;
import dfsMgr.ListFiles;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static dfsMgr.Upload.start;

public class UFSUI extends JFrame {
    private final JMenuBar menu;
    private final JMenu m1;
    private JMenuItem View, Upload, Download, Delete, Exit, Modify;
    private JButton btnView, btnUpload, btnDelete, btnDownload, btnModify, btnExit;
    private JPanel pMain, pCenter;
    private JTextArea tac;
    private JLabel lbllogo;

    public UFSUI() {
        //menu bar and menu item initialization
        menu = new JMenuBar();
        m1 = new JMenu("Options");

        View = new JMenuItem("My UFS Drive");
        Upload = new JMenuItem("Upload");
        Download = new JMenuItem("Download");
        Delete = new JMenuItem("Delete");
        Modify = new JMenuItem("Modify");
        Exit = new JMenuItem("Exit");

        //button intialization
        // The first line initialises the button with text as displayed against
        // each. the second line shows the tip related to the button

        btnView = new JButton("My UFS");
        btnView.setToolTipText("Show the contents in UFS");

        btnUpload = new JButton("Upload");
        btnUpload.setToolTipText("Upload to UFS");

        btnDelete = new JButton("DELETE");
        btnDelete.setToolTipText("Delete from UFS");

        btnDownload = new JButton("Download");
        btnDownload.setToolTipText("Download from UFS");

        btnModify = new JButton("Modify");
        btnModify.setToolTipText("Modify the contents in UFS");

        btnExit = new JButton("Exit");
        btnExit.setToolTipText("Exit from UFS");
        //initialization of panel
        // change these to add or remove any of the panel
        pMain = new JPanel();
        pCenter = new JPanel();
        // lbllogo change here to display a logo
        lbllogo = new JLabel(new ImageIcon("//G:/MFCfinish.gif"), JLabel.CENTER);
        //add menu items to Options menu
        m1.add(View);
        m1.add(Upload);
        m1.add(Download);
        m1.add(Delete);
        m1.add(Modify);
        m1.add(Exit);
        // add the menu items to menu bar
        menu.add(m1);
        // add buttons to the panel
        pMain.add(btnView);
        pMain.add(btnUpload);
        pMain.add(btnDownload);
        pMain.add(btnDelete);
        pMain.add(btnModify);
        pMain.add(btnExit);
        // set layout for the panel main change here for background, layout text displayed etc
        pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));
        pMain.setBorder(BorderFactory.createTitledBorder("OPTIONS"));
        pMain.setLayout(new GridLayout(6, 1));
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
        this.setTitle("UFS");
        this.show();
// The following sections list all the action listeners in order
// change here to modify what a button does
///////////////////////////////////////////////////////////////////////////////////////////////////////
        btnUpload.addActionListener(e -> {
            //call the Upload class once user clicks on Upload
            try {
                start(false);//reference to Upload.start()
            } catch (IOException | GeneralSecurityException ex) {
                ex.printStackTrace();
            }
        });
/////////////////////////////////////////////////////////////////////////////////////////////////////////
        btnDownload.addActionListener(e -> {
            //call the Upload class once user clicks on Upload
            //ListFiles.start(false);//reference to Upload.start()
            ufsOptions();
        });
/////////////////////////////////////////////////////////////////////////////////////////////////////////
        btnDelete.addActionListener(e -> {
            //call the Upload class once user clicks on Upload
            try {
                ListFiles.start(false);//reference to Upload.start()
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }
    public static void ufsOptions() {

        /*JFrame frame = new JFrame("UFS Download Options");
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLocation(430, 100);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        frame.add(panel);

        JLabel lbl = new JLabel("Select one of the possible choices and click OK");
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(lbl);

        String[] choices = { "Hash of File", "Name of File with timestamp", "Email ID of Originator"};

        final JComboBox<String> cb = new JComboBox<String>(choices);

        cb.setMaximumSize(cb.getPreferredSize());
        cb.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(cb);

        JButton btn = new JButton("OK");
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(btn);

        frame.setVisible(true);

        btn.addActionListener(e -> {
            //Take the input from the user and pass to Dfs3dDownload
            try {
                ListFiles.start(false);//reference to Upload.start()
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });*/
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

