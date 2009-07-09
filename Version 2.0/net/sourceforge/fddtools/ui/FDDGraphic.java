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

import com.nebulon.xml.fddi.Feature;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;

import java.text.SimpleDateFormat;

import net.sourceforge.fddtools.model.FDDINode;

class FDDGraphic
{
    private FDDINode fddiNode = null;
    private int originX = 0;
    private int originY = 0;
    private int width = 150;
    private int height = 200;

    public FDDGraphic(FDDINode node, int x, int y, int w, int h)
    {
        fddiNode = node;
        originX = x;
        originY = y;
        width = w;
        height = h;
    }

    public FDDGraphic(FDDINode node, int x, int y)
    {
        fddiNode = node;
        originX = x;
        originY = y;
    }

    public FDDGraphic(FDDINode node)
    {
        fddiNode = node;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public void draw(Graphics g)
    {
        int ownerNameWidth = 0;
        g.setColor(Color.black);
        FontMetrics metrics = g.getFontMetrics();
        int ownerNameHeight = metrics.getHeight() + (int) (height / 32); // Text height + gap

        if(fddiNode instanceof Feature)
        {
            // draw the owner name at the upper right conner
            String owner = ((Feature) fddiNode).getInitials();
            if(owner != null)
            {
                ownerNameWidth = metrics.stringWidth(owner);
                g.drawString(owner, (originX + width) - ownerNameWidth,
                    originY + metrics.getAscent());
            }
        }

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
        g.setColor(determineColor(fddiNode));
        g.fillRect(x, y, w, h);

        g.setColor(Color.black);

        //draw name
        int occupiedH = CenteredTextDrawer.draw(g, fddiNode.getName(), x,
                y + (h / 20), w);

        //draw Children count
        if(fddiNode.getChildCount() > 0)
        {
            CenteredTextDrawer.draw(g,
                    "(" + fddiNode.getChildCount() +
                    ")", x, y + (h / 20) + occupiedH, w);
        }

        CenteredTextDrawer.draw(g, Integer.toString(fddiNode.getProgress().getCompletion()) + "%", x,
                (y + h) - g.getFontMetrics().getHeight(), w);
    }

    private void drawMiddleBox(Graphics g, int x, int y, int w, int h)
    {
        int percent = (w * fddiNode.getProgress().getCompletion()/100);

        if(0 != percent)
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
        Color bgColor = Color.WHITE;
        if(fddiNode.getProgress() != null)
            bgColor = (fddiNode.getProgress().getCompletion() == 100) ? Color.GREEN : Color.WHITE;
        g.setColor(bgColor);
        g.fillRect(x, y, w, h);
        g.setColor(Color.black);
        
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
//        SimpleDateFormat formatter = new SimpleDateFormat("MMM yy");


        int oneLineh = g.getFontMetrics().getHeight();
        if(fddiNode.getTargetDate() != null)
        {
            CenteredTextDrawer.draw(g, formatter.format(fddiNode.getTargetDate()),
                x, y + ((h - oneLineh) / 2), w);
        }
    }

    private Color determineColor(FDDINode fddiNode)
    {
        Color bgColor = Color.WHITE;
        if(fddiNode.getProgress() != null)
        {
            if(fddiNode.getProgress().getCompletion() == 100)
                bgColor = Color.GREEN;
            else if(fddiNode.getProgress().getCompletion() > 0)
                bgColor = Color.CYAN;
        }

        if(fddiNode.isLate())
        {
            return Color.red;
        }

        return bgColor;
    }
}
