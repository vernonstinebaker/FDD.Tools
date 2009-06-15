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
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="info" type="{http://www.nebulon.com/xml/2004/fddi}aspectInfo" minOccurs="0"/>
 *         &lt;element name="subject" type="{http://www.nebulon.com/xml/2004/fddi}subject" maxOccurs="unbounded" minOccurs="0"/>
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
    "info",
    "subject",
    "progress",
    "any"
})

@XmlRootElement(name = "aspect")
public class Aspect extends FDDINode
{

    protected AspectInfo info;
    protected List<Subject> subject;

    public AspectInfo getInfo()
    {
        return info;
    }

    public void setInfo(AspectInfo value)
    {
        this.info = value;
    }

    public List<Subject> getSubject()
    {
        if(subject == null)
        {
            subject = new ArrayList<Subject>();
        }
        return this.subject;
    }

    @Override
    public void add(List children)
    {
        for(Object child : children)
        {
            ((Subject) child).setParent(this);
        }
        getSubject().add((Subject) children);
    }

    @Override
    public void add(FDDINode child)
    {
        ((Subject) child).setParent(this);
        getSubject().add((Subject) child);
    }

    @Override
    public void insert(MutableTreeNode node, int index)
    {
        ((Subject) node).setParent(this);
        subject.add(index, (Subject) node);
    }

    @Override
    public void remove(int index)
    {
        if(subject != null && subject.size() > index)
        subject.remove(index);
    }

    @Override
    public void remove(MutableTreeNode node)
    {
        subject.remove((Subject) node);
    }

    @Override
    public void setUserObject(Object arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TreeNode getChildAt(int index)
    {
        if(subject != null && subject.size() > index)
            return subject.get(index);
        else
            return null;
    }

    @Override
    public int getChildCount()
    {
        if(subject != null && subject.size() > 0)
            return subject.size();
        else
            return 0;
    }

    @Override
    public int getIndex(TreeNode node)
    {
        if(subject != null && subject.size() > 0)
            return subject.indexOf((Subject) node);
        else
            return -1;
    }

    @Override
    public boolean getAllowsChildren()
    {
        return true;
    }

    @Override
    public boolean isLeaf()
    {
        return (subject != null && (subject.size() <= 0));
    }

    @Override
    public Enumeration children()
    {
        if(subject != null)
            return Collections.enumeration(subject);
        else
            return null;
    }
}
