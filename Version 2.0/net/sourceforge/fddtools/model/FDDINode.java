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
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
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
        if(targetDate == null || targetDate.before(date))
            targetDate = date;

        if(getParent() != null)
            ((FDDINode) getParent()).setTargetDate(date);
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

    @Override
    public void setParent(MutableTreeNode node)
    {
        parent = (FDDINode) node;
    }

    @Override
    public TreeNode getParent()
    {
        return parent;
    }

    @Override
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
                if(target instanceof FDDINode && parent instanceof FDDINode)
                    ((FDDINode) target).setParent((FDDINode) parent);
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
        if(children() != null && getChildCount() > 0)
        {
            for(Enumeration e = children(); e.hasMoreElements(); )
            {
                FDDINode node = (FDDINode) e.nextElement();
                childrenProgress += node.getProgress().getCompletion();
            }
            p.setCompletion(childrenProgress/getChildCount());
        }
        else
        {
            p.setCompletion(0);
        }
        setProgress(p);
        if(getParent() != null)
        {
            ((FDDINode) getParent()).calculateProgress();
        }
    }

    public void calculateTargetDate()
    {
        if(children() != null)
        {
            for(Enumeration e = children(); e.hasMoreElements(); )
            {
                FDDINode node = (FDDINode) e.nextElement();
                if(targetDate == null && node.targetDate != null && node.getTargetDate().after(targetDate))
                {
                    setTargetDate(node.getTargetDate());
                }
                node.calculateTargetDate();
            }
        }
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
        TreePath path = getTreePath(this);

        for(Object pathNode : path.getPath())
        {
            if(pathNode instanceof Aspect)
            {
                return (Aspect) pathNode;
            }
        }
        return null;
    }

    public TreePath getTreePath(FDDINode node)
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

    public List<Feature> getFeaturesForNode()
    {
        List<Feature> features = new ArrayList<Feature>();
        collectFeatures(features);
        return features;
    }

    private void collectFeatures(List<Feature> features)
    {
        if(children() != null)
        {
            for(Enumeration e = children(); e.hasMoreElements(); )
            {
                FDDINode node = (FDDINode) e.nextElement();
                if(node instanceof Feature)
                {
                    features.add((Feature) node);
                }
                node.collectFeatures(features);
            }
        }
    }

    public void addTreeModelListener(javax.swing.event.TreeModelListener l) {}
    public void removeTreeModelListener(javax.swing.event.TreeModelListener l) {}
    public void valueForPathChanged(TreePath path, Object newValue) {}
    abstract public void add(List children);
    abstract public void add(FDDINode child);
}
