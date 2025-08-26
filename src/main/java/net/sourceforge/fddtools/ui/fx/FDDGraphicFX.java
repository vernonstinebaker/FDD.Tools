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
        draw(gc, 1.0); // Default zoom level
    }
    
    /**
     * Draws the FDD graphic on the specified graphics context with zoom information for crisp text rendering.
     */
    public void draw(GraphicsContext gc, double zoomLevel) {
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
            
            // Optimize font size for the graphic dimensions, considering zoom level
            // Use the overall graphic dimensions to ensure consistent sizing across all sections
            Font optimizedFont = getOptimizedFont(gc.getFont(), width, height, zoomLevel);
            gc.setFont(optimizedFont);
            
            // Store the optimized font to pass to all drawing methods for consistency
            Font consistentFont = optimizedFont;
            
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
            
            // Draw main rectangle with pixel alignment and ensure visibility
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1.0);
            gc.strokeRect(Math.round(originX), Math.round(boxOriginY), 
                         Math.round(width), Math.round(boxHeight));
            
            // Draw dividing lines with pixel alignment and consistent thickness
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1.0);
            gc.strokeLine(Math.round(originX + 1), Math.round(boxOriginY + upperLineY), 
                         Math.round((originX + width) - 1), Math.round(boxOriginY + upperLineY));
            gc.strokeLine(Math.round(originX + 1), Math.round(boxOriginY + lowerLineY), 
                         Math.round((originX + width) - 1), Math.round(boxOriginY + lowerLineY));
            
            // Draw the three sections with full coordinates for complete background fills
            // Pass the consistent font to all sections to ensure identical font sizing
            drawUpperBox(gc, originX, boxOriginY, width, upperBoxHeight + 1, zoomLevel, consistentFont);
            drawMiddleBox(gc, originX, boxOriginY + upperLineY, width, middleBoxHeight + 1, zoomLevel, consistentFont);
            drawLowerBox(gc, originX, boxOriginY + lowerLineY, width, lowerBoxHeight + 1, zoomLevel, consistentFont);
            
            // Ensure borders are always visible by redrawing them after background fills
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1.0);
            gc.strokeRect(Math.round(originX), Math.round(boxOriginY), 
                         Math.round(width), Math.round(boxHeight));
            gc.strokeLine(Math.round(originX + 1), Math.round(boxOriginY + upperLineY), 
                         Math.round((originX + width) - 1), Math.round(boxOriginY + upperLineY));
            gc.strokeLine(Math.round(originX + 1), Math.round(boxOriginY + lowerLineY), 
                         Math.round((originX + width) - 1), Math.round(boxOriginY + lowerLineY));
            
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
    private void drawUpperBox(GraphicsContext gc, double x, double y, double w, double h, double zoomLevel, Font consistentFont) {
        // Fill background with status color - completely fill the box area to the edges
        Color bgColor = determineColor(fddiNode);
        gc.setFill(bgColor);
        gc.fillRect(x, y, w, h);
        
        // Ensure we use the consistent font (no modifications allowed)
        gc.setFont(consistentFont);
        
        // Draw text in contrasting color
        gc.setFill(Color.BLACK);
        
        // Calculate text layout with proper padding from the border edges
        double textMargin = h / 20;
        double horizontalPadding = w / 20; // Add horizontal padding (5% on each side)
        
        // Text positioning uses border-inset coordinates to avoid overlap with borders
        double textX = x + 1 + horizontalPadding; // Start inside border + padding
        double textY = y + 1 + textMargin; // Start inside border + margin
        double textWidth = w - 2 - (2 * horizontalPadding); // Account for border and padding
        
        // Draw name in upper portion with horizontal padding
        double nameHeight = CenteredTextDrawerFX.draw(gc, fddiNode.getName(), 
                                                     textX, textY, textWidth);
        
        // Draw children count if applicable - with proper spacing and padding
        if (!fddiNode.getChildren().isEmpty()) {
            String childText = "(" + fddiNode.getChildren().size() + ")";
            double countY = textY + nameHeight + (textMargin / 2);
            
            // Draw the count with adequate spacing from the name and horizontal padding
            CenteredTextDrawerFX.draw(gc, childText, textX, countY, textWidth);
        }
        
        // Note: Completion percentage is now only displayed in the progress bar (middle box)
    }
    
    /**
     * Draws the middle box showing progress bar.
     */
    private void drawMiddleBox(GraphicsContext gc, double x, double y, double w, double h, double zoomLevel, Font consistentFont) {
        int completion = fddiNode.getProgress().getCompletion();
        double percent = (w * completion) / 100.0;
        
        // Fill background first - complete fill to edges
        gc.setFill(Color.WHITE);
        gc.fillRect(x, y, w, h);
        
        if (percent > 0) {
            // Draw completed portion (green bar) - fill to edges
            gc.setFill(Color.GREEN);
            gc.fillRect(x, y, percent, h);
            
            // NO BLACK DIVIDING LINE - removed the unwanted black bar
        }
        
        // Draw progress text OVER the bar for visibility
        String progressText = completion + "%";
        
        // Use the exact same consistent font - NO modifications or shrinking allowed
        gc.setFont(consistentFont);
        
        // Get precise text measurements for perfect centering
        Text tempText = new Text(progressText);
        tempText.setFont(consistentFont);
        double textWidth = tempText.getBoundsInLocal().getWidth();
        
        // Calculate perfect horizontal centering with generous padding for clipping prevention
        double safePadding = Math.max(8.0, w * 0.08); // Generous 8px minimum or 8% padding
        double availableWidth = w - (2 * safePadding);
        
        // Center text within the available safe area
        double centerX = x + safePadding + (availableWidth / 2);
        double textX = centerX - (textWidth / 2);
        
        // Ensure text doesn't go outside safe bounds (clipping prevention)
        double minX = x + safePadding;
        double maxX = x + w - safePadding - textWidth;
        textX = Math.max(minX, Math.min(textX, maxX));
        
        // Proper vertical centering for fillText() baseline positioning
        // In JavaFX, fillText() positions text by its baseline, not top-left corner
        // We need to account for the text's baseline offset for perfect centering
        double textBaseline = tempText.getBaselineOffset();
        double centerY = y + (h / 2);
        double textY = centerY + (textBaseline / 2); // Position baseline at center
        
        // Draw the percentage text with black color for visibility
        gc.setFill(Color.BLACK);
        gc.fillText(progressText, textX, textY);
    }
    
    /**
     * Draws the lower box containing target date.
     */
    private void drawLowerBox(GraphicsContext gc, double x, double y, double w, double h, double zoomLevel, Font consistentFont) {
        // Background color based on completion status
        Color bgColor = Color.WHITE;
        if (fddiNode.getProgress() != null) {
            bgColor = (fddiNode.getProgress().getCompletion() == 100) ? Color.GREEN : Color.WHITE;
        }
        
        // Fill background completely to edges
        gc.setFill(bgColor);
        gc.fillRect(x, y, w, h);
        
        // Ensure we use the consistent font (no modifications allowed)
        gc.setFont(consistentFont);
        
        // Draw target date if available with border consideration
        Date targetDate = fddiNode.getTargetDate();
        if (targetDate != null) {
            gc.setFill(Color.BLACK);
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
            String dateText = formatter.format(targetDate);
            
            double textHeight = getTextHeight(consistentFont);
            // Position text within border area
            CenteredTextDrawerFX.draw(gc, dateText, x + 1, y + ((h - textHeight) / 2), w - 2);
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
     * Gets an optimized font size for the given dimensions, considering zoom level for crisp rendering.
     * Uses a space-constrained approach that prevents overflow at high zoom levels.
     */
    private Font getOptimizedFont(Font baseFont, double availableWidth, double availableHeight, double zoomLevel) {
        // Calculate optimal font size based on available space
        double baseFontSize = baseFont.getSize();
        double widthBasedSize = availableWidth / 8; // Approximate character width ratio
        double heightBasedSize = availableHeight / 12; // Approximate line height ratio
        
        // Use the smaller of the two to ensure text fits
        double optimalSize = Math.min(widthBasedSize, heightBasedSize);
        
        // Clamp to reasonable bounds
        optimalSize = Math.max(8, Math.min(optimalSize, baseFontSize * 1.2));
        
        // Smart zoom scaling: provide readability improvement without overflow
        // Use logarithmic scaling to prevent excessive growth at high zoom levels
        // This gives moderate improvement while respecting space constraints
        double zoomFactor = 1.0 + (Math.log(Math.max(1.0, zoomLevel)) * 0.15); // Much more conservative
        zoomFactor = Math.min(zoomFactor, 1.4); // Cap at 40% increase maximum
        
        // Apply zoom factor but ensure we still respect space constraints
        double zoomAdjustedSize = optimalSize * zoomFactor;
        
        // Final safety check: never exceed space-based optimal size by more than 20%
        // This ensures text always fits regardless of zoom level
        double finalSize = Math.min(zoomAdjustedSize, optimalSize * 1.2);
        
        // Return font with safe size that won't overflow
        return Font.font(baseFont.getFamily(), 
                        javafx.scene.text.FontWeight.NORMAL, 
                        javafx.scene.text.FontPosture.REGULAR, 
                        finalSize);
    }
}
