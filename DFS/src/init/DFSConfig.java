package init;

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

import dfsMgr.Util;
import javax.swing.*;

public class DFSConfig implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static volatile DFSConfig config;
    private static String ufsDir;
    private static String ufsCache;

    private DFSConfig(){
        //Prevent form the reflection api.
        if (config != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static DFSConfig getInstance() {
        if (config == null) { //if there is no instance available... create new one
            synchronized (DFSConfig.class) {
                if (config == null) config = new DFSConfig();
                configFile=System.getProperty("user.dir")+System.getProperty("file.separator")+ "b4dfs"+System.getProperty("file.separator")+ "configFile.txt";
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

    //Make singleton safe from serialize and deserialize operation.
    protected DFSConfig readResolve() {
        return getInstance();
    }

    public static long getInitlocalFree() {
        return config.initlocalFree;
    }

    void firstInit()
    {
        JFrame frame;
        frame = new JFrame("Brihaspati-4 DFS/UFS");
        JOptionPane.showMessageDialog(frame, "This is Brihaspati 4 Distributed File System : A peer-to-peer cloud storage system" +
                "\nYou will get cloud storage 50% that of local storage offered (e.g.500MB cloud storage for 1GB local disk space offered)",
                "Brihaspati-4 DFS/UFS",JOptionPane.PLAIN_MESSAGE);
        //System.out.println("This is Brihaspati 4 Distributed File System : A peer-to-peer cloud storage system");
        //System.out.println("You will get cloud storage 50% that of local storage offered (e.g.500MB cloud storage for 1GB local disk space offered)");
        //Functions to get emailID, pubU and and pvtU

        //Obtain the free space in current working directory at the time of initialization
        File file = new File(System.getProperty("user.dir"));
        config.initlocalFree= file.getFreeSpace();
        int i=0;
        while(i<3) {
            String mailID = JOptionPane.showInputDialog("Please specify your registered email ID for B4:");
            String localdisk = JOptionPane.showInputDialog("Please specify local disk to be offered in GB: ");
            //System.out.println("Please specify local disk to be offered in GB:");
            //String localdisk = sc.nextLine();
            if (Util.isValidEmail(mailID) && Util.isValidFloat(localdisk))
            {
                config.rootinode = "dfs://"+mailID+"/";
                float localoffered = Float.parseFloat(localdisk)*1024*1024*1024;
                long localOffered = (long)localoffered;
                if(config.initlocalFree>localOffered)
                {
                    config.localOffered=localOffered;
                    config.cloudAuth=config.localOffered/2;
                    config.cloudAvlb=config.cloudAuth;
                    config.localCacheSize=config.cloudAuth/10;
                    //setCacheOccupied();
                }
                else
                {
                    System.out.println("Disk space available is insufficient.");
                    System.out.println("Disk space is: "+(config.initlocalFree/(1024*1024*1024))+"GB");
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

        if(i==3)
        {
            System.out.println("You have exceeded the attempts.");
            System.exit(i);
        }

        config.dfsDir = System.getProperty("user.dir")+System.getProperty("file.separator")+ "b4dfs";
        Path pathDir = Paths.get(config.dfsDir);

        ufsDir = System.getProperty("user.dir")+System.getProperty("file.separator")+ "b4ufs";
        Path pathuDir = Paths.get(ufsDir);

        config.dfsSrvr = config.dfsDir + System.getProperty("file.separator")+ "dfsSrvr";
        Path pathSrvr = Paths.get(config.dfsSrvr);

        dfsCache = config.dfsDir + System.getProperty("file.separator")+ "dfsCache";
        Path pathCache = Paths.get(dfsCache);

        ufsCache = ufsDir + System.getProperty("file.separator")+ "ufsCache";
        Path pathuCache = Paths.get(ufsCache);

        Path pathFile = Paths.get(configFile);


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
            setCloudOccupied(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean flag = writeConfig(config);
        if(flag)
            JOptionPane.showMessageDialog(frame,"DFS initialized successfully.\nUFS initialized successfully","Brihaspati-4 DFS/UFS",JOptionPane.PLAIN_MESSAGE);
        else
            JOptionPane.showMessageDialog(frame,"DFS initialization failed.\nUFS initialization failed","Brihaspati-4 DFS/UFS",JOptionPane.PLAIN_MESSAGE);

    }


    void regInit() throws IOException {
        JFrame frame;
        frame = new JFrame("Brihaspati-4 DFS/UFS");
        JOptionPane.showMessageDialog(frame,"DFS is already initialized.\nUFS is already initialized","Brihaspati-4 DFS/UFS",JOptionPane.PLAIN_MESSAGE);
        //System.out.println("DFS is already initialized");
        //System.out.println("Configuration file is in: "+configFile);
        boolean flag = readConfig();
        if(flag)
        {
            System.out.println("Your root inode is: "+config.rootinode);
            System.out.println("Your authorized cloud space is: "+ (config.getCloudAuth()/(1024*1024*1024))+"GB");
            System.out.println("You have already used: "+ (config.getCloudOccupied()/(1024*1024))+"MB cloud space");
            System.out.println("Cloud space available is:" + (config.getCloudAvlb()/(1024*1024))+"MB");
            setCacheOccupied();
            JOptionPane.showMessageDialog(frame,"Your authorized cloud space is: "+ (config.getCloudAuth()/(1024*1024*1024))+"GB" +
                    "\nCloud space available is:" + (getCloudAvlb()/(1024*1024))+"MB"
                    +"\nCache occupied is:"+ (cacheOccupied/(1024*1024))+"MB","Brihaspati-4 DFS/UFS",JOptionPane.PLAIN_MESSAGE);
            long localSpace = getLocalOccupied();
            if(localSpace<(0.75*localOffered))
                System.out.println("Adequate local space available.");
            if(localSpace>(0.75*localOffered)&&localSpace<(0.9*localOffered))
            {
                JOptionPane.showMessageDialog(frame,"Local space occupied is more than 75% of space offered"
                        +"\nPlease free up local space","Brihaspati-4 DFS/UFS",JOptionPane.WARNING_MESSAGE);
                if(localSpace>(0.9*localOffered))
                {
                    JOptionPane.showMessageDialog(frame,"Local space occupied is more than 90% of space offered"
                            +"\nYou cannot use B4 DFS unless you free up local space ","Brihaspati-4 DFS/UFS",JOptionPane.WARNING_MESSAGE);
                    System.exit(1);
                }
            }

        }
    }

    private boolean writeConfig(DFSConfig config) {
        try {
            FileOutputStream fos = new FileOutputStream(configFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(config);
            oos.close();
            System.out.println("DFS Config file updated successfully");
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Failed to write configuration in file");
            return false;
        }
    }
    private boolean readConfig() {
        try {
            FileInputStream fis= new FileInputStream(configFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            config = (DFSConfig)ois.readObject();
            ois.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Config File not found");
            return false;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Error in config file reading");
            return false;
        }
    }


    public static String getRootinode() {
        boolean flag = config.readConfig();
        if(flag)
            return config.rootinode;
        else
            return null;
    }

    String rootinode;
    //String PubU = null;
    //String PubN = null;
    long initlocalFree;
    public long localfree;
    public long localOffered;

    public static long setLocalOccupied(long localOccupied) {
        config.localOccupied = config.localOccupied+localOccupied;
        System.out.println("Local space Occupied:"+config.localOccupied);
        return config.localOccupied;
    }

    public long localOccupied;

    public static String getMailID() {
        boolean flag = config.readConfig();
        if(flag)
            return config.mailID;
        else
            return null;
    }

    public String mailID;

    public static void update(long fileSize) throws IOException {
        setCloudOccupied(fileSize);
        setCloudAvlb(config.cloudOccupied);
        setCacheOccupied();
        boolean flag = config.writeConfig(config);
        if(flag) {
            System.out.println("Now cloud occupied is: " + (getCloudOccupied()/(1024*1024))+"MB");
            System.out.println("Now cloud available is: " + (getCloudAvlb()/(1024*1024))+"MB");
        }
        else
            System.out.println("DFS config update failed");
        }


    /**
     * @return the cloudAuth
     */
    public double getCloudAuth() {
        boolean flag = config.readConfig();
        if(flag)
            return config.cloudAuth;
        else
            return 0;
    }

    /**
     * @return the cloudOccupied
     */
    public static long getCloudOccupied() {
        boolean flag = config.readConfig();
        if(flag)
            return config.cloudOccupied;
        else
            return 0;
    }

    /**
     * @param fileSize the cloudOccupied to set
     */
    public static void setCloudOccupied(long fileSize) {
        config.cloudOccupied = config.cloudOccupied + fileSize;
        boolean flag = config.writeConfig(config);
        if(flag);
        else System.out.println("Cloud Occupied update filed");
    }

    /**
     * @return the cloudAvlb
     */
    public static long getCloudAvlb() throws IOException{
        boolean flag = config.readConfig();
        if(flag)
            return config.cloudAvlb;
        else
            return 0;
    }

    /**
     * @param cloudOccupied
     */
    public static void setCloudAvlb(long cloudOccupied) {
        config.cloudAvlb = config.cloudAvlb- cloudOccupied;
        boolean flag = config.writeConfig(config);
        if(flag);
        else System.out.println("Cloud Available update filed");
    }
    long cloudAuth;
    long cloudOccupied;
    long cloudAvlb;
    long localCacheSize;
    public static long getLocalCacheSize() {
        boolean flag = config.readConfig();
        if(flag)
            return config.localCacheSize;
        else
            return 0;
    }

    static long cacheOccupied;
    String dfsDir;
    String dfsSrvr;
    public static String dfsCache=System.getProperty("user.dir") +System.getProperty("file.separator")+
            "b4dfs"+System.getProperty("file.separator")+"dfsCache"+System.getProperty("file.separator");
    static String configFile;

    /**
     * @return the localfree
     */
    public long getLocalfree() {
            boolean flag = config.readConfig();
            if(flag)
                return config.localfree;
            else
                return 0;
    }

    /**
     *
     */
    public void setLocalfree() {
        String dfsSrvr=config.dfsDir+System.getProperty("file.separator")+"b4dfs"+System.getProperty("file.separator")+"dfsSrvr"+System.getProperty("file.separator");
        File file = new File(dfsSrvr);
        config.localfree=getLocalOffered()-file.getFreeSpace();
        boolean flag = config.writeConfig(config);
        if(flag);
        else System.out.println("Local free update filed");
    }

    /**
     * @return the localOffered
     */
    public long getLocalOffered() {
        boolean flag = config.readConfig();
        if(flag)
            return config.localOffered;
        else
            return 0;
    }

    /**
     * @return the localOccupied
     */
    public long getLocalOccupied() {
        String dfsSrvr=config.dfsDir+System.getProperty("file.separator")+"dfsSrvr"+System.getProperty("file.separator");
        System.out.println(dfsSrvr);
        File file = new File(dfsSrvr);
        config.localOccupied=file.getTotalSpace();
        System.out.println("Local Occupied: "+config.localOccupied);
        return config.localOccupied;
    }

    /**
     * @return the cacheOccupied
     */
    public static long getCacheOccupied() {
        boolean flag = config.readConfig();
        if(flag)
            return cacheOccupied;
        else
            return 0;
    }

    public static void setCacheOccupied() {

        File cacheDir = new File(dfsCache);
        File[] files = cacheDir.listFiles();
        long length = 0;
        int count;
        count = Objects.requireNonNull(files).length;
        // loop for traversing the directory
        for (int i = 0; i < count; i++) {
            length += files[i].length();
        }
        cacheOccupied=length;
        System.out.println("Cache Size Updated.."+ (cacheOccupied/(1024*1024))+"MB");
    }
}