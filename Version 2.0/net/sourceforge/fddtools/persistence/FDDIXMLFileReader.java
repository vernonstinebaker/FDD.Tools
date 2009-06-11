/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.fddtools.persistence;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Program;
import net.sourceforge.fddtools.model.FDDINode;

/**
 *
 * @author vds
 */
public class FDDIXMLFileReader
{
    public static Object read(String fileName)
    {
       Object rootNode = null;
       ObjectFactory of = new ObjectFactory();
       Program program = of.createProgram();

        try
        {
            JAXBContext jaxbCtx = JAXBContext.newInstance("com.nebulon.xml.fddi:net.sourceforge.fddtools.fddi.extension");
            Unmarshaller u = jaxbCtx.createUnmarshaller();
            u.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
            u.setListener(((FDDINode) program).createListener());
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

