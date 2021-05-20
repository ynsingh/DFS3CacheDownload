package dfsMgr;

import dfs3test.communication.Sender;
import dfs3test.xmlHandler.InodeReader;
import dfs3test.xmlHandler.ReadInode;
import dfs3test.xmlHandler.ReadObject;
import dfs3test.xmlHandler.XMLReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import static dfs3Util.file.deconcat;
import static dfs3Util.file.readdata;
import static dfs3test.xmlHandler.XMLWriter.writer;

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
        //dfs3Util.file.writeData(xmlData, tempXml);
        //System.out.println("reached here:"+xmlpath);
        //boolean isInode = isInodeReader(tempXml);
        //System.out.println("isInode:"+isInode);
        //System.out.println("reached here");
        // reply through XML. tag for reply to download is 20
        String xmlPath = writer(20, inode, encFile, false);
        // TODO - handover the file to file sender and delete the line below
        Sender.start(xmlPath, "localhost");//shortcut
        dfs3Util.file.deleteFile(xmlPath);
    }

    private static boolean isInodeReader(String path) throws FileNotFoundException, XMLStreamException {

        String xmlPath = path+".xml";
        ReadObject query = null;
        // Initialise Input factory
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            // Initialise a stream reader with fileName
            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader
                    (new FileInputStream(path));
            // Each event is identified with a integer value
            int event = xmlStreamReader.getEventType();
            // till there is a next event the loop will continue
            while (xmlStreamReader.hasNext()) {
                switch(event) {
                    // if it is the start element in the XML
                    case XMLStreamConstants.START_ELEMENT:
                        // within start element if it is Service then retrieve the attribute
                        // put the attribute value by setId into the object
                        if(xmlStreamReader.getLocalName().equals("Service")){
                            query = new ReadObject();
                            query.setId(Integer.parseInt(xmlStreamReader.getAttributeValue(0)));
                            // within start element if it is Inode then set hash true
                        }else if(xmlStreamReader.getLocalName().equals("Inode")){
                            hash=true;
                            // if within start element it is Data then read data
                            // and put it through method set Data into the object
                        }else if(xmlStreamReader.getLocalName().equals("Data")){
                            query.setData(xmlStreamReader.getElementText());
                        }else if(xmlStreamReader.getLocalName().equals("isInode")){
                            query.setIsInode(Boolean.parseBoolean(xmlStreamReader.getElementText()));
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        // if hash is true read the content and put the value of the
                        // inode into the object
                        if(hash){
                            query.setInode(xmlStreamReader.getText());
                            hash = false;
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        //  if the End element is encountered and if it is service
                        // add the object query to the query list
                        if(xmlStreamReader.getLocalName().equals("Service")){
                            //queryList.add(query);
                        }
                        break;
                }
                // the loop control goes to the next event
                // In our case I have kept only one event that is
                // one set of tag, inode and data you can have more if
                // needed thats why the query list is there
                event = xmlStreamReader.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("xml reading error!");

        }
        return query.getIsInode();
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
