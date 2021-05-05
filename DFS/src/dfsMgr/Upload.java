package dfsMgr;

import dfs3Util.TLVParser;
import dfs3test.communication.Sender;
import dfs3test.encrypt.*;
import init.DFSConfig;

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
import dfs3test.xmlHandler.*;

import static dfs3Util.file.*;
import static dfs3test.encrypt.Encrypt.concat;
import static dfs3test.encrypt.Hash.hashgenerator;
import static dfs3test.xmlHandler.XMLWriter.writer;

/**
 * Class responsible for uploading a File into DFS.
 * This class gets the file selected by user and uploads it
 * into the DFS using DHT
 * <p><b>Functions:</b> At the user end of DFS</p>
 * <b>Note:</b> Change this file to change functionality
 * related to the upload function
 * @author <a href="https://t.me/sidharthiitk">Sidharth Patra</a>
 * @since 13th Feb 2020
 */
public class Upload {
    /**
     * Starts the upload process
     * <p> This method executes all the functions related to uploading
     * of a file like Encryption, hashing, segmentation and indexing</p>
     *
     * //@throws NullPointerException     Incase user invokes the method but doesn't upload anything
     * //@throws IOException              for input output exception
     * //@throws GeneralSecurityException In case of general security violation occurs
     */

    // get path of the selected file
    static String path;
    static Path path1;
    static String fileName;
    public static String fileURI;
    public static Boolean fBit= Boolean.TRUE;
    static String fileSuffix;
    static long fileSize;

    public static void start(boolean isDFS) throws NullPointerException, IOException,
            GeneralSecurityException {

        path=readpath();
        path1= Paths.get(path);
        fileName = path1.getFileName().toString();
        fileSuffix = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        fileURI = DFSConfig.getRootinode() + fileName+ "@@" + fileSuffix ;
        fileSize = Util.checkFileSize(path);
        //check whether adequate space is available in the user cloud
        try {
            long cloudAvlb = DFSConfig.getCloudAvlb();
            if (cloudAvlb > fileSize|| !isDFS) {
                //System.out.println("Cloud space available is:" + (cloudAvlb / (1024 * 1024 * 1024)) + "GB");
                System.out.println("File Size is: " + (fileSize / (1024 * 1024)) + "MB");
                System.out.println("File can be uploaded");
                //reading file using FileChannel and ByteBuffer
                RandomAccessFile randomAccessFile = new RandomAccessFile(path, "r");
                ByteBuffer encData;
                try (FileChannel channel = randomAccessFile.getChannel()) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());//alocate new ByteBuffer of size of file
                    //byteBuffer.put(bytes);
                    long bufferSize = channel.size();
                    ByteBuffer buff = ByteBuffer.allocate((int) bufferSize);
                    channel.read(buff);
                    buff.flip();
                    byte[] plainData = buff.array();
                    byteBuffer.flip();
                    //channel.write(byteBuffer);
                    randomAccessFile.close();
                    channel.close();
                    //Encrypt the file and key and combine both using TLV framing for DFS
                    byte[] filePlusKey=null;
                    if(isDFS) {
                        filePlusKey = Encrypt.startEnc(plainData);
                        System.out.println("file encrypted successfully!");
                    }
                    else filePlusKey= plainData;
                    encData = ByteBuffer.wrap(filePlusKey);
                    //send the file for segmentation
                    Segmentation.start(encData, path, isDFS);
                    System.out.println("file segmented successfully!");
                    //write inode for the file being uploaded
                    InodeWriter.writeInode(Segmentation.nameOfFile, fileSize, Segmentation.index, isDFS);
                }
                //Retrieve the segments and upload them one by one
                String splitFile = null;
                if(isDFS)
                splitFile = System.getProperty("user.dir") +
                        System.getProperty("file.separator")+"b4dfs"+System.getProperty("file.separator")+"dfsCache"+System.getProperty("file.separator") + Segmentation.nameOfFile +"_Inode.csv";
                else
                    splitFile = System.getProperty("user.dir") +
                            System.getProperty("file.separator")+"b4ufs"+System.getProperty("file.separator")+"ufsCache"+System.getProperty("file.separator") + Segmentation.nameOfFile +"_Inode.csv";

                String[] segmentInode = csvreader(splitFile, path);
                for (int i = 0; i < segmentInode.length && !(segmentInode[i] == null); i++) {
                    despatch(segmentInode[i], i, isDFS);
                }
                //Now uploading the inode of the file
                String inode = null;
                if(isDFS)
                    inode = System.getProperty("user.dir") + System.getProperty("file.separator") +"b4dfs"+System.getProperty("file.separator")+"dfsCache"+System.getProperty("file.separator")+ Segmentation.nameOfFile + "_Inode.xml";
                else
                    inode = System.getProperty("user.dir") + System.getProperty("file.separator") +"b4ufs"+System.getProperty("file.separator")+"ufsCache"+System.getProperty("file.separator")+ Segmentation.nameOfFile + "_Inode.xml";
                despatch(inode, 0, isDFS);
                //Now uploading the updated root directory in the cloud
                String rootDir=null;
                if(isDFS)
                    rootDir = System.getProperty("user.dir") + System.getProperty("file.separator") +"b4dfs"+System.getProperty("file.separator")+"dfsCache"+System.getProperty("file.separator")+ "DFSuploaded.csv";
                else
                    rootDir = System.getProperty("user.dir") + System.getProperty("file.separator") +"b4ufs"+System.getProperty("file.separator")+"ufsCache"+System.getProperty("file.separator")+ "UFSuploaded.csv";
                String hashofFile = hashgenerator(encData.array());
                if(isDFS)
                    index(fileURI, hashofFile, isDFS);
                else
                {
                    String file=fileName+"@@"+fileSuffix;
                    index(file, hashofFile, isDFS);
                }
                despatch(rootDir, 0, isDFS);
                System.out.println("Updated root directory uploaded.");
                System.out.println("Upload completed");
                // compute hash of the combination of encrypted Key and data of original file

                // index the hash against the original inode for comparing after
                // downloading the file from cloud. DbaseAPI.index


                //inodeIndex(inodeFileName, hashofInode);
                //System.out.println("hash of inode being uploaded: "+hashedInodeInode);
                if(isDFS)
                    DFSConfig.update(fileSize);
                else
                    System.out.println("File Successfully uploaded in UFS");
            }
            else {

                System.out.println("Cloud space available is:" + cloudAvlb);
                System.out.println("File Size is: " + fileSize);
                System.out.println("Cloud space available is not sufficient");
            }
        }

     catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }//end of start
    private static void despatch(String segmentInode,int i, boolean isDFS) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        byte[] segmentData = readdata(segmentInode);
        //Delete the segments once the data is read into byte array
        File f=new File(segmentInode);
        //f.delete();
        //extract segment name from the inode
        String segmentName = f.getName();
        //insert sequence number into the segment
        byte[] segmentData1 = TLVParser.startFraming(segmentData, i + 1);
        //insert tag to identify the segment as already sequenced
        byte[] segmentData2 = TLVParser.startFraming(segmentData1, 4);
        //Generate the inode of segment and compute the hash of the same
        String hashedInode = null;
        if(isDFS)
            hashedInode = Hash.hashpath(DFSConfig.getRootinode() + segmentName);
        else
            if(segmentName.equals("UFSuploaded.csv")) {
                String dfsID = DFSConfig.getRootinode();
                String uploadInode = dfsID.split("/")[2]+"/"+segmentName;
                hashedInode = Hash.hashpath(uploadInode);
            }
                else
                hashedInode = Hash.hashpath(segmentName);

        System.out.println("Hash of segment URL: "+hashedInode);
        //compute the hash of segment
        String hashofSegment = hashgenerator(segmentData2);
        //Sign the hash
        byte[] signedHash = GenerateKeys.signHash(hashofSegment.getBytes());
        //get the file ready to transmit after adding signed hash into the segment
        byte[] fileTx = concat(signedHash, segmentData2);// combine the file,key and hash of Inode
        // Write the XML query. Tag for upload is 1
        String[] inodeCheck = segmentName.split("_");
        int l = inodeCheck.length;
        String xmlPath=null;
        if(inodeCheck[l-1]=="Inode.xml")
            xmlPath = writer(1, hashedInode, fileTx, true);
        else
            xmlPath = writer(1, hashedInode, fileTx, false);
        // handover the xml query to xmlSender (token for upload is 1)
        // TODO - query the dht and get the IP
        Sender.start(xmlPath, "localhost");
        System.out.println("Uploading Segment No " + (i + 1));
    }
}
class Segmentation {

    // the variable that get the current directory
    private static final String dfsDir = System.getProperty("user.dir") +
            System.getProperty("file.separator")+"b4dfs"+System.getProperty("file.separator")+"dfsCache"+System.getProperty("file.separator");
    private static final String ufsDir = System.getProperty("user.dir") +
            System.getProperty("file.separator")+"b4ufs"+System.getProperty("file.separator")+"ufsCache"+System.getProperty("file.separator");
    // suffix splitpart is writen as part of file name
    // for the programmer to under stand that its a segment of original file
    private static final String suffix = ".splitPart";
    static String iNode = null;
    static String nameOfFile;
    static HashMap<String, String> index=new HashMap<>();

    public static void start(ByteBuffer encData, String path, boolean isDFS) throws IOException {
        //new File(dir+"upload").mkdir();
        // Create a path where the byte array filepluskey will be  written for
        // performing segmentation.
        // the addition of DFS2 ensures the existing file is not overwritten
        String writePath = System.getProperty("user.dir") +
                System.getProperty("file.separator") + "DFS3" + System.getProperty("file.separator");
        writeData(encData.array(), writePath);
        // call the method splitFile with original path which is used for indexing
        // temporary path writePath from where the segmentation will take place
        // and size of each segment in KB
        splitFile(path, writePath, 512, isDFS);
    }

    /**
     * Split a file into multiples files.
     *  @param tempPath   Name of file to be split.
     * @param kbPerSplit number of kilo bytes per chunk.
     * @param isDFS
     */
    public static void splitFile(String inode, final String tempPath, final int kbPerSplit, boolean isDFS) throws IOException {

        File f;
        f = new File(inode);
        // get name of the file containing file plus key from the disk
        nameOfFile = f.getName()+"@@"+Upload.fileSuffix;
        // get the inode
        iNode = inode;
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
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(tempPath));
            //FileChannel sourceChannel = sourceFile.getChannel()) {
            // the loop traverses the channel using position
            // position is multiplied with number of bytes per segment every time
            for (; position < numSplits; position++) {
                //write the content to different segments
                writePartToFile(bytesPerSplit, position * bytesPerSplit, bis, partFiles, isDFS);
                // if some bytes are remaining after the whole division
                // write them as well to the segments
            }
            if (remainingBytes > 0)
                writePartToFile(remainingBytes, position * bytesPerSplit, bis, partFiles, isDFS);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            //Delete the temporary encrypted file
            f = new File(tempPath);
            f.delete();
            //return partFiles;

        }
    }

    // this method makes the segment to be written to the disk
    // it receives the channel and traverses the channel
    // writes the segments with unique name ( name of file followed by suffix .splitpart
    // followed by an integer) example xyz.splitpart.1
    private static void writePartToFile(long byteSize, long position, BufferedInputStream bis,
                                        List<Path> partFiles, boolean isDFS) throws IOException, NoSuchAlgorithmException {
        // path for the segment current directory followed by the inode followedby .splitpart
        // followed by the segment number
        Path segmentName = null;
        if(isDFS)
        segmentName = Paths.get(dfsDir + nameOfFile + suffix + (int) ((position / (512 * 1024)) + 1));//TODO - replace the UUID with Integer.toString((position/512) - 1))
        else
            segmentName = Paths.get(ufsDir + nameOfFile + suffix + (int) ((position / (512 * 1024)) + 1));
        try {
            //RandomAccessFile tofile = new RandomAccessFile(segmentName.toString(),"rw");
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream (segmentName.toString()))) {

                byte[] buf = new byte[(int) byteSize];
                int val = bis.read(buf);
                if(val!=-1) {
                    bos.write(buf, 0, val);
                }
                //FileChannel toChannel = toFile.getChannel()) {
                //sourceChannel.position(position);
                //toChannel.transferFrom(sourceChannel, 0, byteSize);
            } catch (IOException e) {
                e.printStackTrace();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // add the segment name to the list
        partFiles.add(segmentName);

        // create index of the segments created with inode as the primary key
        splitIndex(iNode, segmentName.toString(), isDFS);
    }




    /**
     * Split a file into multiples files.
     *  @param inode Name of file to be split.
     * @param segmentName number of kilo bytes per chunk.
     * @param isDFS
     */
    public static void splitIndex(String inode, String segmentName, boolean isDFS) throws IOException, NoSuchAlgorithmException {

        Path segmentPath = Path.of(segmentName);
        String nameOfSegment = segmentPath.getFileName().toString();
        byte[] segmentData = readdata(segmentName);
        String hashOfSegment = dfs3test.encrypt.Hash.hashgenerator(segmentData);
        index.put(nameOfSegment, hashOfSegment);
        index.entrySet().toArray();
        HashMap<String, String> csvIndex = new HashMap<>();
        //Put elements to the map
        csvIndex.put(inode, segmentName);// Put elements to the map
        String fileName = inode + "_inode.csv";
        // Write CSV
        String uploadPath = null;
        if(isDFS)
        uploadPath = System.getProperty("user.dir") +
                System.getProperty("file.separator")+"b4dfs"+System.getProperty("file.separator")+"dfsCache"+System.getProperty("file.separator")+ nameOfFile + "_Inode.csv";
        else
            uploadPath = System.getProperty("user.dir") +
                    System.getProperty("file.separator")+"b4ufs"+System.getProperty("file.separator")+"ufsCache"+System.getProperty("file.separator")+ nameOfFile + "_Inode.csv";
        try {
            // true is for appending and false is for over writing
            FileWriter writer = new FileWriter(uploadPath, true);
            Set set = csvIndex.entrySet();
            // Get an iterator for entering the data from hash map
            // to csv file
            for (Object o : set) {
                Map.Entry firstEntry = (Map.Entry) o;
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