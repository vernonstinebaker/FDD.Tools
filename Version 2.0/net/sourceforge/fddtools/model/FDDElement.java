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
/**
 * Base class for all the element in FDD, including Project, MajorFeatureSet,
 * FeatureSet, Feature, abstracting the attributes all the elements share:
 * name, progress, target month, owner and parent.
 */
package net.sourceforge.fddtools.model;

import java.util.Date;
import java.util.Enumeration;

import net.sourceforge.fddtools.internationalization.Messages;

abstract public class FDDElement
{
    private String name = null;
    private int progress = 0;
    private Date targetMonth = null;
    private String owner = null;
    private FDDElement parent = null;

    /**
     * Constructors
     *
     * @param name TODO: Document this parameter!
     * @param progress TODO: Document this parameter!
     * @param targetMonth TODO: Document this parameter!
     * @param owner TODO: Document this parameter!
     * @param parent TODO: Document this parameter!
     */
    public FDDElement(String name, int progress, Date targetMonth,
                      String owner, FDDElement parent)
               throws IllegalArgumentException
    {
        this.name = name;
        this.progress = progress;
        this.targetMonth = targetMonth;
        this.owner = owner;

        if (null != parent)
        {
            if (!isLegalParent(parent))
            {
                throw new IllegalArgumentException("Illegal parent");
            }
        }

        this.parent = parent;
    }

    public FDDElement(String name, int progress, Date targetMonth, String owner)
    {
        this(name, progress, targetMonth, owner, null);
    }

    public FDDElement()
    {
        this(null, 0, null, null, null);
    }

    /**
     * getters and setters
     *
     * @param name TODO: Document this parameter!
     */
    public void setName(String name)
    {
        this.name = name;
    }

    public void setProgress(int progress)
    {
        this.progress = progress;
    }

    public void setTargetMonth(Date targetMonth)
    {
        this.targetMonth = targetMonth;
    }

    public void setOwner(String owner)
    {
        this.owner = owner;
    }

    public String getName()
    {
        return name;
    }

    public int getProgress()
    {
        Enumeration subFDDElements = this.getAllSubFDDElements();

        int percentage = 0;

        if (subFDDElements.hasMoreElements())
        {
            while (subFDDElements.hasMoreElements())
            {
                percentage += ((FDDElement) subFDDElements.nextElement()).getProgress();
            }

            return (int)((float)percentage /(this.getSubFDDElementCount()*100)*100) ;
        }
        else
        {
            return this.progress;
        }
    }

    public Date getTargetMonth()
    {
        Enumeration subFDDElements = this.getAllSubFDDElements();

        Date maxDate = null;

        if (subFDDElements.hasMoreElements())
        {
            maxDate = ((FDDElement) subFDDElements.nextElement()).getTargetMonth();

            while (subFDDElements.hasMoreElements())
            {
                FDDElement child = (FDDElement) subFDDElements.nextElement();

                if (child.getTargetMonth().after(maxDate))
                {
                    maxDate = child.getTargetMonth();
                }
            }
        }
        else
        {
            maxDate = this.targetMonth;
        }

        return maxDate;
    }

    public String getOwner()
    {
        return owner;
    }

    public FDDElement getParentFDDElement()
    {
        return parent;
    }

    public void setParentFDDElement(FDDElement parent)
                             throws IllegalArgumentException
    {
        if ((null != parent) && !isLegalParent(parent))
        {
            throw new IllegalArgumentException("Illegal parent");
        }

        this.parent = parent;
    }

    /**
     * methods allows insertion, removal and query of a FDDElement
     *
     * @return TODO: Document this return value!
     */
    abstract public int getSubFDDElementCount();

    abstract public void insertFDDElement(FDDElement child)
                                   throws IllegalArgumentException;

    abstract public void insertFDDElement(FDDElement child, int index)
                                   throws IllegalArgumentException;

    abstract public void removeFDDElement(FDDElement child);

    abstract public void removeFDDElement(int index);

    abstract public FDDElement getFDDElementAt(int index);

    abstract public Enumeration getAllSubFDDElements();

    /**
     * isLegalParent can't be implemented in FDDElementNode
     *
     * @param parent TODO: Document this parameter!
     *
     * @return TODO: Document this return value!
     */
    abstract public boolean isLegalParent(FDDElement parent);
}
