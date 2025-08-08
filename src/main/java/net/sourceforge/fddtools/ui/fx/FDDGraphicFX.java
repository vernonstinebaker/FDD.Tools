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
            
            // Optimize font size for the graphic dimensions
            Font optimizedFont = getOptimizedFont(gc.getFont(), width, height);
            gc.setFont(optimizedFont);
            
            double ownerNameHeight = 0;
            // Always reserve a fixed band for feature owner initials so all feature boxes are consistent height
            if (fddiNode instanceof Feature) {
                Font font = gc.getFont();
                // Measure a representative sample to establish reserved height (even if no initials)
                Text measure = new Text("WW"); // wide characters for height/ascender baseline
                measure.setFont(font);
                ownerNameHeight = measure.getBoundsInLocal().getHeight() + (height / 32);

                String owner = ((Feature) fddiNode).getInitials();
                if (owner != null && !owner.trim().isEmpty()) {
                    Text ownerText = new Text(owner);
                    ownerText.setFont(font);
                    double ownerWidth = ownerText.getBoundsInLocal().getWidth();
                    // Right-align with a small padding so it does not touch the border
                    double padding = 2.0;
                    double textX = Math.round((originX + width) - ownerWidth - padding);
                    double textY = Math.round(originY + ownerNameHeight * 0.75);
                    gc.fillText(owner, textX, textY);
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
            
            // Draw main rectangle with pixel alignment
            gc.strokeRect(Math.round(originX), Math.round(boxOriginY), 
                         Math.round(width), Math.round(boxHeight));
            
            // Draw dividing lines with pixel alignment
            gc.strokeLine(Math.round(originX + 1), Math.round(boxOriginY + upperLineY), 
                         Math.round((originX + width) - 1), Math.round(boxOriginY + upperLineY));
            gc.strokeLine(Math.round(originX + 1), Math.round(boxOriginY + lowerLineY), 
                         Math.round((originX + width) - 1), Math.round(boxOriginY + lowerLineY));
            
            // Draw the three sections. Use width-2 so interior content has symmetric 1px inset on both sides
            drawUpperBox(gc, originX + 1, boxOriginY + 1, width - 2, upperBoxHeight);
            drawMiddleBox(gc, originX + 1, boxOriginY + upperLineY + 1, width - 2, middleBoxHeight);
            drawLowerBox(gc, originX + 1, boxOriginY + lowerLineY + 1, width - 2, lowerBoxHeight);
            
        } finally {
            // Restore original state
            gc.setFont(originalFont);
            gc.setFill(originalFill);
            gc.setStroke(originalStroke);
            gc.setLineWidth(originalLineWidth);
        }
    }
    
    /**
     * Draws the upper box containing name and child count only.
     */
    private void drawUpperBox(GraphicsContext gc, double x, double y, double w, double h) {
        // Fill background with status color
        Color bgColor = determineColor(fddiNode);
        gc.setFill(bgColor);
        gc.fillRect(x, y, w, h);
        
        // Draw text in contrasting color
        gc.setFill(Color.BLACK);
        
        // Calculate text layout for proper spacing
        double textMargin = h / 20;
        
        // Draw name in upper portion
        double nameHeight = CenteredTextDrawerFX.draw(gc, fddiNode.getName(), 
                                                     x, y + textMargin, w);
        
        // Draw children count if applicable - with proper spacing
        if (!fddiNode.getChildren().isEmpty()) {
            String childText = "(" + fddiNode.getChildren().size() + ")";
            double countY = y + textMargin + nameHeight + (textMargin / 2);
            
            // Draw the count with adequate spacing from the name
            CenteredTextDrawerFX.draw(gc, childText, x, countY, w);
        }
        
        // Note: Completion percentage is now only displayed in the progress bar (middle box)
    }
    
    /**
     * Draws the middle box showing progress bar.
     */
    private void drawMiddleBox(GraphicsContext gc, double x, double y, double w, double h) {
        int completion = fddiNode.getProgress().getCompletion();
        double percent = (w * completion) / 100.0;
        
        // Fill background first
        gc.setFill(Color.WHITE);
        gc.fillRect(x, y, w, h);
        
        if (percent > 0) {
            // Draw completed portion (green bar)
            gc.setFill(Color.GREEN);
            gc.fillRect(x, y, percent, h);
            
            // Draw dividing line if there's remaining space
            if (percent < w) {
                gc.setStroke(Color.BLACK);
                gc.strokeLine(x + percent, y, x + percent, y + h);
            }
        }
        
        // Draw progress text OVER the bar for visibility
        String progressText = completion + "%";
        Font originalFont = gc.getFont();
        
        try {
            // Use bold font for better visibility and clarity
            Font progressFont = Font.font(originalFont.getFamily(), 
                                        javafx.scene.text.FontWeight.BOLD,
                                        javafx.scene.text.FontPosture.REGULAR,
                                        Math.max(8, originalFont.getSize() * 0.8));
            gc.setFont(progressFont);
            
            // Calculate text positioning
            Text textNode = new Text(progressText);
            textNode.setFont(progressFont);
            double textWidth = textNode.getBoundsInLocal().getWidth();
            double textHeight = textNode.getBoundsInLocal().getHeight();
            
            double textX = x + (w - textWidth) / 2;
            double textY = y + (h + textHeight) / 2;
            
            // Smart contrast: determine if text center is over the green progress bar
            double textCenterX = textX + textWidth / 2;
            boolean textOverProgressBar = (textCenterX - x) <= percent;
            
            // Use appropriate contrast color based on what's underneath the text
            if (textOverProgressBar) {
                // Text is over green progress bar - use white for contrast
                gc.setFill(Color.WHITE);
            } else {
                // Text is over white background - use black for contrast
                gc.setFill(Color.BLACK);
            }
            
            // Use pixel-aligned coordinates for crisp rendering
            gc.fillText(progressText, Math.round(textX), Math.round(textY));
            
        } finally {
            gc.setFont(originalFont);
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
    
    /**
     * Gets an optimized font size for the given dimensions.
     */
    private Font getOptimizedFont(Font baseFont, double availableWidth, double availableHeight) {
        // Calculate optimal font size based on available space
        double baseFontSize = baseFont.getSize();
        double widthBasedSize = availableWidth / 8; // Approximate character width ratio
        double heightBasedSize = availableHeight / 12; // Approximate line height ratio
        
        // Use the smaller of the two to ensure text fits
        double optimalSize = Math.min(widthBasedSize, heightBasedSize);
        
        // Clamp to reasonable bounds
        optimalSize = Math.max(8, Math.min(optimalSize, baseFontSize * 1.2));
        
        // Return font with optimal size, preserving family and weight
        return Font.font(baseFont.getFamily(), 
                        javafx.scene.text.FontWeight.NORMAL, 
                        javafx.scene.text.FontPosture.REGULAR, 
                        optimalSize);
    }
}
