package com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Mgr;

//import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import javax.swing.*;

/**
 * This class is responsible for configuration of DFS-UFS and maintaining its state.
 * <p>1. Initializes and configures DFS if accessed for the first time</p>
 * <p>2. Reads the stored state and resumes DFS as regular initialization</p>
 * <p>3. Creates singleton object</p>
 */
public class DFS3Config implements Serializable  {

    private static final long serialVersionUID = 1L;
    private static volatile DFS3Config config;
    public static DFS3BufferMgr bufferMgr = DFS3BufferMgr.getInstance();
   // private static final Logger log = Logger.getLogger(DFS3Config.class);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //All String variables.

    //String PubU = null; //Public key of the user. After integration, this variable will be set by reading certificate.
    //String PubN = null; //Public key of the node. After integration, this variable will be set by reading certificate.
    private String rootInode; //creates root inode for a DFS user.
    /**
     * Getter for root Inode of DFS
     * @return root Inode of the DFS
     */
    public String getRootInode() { return config.rootInode; }

    String mailID; //Stores email ID of the user registered with Brihaspati-4.
    /**
     * Getter for email ID of the user.
     * @return String mailID.
     */
    public String getMailID() { return config.mailID;}
    private void setMailID(String mailID) {
        config.mailID = mailID;
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
    public long getLocalOffered() { return config.localOffered; }

    private long localOccupied; //Stores amount of local disk space occupied at the node by DFS as part of the cloud.

    /**
     * Setter for localOccupied
     */
    public void setLocalOccupied() {
        String dfsSrvr=config.dfsDir+System.getProperty("file.separator")+"dfsSrvr";
        File file = new File(dfsSrvr);
        File[] files = file.listFiles();
        long length = 0;
        int count;
        assert files != null;
        count = files.length;
        /* loop for traversing the directory */
        for (int i = 0; i < count; i++) {
            length += files[i].length();
        }
        config.localOccupied=length;
        boolean flag = config.writeConfig(config);
        if(flag)
            System.out.println("Local occupied Updated: "+ (config.localOccupied/(1024*1024))+"MB");
        else
            System.out.println("Local occupied Update failed");
    }
    /**
     * Getter for localOccupied
     * @return the localOccupied
     */
    public long getLocalOccupied()  {
        return config.localOccupied;
    }

    private long localFree; //Stores current free disk space in the current working directory
    /**
     * Getter for localFree
     * @return long localFree
     */
    public long getLocalFree() { return config.localFree;}

    /**
     *Setter for localFree
     */
    public void setLocalFree() {
        String cwd=System.getProperty("user.dir");
        File cwDir = new File(cwd);
        config.localFree=cwDir.getFreeSpace();
        boolean flag = config.writeConfig(config);
        if(flag)
            System.out.println("Local free Updated: "+ (config.localFree/(1024*1024))+"MB");
        else
            System.out.println("Local free Update failed");
    }

    private long localBalance; //variable that maintains balance of the local space that was initially offered.

    /**
     * Getter for localBalance
     * @return long localBalance
     */
    public long getLocalBalance() { return localBalance; }

    /**
     * Setter for long localBalance
     */
    public void setLocalBalance() {
        localBalance = config.getLocalOffered()-config.getLocalOccupied();
        boolean flag = config.writeConfig(config);
        if (flag)
            System.out.println("Local balance space updated successfully");
        else
            System.out.println("Local balance space update failed");
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //All cloud space related variables.

    private long cloudAuth; //stores authorized cloud space based on offered local disk space.
    /**
     * Getter for Cloud space authorized.
     * @return the cloudAuth
     */
    public double getCloudAuth() { return config.cloudAuth; }

    private long cloudOccupied;
    /**
     * @return the Cloud Occupied
     */
    public long getCloudOccupied() { return config.cloudOccupied; }

    /**
     * @param fileSize file size is added to cloudOccupied.
     */
    public void setCloudOccupied(long fileSize) {
            config.cloudOccupied = config.cloudOccupied + fileSize;
            boolean flag = config.writeConfig(config);
            if(flag)
                System.out.println("Cloud occupied Updated: " + (config.cloudOccupied / (1024 * 1024)) + "MB");
    }

    private long cloudAvlb;
    /**
     * Getter for Cloud Available.
     * @return the cloudAvlb
     */
    public long getCloudAvlb() throws IOException { return config.cloudAvlb; }

    /**
     * @param  cloudOccupied long cloud occupied file size.
     */
    public void setCloudAvlb(long cloudOccupied) {
            config.cloudAvlb = config.cloudAvlb - cloudOccupied;
            boolean flag = config.writeConfig(config);
            if (flag)
                System.out.println("Cloud available Updated: " + (config.cloudAvlb / (1024 * 1024)) + "MB");
            else
                System.out.println("Cloud Available update filed");
        }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // All local cache related variables.
    private long localCacheSize;
    public long getLocalCacheSize() { return config.localCacheSize; }

    long cacheOccupied;
    /**
     * @return the cacheOccupied
     */
    public long getCacheOccupied() { return cacheOccupied; }

    public void setCacheOccupied() {

        String cache = config.getDfsCache();
        System.out.println(cache);
        File cacheDir = new File(config.getDfsCache());
        File[] files = cacheDir.listFiles();
        long length = 0;
        int count;
        count = Objects.requireNonNull(files).length;
        // loop for traversing the directory
        for (int i = 0; i < count; i++) {
            length += files[i].length();
        }
        cacheOccupied=length;
        boolean flag = config.writeConfig(config);
        if(flag)
            System.out.println("Cache Size Updated.."+ (cacheOccupied/(1024*1024))+"MB");
        else
            System.out.println("Cache Size Updated failed");
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
                configFile=config.getDfsDir()+ "configFile.txt";
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
     * Method to deserialize the object and write the config file on the disk
     * @param config singleton object config.
     * @return success or failure flag.
     */
    private boolean writeConfig(DFS3Config config) {
        try {
            FileOutputStream fos = new FileOutputStream(configFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(config);
            oos.close();
            fos.flush();
            fos.close();
            //System.out.println("DFS Config file updated successfully");
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Failed to write configuration in file");
            return false;
        }
    }

    /**
     * Method to read the state variables from the config file.
     * @return flag for success or failure.
     */
    private boolean readConfig() {
        boolean result = false;
        try {
            FileInputStream fis = new FileInputStream(configFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            config = (DFS3Config) ois.readObject();
            ois.close();
            fis.close();
            result = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Config File not found");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Error in config file reading");
        }
        return result;
    }

    /**
     * Updates the cache and cloud space related variables once a file is uploaded.
     * @param fileSize size of the file being uploaded.
     * @throws IOException in case of IO exception
     */
    public void update(long fileSize) throws IOException {
        setCloudOccupied(fileSize);
        setCloudAvlb(config.cloudOccupied);
        setCacheOccupied();
        boolean flag = config.writeConfig(config);
        if(flag) {
            System.out.println("Now cloud occupied is: " + (getCloudOccupied()/(1024*1024))+"MB");
            System.out.println("Now cloud available is: " + (config.getCloudAvlb()/(1024*1024))+"MB");
        }
        else
            System.out.println("DFS config update failed");
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
            //If both inputs are valid, proceed to initialize various variables.
            if (com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Util.file.isValidEmail(getMailID()) && com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Util.file.isValidFloat(localDisk))
            {
                config.rootInode = "dfs://"+mailID+"/";
                float localoffered = Float.parseFloat(localDisk)*1024*1024*1024;
                long localOffered = (long)localoffered;
                if(config.initLocalFree >localOffered)
                {
                    config.localOffered=localOffered;
                    config.cloudAuth=config.localOffered/2;
                    config.cloudAvlb=config.cloudAuth;
                    config.localCacheSize=config.cloudAuth/10;
                    config.localOccupied=0;
                }
                else
                {
                    System.out.println("Disk space available is insufficient.");
                    System.out.println("Disk space is: "+(config.initLocalFree /(1024*1024*1024))+"GB");
                    continue;
                }
                break;
            }

            else
            {
                System.out.println("Invalid email ID or space offered.");
                i=i+1;
            }
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
            setCacheOccupied();
            setLocalOccupied();
            setLocalBalance();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Deserialize the object and write the config file in local disk.
        boolean flag = writeConfig(config);
        if(flag)
            JOptionPane.showMessageDialog(frame,"DFS initialized successfully.\nUFS initialized successfully","Brihaspati-4 DFS/UFS",JOptionPane.PLAIN_MESSAGE);
        else
            JOptionPane.showMessageDialog(frame,"DFS initialization failed.\nUFS initialization failed","Brihaspati-4 DFS/UFS",JOptionPane.PLAIN_MESSAGE);

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
        boolean flag = readConfig();
        if(flag)
        {
            System.out.println("Your root inode is: "+config.rootInode);
            System.out.println("Your authorized cloud space is: "+ (config.getCloudAuth()/(1024*1024*1024))+"GB");
            System.out.println("You have already used: "+ (getCloudOccupied()/(1024*1024))+"MB cloud space");
            System.out.println("Cloud space available is:" + (getCloudAvlb()/(1024*1024))+"MB");
            setCacheOccupied();
            setLocalOccupied();
            setLocalBalance();
            setLocalFree();
            //Welcome back and state showing message. To be stripped off when common GUI is developed.
            JOptionPane.showMessageDialog(frame,"Welcome back to B4 DFS/UFS.\n"+
                            "Cloud space utilization: "+(getCloudOccupied()/(1024*1024))+"MB / "+(getCloudAuth()/(1024*1024))+"MB\n"
                            +"Cache utilization: "+ (getCacheOccupied()/(1024*1024))+"MB / "+(getLocalCacheSize()/(1024*1024))+"MB\n"
                            +"Local space utilization: "+ (getLocalOccupied()/(1024*1024))+"MB / "+(getLocalOffered()/(1024*1024))+"MB\n",
                    "Brihaspati-4 DFS/UFS",JOptionPane.PLAIN_MESSAGE);
            //Check whether adequate local space as initially offered is available or not.
            System.out.println("local space available for DFS: "+(getLocalBalance()/(1024*1024))+"MB");
            System.out.println("local free: "+(getLocalFree()/(1024*1024))+"MB");
            if(getLocalFree()<getLocalBalance())
            {
                System.out.println("Local space less than guaranteed. System exit.");
                JOptionPane.showMessageDialog(frame,"Local disk space guaranteed has been  violated."
                        +"\nYou cannot use B4 DFS unless you clear local drive space","Brihaspati-4 DFS/UFS",JOptionPane.WARNING_MESSAGE);
                System.exit(1);
            }

            else if(getLocalFree()< 1.1*getLocalBalance())
            {
                System.out.println("Local space warning 90%.");
                JOptionPane.showMessageDialog(frame,"Local space guranteed is about to be encroached by other files."
                        +"\nPlease free up local space","Brihaspati-4 DFS/UFS",JOptionPane.WARNING_MESSAGE);

            }
            else
                System.out.println("Adequate local space available.");
        }
    }
}