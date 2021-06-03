package dfsUfsCore.xmlHandler;

import dfsUfsCore.dfsMgr.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class XMLReader {

    private static boolean hash;
    // this method parses the xml using StAX CURSOR API
    public static void reader(File xmlFile) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        List<ReadObject> queryList = new ArrayList<>();
        ReadObject query = null;
        // Initialise Input factory
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(xmlFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            // Initialise a stream reader with fileName
            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(fis);
            // Each event is identified with a integer value
            int event = xmlStreamReader.getEventType();
            // till there is a next event the loop will continue
            while(xmlStreamReader.hasNext()){
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
                        }else if(xmlStreamReader.getLocalName().equals("pubKey")){
                            query.setPubKey(xmlStreamReader.getElementText());
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
                            queryList.add(query);
                        }
                        break;
                }
                // the loop control goes to the next event
                // In our case I have kept only one event that is
                // one set of tag, inode and data you can have more if
                // needed thats why the query list is there
                event = xmlStreamReader.next();
            }

        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        fis.close();
        // reading individual fields from the object query
        byte[] decoded = new byte[0];
        String inode = null;
        int id = 0;
        PublicKey publicKey=query.getPubKey();
        // retrieve each element from querylist
        for(ReadObject value : queryList){
            // get the value of id from the object
            id = value.getId();
            // get the value of inode from the object
            inode = value.getInode();
            // if id is 1 which is for upload or id is 20 which is for Locate
            // then read data
            if(id == 1|id == 20|id == 4|id==2)
            {
                // read the string into encoded bytes and then decode the base64 code
                byte[] encoded = value.getData().getBytes(StandardCharsets.UTF_8);
                decoded = Base64.getDecoder().decode(encoded);
            }
        }
////////////////////////////////////////////////////////////////////////////////////////////////
// this section is for calling the relevant programs based on tag
        // if id is 1 then query message from upload hence call store
        if(id == 1)
        try {
            Store.start(decoded,inode, publicKey);
            dfsUfsCore.dfs3Util.file.deleteFile(xmlFile.getName());
        } catch (IOException | InvalidKeyException | InvalidKeySpecException
                | SignatureException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // if id is 2 query message from download call locate
        else if(id == 2)
            try {
                Locate.start(inode,new String(decoded));
                dfsUfsCore.dfs3Util.file.deleteFile(xmlFile.getName());
            } catch (IOException | GeneralSecurityException | XMLStreamException e) {
                e.printStackTrace();
            }

            // if id is 20, it is a response message to download from Locate
        else if(id == 20)
            try {
                Dfs3Download.segmentDownload(decoded);
                dfsUfsCore.dfs3Util.file.deleteFile(xmlFile.getName());

            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
            }
        // if id is 4  query for replicate
    }
}