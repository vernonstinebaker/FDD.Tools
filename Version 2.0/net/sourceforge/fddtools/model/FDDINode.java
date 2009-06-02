/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.fddtools.model;

import com.nebulon.xml.fddi.Aspect;
import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Progress;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.bind.Element;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

/**
 *
 * @author vds
 */
@XmlTransient
public abstract class FDDINode implements MutableTreeNode, Serializable
{

    @XmlTransient
    private FDDINode parent;
    @XmlTransient
    protected Date targetDate;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(namespace = "http://www.nebulon.com/xml/2004/fddi")
    protected Progress progress;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the name property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setName(String value)
    {
        this.name = value;
    }

    /**
     * Gets the value of the progress property.
     * 
     * @return
     *     possible object is
     *     {@link Progress }
     *
     */
    public Progress getProgress()
    {
        if(progress == null)
            calculateProgress();
        return progress;
    }

    /**
     * Sets the value of the progress property.
     *
     * @param value
     *     allowed object is
     *     {@link Progress }
     *
     */
    public void setProgress(Progress value)
    {
        this.progress = value;
    }

    /**
     * Gets the value of the any property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Element }
     * {@link Object }
     *
     *
     */
    public List<Object> getAny()
    {
        if(any == null)
        {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

    /**
     * Gets the value of the id property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getId()
    {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setId(String value)
    {
        this.id = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     *
     * <p>
     * the map is keyed by the name of the attribute and
     * the value is the string value of the attribute.
     *
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     *
     *
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes()
    {
        return otherAttributes;
    }

    public void setParent(MutableTreeNode node)
    {
        parent = (FDDINode) node;
    }

    public TreeNode getParent()
    {
        return parent;
    }

    public void removeFromParent()
    {
        parent.remove(this);
    }

    @Override
    public String toString()
    {
        return ((FDDINode) this).getName();
    }
    
    public Unmarshaller.Listener createListener()
    {
        return new Unmarshaller.Listener()
        {

            @Override
            public void afterUnmarshal(Object target, Object parent)
            {
                if(target instanceof FDDINode)
                    ((FDDINode) target).setParent((FDDINode) parent);
            }
        };
    }

    public void calculateProgress()
    {
        int childrenProgress = 0;
        ObjectFactory of = new ObjectFactory();
        Progress p = of.createProgress();
        if(children() != null)
        {
            for(Enumeration e = children(); e.hasMoreElements(); )
            {
                FDDINode node = (FDDINode) e.nextElement();
                childrenProgress += node.getProgress().getCompletion();
            }
            p.setCompletion(childrenProgress/getChildCount());
        }
        else
            p.setCompletion(0);
        setProgress(p);
        if(getParent() != null)
            ((FDDINode) getParent()).calculateProgress();
    }

    public Date getTargetDate()
    {
        if(targetDate == null)
            calculateTargetDate();
        return targetDate;
    }

    public void setTargetDate(Date date)
    {
        this.targetDate = date;
    }

    public void calculateTargetDate()
    {
        targetDate = null;
        if(children() != null)
        {
            for(Enumeration e = children(); e.hasMoreElements(); )
            {
                FDDINode node = (FDDINode) e.nextElement();
                if(node.getTargetDate() != null)
                    if(targetDate == null || node.getTargetDate().after(targetDate))
                        targetDate = node.getTargetDate();
            }
        if(getParent() != null)
            ((FDDINode) getParent()).calculateTargetDate();
        }
    }
    
    public boolean isLate()
    {
        if(getTargetDate() != null &&
           getProgress().getCompletion() != 100 &&
           getTargetDate().before(new Date()))
            return true;
        else
            return false;
    }

    
    public Aspect getAspectForNode()
    {
        TreePath path = getPath(this);

        for(Object pathNode : path.getPath())
        {
            if(pathNode instanceof Aspect)
            {
                return (Aspect) pathNode;
            }
        }
        return null;
    }

    public TreePath getPath(FDDINode node)
    {
        List<FDDINode> list = new ArrayList<FDDINode>();

        while(node != null)
        {
            list.add(node);
            node = (FDDINode) node.getParent();
        }
        Collections.reverse(list);

        return new TreePath(list.toArray());
    }

    public void addTreeModelListener(javax.swing.event.TreeModelListener l) {}
    public void removeTreeModelListener(javax.swing.event.TreeModelListener l) {}
    public void valueForPathChanged(TreePath path, Object newValue) {}
    abstract public void add(List children);
    abstract public void add(FDDINode child);
}
