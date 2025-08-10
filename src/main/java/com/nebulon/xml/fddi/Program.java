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
// Removed Swing Enumeration usage
import java.util.List;
// Swing tree imports removed
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
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

    public List<Program> getProgram()
    {
        if(program == null)
        {
            program = new ArrayList<Program>();
        }
        return this.program;
    }

    public List<Project> getProject()
    {
        if(project == null)
        {
            project = new ArrayList<Project>();
        }
        return this.project;
    }

    @Override
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

    // children() removed; use getChildren()

    @Override
    public void add(List<FDDINode> children)
    {
        if(children instanceof Program)
        {
            for(Object child : children)
               ((Program) child).setParentNode(this);
            getProgram().add((Program) children);
        }
        else if (children instanceof Project)
        {
            for(Object child : children)
               ((Project) child).setParentNode(this);
            getProject().add((Project) children);
        }
    }

    @Override
    public void add(FDDINode child)
    {
        if(child instanceof Program)
        {
           ((Program) child).setParentNode(this);
            getProgram().add((Program) child);
        }
        else if (child instanceof Project)
        {
            getProject().add((Project) child);
           ((Project) child).setParentNode(this);
        }
    }

    // FDDTreeNode interface implementation (Swing-free)
    @Override
    public java.util.List<? extends net.sourceforge.fddtools.model.FDDTreeNode> getChildren() {
        java.util.List<net.sourceforge.fddtools.model.FDDTreeNode> list = new java.util.ArrayList<>();
        if (program != null) list.addAll(program);
        if (project != null) list.addAll(project);
        return java.util.Collections.unmodifiableList(list);
    }

    @Override
    public void addChild(net.sourceforge.fddtools.model.FDDTreeNode child) {
        add((FDDINode) child);
    }

    @Override
    public void removeChild(net.sourceforge.fddtools.model.FDDTreeNode child) {
        if (child instanceof Program && program != null) {
            program.remove(child);
        } else if (child instanceof Project && project != null) {
            project.remove(child);
        }
    }

    @Override
    public void insertChildAt(net.sourceforge.fddtools.model.FDDTreeNode child, int index) {
        if (child instanceof Program) {
            ((Program) child).setParentNode(this);
            if (program == null) program = new java.util.ArrayList<>();
            if (index < 0 || index > program.size()) program.add((Program) child); else program.add(index,(Program) child);
        } else if (child instanceof Project) {
            ((Project) child).setParentNode(this);
            if (project == null) project = new java.util.ArrayList<>();
            if (index < 0 || index > project.size()) project.add((Project) child); else project.add(index,(Project) child);
        } else {
            addChild(child); // fallback
        }
    }
}
