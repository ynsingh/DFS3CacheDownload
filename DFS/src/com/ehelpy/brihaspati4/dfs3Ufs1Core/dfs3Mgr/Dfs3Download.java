package com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Mgr;

import com.ehelpy.brihaspati4.simulateGC.communication.Sender; // Demo/testing. TO be integrated with Communication Manager.
import com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Util.TLVParser;
import com.ehelpy.brihaspati4.simulateGC.encrypt.Encrypt; // Demo/testing. TO be integrated with isec/encryption Manager.
import com.ehelpy.brihaspati4.simulateGC.encrypt.GenerateKeys;
import com.ehelpy.brihaspati4.simulateGC.encrypt.Hash; //Demo/testing. TO be integrated with isec/encryption Manager
import com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3xmlHandler.InodeReader;
import com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3xmlHandler.ReadInode;

import static com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Util.file.*;
import static com.ehelpy.brihaspati4.simulateGC.encrypt.Hash.comparehash; //Demo/testing. TO be integrated with isec/encryption Manager
import static com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3xmlHandler.XMLWriter.writer;

import org.xml.sax.SAXException;
import javax.swing.*;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

/**
 * Class responsible for performing Download.
 * This class downloads the file from the DFS.
 * <p><b>Functions:</b> At the user end of DFS.</p>
 * <b>Note:</b> Change this file to change functionality
 * related to Download.
 * @author <a> href="https://t.me/sidharthiitk">Sidharth Patra</a>
 * @since   15th Feb 2020
 */
public class Dfs3Download{
    static DFS3Config dfs3_ufs1 = DFS3Config.getInstance();
    static String fileName = null;
    static int segmentCount = 0;
    static TreeMap<String, String> splits= new TreeMap<>();
    static String fileURI=null;
    static boolean isDFS;
    static PublicKey pubKey;
    static JDialog dialog = new JDialog();
    /**
     * This method starts the download of the file from DFS.
     * It interacts with the communication manager on behalf of DFS behalf for download process.
     * @param fileUri fileUri of the file that user selected.
     * @param isDfs Indicates whether DFS or UFS.
     * @throws IOException for input output exception
     * @throws GeneralSecurityException In case of general security violation occurs
     */
    public static void start(String fileUri, boolean isDfs) throws IOException, GeneralSecurityException {

        //Display wait message.
        JLabel label = new JLabel("Downloading. Please wait..."); //For demo/testing. To be replaced by GUI once developed.
        dialog.setLocationRelativeTo(null);
        dialog.setTitle("Downloading. Please Wait...");
        dialog.add(label);
        dialog.pack();
        dialog.setVisible(true);

        //String fileName;
        fileURI=fileUri;
        String xmlCachePath;
        isDFS=isDfs;
        System.out.println("File URI: "+ fileURI);
        String hash;
        //Check whether file is being downloaded using the hash of its inode file in UFS.
        boolean isHash = Hash.isValidSHA1(fileURI);
        if (isHash) {
            //Fetch inode file from the network
            hash = writer(2, fileURI, "Nothing".getBytes(), true);
            //Send the file to output buffer.
            dfs3_ufs1.bufferMgr.addToOutputBuffer(new File(hash));
            // TODO - query the dht and get the IP
            Sender.start(dfs3_ufs1.bufferMgr.fetchFromOutputBuffer(), "192.168.56.125"); //simulates communication manager.
            com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Util.file.deleteFile(hash);
        }
        else
        {
            //Extract file name from file URI.
            System.out.println(fileURI + " being downloaded");
            String[] parts = fileURI.split("/");
            int i = parts.length;
            fileName = parts[i - 1];
            if(isDFS) {
                //path for inode of the file in xml format in local cache.
                xmlCachePath= dfs3_ufs1.getDfsCache()+ fileName + "_Inode.xml";
            }
            else {
                //In case of UFS, check whether file is csv holding index of uploaded file.
                if (fileName.equals("UFSuploaded.csv")) {
                    System.out.println(fileName + " being downloaded");
                } else {
                    //In case of UFS, file is uploaded with file Name itself (without any dfs://emailID.com prefix)
                    fileName = fileURI;
                }
                xmlCachePath = dfs3_ufs1.getUfsCache()+ fileName + "_Inode.xml";
            }
            //Check whether inode xml corresponding to the file exists in cache
            Path file = Paths.get(xmlCachePath);
            if(Files.exists(file)) {
                //if inode and file parts available in cache, stitch them from local cache itself
                System.out.println("File inode found in local cache...");
                //Create inode object and read using inodeReader xml parser.
                byte[] inodeData = readdata(xmlCachePath);
                byte[] padding = new byte[16];
                byte[] inodePadded = com.ehelpy.brihaspati4.simulateGC.encrypt.Encrypt.concat(padding, inodeData);
                parseInode(inodePadded);
            }
            else {
                //If file inode not found in the local cache..
                System.out.println("Being downloaded from the network...");
                //Download the inode from the network
                String inodeURI;
                System.out.println("File Name: " + fileName);
                if (fileName.equals("DFSuploaded.csv") || fileName.equals("UFSuploaded.csv")) {
                    inodeURI = fileURI;
                } else {
                    if (isDFS)
                        inodeURI = fileURI + "_Inode.xml";
                    else
                        inodeURI = fileName + "_Inode.xml";
                }
                System.out.println("inode: " + inodeURI);
                String hashedInode = Hash.hashpath(inodeURI);
                System.out.println("Hash of inode file:" + hashedInode);
                /*xml query  with fileUri.tag for download is 2
                the data filed is blank hence "Nothing" to avoid null pointer exception*/
                String xmlPath;
                if (fileName.equals("DFSuploaded.csv") || fileName.equals("UFSuploaded.csv"))
                    xmlPath = writer(2, hashedInode, "Nothing".getBytes(), false);
                else
                    xmlPath = writer(2, hashedInode, "Nothing".getBytes(), true);
                //System.out.println("xmlPath: "+xmlPath);
                // TODO - retrieve the Ip of the node responsible
                //Send the file to output buffer.
                dfs3_ufs1.bufferMgr.addToOutputBuffer(new File(xmlPath));
                // TODO - query the dht and get the IP
                Sender.start(dfs3_ufs1.bufferMgr.fetchFromOutputBuffer(), "192.168.56.125"); //simulates communication manager.
                //com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Util.file.deleteFile(xmlPath);
            }
        }
    }//end of start

    /**
     * This method stitches all the segments together by reading it from cache one by one in order.
     * @param splitParts hashmap of keys and values of segment names and corresponding hash values.
     * @return returns the completely stitched file in byte array format.
     */
    private static byte[] stitchFromCache(TreeMap<String, String> splitParts) {
        String dir;
        if(isDFS)
            dir = dfs3_ufs1.getDfsCache();
        else
            dir = dfs3_ufs1.getUfsCache();
        byte[] completeFile = new byte[0];
        Set<String> set = splitParts.keySet();
        Object[] o = set.toArray();
        String[] splits = new String[set.size()];
        for (int i =0; i<set.size(); i++)
        {
            splits[i]=dir+o[i].toString();
        }
        for(int i =0;i< splits.length  && !(splits[i]==null);i++){
            // read data of each segment
            byte[] segmentData = readdata(splits[i]);
            // concat the segment data to the completefile byte array
            completeFile = Encrypt.concat(completeFile,segmentData);
        }
        return completeFile;
    }

    /**
     *<p>1.This method receives name of the file and completely stitched file.</p>
     *<p>2. Sends it for post download encryption and/or verification depending upon DFS/UFS. </p>
     * @param fileURI file URI
     * @param completefile completely stitched file.
     * @param pubKey public key of the uploader
     */
    public static void postDownload(String fileURI, byte[] completefile, PublicKey pubKey) throws NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, IOException, InvalidKeyException {

        boolean flag;
        if (isDFS)
            flag = dfsDownload(fileURI, completefile);
        else
            flag = ufsDownload(completefile, pubKey);
        segmentCount=0;
        splits.clear();
        dfs3_ufs1.setCacheOccupied();
        JFrame frame;
        frame = new JFrame();
        dialog.setVisible(false);
        if(flag)
            JOptionPane.showMessageDialog(frame,"Download successful!","Brihaspati-4 DFS/UFS",JOptionPane.PLAIN_MESSAGE);
        else
            JOptionPane.showMessageDialog(frame,"Download failed!","Brihaspati-4 DFS/UFS",JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * This method decrypts the file and writes on local disk if hash is matched with original file hash.
     * @param fileURI name of the file.
     * @param completefile copmletely stitched file.
     * @return boolean flag indicating success of failure.
     */
    private static boolean dfsDownload(String fileURI, byte[] completefile) {
        // compare the hash of completefile with the original filepluskey
        boolean hashMatch = comparehash(fileURI,completefile, isDFS);
        // retrieve data, decrypt data after decrypting key
        // write the file on decrypting data
        if(hashMatch) {
            // if the hash matches then retrieve encrypted key
            byte[] encKey = TLVParser.startParsing(completefile, 2);
            // retrieve framed data
            byte[] serialisedEncData = deconcat(completefile, encKey.length + 8);
            // retrieve encrypted data after parsing the TLV
            byte[] encData = TLVParser.startParsing(serialisedEncData, 3);
            // get decrypted data
            byte[] data = new byte[0];
            try {
                data = Encrypt.startDec(encData, encKey);
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }
            // write the data to original inode for the user
            String[] writePath = fileName.split("@@");
            writeData(data, writePath[0]);
            return true;
        }
        else
        {
            System.out.println("hash mismatch");
            return false;
        }
    }

    /**
     * <p>1.This method detaches hash of file embedded while uploading.</p>
     * <p>2.writes on local disk if hash is matched with original file hash.</p>
     * @param completeFile completely stitched file.
     * @param pubKey public key of the uploader extracted from inode file.
     * @return boolean flag indicating success/failure.
     */
    private static boolean ufsDownload(byte[] completeFile, PublicKey pubKey) throws NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, IOException, InvalidKeyException {

        //discard first eight bytes of tlv frame
        byte[] deFramed = deconcat(completeFile, 8);
        byte[] signedHash= Arrays.copyOf(deFramed,128);
        byte[] data = deconcat(deFramed,128);
        String hashCalc= null;
        try {
            hashCalc = Hash.hashgenerator(data);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        assert hashCalc != null;
        boolean hashMatch = GenerateKeys.verifyHashK(hashCalc.getBytes(), signedHash, pubKey);
        //assert hashCalc != null;
        //byte[] hasharray = hashCalc.getBytes();
        //System.out.println("Hash calculated from received file: "+hashCalc);
        if(hashMatch) {
            String[] writePath = fileName.split("@@");
            writeData(data, writePath[0]);
            System.out.println("Download successfully completed!");
            return true;
        }
        else
        {
            System.out.println("Hash Mismatch");
            return false;
        }
    }

    /**
     * <p>This method receives the downloaded segments by interacting with the communication manager and processes them further.</p>
     * <p>Checks whether receieved file segment is inode itself and proceeds accordingly</p>
     * @param inbound byte array received from the communication manager
     * @throws IOException for input output exception
     * @throws GeneralSecurityException In case of general security violation occurs
     */
    public static void segmentDownload(byte[] inbound) throws GeneralSecurityException, IOException {

        //Checek if downloaded segment is an inode
        boolean flag = checkInode(inbound);
        System.out.println("isInode flag: " + flag);
        //If inode, parse it and fetch segments from the netowrk.
        if (flag) {
            parseInode(inbound);
        }
        //If not inode, then it is a file segment, send it for reassembly.
        else
        {
            Reassembly.start(inbound, fileName);
            segmentCount++;
            System.out.println(segmentCount + " files have been downloaded");
            if (fileName.equals("DFSuploaded.csv") || fileName.equals("UFSuploaded.csv")) {
                /*If downloaded file is an index of uploaded file, then display it to the user,
                so that user can choose file to download.*/
                System.out.println("Root Directory has been downloaded from the cloud successfully!");
                segmentCount--; //index file is not part of segment count.
                DFS3ListFiles.start(isDFS);
            } else {
                //Obtain the list of split parts.
                List<String> splitList = new ArrayList<>(splits.keySet());
                String[] segmentInodes = splitList.toArray(new String[0]);
                System.out.println("Total Number of segments: "+segmentInodes.length);
                //System.out.println("Segments: "+splitList);
                // if all segments have been downloaded then start stitching them
                if (segmentCount == segmentInodes.length) {
                    byte[] completeFile = stitchFromCache(splits);
                    System.out.println("File Stitched");
                    if (isDFS)
                        postDownload(fileURI, completeFile, pubKey);
                    else
                        postDownload(fileName, completeFile, pubKey);
                } else
                    System.out.println("Download in progress");
            }
        }
    }

    /**
     * Method to check whether downloaded segment is an inode.
     * @param inbound byte array of inbound file.
     * @return flag indicating inode or not.
     */
    private static boolean checkInode(byte[] inbound) {
        String tempXml=System.getProperty("user.dir")+System.getProperty("file.separator")+"temp.xml";
        byte[] data = deconcat(inbound,16);
        com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Util.file.writeData(data, tempXml);

        String xsd = System.getProperty("user.dir")+System.getProperty("file.separator")+"inode.xsd";
        SchemaFactory sFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            sFactory.newSchema(new File(xsd));
            Schema schema = sFactory.newSchema(new File(xsd));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new File(tempXml)));
            com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Util.file.deleteFile(tempXml);
            return true;
        } catch (SAXException | IOException e) {
            //e.printStackTrace();
            System.out.println("not an inode");
            com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Util.file.deleteFile(tempXml);
            return false;
        }
    }

    /**
     * <p>1.This method is invoked when inbound file is found to be inode.</p>
     * <p>2.Inode is read using xml parser and various values are obtained and set from it.</p>
     * <p>3.List of hash values of all segments is obtained and forwarded to network for download.</p>
     * @param decoded byte array of inode file.
     */
    public static void parseInode(byte[] decoded) {

        // discard first 16 bytes which indicate type and length.
        byte[] data = deconcat(decoded,16);
        boolean locallyFound;
        String xmlCachePath;
        if(isDFS)
            xmlCachePath= dfs3_ufs1.getDfsCache()+fileName + "_Inode.xml";
        else
            xmlCachePath= dfs3_ufs1.getUfsCache()+fileName + "_Inode.xml";
        writeData(data,xmlCachePath);
        ReadInode inodeReader;
        try{
            inodeReader = InodeReader.reader(xmlCachePath);
            fileName=inodeReader.getFileName();
            System.out.println("File Name:" + inodeReader.getFileName());
            pubKey = inodeReader.getPubKey();
            HashMap<String, String> splitList = inodeReader.getSplitParts();
            splits= new TreeMap<>(splitList);
            System.out.println(splitList);
            for(String splitPart : splitList.keySet()) {
                String hashedInode;
                if(isDFS) {
                    hashedInode = Hash.hashpath(dfs3_ufs1.getRootInode() + splitPart);
                    String dfsLocalPath = dfs3_ufs1.getDfsCache()+splitPart;
                    Path file = Paths.get(dfsLocalPath);
                    locallyFound=Files.exists(file);
                }
                else
                {
                    hashedInode = Hash.hashpath(splitPart);
                    String ufsLocalPath = dfs3_ufs1.getUfsCache()+splitPart;
                    Path file = Paths.get(ufsLocalPath);
                    locallyFound=Files.exists(file);
                }

                // xml query  with inode.tag for download is 2
                //the data filed is blank hence "Nothing" to avoid null pointer exception
                if(!locallyFound) {
                    String xmlPath = writer(2, hashedInode, "nothing".getBytes(), false);
                    if (isDFS)
                        System.out.println("Query for " + dfs3_ufs1.getRootInode() + splitPart + " sent to network");
                    else
                        System.out.println("Query for " + splitPart + " sent to network");
                    System.out.println("Query sent: "+ hashedInode);
                    //Send the file to output buffer.
                    dfs3_ufs1.bufferMgr.addToOutputBuffer(new File(xmlPath));
                    // TODO - query the dht and get the IP
                    Sender.start(dfs3_ufs1.bufferMgr.fetchFromOutputBuffer(), "192.168.56.125"); //simulates communication manager.
                    com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Util.file.deleteFile(xmlPath);
                }
                else {
                    System.out.println(splitPart + " found in local cache");
                    segmentCount++;
                    if (segmentCount == splits.size()) {
                        byte[] completeFile = stitchFromCache(splits);
                        System.out.println("File Stitched");
                        if (isDFS)
                            postDownload(fileURI, completeFile, pubKey);
                        else
                            postDownload(fileName, completeFile, pubKey);
                    }
                }
            }
        }
        catch(Exception e){
            System.out.println("This is not an inode file");
        }

    }

    public static class Reassembly {

        /**
         * Receives various parameters from segment download and sends for sequencing.
          * @param inbound inbound file in byte array form.
         * @param fileName name of the file
         * @throws IOException in case of IO exception.
         */
        public static void start(byte[] inbound, String fileName) throws IOException {
            sequencing(inbound,fileName);
        }

        /**
         * Sequences the segments in proper order.
         *
         * @param fileName  Name of file to be split.
         * @param inbound number of kilo bytes per chunk.
         */
        public static void sequencing(byte[] inbound,String fileName) {
            int sequenceNo = 0;
            // parse the down loaded segements by Type, Length and value
            // here sequenced means the parsed byte array
            byte[] sequenced = TLVParser.startParsing(inbound, 4);
            // if three Zeros follow the value right at start then the value
            // is sequence number
            if(sequenced[1]==0 && sequenced[2]==0 &&sequenced[3]==0)
                sequenceNo = sequenced[0];
            //byte[] data = TLVParser.startParsing(sequenced,4);
            System.out.println("Segment No: "+ sequenceNo +" under progress");
            System.out.println(fileName);
            // after retrieving the sequence number retain only value
            // discard type and length
            byte[] data = deconcat(inbound,16);
            // access the splitIndex to retrieve the inode for the segment
            String[] segmentInodes = new String[1];
            if(fileName.equals("DFSuploaded.csv")||fileName.equals("UFSuploaded.csv")) {
                //System.out.println("reached here..");
                segmentInodes[0] = fileName;
            }
            else
            {
                List<String> splitList = new ArrayList<>(splits.keySet());
                segmentInodes = splitList.toArray(new String[0]);
            }
            for(int i=0; i<=segmentInodes.length; i++)
            {
                if(sequenceNo==i) {
                    String segmentInode = segmentInodes[i-1];
                    System.out.println("Writing segmentInode:" + segmentInode);
                    String writePath;
                    if(isDFS)
                        writePath = dfs3_ufs1.getDfsCache() + segmentInode;
                    else
                        writePath = dfs3_ufs1.getUfsCache() + segmentInode;
                    // write the segmentdata to the segment inode
                    writeData(data, writePath);
                }

            }

        }

    }
}