package com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Mgr;

import com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Util.TLVParser;
import com.ehelpy.brihaspati4.simulateGC.communication.Receiver;
import com.ehelpy.brihaspati4.simulateGC.communication.Sender; // For demo/testing. to be replaced with communication manager post integration with b4mail client.
import com.ehelpy.brihaspati4.simulateGC.encrypt.*;// For demo/testing. to be replaced with encryption/isec module post integration with b4mail client
import static com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Util.file.*;
import static com.ehelpy.brihaspati4.simulateGC.encrypt.Encrypt.concat;
import static com.ehelpy.brihaspati4.simulateGC.encrypt.Hash.hashgenerator;
import static com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3xmlHandler.XMLWriter.writer;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.*;
import com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3xmlHandler.*;
import org.apache.log4j.Logger;

import javax.swing.*;

/**
 * Class responsible for uploading a File into DFS or UFS.
 * This class gets the file selected by user and uploads it
 * into the DFS/UFS using DHT
 * <p><b>Functions:</b> At the user end of DFS</p>
 * <b>Note:</b> Change this file to change functionality
 * related to the upload function
 * @author  for DFS2 - Sidharth Patra
 * @author  for DFS3 - Amey Rajeev Hasabnis
 * @since DFS2 - 13th Feb 2020, DFS3-July 2020
 */
public class DFS3Upload {
    static DFS3Config dfs3_ufs1 = DFS3Config.getInstance();
    //static Boolean fBit= Boolean.TRUE; to be integrated with indexing manager after integration for declaring file perpetual/non-perpetual
    static HashMap<String, String> index=new HashMap<>();
    private static final Logger log = Logger.getLogger(Receiver.class.getName());
    /**
     *
     * @param isDFS variable indicating file is being uploaded to DFS or UFS.
     * @return boolean flag indicating success/failure of the uploading of a file.
     * @throws NullPointerException in case of null pointer
     * @throws IOException in case of IO error
     * @throws GeneralSecurityException in case of security exception
     */
    public static boolean start(boolean isDFS) throws NullPointerException, IOException,
            GeneralSecurityException {
        //Ask user to choose a file through JFileChooser GUI.
        String path = readpath();
        //Display wait dialogue box.
        JDialog dialog = new JDialog(); //for demo/testing. To be replaced with GUI once developed.
        JLabel label = new JLabel("Uploading. Please wait...");
        dialog.setLocationRelativeTo(null);
        dialog.setTitle("Uploading. Please Wait...");
        dialog.add(label);
        dialog.pack();
        dialog.setVisible(true);
        Path path1 = Paths.get(path);
        String fileName = path1.getFileName().toString();
        //fileSuffix appends timestamp to a file.
        String fileSuffix = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String fileWithTimeStamp = fileName+"@@"+fileSuffix;
        /* fileURI is the unique URI of a file in name space of P2P network.
        e.g. for DFS -> dfs://ameyrh@iitk.ac.in/abc.pdf@@20210520150655
        e.g. for UFS -> abc.pdf@@20210520150655 */
        String fileURI = dfs3_ufs1.getRootInode() + fileWithTimeStamp;
        long fileSize = checkFileSize(path);
        //check whether adequate space is available in the user DFS cloud or it is a UFS upload.
        try {
            long cloudAvlb = dfs3_ufs1.getCloudAvlb();
            if (cloudAvlb > fileSize || !isDFS) {
                //System.out.println("File Size is: " + (fileSize / (1024 * 1024)) + "MB");
                log.debug("Available cloud space checked - File can be uploaded.");
                //Read file using FileChannel and ByteBuffer
                RandomAccessFile randomAccessFile = new RandomAccessFile(path, "r");
                ByteBuffer encData;
                try (FileChannel channel = randomAccessFile.getChannel()) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());//allocate new ByteBuffer of size of file
                    long bufferSize = channel.size();
                    ByteBuffer buff = ByteBuffer.allocate((int) bufferSize);
                    channel.read(buff);
                    buff.flip();
                    byte[] plainData = buff.array();
                    byteBuffer.flip();
                    randomAccessFile.close();
                    channel.close();
                    //Encrypt the file and key and combine both using TLV framing for DFS
                    byte[] filePlusKey;
                    if(isDFS) {
                        filePlusKey = Encrypt.startEnc(plainData);
                        log.debug("file encryption successful!");
                    }
                    //Append hash of file in front of the file data - to be used to verify file integrity post download.
                    else {
                        String fileHash=hashgenerator(plainData);
                        byte [] hashByteArray=fileHash.getBytes();
                        byte[] signedHash = GenerateKeys.signHash(hashByteArray);
                        filePlusKey = concat(signedHash,plainData);
                        log.debug("Signed Hash for UFS apprended.");
                    }
                    encData = ByteBuffer.wrap(filePlusKey);
                    //send the file for segmentation
                    Segmentation.start(encData, path, isDFS, fileWithTimeStamp);
                    log.debug("file segmentation successful!");
                    //write inode for the file being uploaded
                    InodeWriter.writeInode(fileWithTimeStamp, fileSize, index, isDFS);
                }
                //Read  the segments from index and upload them one by one
                String splitFile;
                if(isDFS)
                    splitFile = dfs3_ufs1.getDfsCache() + fileWithTimeStamp +"_Inode.csv";
                else
                    splitFile = dfs3_ufs1.getUfsCache() + fileWithTimeStamp +"_Inode.csv";

                String[] segmentInode = csvreader(splitFile, path);
                for (int i = 0; i < segmentInode.length && !(segmentInode[i] == null); i++) {
                    dispatch(segmentInode[i], i, isDFS);
                }
                com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Util.file.deleteFile(splitFile);
                //Now uploading the inode of the file
                String inode;
                if(isDFS)
                    inode =  dfs3_ufs1.getDfsCache()+fileWithTimeStamp + "_Inode.xml";
                else
                    inode = dfs3_ufs1.getUfsCache()+ fileWithTimeStamp + "_Inode.xml";
                dispatch(inode, 0, isDFS);
                log.debug("Inode uploaded.");
                //Now uploading the updated root directory in the cloud
                String rootDir;
                if(isDFS)
                    rootDir = dfs3_ufs1.getDfsCache()+ "DFSuploaded.csv";
                else
                    rootDir = dfs3_ufs1.getUfsCache()+ "UFSuploaded.csv";
                String hashOfFile =hashgenerator(encData.array());
                if(isDFS) {
                    index(fileURI, hashOfFile, true); // For demo/testing. To be integrated with Indexing manager.
                }
                else
                {
                    String file= fileName +"@@"+fileSuffix;
                    index(file, hashOfFile, false); // For demo/testing. To be integrated with Indexing manager.
                }
                dispatch(rootDir, 0, isDFS);
                log.debug("Updated root directory uploaded.");
                log.debug("Upload completed");

                if(isDFS)
                    dfs3_ufs1.updateUpload(fileSize);
                dialog.setVisible(false);
                index.clear();
                return true;
            }
            else {
                dialog.setVisible(false);
                index.clear();
                return false;
            }
        }

        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            dialog.setVisible(false);
            return false;
        }

    }//end of start

    /**
     * This functions dispatches the file/segment into the cloud.
     * @param segmentInode segment inode name
     * @param i segment number
     * @param isDFS indicates whether DFS or UFS
     * @throws NoSuchAlgorithmException in case of encryption/hashing algorithm error
     * @throws IOException in case of IO exception
     * @throws InvalidKeySpecException in case of key error
     * @throws InvalidKeyException in case of key error
     * @throws SignatureException in case of signature error
     */
    private static void dispatch(String segmentInode, int i, boolean isDFS) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        //read data of the segment
        byte[] segmentData = readdata(segmentInode);
        //extract segment name from the inode
        File f=new File(segmentInode);
        String segmentName = f.getName();
        //insert sequence number into the segment
        byte[] segmentData1 = TLVParser.startFraming(segmentData, i + 1);
        //insert tag to identify the segment as already sequenced
        byte[] segmentData2 = TLVParser.startFraming(segmentData1, 4);
        //Generate the inode of segment and compute the hash of the same
        String hashedInode;
        if(isDFS)
            hashedInode = Hash.hashpath(dfs3_ufs1.getRootInode() + segmentName);
        else
        if(segmentName.equals("UFSuploaded.csv")) {
            //String dfsID = dfs3_ufs1.getRootInode();
            String uploadInode = dfs3_ufs1.getMailID()+"/"+segmentName;
            hashedInode = Hash.hashpath(uploadInode);
        }
        else
            hashedInode = Hash.hashpath(segmentName);

        log.debug("Uploading:"+segmentName+",\tHash:"+hashedInode);
        //compute the hash of segment
        String hashOfSegment = hashgenerator(segmentData2);
        //Sign the hash
        byte[] signedHash = GenerateKeys.signHash(hashOfSegment.getBytes());
        //get the file ready to transmit after adding signed hash into the segment
        byte[] fileTx = concat(signedHash, segmentData2);// combine the file,key and hash of Inode
        // Write the XML query. Tag for upload is 1
        String[] inodeCheck = segmentName.split("_");
        int l = inodeCheck.length;
        String xmlPath;
        if(inodeCheck[l-1].equals("Inode.xml"))
            xmlPath = writer(1, hashedInode, fileTx, true);
        else
            xmlPath = writer(1, hashedInode, fileTx, false);
        //Send the file to output buffer.
        DFS3Config.bufferMgr.addToOutputBuffer(new File(xmlPath));
        // TODO - query the dht and get the IP
        Sender.start(dfs3_ufs1.bufferMgr.fetchFromOutputBuffer(), "localhost"); //simulates communication manager.
        //Delete the xml file created for transmitting.
        com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Util.file.deleteFile(xmlPath);
    }
}

/**
 * This class is responsible for segmentation of a file into chunk size of 512kB (configurable)
 */
class Segmentation {
    static DFS3Config dfs3_ufs1 = DFS3Config.getInstance();
    //Hashmap that stores key and values corresponding to segments.

    /**
     * This method creates a temporary copy of encrypted file plus key to work with and sends directories to splitFile function.
     * @param encData byte array of encrypted file plus key
     * @param path path of the file to be uploaded.
     * @param isDFS indicator whether DFS or UFS
     * @param fileWithTimeStamp name of file with timstamp of upload.
     * @throws IOException in case of IO exception.
     */
    public static void start(ByteBuffer encData, String path, boolean isDFS, String fileWithTimeStamp) throws IOException {

        /* Create a path where the byte array filePlusKey will be  written for performing segmentation.
        the addition of DFS3 ensures the existing file is not overwritten */
        String writePath = System.getProperty("user.dir") + System.getProperty("file.separator") + "DFS3";
        writeData(encData.array(), writePath);
        /* call the method splitFile with original path which is used for indexing
         temporary path writePath from where the segmentation will take place
         and size of each segment in KB */
        splitFile(path, writePath, 512, isDFS, fileWithTimeStamp);
    }

    /**
     * Split a file into multiples files.
     * @param tempPath   Name of file to be split.
     * @param kbPerSplit number of kilo bytes per chunk.
     * @param isDFS variable indicating file is being uploaded to DFS or UFS.
     * @param fileWithTimeStamp file name with appended time stamp
     */
    public static void splitFile(String inode, final String tempPath, final int kbPerSplit, boolean isDFS, String fileWithTimeStamp) throws IOException {

        // enforce condition for the chunk size to be more than 0
        if (kbPerSplit <= 0)
            throw new IllegalArgumentException("chunkSize must be more than zero");
        // create an array list of Paths for the segments
        List<Path> partFiles = new ArrayList<>();
        // get the size of the file to be divided into segments
        final long sourceSize = Files.size(Paths.get(tempPath));
        // bytes per segment (convert KB to Bytes)
        final long bytesPerSplit = 1024L * kbPerSplit;
        // number of splits ( Total size divide by segment size)
        final long numSplits = sourceSize / bytesPerSplit;
        // remainder after the above division
        final long remainingBytes = sourceSize % bytesPerSplit;
        int position = 0;
        // create a file channel and access the file in read mode
        try {
            FileInputStream fis = new FileInputStream(tempPath);
            BufferedInputStream bis = new BufferedInputStream(fis);
            // the loop traverses the channel using position
            // position is multiplied with number of bytes per segment every time
            for (; position < numSplits; position++) {
                //write the content to different segments
                writePartToFile(bytesPerSplit, position * bytesPerSplit, bis, partFiles, isDFS, fileWithTimeStamp, inode);
                // if some bytes are remaining after the whole division
                // write them as well to the segments
            }
            if (remainingBytes > 0)
                writePartToFile(remainingBytes, position * bytesPerSplit, bis, partFiles, isDFS, fileWithTimeStamp, inode);
            fis.close();
            //Delete the temporary encrypted file
            com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Util.file.deleteFile(tempPath);
            //return partFiles;
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    // this method makes the segment to be written to the disk
    // it receives the channel and traverses the channel
    // writes the segments with unique name ( name of file followed by suffix .splitPart
    // followed by an integer) example xyz.splitPart.1

    /**
     * This method writes each segment/chunk to the local disk.
     * @param byteSize Size of chunk in bytes to be written.
     * @param position buffer position.
     * @param bis buffered input stream of the segment data.
     * @param partFiles List of file segments.
     * @param isDFS Indicator whether DFS or UFS.
     * @param fileWithTimeStamp File name with time stamp of upload.
     * @param inode Original path of the file.
     * @throws IOException in case of IO error.
     * @throws NoSuchAlgorithmException In case of algorithm error.
     */
    private static void writePartToFile(long byteSize, long position, BufferedInputStream bis,
                                        List<Path> partFiles, boolean isDFS, String fileWithTimeStamp, String inode) throws IOException, NoSuchAlgorithmException {
        // path for the segment current directory followed by the inode followed by .splitPart
        // followed by the segment number
        Path segmentName;
        String suffix = ".splitPart";
        if(isDFS)
            segmentName = Paths.get(dfs3_ufs1.getDfsCache() + fileWithTimeStamp + suffix + (int) ((position / (512 * 1024)) + 1));//TODO - replace the UUID with Integer.toString((position/512) - 1))
        else
            segmentName = Paths.get(dfs3_ufs1.getUfsCache() + fileWithTimeStamp + suffix + (int) ((position / (512 * 1024)) + 1));
        try {
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream (segmentName.toString()))) {

                byte[] buf = new byte[(int) byteSize];
                int val = bis.read(buf);
                if(val!=-1) {
                    bos.write(buf, 0, val);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // add the segment name to the list
        partFiles.add(segmentName);

        // create index of the segments created with inode as the primary key
        splitIndex(inode, segmentName.toString(), isDFS, fileWithTimeStamp);
    }

    /**
     * Method that generates a hash map of segment name and corresponding hash value and stores into a csv file.
     * @param inode Name of file to be split.
     * @param segmentName Name of a segment.
     * @param isDFS variable indicating file is being uploaded to DFS or UFS.
     * @param fileWithTimeStamp file name with appended time stamp.
     */
    public static void splitIndex(String inode, String segmentName, boolean isDFS, String fileWithTimeStamp) throws IOException, NoSuchAlgorithmException {

        Path segmentPath = Path.of(segmentName);
        String nameOfSegment = segmentPath.getFileName().toString();
        byte[] segmentData = readdata(segmentName);
        String hashOfSegment = com.ehelpy.brihaspati4.simulateGC.encrypt.Hash.hashgenerator(segmentData);
        DFS3Upload.index.put(nameOfSegment, hashOfSegment);
        //noinspection ResultOfMethodCallIgnored
        DFS3Upload.index.entrySet().toArray();
        HashMap<String, String> csvIndex = new HashMap<>();
        //Put elements to the map
        csvIndex.put(inode, segmentName);// Put elements to the map
        // Write CSV
        String uploadPath;
        if(isDFS)
            uploadPath = System.getProperty("user.dir") +
                    System.getProperty("file.separator")+"b4dfs"+System.getProperty("file.separator")+"dfsCache"+System.getProperty("file.separator")+ fileWithTimeStamp + "_Inode.csv";
        else
            uploadPath = System.getProperty("user.dir") +
                    System.getProperty("file.separator")+"b4ufs"+System.getProperty("file.separator")+"ufsCache"+System.getProperty("file.separator")+ fileWithTimeStamp + "_Inode.csv";
        try {
            // true is for appending and false is for over writing
            FileWriter writer = new FileWriter(uploadPath, true);
            Set<Map.Entry<String, String>> set = csvIndex.entrySet();
            // Get an iterator for entering the data from hash map
            // to csv file
            for (Object o : set) {
                @SuppressWarnings("rawtypes") Map.Entry firstEntry;
                //noinspection rawtypes
                firstEntry = (Map.Entry) o;
                // write the key
                writer.write(firstEntry.getKey().toString());
                // write the comma
                writer.write(",");//Explore how to write key and value in different fields
                // write the value against key
                writer.write(firstEntry.getValue().toString());
                // create a new line
                writer.write("\n");
            }
            // close the writer
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(index.entrySet());

    }

}
//end of class