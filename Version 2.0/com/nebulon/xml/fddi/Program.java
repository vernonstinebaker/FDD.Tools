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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import net.sourceforge.fddtools.model.FDDINode;

/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element ref="{http://www.nebulon.com/xml/2004/fddi}program" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element ref="{http://www.nebulon.com/xml/2004/fddi}project" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;/choice>
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
@XmlType(name = "", propOrder =
{
    "name",
    "program",
    "project",
    "progress",
    "any"
})
@XmlRootElement(name = "program")
public class Program extends FDDINode
{

    @XmlElement(namespace = "http://www.nebulon.com/xml/2004/fddi")
    protected List<Program> program;
    @XmlElement(namespace = "http://www.nebulon.com/xml/2004/fddi")
    protected List<Project> project;

    /**
     * Gets the value of the program property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the program property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProgram().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Program }
     * 
     * 
     */
    public List<Program> getProgram()
    {
        if(program == null)
        {
            program = new ArrayList<Program>();
        }
        return this.program;
    }

    /**
     * Gets the value of the project property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the project property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProject().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Project }
     * 
     * 
     */
    public List<Project> getProject()
    {
        if(project == null)
        {
            project = new ArrayList<Project>();
        }
        return this.project;
    }

    public void insert(MutableTreeNode node, int index)
    {
        if(node instanceof Program)
        {
            ((Program) node).setParent(this);
            program.add(index, (Program) node);
        }
        else if(node instanceof Project)
        {
            ((Project) node).setParent(this);
            project.add(index, (Project) node);
        }
    }

    public void remove(int arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void remove(MutableTreeNode node)
    {
        if(node instanceof Program)
        {
            program.remove((Program) node);
        }
        else
        {
            project.remove((Project) node);
        }
    }

    public void setUserObject(Object arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeFromParent()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //@todo this doesn't work right yet
    // need to figure out how to choose which to return when
    // we have two possibilities. This problem only effects the
    // Program node type
    public TreeNode getChildAt(int index)
    {
        if(program != null && program.size() > index)
        {
            return program.get(index);
        }
        if(project != null && project.size() > index)
        {
            return project.get(index);
        }
        return null;
    }

    public int getChildCount()
    {
        int size = 0;
        if(program != null)
        {
            size += program.size();
        }
        if(project != null)
        {
            size += project.size();
        }
        return size;
    }

    public int getIndex(TreeNode node)
    {
        if(node instanceof Program)
        {
            return program.indexOf(node);
        }
        else
        {
            return project.indexOf(node);
        }
    }

    public boolean getAllowsChildren()
    {
        return true;
    }

    public boolean isLeaf()
    {
        if((program != null && program.size() > 0) ||
           (project != null && project.size() > 0))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public Enumeration children()
    {
        if(program != null && program.size() > 0)
            return Collections.enumeration(program);
        if(project != null && project.size() > 0)
            return Collections.enumeration(project);
        else
            return null;
    }

    public void add(List children)
    {
        if(children instanceof Program)
        {
            for(Object child : children)
               ((Program) child).setParent(this);
            getProgram().add((Program) children);
        }
        else if (children instanceof Project)
        {
            for(Object child : children)
               ((Project) child).setParent(this);
            getProject().add((Project) children);
        }
    }

    public void add(FDDINode child)
    {
        if(child instanceof Program)
        {
           ((Program) child).setParent(this);
            getProgram().add((Program) child);
        }
        else if (child instanceof Project)
        {
            getProject().add((Project) child);
           ((Project) child).setParent(this);
        }
    }
}
