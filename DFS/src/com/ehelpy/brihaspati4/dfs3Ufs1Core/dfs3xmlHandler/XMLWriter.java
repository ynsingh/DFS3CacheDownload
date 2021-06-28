package com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3xmlHandler;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class XMLWriter {
    // Write the XML using StAX CURSOR API
    public static String writer(int tag, String hashInode,byte[] data, boolean isInode) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        String xmlFileName = hashInode+".xml";
        //Read public key from key store
        PublicKey pubKey= com.ehelpy.brihaspati4.simulateGC.encrypt.Encrypt.getPublic();
        Base64.Encoder encoder = Base64.getEncoder();
        String publicKeyStr = encoder.encodeToString(pubKey.getEncoded());
        WriteObject query = new WriteObject(tag,hashInode,data, publicKeyStr);
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        try{
            FileOutputStream fos = new FileOutputStream(xmlFileName);
            XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(fos, "UTF-8");
            //start writing xml file
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeCharacters("\n");
            writer.writeStartElement("Services");

            //write id as attribute
            writer.writeCharacters("\n\t");
            writer.writeStartElement("Service");
            writer.writeAttribute("id", String.valueOf(query.getId()));

            //write Inode
            writer.writeCharacters("\n\t\t");
            writer.writeStartElement("Inode");
            writer.writeCharacters(String.valueOf(query.getInode()));
            writer.writeEndElement();
            // write Data
            writer.writeCharacters("\n\t\t");
            writer.writeStartElement("Data");
            writer.writeCharacters(query.getData());
            writer.writeEndElement();

            //start pubKey
            writer.writeStartElement("pubKey");
            writer.writeCharacters(query.getPublicKeyStr());
            //end pubKey
            writer.writeEndElement();
            //write end tag of Service element
            writer.writeCharacters("\n\t");
            writer.writeEndElement();
            // write end tag of Services element
            writer.writeCharacters("\n");
            writer.writeEndElement();
            //write end document
            writer.writeEndDocument();

            //flush data to file and close writer
            writer.flush();
            writer.close();
            fos.flush();
            fos.close();

        }catch(XMLStreamException | FileNotFoundException e){
            e.printStackTrace();
        }
        //reader(fileName);//shortcut
        return xmlFileName;
    }
}
