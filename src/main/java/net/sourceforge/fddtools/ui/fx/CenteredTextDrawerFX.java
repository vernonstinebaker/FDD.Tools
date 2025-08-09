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

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * JavaFX implementation of centered text drawing functionality.
 * This is the modern replacement for the Swing-based CenteredTextDrawer.
 */
public final class CenteredTextDrawerFX {
    
    private CenteredTextDrawerFX() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Draws centered text within the specified bounds, wrapping as needed.
     * 
     * @param gc Graphics context to draw on
     * @param text Text to draw
     * @param x Left bound
     * @param y Top bound
     * @param width Available width
     * @return Height of the drawn text
     */
    public static double draw(GraphicsContext gc, String text, double x, double y, double width) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        
        Font font = gc.getFont();
        List<String> lines = wrapText(text, font, width);
        
        double lineHeight = getTextHeight(font);
        double totalHeight = lines.size() * lineHeight;
        double currentY = y;
        
        // Save current graphics state
        javafx.scene.paint.Paint originalFill = gc.getFill();
        
        try {
            // Set high-contrast black for text visibility
            gc.setFill(Color.BLACK);
            
            for (String line : lines) {
                double centeredX = computeCenteredX(line, font, x, width);
                // Use half-pixel vertical snap for sharper baseline at fractional scales
                double baselineY = currentY + lineHeight * 0.8; // adjust baseline factor slightly
                double pixelAlignedY = snapHalf(baselineY);
                gc.fillText(line, centeredX, pixelAlignedY);
                currentY += lineHeight;
            }
        } finally {
            // Restore original fill
            gc.setFill(originalFill);
        }
        
        return totalHeight;
    }
    
    /**
     * Calculates the height required for the given text when wrapped to the specified width.
     * 
     * @param gc Graphics context (for font information)
     * @param text Text to measure
     * @param maxWidth Maximum width available
     * @return Required height
     */
    public static double getTitleTextHeight(GraphicsContext gc, String text, double maxWidth) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        
        Font font = gc.getFont();
        List<String> lines = wrapText(text, font, maxWidth);
        return lines.size() * getTextHeight(font);
    }
    
    /**
     * Wraps text to fit within the specified width.
     * 
     * @param text Text to wrap
     * @param font Font to use for measurements
     * @param maxWidth Maximum width
     * @return List of wrapped lines
     */
    private static List<String> wrapText(String text, Font font, double maxWidth) {
        List<String> lines = new ArrayList<>();
        
        if (text == null || text.trim().isEmpty()) {
            return lines;
        }
        
        // Split text into words
        String[] words = text.trim().split("\\s+");
        
        if (words.length == 0) {
            return lines;
        }
        
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            String testLine = currentLine.length() > 0 ? 
                            currentLine + " " + word : word;
            
            double testWidth = getTextWidth(testLine, font);
            
            if (testWidth <= maxWidth || currentLine.length() == 0) {
                // Word fits or it's the first word (even if it doesn't fit)
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                // Word doesn't fit, start new line
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                }
            }
        }
        
        // Add the last line if it has content
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines;
    }
    
    /**
     * Gets the width of text when rendered with the specified font.
     * 
     * @param text Text to measure
     * @param font Font to use
     * @return Text width
     */
    private static double getTextWidth(String text, Font font) {
        // Create a temporary Text node to measure text width
        Text textNode = new Text(text);
        textNode.setFont(font);
        return textNode.getBoundsInLocal().getWidth();
    }

    /** Compute horizontally centered X coordinate (package-private for testing). */
    static double computeCenteredX(String line, Font font, double x, double width) {
        double textWidth = getTextWidth(line, font);
        double raw = x + (width - textWidth) / 2.0;
        return snapHalf(raw);
    }

    private static double snapHalf(double value) { return Math.round(value * 2.0) / 2.0; }
    
    /**
     * Gets the height of text when rendered with the specified font.
     * 
     * @param font Font to use
     * @return Text height
     */
    private static double getTextHeight(Font font) {
        // Create a temporary Text node to measure text height
        Text textNode = new Text("Mg"); // Use characters with ascenders and descenders
        textNode.setFont(font);
        return textNode.getBoundsInLocal().getHeight();
    }
    
    /**
     * Draws centered text with automatic color handling for contrast.
     * 
     * @param gc Graphics context to draw on
     * @param text Text to draw
     * @param x Left bound
     * @param y Top bound
     * @param width Available width
     * @param backgroundColor Background color for contrast calculation
     * @return Height of the drawn text
     */
    public static double drawWithContrast(GraphicsContext gc, String text, double x, double y, 
                                        double width, Color backgroundColor) {
        // Save current fill color
        javafx.scene.paint.Paint originalFill = gc.getFill();
        
        // Calculate contrasting text color
        Color textColor = getContrastingColor(backgroundColor);
        gc.setFill(textColor);
        
        // Draw the text
        double height = draw(gc, text, x, y, width);
        
        // Restore original fill color
        gc.setFill(originalFill);
        
        return height;
    }
    
    /**
     * Calculates a contrasting color for the given background color.
     * 
     * @param backgroundColor Background color
     * @return Contrasting text color (black or white)
     */
    private static Color getContrastingColor(Color backgroundColor) {
        if (backgroundColor == null) {
            return Color.BLACK;
        }
        
        // Calculate luminance using standard formula
        double luminance = 0.299 * backgroundColor.getRed() + 
                          0.587 * backgroundColor.getGreen() + 
                          0.114 * backgroundColor.getBlue();
        
        // Return black for light backgrounds, white for dark backgrounds
        return luminance > 0.5 ? Color.BLACK : Color.WHITE;
    }
    
    /**
     * Draws text with a border/outline effect for better visibility.
     * 
     * @param gc Graphics context to draw on
     * @param text Text to draw
     * @param x Left bound
     * @param y Top bound
     * @param width Available width
     * @param fillColor Fill color for the text
     * @param strokeColor Stroke color for the border
     * @return Height of the drawn text
     */
    public static double drawWithBorder(GraphicsContext gc, String text, double x, double y, 
                                      double width, Color fillColor, Color strokeColor) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        
        Font font = gc.getFont();
        List<String> lines = wrapText(text, font, width);
        
        double lineHeight = getTextHeight(font);
        double totalHeight = lines.size() * lineHeight;
        double currentY = y;
        
        // Save current state
        javafx.scene.paint.Paint originalFill = gc.getFill();
        javafx.scene.paint.Paint originalStroke = gc.getStroke();
        double originalLineWidth = gc.getLineWidth();
        
        try {
            for (String line : lines) {
                double textWidth = getTextWidth(line, font);
                double centeredX = x + (width - textWidth) / 2;
                double textY = currentY + lineHeight * 0.75;
                
                // Draw stroke (border)
                if (strokeColor != null) {
                    gc.setStroke(strokeColor);
                    gc.setLineWidth(1);
                    gc.strokeText(line, centeredX, textY);
                }
                
                // Draw fill
                if (fillColor != null) {
                    gc.setFill(fillColor);
                    gc.fillText(line, centeredX, textY);
                }
                
                currentY += lineHeight;
            }
        } finally {
            // Restore original state
            gc.setFill(originalFill);
            gc.setStroke(originalStroke);
            gc.setLineWidth(originalLineWidth);
        }
        
        return totalHeight;
    }
}
