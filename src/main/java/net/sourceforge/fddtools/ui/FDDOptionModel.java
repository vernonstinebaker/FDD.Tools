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
 * Description: Model to keep all of FDD optionsModel.
 * @author Kenneth Jiang  3/13/2001   created
 * @version 1.0
 */
package net.sourceforge.fddtools.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

public class FDDOptionModel implements Cloneable
{

    Map<FDDOptionListener, Object> listeners = new WeakHashMap<FDDOptionListener, Object>();
    private final String preferredFont = "Impact";
    private final String safeFont = "Arial";
    private final int preferredSize = 10;
    private Font textFont = null;
    private Dimension picSize = new Dimension(500, 800);
    private Dimension imageSize = new Dimension(150, 200);
    // Unused field - commented out to remove warning
    // private int multiSaveLevel = 0;

    public FDDOptionModel(String fontName, int fontStyle, int fontSize)
    {
        textFont = new Font(fontName, fontStyle, fontSize);
    }

    public FDDOptionModel(Font textFont)
    {
        this.textFont = textFont;
    }

    public FDDOptionModel()
    {
        GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String envfonts[] = gEnv.getAvailableFontFamilyNames();
        String fontName = safeFont;
        for(int i = 0; i < envfonts.length; i++)
        {
            if(preferredFont.equals(envfonts[i]))
            {
                fontName = preferredFont;
            }
        }
        textFont = new Font(fontName, Font.PLAIN, preferredSize);
    }

    @Override
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch(CloneNotSupportedException e)
        {
        }
        return this;     //we will never go here, add it to make it compile
    }

    public synchronized void setImageSize(Dimension imageDim)
    {
        this.imageSize = imageDim;
    }

    public synchronized Dimension getImageSize()
    {
        return this.imageSize;
    }

    public synchronized void setPicSize(Dimension picDim)
    {
        this.picSize = picDim;
    }

    public synchronized Dimension getPicSize()
    {
        return this.picSize;
    }

    public synchronized void setTextFont(Font textFont)
    {
        this.textFont = textFont;
    }

    public synchronized Font getTextFont()
    {
        return this.textFont;
    }

    public synchronized boolean addFDDOptionListener(FDDOptionListener l)
    {
        return listeners.put(l, null) != null;
    }

    public synchronized boolean removeFDDOptionListener(FDDOptionListener l)
    {
        return listeners.remove(l) != null;
    }

    public void valueChangeTo(FDDOptionModel model)
    {
        setTextFont(model.getTextFont());
        setImageSize(model.getImageSize());
        setPicSize(model.getPicSize());

        for(final Iterator<FDDOptionListener> iter = listeners.keySet().iterator(); iter.hasNext();)
        {
            iter.next().optionChanged(new FDDOptionEvent(this));
        }
    }
}
