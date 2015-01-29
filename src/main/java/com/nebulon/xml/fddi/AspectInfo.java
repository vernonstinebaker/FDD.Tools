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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;


/**
 * 
 *             An aspect description is the set of milestone definitions and the
 *             names of key concepts: subject area, business activity, feature,
 *             milestone.
 *           
 * 
 * <p>Java class for aspectInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="aspectInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="subjectName" type="{http://www.w3.org/2001/XMLSchema}token"/>
 *         &lt;element name="activityName" type="{http://www.w3.org/2001/XMLSchema}token"/>
 *         &lt;element name="featureName" type="{http://www.w3.org/2001/XMLSchema}token"/>
 *         &lt;element name="milestoneName" type="{http://www.w3.org/2001/XMLSchema}token"/>
 *         &lt;element name="milestoneInfo" type="{http://www.nebulon.com/xml/2004/fddi}milestoneInfo" maxOccurs="unbounded"/>
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
@XmlType(name = "aspectInfo", propOrder = {
    "subjectName",
    "activityName",
    "featureName",
    "milestoneName",
    "milestoneInfo",
    "any"
})

public class AspectInfo implements Serializable
{
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String subjectName;
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String activityName;
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String featureName;
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String milestoneName;
    @XmlElement(required = true)
    protected List<MilestoneInfo> milestoneInfo;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    public String getSubjectName()
    {
        return subjectName;
    }

    public void setSubjectName(String value)
    {
        this.subjectName = value;
    }

    public String getActivityName()
    {
        return activityName;
    }

    public void setActivityName(String value)
    {
        this.activityName = value;
    }

    public String getFeatureName()
    {
        return featureName;
    }

    public void setFeatureName(String value)
    {
        this.featureName = value;
    }

    public String getMilestoneName()
    {
        return milestoneName;
    }

    public void setMilestoneName(String value)
    {
        this.milestoneName = value;
    }

    public List<MilestoneInfo> getMilestoneInfo()
    {
        if(milestoneInfo == null)
        {
            milestoneInfo = new ArrayList<MilestoneInfo>();
        }
        return this.milestoneInfo;
    }

    public void addMilestone(List children)
    {
        getMilestoneInfo().add((MilestoneInfo) children);
    }

    public void addMilestone(MilestoneInfo child)
    {
        getMilestoneInfo().add((MilestoneInfo) child);
    }


    public List<Object> getAny()
    {
        if(any == null)
        {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String value)
    {
        this.id = value;
    }

    public Map<QName, String> getOtherAttributes()
    {
        return otherAttributes;
    }

}
