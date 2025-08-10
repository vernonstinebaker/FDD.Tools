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
// Removed Swing Enumeration usage after migration
import java.util.List;
// Swing tree imports removed
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
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
    public void add(List<FDDINode> children)
    {
        for(Object child : children)
        {
            ((Subject) child).setParentNode(this);
        }
        getSubject().add((Subject) children);
    }

    @Override
    public void add(FDDINode child)
    {
    ((Subject) child).setParentNode(this);
        getSubject().add((Subject) child);
    }

    @Override
    public boolean isLeaf()
    {
        return (subject != null && (subject.size() <= 0));
    }

    // FDDTreeNode overrides for Swing-free API
    @Override
    public java.util.List<? extends net.sourceforge.fddtools.model.FDDTreeNode> getChildren() {
        return subject == null ? java.util.Collections.emptyList() : java.util.Collections.unmodifiableList(subject);
    }
    @Override
    public void addChild(net.sourceforge.fddtools.model.FDDTreeNode child) {
        add((FDDINode) child);
    }
    @Override
    public void removeChild(net.sourceforge.fddtools.model.FDDTreeNode child) {
        if (subject != null) subject.remove(child);
    }

    @Override
    public void insertChildAt(net.sourceforge.fddtools.model.FDDTreeNode child, int index) {
        if (child instanceof Subject) {
            ((Subject) child).setParentNode(this);
            if (subject == null) subject = new java.util.ArrayList<>();
            if (index < 0 || index > subject.size()) subject.add((Subject) child); else subject.add(index,(Subject) child);
        } else {
            addChild(child);
        }
    }

    public void setStandardMilestones()
    {
        ObjectFactory of = new ObjectFactory();
        MilestoneInfo domainWalkthrough = of.createMilestoneInfo();
        MilestoneInfo design = of.createMilestoneInfo();
        MilestoneInfo designInspection = of.createMilestoneInfo();
        MilestoneInfo code = of.createMilestoneInfo();
        MilestoneInfo codeInspection = of.createMilestoneInfo();
        MilestoneInfo promoteToBuild = of.createMilestoneInfo();
        domainWalkthrough.setName("Domain Walkthrough");
        design.setName("Design");
        designInspection.setName("Design Inspection");
        code.setName("Code");
        codeInspection.setName("Code Inspection");
        promoteToBuild.setName("Promote to Build");
        domainWalkthrough.setEffort(1);
        design.setEffort(40);
        designInspection.setEffort(3);
        code.setEffort(45);
        codeInspection.setEffort(10);
        promoteToBuild.setEffort(1);
        
        // Create info if it doesn't exist
        if(info == null) {
            info = of.createAspectInfo();
        }
        
        // Clear existing milestones and set default values
        info.getMilestoneInfo().clear();
        info.setSubjectName("<Edit Subject Name>");
        info.setActivityName("<Edit Activity Name>");
        info.setFeatureName("<Feature Name>");
        info.setMilestoneName("<Milestone Name>");
        info.addMilestone(domainWalkthrough);
        info.addMilestone(design);
        info.addMilestone(designInspection);
        info.addMilestone(code);
        info.addMilestone(codeInspection);
        info.addMilestone(promoteToBuild);
    }
}
