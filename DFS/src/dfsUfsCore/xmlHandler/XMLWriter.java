package dfsUfsCore.xmlHandler;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class XMLWriter {
    // Write the XML using StAX CURSOR API
    public static String writer(int tag, String hashInode,byte[] data, boolean isInode) throws IOException {
        String xmlFileName = hashInode+"xml";
        //List<Query> queryList = new ArrayList<>();
        //queryList.add(new query(attributes to be passed));
        WriteObject query = new WriteObject(tag,hashInode,data, isInode);
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

            //write isInode
            writer.writeCharacters("\n\t\t");
            writer.writeStartElement("isInode");
            writer.writeCharacters(String.valueOf(query.getIsInode()));
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
