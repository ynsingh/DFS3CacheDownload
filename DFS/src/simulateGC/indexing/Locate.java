package simulateGC.indexing;

import dfsUfsCore.dfs3Mgr.DFS3Config;
import simulateGC.communication.Sender;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.security.GeneralSecurityException;

import static dfsUfsCore.dfs3Util.file.readdata;
import static dfsUfsCore.dfs3xmlHandler.XMLWriter.writer;

/**
 * Class responsible for locating the segment in the  local Disk.
 * This class locates the segment stored in the local disk
 * based on the segment inode.
 *<p><b>Functions:</b> At the cloud end of DFS</p>
 * <b>Note:</b> Change this file to change functionality
 * related to finding a segment in the local disk
 * @author <a href="https://t.me/sidharthiitk">Sidharth Patra</a>
 * @since   2020-02-16
 */
public class Locate {
    /**
     * Starts the Locate process.
     * This method is responsible for receiving the segment inode and locate the
     * same in the local disk. Once located read the segment data and return it
     * to the user requesting the segment.
     *
     * @param inode hashed segment inode (DFS://emailID//segmentpath)
     * this is used as the primary key for looking up the file from local disk.
     * @throws IOException for input output exception
     * @throws GeneralSecurityException In case of general security violation occurs
     */
    private static boolean hash;

    public static void start(String inode, String askerIP) throws IOException, GeneralSecurityException, XMLStreamException {
        //get the file where index is maintained by storage
        String path = System.getProperty("user.dir") + System.getProperty("file.separator")
                + "root_index.csv";
        //read the index and retrieve the local path
        String localPath = csvreader(path, inode);
        // read the file from local path
        byte[] encFile = readdata(localPath);
        //byte[] xmlData = deconcat(encFile,16);
        //String tempXml = System.getProperty("user.dir") + System.getProperty("file.separator")+inode+".xml";
        //dfsUfsCore.dfs3Util.file.writeData(xmlData, tempXml);
        //System.out.println("reached here:"+xmlpath);
        //boolean isInode = isInodeReader(tempXml);
        //System.out.println("isInode:"+isInode);
        //System.out.println("reached here");
        // reply through XML. tag for reply to download is 20
        String xmlPath = writer(20, inode, encFile, false);
        // TODO - handover the file to file sender and delete the line below
        //Send the file to output buffer.
        DFS3Config.bufferMgr.addToOutputBuffer(new File(xmlPath));
        // TODO - query the dht and get the IP
        Sender.start(DFS3Config.bufferMgr.fetchFromOutputBuffer(), "localhost"); //simulates communication manager.
        dfsUfsCore.dfs3Util.file.deleteFile(xmlPath);
    }

    /**
     * This method is responsible to read the index "root_index.csv"
     * and return the local path corresponding to a particular
     * inode to the calling function
     * @param inode hashed segment inode (DFS://emailID//segmentpath)
     * this is used as the primary key
     * @param fileName name of the file where index is maintained
     * @return localPath returns local path of a segment to the calling function
     */
    public static String csvreader (String fileName, String inode){
        String line;
        // CSV records are separated by comma
        String cvsSplitBy = ",";
        String localPath = null;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            //loop continues till all lines are finished
            while ((line = br.readLine()) != null) {
                // String array to store the records
                String[] record = line.split(cvsSplitBy);
                // if the first record matches inode then the store the
                // value against it in the variable localPath
                if (record[0].equals(inode)) {
                    localPath = record[1];
                    //System.out.println("[inode = " + record[0] + " , localPath =" + record[1] + "]");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return localPath;
    }
}
