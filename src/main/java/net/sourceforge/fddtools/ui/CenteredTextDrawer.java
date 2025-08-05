/*
 * ==================================================================== The
 * Apache Software License, Version 1.1 Copyright (c) 1999-2003 The Apache
 * Software Foundation. All rights reserved. Redistribution and use in source
 * and binary forms, with or without modification, are permitted provided that
 * the following conditions are met: 1. Redistributions of source code must
 * retain the above copyright notice, this list of conditions and the following
 * disclaimer. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. 3. The
 * end-user documentation included with the redistribution, if any, must include
 * the following acknowlegement: "This product includes software developed by
 * the Apache Software Foundation (http://www.apache.org/)." Alternately, this
 * acknowlegement may appear in the software itself, if and wherever such
 * third-party acknowlegements normally appear. 4. The names "The Jakarta
 * Project", "Commons", and "Apache Software Foundation" must not be used to
 * endorse or promote products derived from this software without prior written
 * permission. For written permission, please contact apache@apache.org. 5.
 * Products derived from this software may not be called "Apache" nor may
 * "Apache" appear in their names without prior written permission of the Apache
 * Group. THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ==================================================================== This
 * software consists of voluntary contributions made by many individuals on
 * behalf of the Apache Software Foundation. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
/**
 * Description: This class is seperated from FDDGraphic and to provide feature
 * of drawing centered text in more generic way
 */

package net.sourceforge.fddtools.ui;

import java.awt.FontMetrics;
import java.awt.Graphics;

import java.util.Iterator;

final class CenteredTextDrawer
{
    private CenteredTextDrawer()
    {
    }

    public static int draw(final Graphics g, final String s, final int x, final int y, final int w)
    {
        FontMetrics metrics = g.getFontMetrics();
        WordsInLines lines = new WordsInLines(s);
        adjustToMaxWidth(g.getFontMetrics(), lines, w);

        // display text line by line
        Iterator<String> formattedText = lines.getAllText();
        int currentHeight = 0;

        while (formattedText.hasNext())
        {
            String thisLine = (String) formattedText.next();
            g.drawString(thisLine, x + ((w - metrics.stringWidth(thisLine)) / 2), y + currentHeight
                    + metrics.getAscent());
            currentHeight += metrics.getHeight();
        }
        return currentHeight;
    }

    public static int getTitleTextHeight(final Graphics g, final String text, final int maxWidth)
    {
        WordsInLines lines = new WordsInLines(text);
        adjustToMaxWidth(g.getFontMetrics(), lines, maxWidth);

        // Calculate the total height
        return g.getFontMetrics().getHeight() * lines.getLinesCount();
    }

    private static boolean adjustToMaxWidth(final FontMetrics metrics, final WordsInLines lines,
            final int maxWidth)
    {
        boolean moved = false;
        int i = 0;

        while (i < (lines.getLinesCount() - 1))
        {
            if (metrics.stringWidth(lines.getLine(i) + lines.getFirstWordOfLine(i + 1)) <= maxWidth)
            {
                if (lines.moveUpFirstWordOfLine(i + 1))
                {
                    moved = true;
                }
            }
            else
            {
                i++; // Try next line
            }
        }
        return moved;
    }
}
