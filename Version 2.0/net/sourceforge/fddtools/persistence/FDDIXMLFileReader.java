/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.fddtools.persistence;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author vds
 */
public class FDDIXMLFileReader
{
    public static Object read(String fileName)
    {
       Object rootNode = null;

        try
        {
            JAXBContext jaxbCtx = JAXBContext.newInstance("com.nebulon.xml.fddi");
            Unmarshaller u = jaxbCtx.createUnmarshaller();
            u.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
            rootNode = u.unmarshal(new File(fileName));
        }
        catch(javax.xml.bind.JAXBException ex)
        {
            //@todo Handle exception
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE, null, ex); //NOI18N
        }
        return rootNode;
    }
}

