/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sourceforge.fddtools.fddi.extension;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author vds
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder =
{
    "sequence",
    "name"
//    "featureList"
})
@XmlRootElement(name = "workpackage", namespace = "net.sourceforge.fddtools.fddi.extension")
public class WorkPackage implements Serializable
{
    @XmlTransient
    private static int seq = 0;
    @XmlElement
    protected int sequence;
    @XmlElement
    protected String name;
//    @XmlElement
//    protected ArrayList<TreePath> featureList;

    public WorkPackage()
    {
        sequence = seq++;
    }

    public int getSequence()
    {
        return sequence;
    }

    public void setSequence(int value)
    {
        sequence = value;

    }

    public String getName()
    {
        return name;
    }

    public void setName(String n)
    {
        name = n;
    }

//    public List<TreePath> getFeatureList()
//    {
//        if(featureList == null)
//        {
//            featureList = new ArrayList<TreePath>();
//        }
//        return featureList;
//    }
}
