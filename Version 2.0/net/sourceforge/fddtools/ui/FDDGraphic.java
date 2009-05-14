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
 * FDDGraphic provide graphic component for the View in FDD MVC pattern.
 */
package net.sourceforge.fddtools.ui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;

import java.text.SimpleDateFormat;

import java.util.Date;

import net.sourceforge.fddtools.model.FDDElement;

class FDDGraphic
{
    protected FDDElement element = null;

    /** The original point from which the image is drawn on graphics */
    protected int originX = 0;
    protected int originY = 0;

    /** Width and Height the image will be */
    protected int width = 150;
    protected int height = 200;

    /**
     * Constructors
     *
     * @param element The FDD element this FddGraphics object represents
     * @param x X coordinate of the original point from which the image is drawn on graphics
     * @param y Y coordinate of the original point from which the image is drawn on graphics
     * @param w Width the image will be
     * @param h Height the image will be
     */
    public FDDGraphic(FDDElement element, int x, int y, int w, int h)
    {
        this.element = element;
        this.originX = x;
        this.originY = y;
        this.width = w;
        this.height = h;
    }

    public FDDGraphic(FDDElement element, int x, int y)
    {
        this.element = element;
        this.originX = x;
        this.originY = y;
    }

    public FDDGraphic(FDDElement element)
    {
        this.element = element;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    /**
     *
     *
     */
    public void draw(Graphics g)
    {
        g.setColor(Color.black);
        FontMetrics metrics = g.getFontMetrics();

        // draw the owner name at the upper right conner
        int ownerNameHeight = metrics.getHeight() + (int) (height / 32); // Text height + gap
        int ownerNameWidth = metrics.stringWidth(element.getOwner());
        g.drawString(element.getOwner(), (originX + width) - ownerNameWidth,
                     originY + metrics.getAscent());

        //determine height and width of upper, middle and lower box
        int boxHeight = height - ownerNameHeight;
        int boxOriginY = originY + ownerNameHeight;
        int upperLineY = (int) (0.6 * boxHeight);
        int lowerLineY = (int) (0.8 * boxHeight);

        int upperBoxBottomY = 1;
        int upperBoxHeight = upperLineY - 1;
        int middleBoxBottomY = upperLineY + 1;
        int middleBoxHeight = lowerLineY - upperLineY - 1;
        int lowerBoxHeight = boxHeight - lowerLineY - 1;
        int lowerBoxBottomY = lowerLineY + 1;

        g.drawRect(originX, boxOriginY, width, boxHeight);
        g.drawLine(originX + 1, boxOriginY + upperLineY, (originX + width) - 1,
                   boxOriginY + upperLineY);
        g.drawLine(originX + 1, boxOriginY + lowerLineY, (originX + width) - 1,
                   boxOriginY + lowerLineY);

        // draw the content of these 3 boxes
        drawUpperBox(g, originX + 1, boxOriginY + upperBoxBottomY, width - 1,
                     upperBoxHeight);
        drawMiddleBox(g, originX + 1, boxOriginY + middleBoxBottomY, width - 1,
                      middleBoxHeight);
        drawLowerBox(g, originX + 1, boxOriginY + lowerBoxBottomY, width - 1,
                     lowerBoxHeight);
    }

    private void drawUpperBox(Graphics g, int x, int y, int w, int h)
    {
        g.setColor(determineColor(element));
        g.fillRect(x, y, w, h);

        g.setColor(Color.black);

        //draw name
        int occupiedH = CenteredTextDrawer.draw(g, element.getName(), x,
                                                y + (h / 20), w);

        //draw Children count
        if (0 != element.getSubFDDElementCount())
        {
            CenteredTextDrawer.draw(g,
                                    "(" + element.getSubFDDElementCount() +
                                    ")", x, y + (h / 20) + occupiedH, w);
        }

        //draw Progress number (in percent)
        CenteredTextDrawer.draw(g, element.getProgress() + "%", x,
                                (y + h) - g.getFontMetrics().getHeight(), w);
    }

    private void drawMiddleBox(Graphics g, int x, int y, int w, int h)
    {
        int percent = (w * element.getProgress()) / 100;

        if (0 != percent)
        {
            g.setColor(Color.green);
            g.fillRect(x, y, percent, h);
            g.setColor(Color.black);
            g.drawLine(x + percent, y, x + percent, y + h);
            g.setColor(Color.white);
            g.fillRect(x + percent + 1, y, w - percent - 1, h);
        }
        else
        {
            g.setColor(Color.white);
            g.fillRect(x, y, w, h);
        }
    }

    private void drawLowerBox(Graphics g, int x, int y, int w, int h)
    {
        Color bgColor = (100 == element.getProgress()) ? Color.green : Color.white;
        g.setColor(bgColor);
        g.fillRect(x, y, w, h);
        g.setColor(Color.black);
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");

        int oneLineh = g.getFontMetrics().getHeight();
        CenteredTextDrawer.draw(g, formatter.format(element.getTargetMonth()),
                                x, y + ((h - oneLineh) / 2), w);
    }

    private Color determineColor(FDDElement element)
    {
        Date now = new Date();

        if (100 == element.getProgress()) // Completed!
        {
            return Color.green;
        }

        if (element.getTargetMonth().before(now)) // We're late
        {
            return Color.red;
        }

        if (0 == element.getProgress())
        {
            return Color.white;
        }

        return Color.yellow;
    }
}
