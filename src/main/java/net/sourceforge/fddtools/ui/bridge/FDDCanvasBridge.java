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

package net.sourceforge.fddtools.ui.bridge;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.ui.fx.FDDCanvasFX;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bridge component that embeds JavaFX Canvas in Swing interface.
 * Provides seamless integration with existing Swing-based FDD application.
 */
public class FDDCanvasBridge extends JPanel implements TreeSelectionListener, ComponentListener {
    
    private static final Logger LOGGER = Logger.getLogger(FDDCanvasBridge.class.getName());
    
    private final JFXPanel jfxPanel;
    private FDDCanvasFX fddCanvasFX;
    private FDDINode currentNode;
    private Font textFont;
    
    /**
     * Creates a new FDD Canvas Bridge with the specified node and font.
     */
    public FDDCanvasBridge(FDDINode fddiNode, java.awt.Font swingFont) {
        super(new BorderLayout());
        
        this.currentNode = fddiNode;
        this.textFont = convertSwingFontToJavaFX(swingFont);
        
        // Initialize JavaFX if not already done
        SwingFXBridge.initializeJavaFX();
        
        // Create JavaFX panel
        this.jfxPanel = new JFXPanel();
        add(jfxPanel, BorderLayout.CENTER);
        
        // Initialize JavaFX content on JavaFX Application Thread
        Platform.runLater(this::initializeFXContent);
        
        // Add component listener for resize handling
        addComponentListener(this);
    }
    
    /**
     * Initializes the JavaFX content. Must be called on JavaFX Application Thread.
     */
    private void initializeFXContent() {
        try {
            // Create JavaFX canvas
            fddCanvasFX = new FDDCanvasFX(currentNode, textFont);
            
            // Create scene
            Scene scene = new Scene(fddCanvasFX, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/styles/fdd-canvas.css") != null ?
                                     getClass().getResource("/styles/fdd-canvas.css").toExternalForm() :
                                     "");
            
            // Set scene to panel
            jfxPanel.setScene(scene);
            
            LOGGER.info("JavaFX Canvas initialized successfully");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize JavaFX canvas", e);
            
            // Fallback to error display
            Platform.runLater(() -> {
                Label errorLabel = new Label("Failed to initialize JavaFX Canvas: " + e.getMessage());
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px; -fx-padding: 20;");
                Scene errorScene = new Scene(errorLabel, 400, 200);
                jfxPanel.setScene(errorScene);
            });
        }
    }
    
    /**
     * Converts Swing Font to JavaFX Font.
     */
    private Font convertSwingFontToJavaFX(java.awt.Font swingFont) {
        if (swingFont == null) {
            return Font.font("Arial", 12);
        }
        
        String family = swingFont.getFamily();
        double size = swingFont.getSize();
        
        // Convert font style
        javafx.scene.text.FontWeight weight = swingFont.isBold() ? 
                                             javafx.scene.text.FontWeight.BOLD : 
                                             javafx.scene.text.FontWeight.NORMAL;
        javafx.scene.text.FontPosture posture = swingFont.isItalic() ? 
                                                javafx.scene.text.FontPosture.ITALIC : 
                                                javafx.scene.text.FontPosture.REGULAR;
        
        return Font.font(family, weight, posture, size);
    }
    
    /**
     * Updates the current node and refreshes the canvas.
     */
    public void setCurrentNode(FDDINode node) {
        System.out.println("DEBUG: FDDCanvasBridge.setCurrentNode() called with: " + (node != null ? node.getName() : "null"));
        this.currentNode = node;
        
        if (fddCanvasFX != null) {
            Platform.runLater(() -> {
                System.out.println("DEBUG: Updating JavaFX canvas with new node: " + (node != null ? node.getName() : "null"));
                fddCanvasFX.setCurrentNode(node);
            });
        } else {
            System.out.println("DEBUG: fddCanvasFX is null, cannot update");
        }
    }
    
    /**
     * Gets the current node.
     */
    public FDDINode getCurrentNode() {
        return currentNode;
    }
    
    /**
     * Sets the text font and updates the canvas.
     */
    public void setTextFont(java.awt.Font swingFont) {
        this.textFont = convertSwingFontToJavaFX(swingFont);
        
        if (fddCanvasFX != null) {
            Platform.runLater(() -> fddCanvasFX.setTextFont(textFont));
        }
    }
    
    /**
     * Gets the current text font as Swing font.
     */
    public java.awt.Font getTextFont() {
        if (textFont == null) {
            return new java.awt.Font("Arial", java.awt.Font.PLAIN, 12);
        }
        
        String family = textFont.getFamily();
        int size = (int) textFont.getSize();
        int style = java.awt.Font.PLAIN;
        
        // Note: JavaFX font style conversion is limited
        // We could enhance this if needed
        
        return new java.awt.Font(family, style, size);
    }
    
    /**
     * Forces a reflow of the canvas layout.
     */
    public void reflow() {
        if (fddCanvasFX != null) {
            Platform.runLater(() -> fddCanvasFX.reflow());
        }
    }
    
    /**
     * Revalidates and repaints the canvas.
     */
    @Override
    public void revalidate() {
        super.revalidate();
        reflow();
    }
    
    /**
     * Repaints the canvas.
     */
    @Override
    public void repaint() {
        super.repaint();
        if (fddCanvasFX != null) {
            Platform.runLater(() -> fddCanvasFX.redraw());
        }
    }
    
    /**
     * Prints the canvas image.
     */
    public void printImage() {
        // This would be called from Swing, so we delegate to JavaFX
        if (fddCanvasFX != null) {
            Platform.runLater(() -> {
                // The JavaFX canvas has its own print method
                // For now, show a message that it's not implemented
                // TODO: Implement printing
            });
        }
    }
    
    /**
     * Gets the outer scroll pane (compatibility method).
     */
    public JScrollPane getOuterScrollPane() {
        // Not applicable for JavaFX implementation
        // Return null to indicate this is handled internally
        return null;
    }
    
    /**
     * Sets the outer scroll pane (compatibility method).
     */
    public void setOuterScrollPane(JScrollPane scrollPane) {
        // Not applicable for JavaFX implementation
        // JavaFX canvas handles scrolling internally
    }
    
    // TreeSelectionListener implementation
    
    @Override
    public void valueChanged(TreeSelectionEvent e) {
        System.out.println("DEBUG: FDDCanvasBridge.valueChanged() called");
        if (e.getPath() != null && e.getPath().getLastPathComponent() instanceof FDDINode) {
            FDDINode selectedNode = (FDDINode) e.getPath().getLastPathComponent();
            System.out.println("DEBUG: Selected node: " + selectedNode.getName());
            setCurrentNode(selectedNode);
        } else {
            System.out.println("DEBUG: No valid node selected");
        }
    }
    
    // ComponentListener implementation
    
    @Override
    public void componentResized(ComponentEvent e) {
        reflow();
    }
    
    @Override
    public void componentMoved(ComponentEvent e) {
        // No action needed
    }
    
    @Override
    public void componentShown(ComponentEvent e) {
        // No action needed
    }
    
    @Override
    public void componentHidden(ComponentEvent e) {
        // No action needed
    }
    
    /**
     * Gets the JavaFX canvas component (for advanced usage).
     */
    public FDDCanvasFX getFDDCanvasFX() {
        return fddCanvasFX;
    }
    
    /**
     * Checks if JavaFX canvas is ready.
     */
    public boolean isCanvasReady() {
        return fddCanvasFX != null;
    }
    
    /**
     * Sets zoom level on the canvas.
     */
    public void setZoom(double zoom) {
        if (fddCanvasFX != null) {
            Platform.runLater(() -> fddCanvasFX.setZoom(zoom));
        }
    }
    
    /**
     * Gets current zoom level.
     */
    public double getZoom() {
        return fddCanvasFX != null ? fddCanvasFX.getZoom() : 1.0;
    }
    
    /**
     * Zooms in.
     */
    public void zoomIn() {
        if (fddCanvasFX != null) {
            Platform.runLater(() -> fddCanvasFX.zoomIn());
        }
    }
    
    /**
     * Zooms out.
     */
    public void zoomOut() {
        if (fddCanvasFX != null) {
            Platform.runLater(() -> fddCanvasFX.zoomOut());
        }
    }
    
    /**
     * Resets zoom to 100%.
     */
    public void resetZoom() {
        if (fddCanvasFX != null) {
            Platform.runLater(() -> fddCanvasFX.resetZoom());
        }
    }
    
    /**
     * Fits content to window.
     */
    public void fitToWindow() {
        if (fddCanvasFX != null) {
            Platform.runLater(() -> fddCanvasFX.fitToWindow());
        }
    }
}
