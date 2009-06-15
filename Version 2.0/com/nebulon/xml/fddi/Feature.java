/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

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

    public Feature()
    {
        seq = sequence++;
    }

    public int getNextSequence()
    {
        return sequence++;
    }
    
    public int getSequence()
    {
        return sequence;
    }

    public void setSequence(int maxSequence)
    {
        sequence = maxSequence;
    }

    public String getInitials()
    {
        return initials;
    }

    public void setInitials(String value)
    {
        this.initials = value;
    }

    public List<Milestone> getMilestone()
    {
        if(milestone == null)
        {
            milestone = new ArrayList<Milestone>();
        }
        return this.milestone;
    }

    public List<Note> getRemarks()
    {
        if(remarks == null)
        {
            remarks = new ArrayList<Note>();
        }
        return this.remarks;
    }

    public int getSeq()
    {
        return seq;
    }

    public void setSeq(int value)
    {
        this.seq = value;
    }

    @Override
    public void insert(MutableTreeNode node, int index)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove(int arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove(MutableTreeNode arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setUserObject(Object arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TreeNode getChildAt(int arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getChildCount()
    {
        return 0;
    }

    @Override
    public int getIndex(TreeNode arg0)
    {
        return -1;
    }

    @Override
    public boolean getAllowsChildren()
    {
        return false;
    }

    @Override
    public boolean isLeaf()
    {
        return true;
    }

    @Override
    public Enumeration children()
    {
        return null;
    }

    @Override
    public void add(List children)
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void add(FDDINode child)
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void calculateProgress()
    {
        int featureProgress = 0;
        if(getMilestone().size() > 0)
        {
            Aspect aspect = (Aspect) getParent().getParent().getParent();
            for(int i = 0; i < aspect.getInfo().getMilestoneInfo().size(); i++)
            {
                if(getMilestone().get(i).getStatus() == StatusEnum.COMPLETE)
                {
                    featureProgress += aspect.getInfo().getMilestoneInfo().get(i).getEffort();
                }
            }
        }
        ObjectFactory of = new ObjectFactory();
        Progress p = of.createProgress();
        p.setCompletion(featureProgress);
        setProgress(p);
        ((FDDINode) getParent()).calculateProgress();
    }

    @Override
    public void calculateTargetDate()
    {
        targetDate = null;
        for(Milestone m : getMilestone())
        {
            if(m.getPlanned() != null)
            {
                if(targetDate == null || m.getPlanned().toGregorianCalendar().getTime().after(targetDate))
                {
                    targetDate = m.getPlanned().toGregorianCalendar().getTime();
                }
            }
        }
        ((FDDINode) getParent()).calculateTargetDate();
    }

    @Override
    public boolean isLate()
    {
        boolean late = false;
        if(getMilestone().size() > 0)
        {
            for(Milestone m : getMilestone())
            {
                if(m.getPlanned() != null &&
                        m.getPlanned().toGregorianCalendar().getTime().before(new Date()) &&
                        m.getStatus() != StatusEnum.COMPLETE)
                {
                    late = true;
                }
            }
        }
        return late;
    }
}
