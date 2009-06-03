//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.05.11 at 08:52:34 PM CST 
//
package com.nebulon.xml.fddi;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import net.sourceforge.fddtools.model.FDDINode;

/**
 * 
 *             A feature is some small piece of client valued function.
 *           
 * 
 * <p>Java class for feature complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="feature">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="initials" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="milestone" type="{http://www.nebulon.com/xml/2004/fddi}milestone" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="remarks" type="{http://www.nebulon.com/xml/2004/fddi}note" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.nebulon.com/xml/2004/fddi}progress" minOccurs="0"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.nebulon.com/xml/2004/fddi}baseAttrs"/>
 *       &lt;attribute name="seq" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "feature", propOrder =
{
    "name",
    "initials",
    "milestone",
    "remarks",
    "progress",
    "any"
})
public class Feature extends FDDINode
{
    @XmlTransient
    private static int sequence = 1;
    protected String initials;
    protected List<Milestone> milestone;
    protected List<Note> remarks;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected int seq;

    /**
     * Gets the value of the initials property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Feature()
    {
        seq = sequence++;
    }

    public String getInitials()
    {
        return initials;
    }

    /**
     * Sets the value of the initials property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInitials(String value)
    {
        this.initials = value;
    }

    /**
     * Gets the value of the milestone property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the milestone property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMilestone().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Milestone }
     * 
     * 
     */
    public List<Milestone> getMilestone()
    {
        if(milestone == null)
        {
            milestone = new ArrayList<Milestone>();
        }
        return this.milestone;
    }

    /**
     * Gets the value of the remarks property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the remarks property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRemarks().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Note }
     * 
     * 
     */
    public List<Note> getRemarks()
    {
        if(remarks == null)
        {
            remarks = new ArrayList<Note>();
        }
        return this.remarks;
    }

    /**
     * Gets the value of the seq property.
     * 
     * @return
     *     possible object is
     *     {@link int }
     *     
     */
    public int getSeq()
    {
        return seq;
    }

    /**
     * Sets the value of the seq property.
     * 
     * @param value
     *     allowed object is
     *     {@link int }
     *     
     */
    public void setSeq(int value)
    {
        this.seq = value;
    }

    public void insert(MutableTreeNode node, int index)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void remove(int arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void remove(MutableTreeNode arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setUserObject(Object arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TreeNode getChildAt(int arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getChildCount()
    {
        return 0;
    }

    public int getIndex(TreeNode arg0)
    {
        return -1;
    }

    public boolean getAllowsChildren()
    {
        return false;
    }

    public boolean isLeaf()
    {
        return true;
    }

    public Enumeration children()
    {
        return null;
    }

    public void add(List children)
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    public void add(FDDINode child)
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void calculateProgress()
    {
        int featureProgress = 0;
        Aspect aspect = (Aspect) getParent().getParent().getParent();
        for(int i = 0; i < aspect.getInfo().getMilestoneInfo().size(); i++)
        {
            if(getMilestone().get(i).getStatus() == StatusEnum.COMPLETE)
            {
                featureProgress += aspect.getInfo().getMilestoneInfo().get(i).getEffort();
            }
        }
        ObjectFactory of = new ObjectFactory();
        Progress p = of.createProgress();
        p.setCompletion(featureProgress);
        setProgress(p);
        ((FDDINode)getParent()).calculateProgress();
    }

    @Override
    public void calculateTargetDate()
    {
        targetDate = null;
        for(Milestone m : getMilestone())
        {
            if(targetDate == null || m.getPlanned().toGregorianCalendar().getTime().after(targetDate))
                targetDate = m.getPlanned().toGregorianCalendar().getTime();
        }
        ((FDDINode) getParent()).calculateTargetDate();
    }

    @Override
    public boolean isLate()
    {
        boolean late = false;
        for(Milestone m : getMilestone())
        {
            if(m.getPlanned().toGregorianCalendar().getTime().before(new Date()) &&
               m.getStatus() != StatusEnum.COMPLETE)
                late = true;
        }
        return late;
    }
}
