package dfs3Ufs1Core.dfs3xmlHandler;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.TreeMap;
import javax.xml.stream.*;


public class InodeReader {
    public static ReadInode reader(String fileName) throws NoSuchAlgorithmException, InvalidKeySpecException {
        boolean bFileName = false;
        boolean bFileSize = false;
        boolean bFileURI = false;
        boolean bAuthor = false;
        boolean bNOS = false;
        boolean bTimeStamp = false;
        boolean bFBit = false;
        boolean bIsInode = false;
        boolean bPubKey = false;
        boolean bSplitPart = false;
        ReadInode readInode = null;
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            // Initialise a stream reader with fileName
            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader
                    (new FileInputStream(fileName));
            // Each event is identified with a integer value
            int event = xmlStreamReader.getEventType();
            // till there is a next event the loop will continue
            while (xmlStreamReader.hasNext()) {
                switch (event) {
                    // if it is the start element in the XML
                    case XMLStreamConstants.START_ELEMENT:
                        if (xmlStreamReader.getLocalName().equals("inode")) {
                            readInode = new ReadInode();

                        } else if (xmlStreamReader.getLocalName().equals("FileName")) {
                            bFileName = true;

                        } else if (xmlStreamReader.getLocalName().equals("FileSize")) {
                            bFileSize = true;
                        } else if (xmlStreamReader.getLocalName().equals("FileURI")) {
                            bFileURI = true;
                        } else if (xmlStreamReader.getLocalName().equals("Author")) {
                            bAuthor = true;
                        } else if (xmlStreamReader.getLocalName().equals("NoOfSegments")) {
                            bNOS = true;
                        } else if (xmlStreamReader.getLocalName().equals("TimeStamp")) {
                            bTimeStamp = true;
                        } else if (xmlStreamReader.getLocalName().equals("FBit")) {
                            bFBit = true;
                        } else if (xmlStreamReader.getLocalName().equals("isInode")) {
                            bIsInode = true;
                        } else if (xmlStreamReader.getLocalName().equals("pubKey")) {
                            bPubKey = true;
                        } else if (xmlStreamReader.getLocalName().equals("splitPart")) {
                            bSplitPart = true;
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        // if hash is true read the content and put the value of the
                        // inode into the object
                        if (bFileName) {
                            readInode.setFileName(xmlStreamReader.getText());
                            bFileName = false;
                            //System.out.println(readInode.getFileName());
                        } else if (bFileSize) {
                            readInode.setFileSize(Long.parseLong(xmlStreamReader.getText()));
                            bFileSize = false;
                            //System.out.println(readInode.getFileSize());
                        } else if (bFileURI) {
                            readInode.setFileURI(xmlStreamReader.getText());
                            //System.out.println(readInode.getFileURI());
                            bFileURI = false;
                        } else if (bAuthor) {
                            readInode.setAuthor(xmlStreamReader.getText());
                            //System.out.println(readInode.getAuthor());
                            bAuthor = false;
                        } else if (bNOS) {
                            readInode.setNoOfSegments(Integer.parseInt(xmlStreamReader.getText()));
                            //System.out.println(readInode.getNoOfSegments());
                            bNOS = false;
                        } else if (bTimeStamp) {
                            readInode.setTimestamp(xmlStreamReader.getText());
                            System.out.println(xmlStreamReader.getText());
                            bTimeStamp = false;
                        } else if (bFBit) {
                            readInode.setFbit(Boolean.parseBoolean(xmlStreamReader.getText()));
                            //System.out.println(readInode.getFbit());
                            bFBit = false;
                        } else if (bIsInode) {
                            readInode.setIsInode(Boolean.parseBoolean(xmlStreamReader.getText()));
                            //System.out.println(readInode.getIsInode());
                            bIsInode = false;
                        }
                            else if (bPubKey) {
                                readInode.setPubKey(xmlStreamReader.getText());
                                //System.out.println(readInode.getIsInode());
                                bPubKey = false;
                        } else if (bSplitPart) {
                            String tuple = xmlStreamReader.getText();
                            //System.out.println(tuple);
                            String[] keyValue = tuple.split("---");
                            readInode.setSplitParts(keyValue[0], keyValue[1]);
                            //System.out.println(keyValue);
                            bSplitPart = false;
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        //  if the End element is encountered and if it is service
                        // add the object query to the query list
                        if (xmlStreamReader.getLocalName().equals("inode")) {
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
        } catch (FileNotFoundException | XMLStreamException e) {
            System.out.println("not an inode file");
            //e.printStackTrace();
        }
        TreeMap<String, String> sortedHash = new TreeMap<>(readInode.getSplitParts());
        //System.out.println(sortedHash);
        return readInode;
    }
}

