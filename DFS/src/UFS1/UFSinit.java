package UFS1;
import dfs3test.communication.Sender;
import dfs3test.encrypt.Hash;
import init.DFSConfig;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static dfs3test.encrypt.Encrypt.concat;
import static dfs3test.encrypt.Hash.hashgenerator;
import static dfs3test.xmlHandler.XMLWriter.writer;

public class UFSinit {
    static String dfsID=DFSConfig.getRootinode();
    static String mailID=dfsID.split("/")[2];
    static String hashOfmailID;
    static String ufsDir;
    static String ufsCacheDir;
    static String ufsSrvrDir;
    static String ufsInodeFile;
    static {
        try {
            hashOfmailID = Hash.hashpath(mailID);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static void initUFS() throws IOException, NoSuchAlgorithmException {
        //Email ID is extracted from dsf configuration file
        System.out.println("Email ID registered for UFS: "+ mailID);
        //List of uploaded public content shall be listed at hash of mailID
        System.out.println("Hash of mail ID: "+hashOfmailID);
        ufsDir=System.getProperty("user.dir")+System.getProperty("file.separator")+"b4ufs"+System.getProperty("file.separator");
        ufsCacheDir=ufsDir+"ufsCache";
        ufsSrvrDir=ufsDir+"ufsSrvr";
        File file = new File(ufsDir);
        File file1 = new File(ufsCacheDir);
        File file2 = new File(ufsSrvrDir);
        if(!file.exists()||!file1.exists()||!file2.exists())
        {
            Boolean dirCreated = file.mkdir();
            Boolean cahceCreated = file1.mkdir();
            Boolean SrvrCreated = file2.mkdir();
            System.out.println("UFS directories created");

        }
        else System.out.println("UFS directories already exist");
        ufsInodeFile=ufsCacheDir+System.getProperty("file.separator")+"ufsInode.csv";
        File file3= new File(ufsInodeFile);
        try {
            if(file3.createNewFile())
                System.out.println("UFS inode created");
            else
                System.out.println ("UFS inode already exists");
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] inodeData= dfs3Util.file.readdata(ufsInodeFile);
        String hashofinode = hashgenerator(inodeData);
        byte[] fileTx = concat(hashofinode.getBytes(), inodeData);
        String xmlPath = writer(1, hashOfmailID, fileTx, false);
        // handover the xml query to xmlSender (token for upload is 1)
        Sender.start(xmlPath, "localhost");
        System.out.println("Uploading UFS segment to the cloud");
    }
}
