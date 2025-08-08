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

package net.sourceforge.fddtools.ui.fx;

import com.nebulon.xml.fddi.Feature;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import net.sourceforge.fddtools.model.FDDINode;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * JavaFX implementation of FDD graphic component for drawing FDD elements.
 * This is the modern replacement for the Swing-based FDDGraphic.
 */
class FDDGraphicFX {
    
    private final FDDINode fddiNode;
    private final double originX;
    private final double originY;
    private final double width;
    private final double height;
    
    /**
     * Creates a new FDD graphic with specified dimensions.
     */
    public FDDGraphicFX(FDDINode node, double x, double y, double w, double h) {
        this.fddiNode = node;
        this.originX = x;
        this.originY = y;
        this.width = w;
        this.height = h;
    }
    
    /**
     * Creates a new FDD graphic with default dimensions.
     */
    public FDDGraphicFX(FDDINode node, double x, double y) {
        this(node, x, y, 150, 200);
    }
    
    /**
     * Creates a new FDD graphic at origin with default dimensions.
     */
    public FDDGraphicFX(FDDINode node) {
        this(node, 0, 0, 150, 200);
    }
    
    /**
     * Gets the width of this graphic.
     */
    public double getWidth() {
        return width;
    }
    
    /**
     * Gets the height of this graphic.
     */
    public double getHeight() {
        return height;
    }
    
    /**
     * Draws the FDD graphic on the specified graphics context.
     */
    public void draw(GraphicsContext gc) {
        if (fddiNode == null) return;
        
        // Save current state
        Font originalFont = gc.getFont();
        javafx.scene.paint.Paint originalFill = gc.getFill();
        javafx.scene.paint.Paint originalStroke = gc.getStroke();
        double originalLineWidth = gc.getLineWidth();
        
        try {
            gc.setStroke(Color.BLACK);
            gc.setFill(Color.BLACK);
            gc.setLineWidth(1);
            
            double ownerNameHeight = 0;
            
            // Draw owner name for features
            if (fddiNode instanceof Feature) {
                String owner = ((Feature) fddiNode).getInitials();
                if (owner != null && !owner.trim().isEmpty()) {
                    Font font = gc.getFont();
                    Text ownerText = new Text(owner);
                    ownerText.setFont(font);
                    double ownerWidth = ownerText.getBoundsInLocal().getWidth();
                    ownerNameHeight = ownerText.getBoundsInLocal().getHeight() + (height / 32);
                    
                    gc.fillText(owner, (originX + width) - ownerWidth, originY + ownerNameHeight * 0.75);
                }
            }
            
            // Calculate box dimensions
            double boxHeight = height - ownerNameHeight;
            double boxOriginY = originY + ownerNameHeight;
            double upperLineY = boxHeight * 0.6;
            double lowerLineY = boxHeight * 0.8;
            
            double upperBoxHeight = upperLineY - 1;
            double middleBoxHeight = lowerLineY - upperLineY - 1;
            double lowerBoxHeight = boxHeight - lowerLineY - 1;
            
            // Draw main rectangle
            gc.strokeRect(originX, boxOriginY, width, boxHeight);
            
            // Draw dividing lines
            gc.strokeLine(originX + 1, boxOriginY + upperLineY, 
                         (originX + width) - 1, boxOriginY + upperLineY);
            gc.strokeLine(originX + 1, boxOriginY + lowerLineY, 
                         (originX + width) - 1, boxOriginY + lowerLineY);
            
            // Draw the three sections
            drawUpperBox(gc, originX + 1, boxOriginY + 1, width - 1, upperBoxHeight);
            drawMiddleBox(gc, originX + 1, boxOriginY + upperLineY + 1, width - 1, middleBoxHeight);
            drawLowerBox(gc, originX + 1, boxOriginY + lowerLineY + 1, width - 1, lowerBoxHeight);
            
        } finally {
            // Restore original state
            gc.setFont(originalFont);
            gc.setFill(originalFill);
            gc.setStroke(originalStroke);
            gc.setLineWidth(originalLineWidth);
        }
    }
    
    /**
     * Draws the upper box containing name, child count, and completion percentage.
     */
    private void drawUpperBox(GraphicsContext gc, double x, double y, double w, double h) {
        // Fill background with status color
        Color bgColor = determineColor(fddiNode);
        gc.setFill(bgColor);
        gc.fillRect(x, y, w, h);
        
        // Draw text in contrasting color
        gc.setFill(Color.BLACK);
        
        // Draw name
        double occupiedH = CenteredTextDrawerFX.draw(gc, fddiNode.getName(), x, y + (h / 20), w);
        
        // Draw children count if applicable
        if (fddiNode.getChildCount() > 0) {
            String childText = "(" + fddiNode.getChildCount() + ")";
            occupiedH += CenteredTextDrawerFX.draw(gc, childText, x, y + (h / 20) + occupiedH, w);
        }
        
        // Draw completion percentage at bottom
        String completionText = fddiNode.getProgress().getCompletion() + "%";
        double textHeight = getTextHeight(gc.getFont());
        CenteredTextDrawerFX.draw(gc, completionText, x, (y + h) - textHeight, w);
    }
    
    /**
     * Draws the middle box showing progress bar.
     */
    private void drawMiddleBox(GraphicsContext gc, double x, double y, double w, double h) {
        int completion = fddiNode.getProgress().getCompletion();
        double percent = (w * completion) / 100.0;
        
        if (percent > 0) {
            // Draw completed portion
            gc.setFill(Color.GREEN);
            gc.fillRect(x, y, percent, h);
            
            // Draw dividing line
            gc.setStroke(Color.BLACK);
            gc.strokeLine(x + percent, y, x + percent, y + h);
            
            // Fill remaining portion
            gc.setFill(Color.WHITE);
            gc.fillRect(x + percent + 1, y, w - percent - 1, h);
        } else {
            // No progress - fill with white
            gc.setFill(Color.WHITE);
            gc.fillRect(x, y, w, h);
        }
    }
    
    /**
     * Draws the lower box containing target date.
     */
    private void drawLowerBox(GraphicsContext gc, double x, double y, double w, double h) {
        // Background color based on completion status
        Color bgColor = Color.WHITE;
        if (fddiNode.getProgress() != null) {
            bgColor = (fddiNode.getProgress().getCompletion() == 100) ? Color.GREEN : Color.WHITE;
        }
        
        gc.setFill(bgColor);
        gc.fillRect(x, y, w, h);
        
        // Draw target date if available
        Date targetDate = fddiNode.getTargetDate();
        if (targetDate != null) {
            gc.setFill(Color.BLACK);
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
            String dateText = formatter.format(targetDate);
            
            double textHeight = getTextHeight(gc.getFont());
            CenteredTextDrawerFX.draw(gc, dateText, x, y + ((h - textHeight) / 2), w);
        }
    }
    
    /**
     * Determines the background color based on the node's status.
     */
    private Color determineColor(FDDINode fddiNode) {
        Color bgColor = Color.WHITE;
        
        if (fddiNode.getProgress() != null) {
            int completion = fddiNode.getProgress().getCompletion();
            if (completion == 100) {
                bgColor = Color.GREEN;
            } else if (completion > 0) {
                bgColor = Color.CYAN;
            }
        }
        
        // Override with red if late
        if (fddiNode.isLate()) {
            return Color.RED;
        }
        
        return bgColor;
    }
    
    /**
     * Gets the height of text for the current font.
     */
    private double getTextHeight(Font font) {
        Text text = new Text("Mg"); // Characters with ascenders and descenders
        text.setFont(font);
        return text.getBoundsInLocal().getHeight();
    }
}
