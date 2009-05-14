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

package net.sourceforge.fddtools.printmanager;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;

public class FDDPrintManager
{
    private final static int POINTS_PER_INCH = 72;
    private PrinterJob printJob;
    private Book book;    
    
    /**
     * Constructor: FDDPrintManager <p>
     *
     */
    public FDDPrintManager()
    {        
        printJob = PrinterJob.getPrinterJob();
        book = new Book();
    }
    
    public void append(Printable page)
    {
        book.append(page, printJob.defaultPage());
    }
    
    public void print()
    {
        printJob.setPageable(book);

        if (printJob.printDialog())
        {
            try
            {
                printJob.print();
            }
            catch (Exception PrintException)
            {
                PrintException.printStackTrace();
            }
        }        
    }    
    
    /**
     * Class: Document <p>
     *
     * This class is the painter for the document content.<p>
     *
     *
     * @author Jean-Pierre Dube <jpdube@videotron.ca>
     * @version 1.0
     * @since 1.0
     * @see Printable
     */
    
/*    
    private class Document implements Printable
    {
        public int print(Graphics g, PageFormat pageFormat, int page)
        {
            //--- Create the Graphics2D object
            Graphics2D g2d = (Graphics2D) g;
            
            //--- Translate the origin to 0,0 for the top left corner
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            
            //--- Set the drawing color to black
            g2d.setPaint(Color.black);
            
            //--- Draw a border around the page using a 12 point border
            g2d.setStroke(new BasicStroke(12));
            Rectangle2D.Double border = new Rectangle2D.Double(0,
                    0,
                    pageFormat.getImageableWidth(),
                    pageFormat.getImageableHeight());
            
            g2d.draw(border);
            
            
            //--- Print page 1
            if (page == 1)
            {
                //--- Print the text one inch from the top and left margins
                g2d.drawString("This the content page of page: " + page, POINTS_PER_INCH, POINTS_PER_INCH);
                return (PAGE_EXISTS);
            }
            
            //--- Print page 2
            else if (page == 2)
            {
                //--- Print the text one inch from the top and left margins
                g2d.drawString("This the content of the second page: " + page, POINTS_PER_INCH, POINTS_PER_INCH);
                return (PAGE_EXISTS);
            }
            
            
            //--- Validate the page
            return (NO_SUCH_PAGE);
            
        }
    }
*/
}
