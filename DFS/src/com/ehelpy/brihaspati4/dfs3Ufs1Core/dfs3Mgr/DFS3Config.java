package com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Mgr;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;
import javax.swing.*;

/**
 * This class is responsible for configuration of DFS-UFS and maintaining its state.
 * <p>1. Initializes and configures DFS if accessed for the first time</p>
 * <p>2. Reads the stored state and resumes DFS as regular initialization</p>
 * <p>3. Creates singleton object</p>
 */
@SuppressWarnings("SpellCheckingInspection")
public class DFS3Config {

    private static volatile DFS3Config config;
    public static DFS3BufferMgr bufferMgr = DFS3BufferMgr.getInstance();
    private static final Logger log = Logger.getLogger(DFS3Config.class.getName());
    Properties prop = new Properties();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //All String variables.

    //String PubU = null; //Public key of the user. After integration, this variable will be set by reading certificate.
    //String PubN = null; //Public key of the node. After integration, this variable will be set by reading certificate.
    private String rootInode; //creates root inode for a DFS user.
    /**
     * Getter for root Inode of DFS
     * @return root Inode of the DFS
     */
    public String getRootInode() {
        config.rootInode=prop.getProperty("rootDir");
        return config.rootInode; }

    String mailID; //Stores email ID of the user registered with Brihaspati-4.
    /**
     * Getter for email ID of the user.
     * @return String mailID.
     */
    public String getMailID() {
        config.mailID=prop.getProperty("mailID");
        return config.mailID;}
    private void setMailID(String mailID) {
        config.mailID = mailID;
        prop.setProperty("mailID", config.mailID);
    }
    private String dfsDir;
    public String getDfsDir() {
        config.dfsDir = System.getProperty("user.dir") +System.getProperty("file.separator")+
                "b4dfs"+System.getProperty("file.separator");
        return config.dfsDir;
    }

    private String ufsDir;
    //public String getUfsDir() { return ufsDir;}

    private String dfsSrvr;
    public String getDfsSrvr() {
        config.dfsSrvr=System.getProperty("user.dir") +
                System.getProperty("file.separator")+"b4dfs"+System.getProperty("file.separator")+"dfsSrvr"+System.getProperty("file.separator");
        return config.dfsSrvr;}

    private String dfsCache;
    public String getDfsCache() {
        config.dfsCache=System.getProperty("user.dir") +System.getProperty("file.separator")+
                "b4dfs"+System.getProperty("file.separator")+"dfsCache"+System.getProperty("file.separator");
        return config.dfsCache;
    }

    private String ufsCache;
    public String getUfsCache() {
        config.ufsCache=System.getProperty("user.dir") +System.getProperty("file.separator")+
                "b4ufs"+System.getProperty("file.separator")+"ufsCache"+System.getProperty("file.separator");
        return config.ufsCache;
    }

    static String configFile;
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //All local disk space related variables.

    private long initLocalFree; //Stores free space available in the current working directory at the time of first initialization.

    private long localOffered; //stores local disk space offered by the user to the DFS.
    /**
     * Getter for the localOffered
     * @return the localOffered
     */
    public long getLocalOffered() {
        config.localOffered= Long.parseLong(prop.getProperty("localOffered"));
        return config.localOffered; }

    private long localOccupied; //Stores amount of local disk space occupied at the node by DFS as part of the cloud.

    /**
     * Setter for localOccupied
     */
    public void setLocalOccupied() {

        File file = new File(config.getDfsSrvr());
        File[] files = file.listFiles();
        long length = 0;
        int count;
        assert files != null;
        count = files.length;
        // loop for traversing the directory
        for (int i = 0; i < count; i++) {
            length += files[i].length();
        }
        config.localOccupied=length;
        prop.setProperty("localOccupied", String.valueOf(config.localOccupied));
        log.debug("Local occupied Updated: "+ (config.localOccupied/(1024*1024))+"MB");
    }
    /**
     * Getter for localOccupied
     * @return the localOccupied
     */
    public long getLocalOccupied()  {

        config.localOccupied=Long.parseLong(prop.getProperty("localOccupied"));
        return config.localOccupied;
    }

    private long localFree; //Stores current free disk space in the current working directory
    /**
     * Getter for localFree
     * @return long localFree
     */
    public long getLocalFree() {
        config.localFree=Long.parseLong(prop.getProperty("localFree"));
        return config.localFree;}

    /**
     *Setter for localFree
     */
    public void setLocalFree() {
        String cwd=System.getProperty("user.dir");
        File cwDir = new File(cwd);
        config.localFree=cwDir.getFreeSpace();
        prop.setProperty("localFree",String.valueOf(config.localFree));
        log.debug("Local free Updated: "+ (config.localFree/(1024*1024))+"MB.");
    }

    private long localBalance; //variable that maintains balance of the local space that was initially offered.

    /**
     * Getter for localBalance
     * @return long localBalance
     */
    public long getLocalBalance() {
        config.localBalance=Long.parseLong(prop.getProperty("localBalance"));
        return config.localBalance; }

    /**
     * Setter for long localBalance
     */
    public void setLocalBalance() {
        config.localBalance = config.getLocalOffered()-config.getLocalOccupied();
        prop.setProperty("localBalance", String.valueOf(config.localBalance));
        log.debug("Local balance space updated successfully");
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //All cloud space related variables.

    private long cloudAuth; //stores authorized cloud space based on offered local disk space.
    /**
     * Getter for Cloud space authorized.
     * @return the cloudAuth
     */
    public long getCloudAuth() {
        config.cloudAuth=Long.parseLong(prop.getProperty("cloudAuth"));
        return  config.cloudAuth; }

    private long cloudOccupied;
    /**
     * @return the Cloud Occupied
     */
    public long getCloudOccupied() {
        config.cloudOccupied=Long.parseLong(prop.getProperty("cloudOccupied"));
        return config.cloudOccupied; }

    /**
     * @param fileSize file size is added to cloudOccupied.
     */
    public void setCloudOccupied(long fileSize) {
        config.cloudOccupied = Long.parseLong(prop.getProperty("cloudOccupied")) + fileSize;
        prop.setProperty("cloudOccupied",String.valueOf(config.cacheOccupied));
        log.debug("Cloud occupied Updated: " + (config.cloudOccupied / (1024 * 1024)) + "MB");

    }

    private long cloudAvlb;
    /**
     * Getter for Cloud Available.
     * @return the cloudAvlb
     */
    public long getCloudAvlb() throws IOException {
        config.cloudAvlb=Long.parseLong(prop.getProperty("cloudAvlb"));
        return config.cloudAvlb;  }

    /**
     * @param  cloudOccupied long cloud occupied file size.
     */
    public void setCloudAvlb(long cloudOccupied) {
        config.cloudAvlb = config.cloudAvlb - cloudOccupied;
        prop.setProperty("cloudAvlb", String.valueOf(config.cloudAvlb));
        log.debug("Cloud available Updated: " + (config.cloudAvlb / (1024 * 1024)) + "MB");

    }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // All local cache related variables.
    private long localCacheSize;
    public long getLocalCacheSize() { return Long.parseLong(prop.getProperty("localCacheSize")); }

    long cacheOccupied;
    /**
     * @return the cacheOccupied
     */
    public long getCacheOccupied()
    {
        if(prop.getProperty("cacheOccupied")==null)
            return 0;
        else
            return Long.parseLong(prop.getProperty("cacheOccupied"));
    }

    public void setCacheOccupied() {

        File cacheDir = new File(config.getDfsCache());
        File[] files = cacheDir.listFiles();
        long length = 0;
        int count;
        count = Objects.requireNonNull(files).length;
        // loop for traversing the directory
        for (int i = 0; i < count; i++) {
            length += files[i].length();
        }
        config.cacheOccupied=length;
        prop.setProperty("cacheOccupied", String.valueOf(config.cacheOccupied));
        log.debug("Cache Size Updated.."+ (cacheOccupied/(1024*1024))+"MB");

    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //All functions related to constructors and ensuring that DFS3Config is a singleton.

    /**
     * Private Constructor of the class. Ensures singleton.
     */
    private DFS3Config(){
        //Prevent form the reflection api.
        if (config != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    /**
     *
     * @return Returns of singleton object of DFS3Config.
     */

    public static DFS3Config getInstance() {
        if (config == null) { //if there is no instance available... create new one
            synchronized (DFS3Config.class) {
                if (config == null) config = new DFS3Config();
                configFile=config.getDfsDir()+ "dfsconfig.properties";
                File configfile = new File(configFile);
                if(!configfile.exists())
                    config.firstInit();
                else {
                    try {
                        config.regInit();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return config;
    }

    /**
     * Make singleton safe from serialize and deserialize operation.
     * */
    protected DFS3Config readResolve() {
        return getInstance();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Methods for reading, writing configuration file and updating state variables.

    /**
     * Reads the congiguration file and loads the state values in the DFS object.
     * @return boolean flag as success or failure
     */
    private boolean readProperties()
    {
        try{
            String configFile = System.getProperty("user.dir")+System.getProperty("file.separator")+"b4dfs"+System.getProperty("file.separator")+"dfsconfig.properties";
            FileInputStream fis = new FileInputStream(configFile);
            prop.load(fis);

            config.rootInode=prop.getProperty("rootDir");
            config.mailID=prop.getProperty("mailID");
            config.dfsDir=prop.getProperty("dfsDir");
            config.dfsCache=prop.getProperty("dfsCache");
            config.ufsCache=prop.getProperty("ufsCache");
            config.dfsSrvr=prop.getProperty("dfsSrvr");
            config.localOffered= Long.parseLong(prop.getProperty("localOffered"));
            config.setLocalOccupied();
            config.setLocalFree();
            config.setLocalBalance();
            config.cloudOccupied=Long.parseLong(prop.getProperty("cloudOccupied"));
            config.cloudAuth=Long.parseLong(prop.getProperty("cloudAuth"));
            config.cloudAvlb=Long.parseLong(prop.getProperty("cloudAvlb"));
            config.localCacheSize=Long.parseLong(prop.getProperty("localCacheSize"));
            config.setCacheOccupied();

            log.debug("Config file read successfully");

            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
            log.debug("Read Config file failed");
            return false;
        }
        return true;
    }

    /**
     * Updates the cache and cloud space related variables once a file is uploaded.
     * @param fileSize size of the file being uploaded.
     * @throws IOException in case of IO exception
     */
    public void updateUpload(long fileSize) throws IOException {
        try{
            String configFile = System.getProperty("user.dir")+System.getProperty("file.separator")+"b4dfs"+System.getProperty("file.separator")+"dfsconfig.properties";
            FileOutputStream fos = new FileOutputStream(configFile);
            setCloudOccupied(fileSize);
            setCloudAvlb(config.getCloudOccupied());
            setCacheOccupied();
            log.debug("DFS state update after upload successful.");
            prop.store(fos, null);
            log.debug("Config file write successful.");
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            log.debug("Config file write failed.");
        }
    }

    private boolean writeProperties()
    {
        try{
            String configFile = System.getProperty("user.dir")+System.getProperty("file.separator")+"b4dfs"+System.getProperty("file.separator")+"dfsconfig.properties";
            FileOutputStream fos = new FileOutputStream(configFile);

            prop.setProperty("rootDir", config.rootInode);
            prop.setProperty("mailID", config.mailID);
            prop.setProperty("dfsDir", config.dfsDir);
            prop.setProperty("dfsCache", config.dfsCache);
            prop.setProperty("ufsCache", config.ufsCache);
            prop.setProperty("dfsSrvr", config.dfsSrvr);
            prop.setProperty("localOffered", String.valueOf(config.localOffered));
            prop.setProperty("localOccupied", String.valueOf(config.localOccupied));
            prop.setProperty("localFree", String.valueOf(config.localFree));
            prop.setProperty("localBalance", String.valueOf(config.localBalance));
            prop.setProperty("cloudAuth", String.valueOf(config.cloudAuth));
            prop.setProperty("cloudAvlb", String.valueOf(config.cloudAvlb));
            prop.setProperty("cloudOccupied", String.valueOf(config.cloudOccupied));
            prop.setProperty("localCacheSize", String.valueOf(config.localCacheSize));
            prop.setProperty("cacheOccupied", String.valueOf(config.cacheOccupied));

            prop.store(fos, null);

            log.debug("Config file write successful.");
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            log.debug("Config file write failed.");
            return false;
        }
        return true;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Methods for first and regular initialization.

    /**
     * Method that initializes the DFS for the first time.
     * <p>1. Takes email ID and local disk offered as inputs from user.</p>
     * <p>2. Initializes local space, cache space and cloud space variables.</p>
     * <p>3. Creates requisite local directories for caches and servers.</p>
     * <p>4. Initializes and stores configuration file.</p>
     */
    private void firstInit()
    {
        //Initial welcome and informative message using swing frame
        JFrame frame; // to be stripped off when common GUI is developed.
        frame = new JFrame("Brihaspati-4 DFS/UFS");
        JOptionPane.showMessageDialog(frame, "Welcome to B4 DFS/UFS.\nThis is Brihaspati 4 Distributed File System : A peer-to-peer cloud storage system" +
                        "\nYou will get cloud storage 50% that of local storage offered (e.g.500MB cloud storage for 1GB local disk space offered)",
                "Brihaspati-4 DFS/UFS",JOptionPane.PLAIN_MESSAGE);

        //Obtain the free space in current working directory at the time of initialization
        File file = new File(System.getProperty("user.dir"));
        config.initLocalFree = file.getFreeSpace();
        //Give user three chances to input correct email ID and local disk space offered.
        int i=0;
        while(i<3) {
            String emailID = JOptionPane.showInputDialog("Please specify your registered email ID for B4:");
            setMailID(emailID);
            String localDisk = JOptionPane.showInputDialog("Please specify local disk to be offered in GB: ");
            float localoffered = Float.parseFloat(localDisk)*1024*1024*1024;
            long localOffered = (long)localoffered;
            //If both inputs are valid, proceed to initialize various variables.
            if (com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Util.file.isValidEmail(getMailID()) && config.initLocalFree >localOffered) {
                config.rootInode = "dfs://" + mailID + "/";
                config.localOffered = localOffered;
                config.cloudAuth = config.localOffered / 2;
                config.cloudAvlb = config.cloudAuth;
                config.localCacheSize = config.cloudAuth / 10;
                config.localOccupied = 0;
            }
            else
            {
                System.out.println("Invalid email ID or space offered.");
                i=i+1;
                JOptionPane.showMessageDialog(frame, "Invalid email ID or space offered." +
                                "\nYou have "+(3-i)+" attempts left."+
                                "\nAvailable disk space is: "+(config.initLocalFree /(1024*1024*1024))+"GB",
                        "Brihaspati-4 DFS/UFS",JOptionPane.WARNING_MESSAGE);
                System.out.println();
                continue;
            }
            break;
        }
        //Exit the system on three erroneous inputs.
        if(i==3)
        {
            System.out.println("You have exceeded the attempts.");
            System.exit(i);
        }
        //Create the requisite directories.
        config.dfsDir = System.getProperty("user.dir")+System.getProperty("file.separator")+ "b4dfs";
        Path pathDir = Paths.get(config.dfsDir);

        config.ufsDir = System.getProperty("user.dir") + System.getProperty("file.separator") + "b4ufs";
        Path pathuDir = Paths.get(ufsDir);

        config.dfsSrvr = config.dfsDir + System.getProperty("file.separator")+ "dfsSrvr";
        Path pathSrvr = Paths.get(config.dfsSrvr);

        config.dfsCache = config.dfsDir + System.getProperty("file.separator")+ "dfsCache";
        Path pathCache = Paths.get(dfsCache);

        config.ufsCache = ufsDir + System.getProperty("file.separator") + "ufsCache";
        Path pathuCache = Paths.get(ufsCache);

        Path pathFile = Paths.get(configFile);

        //Create requisite folders
        try {
            Files.createDirectory(pathDir);
            System.out.println("DFS directory created successfully");
            Files.createDirectory(pathSrvr);
            System.out.println("DFS server directory created successfully");
            Files.createDirectory(pathCache);
            System.out.println("DFS cache directory created successfully");
            Files.createFile(pathFile);
            System.out.println("DFS configuration file created successfully");
            Files.createDirectory(pathuDir);
            System.out.println("UFS directory created successfully");
            Files.createDirectory(pathuCache);
            System.out.println("UFS cache directory created successfully");

        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean writeSuccess=writeProperties();
        if(writeSuccess) {
            log.debug("Config file initialized successfully.");
            JOptionPane.showMessageDialog(frame,"DFS initialized successfully.\nUFS initialized successfully","Brihaspati-4 DFS/UFS",JOptionPane.PLAIN_MESSAGE);
        }
        else {
            log.debug("Config file initialization failed.");
            JOptionPane.showMessageDialog(frame,"DFS initialization failed.\nUFS initialization failed","Brihaspati-4 DFS/UFS",JOptionPane.PLAIN_MESSAGE);

        }
    }

    /**
     * Method that initializes the DFS second time onwards.
     * <p>1. Reads the variables from config file stored in the local disk.</p>
     * <p>2. Initializes local space, cache space and cloud space variables.</p>
     * <p>3. Keeps updating the variables depending upon the usage.</p>
     */
    void regInit() throws IOException {
        JFrame frame; // to be stripped off when common GUI is developed.
        frame = new JFrame("Brihaspati-4 DFS/UFS");
        //boolean flag = readConfig();
        boolean readConfig=readProperties();
        if(readConfig)
        {
            //Welcome back and state showing message. To be stripped off when common GUI is developed.
            JOptionPane.showMessageDialog(frame,"Welcome back to B4 DFS/UFS.\n"+
                            "Cloud space utilization: "+(getCloudOccupied()/(1024*1024))+"MB / "+(getCloudAuth()/(1024*1024))+"MB\n"
                            +"Cache utilization: "+ (getCacheOccupied()/(1024*1024))+"MB / "+(getLocalCacheSize()/(1024*1024))+"MB\n"
                            +"Local space utilization: "+ (getLocalOccupied()/(1024*1024))+"MB / "+(getLocalOffered()/(1024*1024))+"MB\n",
                    "Brihaspati-4 DFS/UFS",JOptionPane.PLAIN_MESSAGE);
            //Check whether adequate local space as initially offered is available or not.
            if(getLocalFree()<getLocalBalance())
            {
                log.debug("Local space less than guaranteed. System exit.");
                JOptionPane.showMessageDialog(frame,"Local disk space guaranteed has been  violated."
                        +"\nYou cannot use B4 DFS unless you clear local drive space","Brihaspati-4 DFS/UFS",JOptionPane.WARNING_MESSAGE);
                System.exit(1);
            }

            else if(getLocalFree()< 1.1*getLocalBalance())
            {
                log.debug("Local space warning 90%.");
                JOptionPane.showMessageDialog(frame,"Local space guaranteed is about to be encroached by other files."
                        +"\nPlease free up local space","Brihaspati-4 DFS/UFS",JOptionPane.WARNING_MESSAGE);

            }
            else
                log.debug("Adequate local space available.");
        }
    }
}