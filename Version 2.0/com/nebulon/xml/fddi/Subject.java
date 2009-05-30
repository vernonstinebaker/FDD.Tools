//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.05.11 at 08:52:34 PM CST 
//
package com.nebulon.xml.fddi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import net.sourceforge.fddtools.model.FDDINode;

/**
 * 
 *             This is the top level of the FBS, named "Subject Area" in a
 *             standard project.
 *           
 * 
 * <p>Java class for subject complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="subject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="prefix" type="{http://www.w3.org/2001/XMLSchema}token"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}token"/>
 *         &lt;element name="activity" type="{http://www.nebulon.com/xml/2004/fddi}activity" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.nebulon.com/xml/2004/fddi}progress" minOccurs="0"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.nebulon.com/xml/2004/fddi}baseAttrs"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "subject", propOrder =
{
    "prefix",
    "name",
    "activity",
    "progress",
    "any"
})
public class Subject extends FDDINode
{

    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String prefix;
    protected List<Activity> activity;

    /**
     * Gets the value of the prefix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrefix()
    {
        return prefix;
    }

    /**
     * Sets the value of the prefix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrefix(String value)
    {
        this.prefix = value;
    }

    /**
     * Gets the value of the activity property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the activity property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getActivity().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Activity }
     * 
     * 
     */
    public List<Activity> getActivity()
    {
        if(activity == null)
        {
            activity = new ArrayList<Activity>();
        }
        return this.activity;
    }

    public void add(List children)
    {
        for(Object child : children)
            ((Activity) child).setParent(this);
        getActivity().add((Activity) children);
    }

    public void add(FDDINode child)
    {
        ((Activity) child).setParent(this);
        getActivity().add((Activity) child);
    }

    public void insert(MutableTreeNode node, int index)
    {
        ((Activity) node).setParent(this);
        activity.add(index, (Activity) node);
    }

    public void remove(int index)
    {
        activity.remove(index);
    }

    public void remove(MutableTreeNode node)
    {
        activity.remove((Activity) node);
    }

    public void setUserObject(Object arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TreeNode getChildAt(int index)
    {
        return activity.get(index);
    }

    public int getChildCount()
    {
        if(activity != null)
            return activity.size();
        else
            return 0;
    }

    public int getIndex(TreeNode node)
    {
        return activity.indexOf(node);
    }

    public boolean getAllowsChildren()
    {
        return true;
    }

    public boolean isLeaf()
    {
        return (activity != null && (activity.size() <= 0));
    }

    public Enumeration children()
    {
        if(activity != null)
            return Collections.enumeration(activity);
        else
            return null;
    }
}
