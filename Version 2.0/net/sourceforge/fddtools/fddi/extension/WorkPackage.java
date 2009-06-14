/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sourceforge.fddtools.fddi.extension;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
    "name",
    "featureSeq"
})
@XmlRootElement(name = "workpackage", namespace = "net.sourceforge.fddtools.fddi.extension")
public class WorkPackage implements Serializable
{
    @XmlTransient
    private static int sequence = 1;
    @XmlAttribute
    protected int seq;
    @XmlElement
    protected String name;
    @XmlElement
    protected ArrayList<Integer> featureSeq;

    public WorkPackage()
    {
        seq = sequence++;
    }

    public int getSequence()
    {
        return seq;
    }

    public void setSequence(int value)
    {
        seq = value;

    }

    public String getName()
    {
        return name;
    }

    public void setName(String n)
    {
        name = n;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public List<Integer> getFeatureList()
    {
        if(featureSeq == null)
        {
            featureSeq = new ArrayList<Integer>();
        }
        return featureSeq;
    }

    public void addFeature(Integer seq)
    {
        getFeatureList().add(seq);
    }
}
