/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.fddtools.persistence;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

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
            m.marshal(rootNode, new File(fileName));
        }
        catch(javax.xml.bind.JAXBException ex)
        {
            //@todo Handle exception
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE, null, ex); //NOI18N
        }
    }
}
