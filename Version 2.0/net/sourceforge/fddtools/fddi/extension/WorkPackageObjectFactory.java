/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sourceforge.fddtools.fddi.extension;

import javax.xml.bind.annotation.XmlRegistry;

/**
 *
 * @author vds
 */

@XmlRegistry
public class WorkPackageObjectFactory
{
        public WorkPackageObjectFactory()
    {
    }

    /**
     * Create an instance of {@link AspectInfo }
     *
     */
    public WorkPackage createWorkPackage()
    {
        return new WorkPackage();
    }
}
