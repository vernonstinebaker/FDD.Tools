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

package net.sourceforge.fddtools.model;

import com.nebulon.xml.fddi.Aspect;
import com.nebulon.xml.fddi.Progress;
import com.nebulon.xml.fddi.Feature;
import com.nebulon.xml.fddi.ObjectFactory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
// Swing tree imports removed after migration to FDDTreeNode API
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// Retaining Swing tree dependencies until legacy Swing UI fully removed
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

/**
 *
 * @author vds
 */
@XmlTransient
public abstract class FDDINode implements FDDTreeNode, Serializable
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


    public String getName()
    {
        return name;
    }

    public void setName(String value)
    {
        name = value;
    }

    public Progress getProgress()
    {
        if(progress == null)
            calculateProgress();
        return progress;
    }

    public void setProgress(Progress value)
    {
        progress = value;
    }

    public Date getTargetDate()
    {
        if(targetDate == null)
            calculateTargetDate();
        return targetDate;
    }

    public void setTargetDate(Date date)
    {
//        if(targetDate == null || targetDate.before(date))
            targetDate = date;

//        if(getParent() != null)
//            ((FDDINode) getParent()).setTargetDate(date);
    }

    public List<Object> getAny()
    {
        if(any == null)
        {
            any = new ArrayList<Object>();
        }
        return any;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String value)
    {
        id = value;
    }

    public Map<QName, String> getOtherAttributes()
    {
        return otherAttributes;
    }

    // FDDTreeNode + Swing legacy implementation
    @Override
    public FDDTreeNode getParentNode() {
        return parent;
    }

    @Override
    public void setParentNode(FDDTreeNode p) { parent = (FDDINode) p; }

    // Legacy Swing methods removed (use getParentNode/setParentNode and addChild/removeChild)
    public FDDINode getParent() { return parent; }

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
                if(target instanceof FDDINode && parent instanceof FDDINode)
                    ((FDDINode) target).setParentNode((FDDINode) parent);
                if(target instanceof Feature)
                {
                    Feature feature = (Feature) target;
                    if(feature.getSeq() > feature.getSequence())
                    {
                        feature.setSequence(feature.getSeq());
                    }
                }
            }
        };
    }

    public void calculateProgress()
    {
        int childrenProgress = 0;
        ObjectFactory of = new ObjectFactory();
        Progress p = of.createProgress();
        List<? extends FDDTreeNode> childList = getChildren();
        if(!childList.isEmpty()) {
            for (FDDTreeNode tn : childList) {
                FDDINode node = (FDDINode) tn; // transitional cast
                childrenProgress += node.getProgress().getCompletion();
            }
            p.setCompletion(childrenProgress / childList.size());
        } else {
            p.setCompletion(0);
        }
        setProgress(p);
        if(getParentNode() != null) {
            ((FDDINode) getParentNode()).calculateProgress();
        }
    }

    public void calculateTargetDate()
    {
        targetDate = null;
        for(Feature f : getFeaturesForNode())
        {
            if(f.getTargetDate() != null && (targetDate == null || targetDate.before(f.getTargetDate())))
            {
                setTargetDate(f.getTargetDate());
            }
        }
        if(getParentNode() != null) {
            ((FDDINode) getParentNode()).calculateTargetDate();
        }
//        if(children() != null)
//        {
//            for(Enumeration e = children(); e.hasMoreElements(); )
//            {
//                FDDINode node = (FDDINode) e.nextElement();
//                if(targetDate == null && node.targetDate != null && node.getTargetDate().after(targetDate))
//                {
//                    setTargetDate(node.getTargetDate());
//                }
//                node.calculateTargetDate();
//            }
//        }
    }
    
    public boolean isLate()
    {
        if(getTargetDate() != null &&
           getProgress().getCompletion() != 100 &&
           getTargetDate().before(new Date()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    
    public Aspect getAspectForNode()
    {
        for (FDDTreeNode tn : buildPath()) {
            if (tn instanceof Aspect) return (Aspect) tn;
        }
        return null;
    }

    // Legacy getTreePath removed; use buildPath() from FDDTreeNode

    public List<Feature> getFeaturesForNode()
    {
        List<Feature> features = new ArrayList<Feature>();
        collectFeatures(features);
        return features;
    }

    // --- FDDTreeNode children adapter methods ---
    @Override
    // Subclasses now provide concrete getChildren()/addChild()/removeChild().
    public abstract List<? extends FDDTreeNode> getChildren();
    public abstract void addChild(FDDTreeNode child);
    public abstract void removeChild(FDDTreeNode child);

    private void collectFeatures(List<Feature> features)
    {
        for (FDDTreeNode tn : getChildren()) {
            FDDINode node = (FDDINode) tn;
            if (node instanceof Feature) {
                features.add((Feature) node);
            }
            node.collectFeatures(features);
        }
    }

    abstract public void add(List<FDDINode> children);
    abstract public void add(FDDINode child);
}
