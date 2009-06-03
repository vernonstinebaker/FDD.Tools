/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.fddtools.persistence;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

/**
 *
 * @author vds
 */
//@todo test FDDIXMLFileWriter
public class FDDIXMLFileWriter
{
    public FDDIXMLFileWriter(Object rootNode, String fileName)
    {
    }

    public static void write(Object rootNode, String fileName)
    {
        try
        {
            JAXBContext jaxbCtx = JAXBContext.newInstance("com.nebulon.xml.fddi");
            Marshaller m = jaxbCtx.createMarshaller();
            m.setProperty(m.JAXB_FORMATTED_OUTPUT, true);
            SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(new File("fddi20060119.xsd"));
            m.setSchema(schema);
            m.marshal(rootNode, new File(fileName));
        }
        catch(javax.xml.bind.JAXBException ex)
        {
            //@todo Handle exception
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE, null, ex); //NOI18N
        }
        catch(org.xml.sax.SAXException ex)
        {
            //@todo Handle exception
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE, null, ex); //NOI18N
        }
    }
}
