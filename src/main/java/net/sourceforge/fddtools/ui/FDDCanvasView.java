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
 * FDDCanvasView is View in FDD MVC pattern, implementing TreeSelectionListener.
 * Meanwhile, FDDCanvasView is a Canvas which is suitable to added to a
 * container
 */
package net.sourceforge.fddtools.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Enumeration;
import java.util.HashMap;

import javax.imageio.ImageIO;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;

import java.awt.event.MouseAdapter;

import net.sourceforge.fddtools.internationalization.Messages;
import com.nebulon.xml.fddi.Feature;
import net.sourceforge.fddtools.printmanager.FDDImagePrinter;
import net.sourceforge.fddtools.model.FDDINode;

public class FDDCanvasView extends JPanel implements TreeSelectionListener, ComponentListener
{

    private static final int FRINGE_WIDTH = 20;
    private static final int FEATURE_ELEMENT_WIDTH = 100;
    private static final int FEATURE_ELEMENT_HEIGHT = 140;
    private static final Dimension FEATURE_ELEMENT_SIZE = new Dimension(FEATURE_ELEMENT_WIDTH, FEATURE_ELEMENT_HEIGHT);
    private static final int BORDER_WIDTH = 5;
    private static final int EXTRA_WIDTH = 5;
    private JScrollPane outerScrollPane;
    private FDDINode currentNode = null;
    private Font textFont = null;
    private JPopupMenu popupMenu;
    private int canvasWidth;
    private int imageWidth;
    private int elementsInRow = 1;
    private BufferedImage offImage;
    private ActionListener saveImageListener = new ActionListener()
    {

        @Override
        public void actionPerformed(final ActionEvent event)
        {
            saveImage();
        }
    };
    private ActionListener printImageListener = new ActionListener()
    {

        @Override
        public void actionPerformed(final ActionEvent event)
        {
            printImage();
        }
    };

    public FDDCanvasView(FDDINode fddiNode, final Font font)
    {
        super();

        this.currentNode = fddiNode;
        this.textFont = font;

        addComponentListener(this);

        JMenuItem menuSaveImage = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_SAVE_AS_CAPTION));
        menuSaveImage.addActionListener(this.saveImageListener);
        JMenuItem menuPrint = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_PRINT_CAPTION));
        menuPrint.addActionListener(this.printImageListener);
        JMenuItem menuProperties = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_PROPERTIES_CAPTION));
        menuProperties.setEnabled(false);

        popupMenu = new JPopupMenu(Messages.getInstance().getMessage(Messages.MENU_CAPTION));
        popupMenu.add(menuSaveImage);
        popupMenu.add(menuPrint);
        popupMenu.add(new JSeparator());
        popupMenu.add(menuProperties);

        this.add(popupMenu);

        this.addMouseListener(new MouseAdapter()
        {

            @Override
            public void mouseReleased(final MouseEvent event)
            {
                if(event.isPopupTrigger())
                {
                    popupMenu.show(event.getComponent(), event.getX(), event.getY());
                }
            }

            @Override
            public void mousePressed(final MouseEvent event)
            {
                if(event.isPopupTrigger())
                {
                    popupMenu.show(event.getComponent(), event.getX(), event.getY());
                }
            }
        });

    }

    public final JScrollPane getOuterScrollPane()
    {
        return outerScrollPane;
    }

    public final void setOuterScrollPane(final JScrollPane outterScrollPane)
    {
        this.outerScrollPane = outterScrollPane;
    }

    public final Font getTextFont()
    {
        return textFont;
    }

    public final void setTextFont(final Font font)
    {
        this.textFont = font;
    }

    @Override
    public final Dimension getMinimumSize()
    {
        return new Dimension(
                (int) (FEATURE_ELEMENT_SIZE.getWidth() + (2 * FRINGE_WIDTH) + (2 * BORDER_WIDTH)),
                (int) (FEATURE_ELEMENT_SIZE.getHeight() + (2 * FRINGE_WIDTH) + (2 * BORDER_WIDTH)));
    }

    @Override
    public final void valueChanged(final TreeSelectionEvent e)
    {
        currentNode = (FDDINode) e.getPath().getLastPathComponent();
        reflow();
    }

    @Override
    public final void paintComponent(final Graphics g)
    {
        super.paintComponent(g);

        g.clearRect(0, 0, this.getWidth(), this.getHeight());
        g.setColor(Color.white);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        if(null == this.offImage)
        {
            this.offImage = new BufferedImage((int) (outerScrollPane.getViewport().getExtentSize().getWidth()), (int) (outerScrollPane.getViewport().getExtentSize().getHeight()),
                    BufferedImage.TYPE_INT_RGB);
        }
        drawFDDGraphics(this.offImage.createGraphics());
        g.drawImage(offImage, 0, 0, this);
    }

    public final void drawFDDGraphics(final Graphics g)
    {
        g.clearRect(0, 0, this.getWidth(), this.getHeight());
        g.setColor(Color.white);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        if(null != textFont)
        {
            g.setFont(textFont);
        }

        //deal with fddiNode in a different way
        if(hasSubFDDElement())
        {
            g.setColor(Color.black);

            int titleTextHeight = CenteredTextDrawer.getTitleTextHeight(g, currentNode.getName(),
                    imageWidth);

            CenteredTextDrawer.draw(g, currentNode.getName(), BORDER_WIDTH, BORDER_WIDTH + FRINGE_WIDTH,
                    imageWidth);

            // draw all the sub elements
            Dimension subImages = drawSubElements(g, BORDER_WIDTH, titleTextHeight + FRINGE_WIDTH + BORDER_WIDTH, canvasWidth - (2 * BORDER_WIDTH) - EXTRA_WIDTH);

            // draw outter rect
            g.draw3DRect(0, 0, (int) subImages.getWidth() + (2 * BORDER_WIDTH), (int) subImages.getHeight() + titleTextHeight + FRINGE_WIDTH + (2 * BORDER_WIDTH), true);
            g.draw3DRect(BORDER_WIDTH, BORDER_WIDTH, (int) subImages.getWidth(), (int) subImages.getHeight() + titleTextHeight + FRINGE_WIDTH, true);
        }
        else
        {
            FDDGraphic currentGraphics = new FDDGraphic(currentNode, FRINGE_WIDTH, FRINGE_WIDTH,
                    (int) FEATURE_ELEMENT_SIZE.getWidth(), (int) FEATURE_ELEMENT_SIZE.getHeight());
            currentGraphics.draw(g);
        }
    }

    private Dimension drawSubElements(final Graphics g, final int x, final int y, final int maxWidth)
    {
        int currentX = FRINGE_WIDTH;
        int currentY = FRINGE_WIDTH;
        int currentHeight = FRINGE_WIDTH;
        int currentWidth = FRINGE_WIDTH;
        int imgWidth = 0;

        Enumeration<? extends TreeNode> children = currentNode.children();

        while(children.hasMoreElements())
        {
            FDDINode child = (FDDINode) children.nextElement();
            FDDGraphic childGraphic = new FDDGraphic(child, x + currentX, y + currentY,
                    (int) FEATURE_ELEMENT_SIZE.getWidth(), (int) FEATURE_ELEMENT_SIZE.getHeight());
            childGraphic.draw(g);
            currentWidth = currentX + childGraphic.getWidth() + FRINGE_WIDTH;
            if(currentWidth > imgWidth)
            {
                imgWidth = currentWidth;
            }

            currentHeight = currentY + childGraphic.getHeight() + FRINGE_WIDTH;

            if((currentWidth + childGraphic.getWidth() + FRINGE_WIDTH) > maxWidth)
            {
                currentX = FRINGE_WIDTH;
                currentY += (childGraphic.getHeight() + FRINGE_WIDTH);
            }
            else
            {
                currentX += (childGraphic.getWidth() + FRINGE_WIDTH);
            }
        }

        return new Dimension(imgWidth, currentHeight);
    }

    private boolean hasSubFDDElement()
    {
        return currentNode.getChildCount() > 0;
    }

    private int calculateCanvasHeight(final int availableWidth)
    {
        // Default to one fddiNode
        int height = (int) FEATURE_ELEMENT_SIZE.getHeight() + (FRINGE_WIDTH * 2) + FRINGE_WIDTH + BORDER_WIDTH;

        if(hasSubFDDElement())
        {
            int oneRowWidth = (int) ((currentNode.getChildCount() *
                    (FRINGE_WIDTH + FEATURE_ELEMENT_SIZE.getWidth())) + FRINGE_WIDTH + (2 * BORDER_WIDTH));

            if(oneRowWidth > availableWidth)
            {
                elementsInRow = (int) Math.floor((availableWidth - (2 * BORDER_WIDTH) - FRINGE_WIDTH) / (FRINGE_WIDTH + FEATURE_ELEMENT_SIZE.getWidth()));
                elementsInRow = Math.max(elementsInRow, 1);

                int rows = (int) Math.ceil((float) currentNode.getChildCount() / elementsInRow);

                height = (int) ((FEATURE_ELEMENT_SIZE.getHeight() + FRINGE_WIDTH) * rows) + (FRINGE_WIDTH * 2) + BORDER_WIDTH; // + (EXTRA_WIDTH - 1);
            }
            else
            {
                elementsInRow = currentNode.getChildCount();
            }
        }

        if(!(currentNode instanceof Feature))
        {
            height += getFontMetrics(textFont).getHeight() + BORDER_WIDTH + 1;
        }

        imageWidth = (int) (elementsInRow * (FEATURE_ELEMENT_SIZE.getWidth() + FRINGE_WIDTH)) + FRINGE_WIDTH + BORDER_WIDTH + (EXTRA_WIDTH + 1);

        return height;
    }

    private void saveAsImage(final OutputStream dest, String formatName) throws IOException
    {
        try
        {
            BufferedImage bi = new BufferedImage(offImage.getWidth(), offImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics g = bi.getGraphics();
            g.drawImage(this.offImage, 0, 0, this);
            ImageIO.write(bi, formatName, dest);
            dest.close();
        }
        catch(IOException e)
        {
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE, null, e); //NOI18N
            JOptionPane.showMessageDialog(this, Messages.getInstance().getMessage(Messages.ERROR_IMAGE_FORMAT));
        }
    }

    public void printImage()
    {
        new FDDImagePrinter(offImage);
    }

    @Override
    public void componentHidden(final ComponentEvent e)
    {
    }

    @Override
    public void componentMoved(final ComponentEvent e)
    {
    }

    @Override
    public final void componentResized(final ComponentEvent e)
    {
        reflow();
    }

    @Override
    public void componentShown(final ComponentEvent e)
    {
    }

    public final void reflow()
    {
        canvasWidth = (int) (outerScrollPane.getViewport().getExtentSize().getWidth());
        int height = calculateCanvasHeight(canvasWidth);
        this.setPreferredSize(new Dimension(imageWidth, height));
        this.offImage = new BufferedImage(imageWidth, height, BufferedImage.TYPE_INT_RGB);
        revalidate();
        repaint();
    }

    private void saveImage()
    {
        HashMap<String[], String> fileTypes = new HashMap<String[], String>();
        fileTypes.put(new String[]
                {
                    "jpg", "jpeg"
                }, "JPEG Files");
        fileTypes.put(new String[]
                {
                    "png"
                }, "PNG Files");
        String imgFileName = ExtensionFileFilter.getFileName(System.getProperty("user.home"), fileTypes,
                ExtensionFileFilter.SAVE);

        if(null != imgFileName && !imgFileName.isEmpty())
        {
            File imgFile = new File(imgFileName);
            if(imgFile.exists())
            {
                if(JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(this,
                        Messages.getInstance().getMessage(Messages.QUESTION_FILE_EXISTS_OVERRIDE),
                        Messages.getInstance().getMessage(Messages.JOPTIONPANE_TITLE),
                        JOptionPane.YES_NO_OPTION))
                {
                    return;
                }
            }
            try
            {
                if(imgFileName.endsWith(".jpg") || imgFileName.endsWith(".jpeg"))
                {
                    saveAsImage(new FileOutputStream(imgFileName), "jpg");
                }
                else
                {
                    saveAsImage(new FileOutputStream(imgFileName), "png");
                }
            }
            catch(FileNotFoundException e)
            {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE, null, e);
                JOptionPane.showMessageDialog(this, Messages.getInstance().getMessage(Messages.ERROR_FILE_NOT_FOUND));
            }
            catch(IOException e)
            {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE, null, e);
                JOptionPane.showMessageDialog(this, Messages.getInstance().getMessage(Messages.ERROR_SAVING_IMAGE));
            }
        }
    }
}
