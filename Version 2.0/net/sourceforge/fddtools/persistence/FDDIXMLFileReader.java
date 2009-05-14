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
   private Object rootNode = null;

    public FDDIXMLFileReader(String fileName)
    {
        try
        {
            JAXBContext jaxbCtx = JAXBContext.newInstance("com.nebulon.xml.fddi");
            System.out.println("Context: " + jaxbCtx.toString());
            Unmarshaller u = jaxbCtx.createUnmarshaller();
            u.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
            rootNode = u.unmarshal(new File(fileName));
        }
        catch(javax.xml.bind.JAXBException ex)
        {
            // XXXTODO Handle exception
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE, null, ex); //NOI18N
        }
    }

    public Object getRootNode()
    {
        return rootNode;
    }
}

