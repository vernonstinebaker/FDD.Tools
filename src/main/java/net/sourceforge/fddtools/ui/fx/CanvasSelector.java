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

import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.ui.FDDCanvasView;
import net.sourceforge.fddtools.ui.bridge.FDDCanvasBridge;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.awt.event.ComponentListener;
import java.util.logging.Logger;

/**
 * Canvas selection utility that provides a unified interface for both Swing and JavaFX canvas implementations.
 * This allows seamless switching between legacy Swing canvas and modern JavaFX canvas with panning and zooming.
 */
public class CanvasSelector {
    
    private static final Logger LOGGER = Logger.getLogger(CanvasSelector.class.getName());
    
    public enum CanvasType {
        SWING("Legacy Swing Canvas"),
        JAVAFX("Modern JavaFX Canvas with Zoom & Pan");
        
        private final String displayName;
        
        CanvasType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Creates a canvas component of the specified type.
     * 
     * @param type Canvas type to create
     * @param fddiNode Initial node to display
     * @param font Font to use for text rendering
     * @return Canvas component that can be added to Swing containers
     */
    public static JComponent createCanvas(CanvasType type, FDDINode fddiNode, Font font) {
        switch (type) {
            case SWING:
                return new FDDCanvasView(fddiNode, font);
                
            case JAVAFX:
                return new FDDCanvasBridge(fddiNode, font);
                
            default:
                LOGGER.warning("Unknown canvas type: " + type + ", falling back to Swing");
                return new FDDCanvasView(fddiNode, font);
        }
    }
    
    /**
     * Creates a unified canvas wrapper that provides common interface for both canvas types.
     */
    public static CanvasWrapper createCanvasWrapper(CanvasType type, FDDINode fddiNode, Font font) {
        return new CanvasWrapper(type, fddiNode, font);
    }
    
    /**
     * Wrapper class that provides a unified interface for both canvas types.
     */
    public static class CanvasWrapper {
        private final CanvasType type;
        private final JComponent component;
        private final FDDCanvasView swingCanvas;
        private final FDDCanvasBridge javaFXCanvas;
        
        private CanvasWrapper(CanvasType type, FDDINode fddiNode, Font font) {
            this.type = type;
            
            switch (type) {
                case SWING:
                    this.swingCanvas = new FDDCanvasView(fddiNode, font);
                    this.javaFXCanvas = null;
                    this.component = swingCanvas;
                    break;
                    
                case JAVAFX:
                    this.swingCanvas = null;
                    this.javaFXCanvas = new FDDCanvasBridge(fddiNode, font);
                    this.component = javaFXCanvas;
                    break;
                    
                default:
                    throw new IllegalArgumentException("Unknown canvas type: " + type);
            }
        }
        
        /**
         * Gets the canvas type.
         */
        public CanvasType getType() {
            return type;
        }
        
        /**
         * Gets the canvas component for adding to containers.
         */
        public JComponent getComponent() {
            return component;
        }
        
        /**
         * Checks if this is a JavaFX canvas.
         */
        public boolean isJavaFX() {
            return type == CanvasType.JAVAFX;
        }
        
        /**
         * Checks if this is a Swing canvas.
         */
        public boolean isSwing() {
            return type == CanvasType.SWING;
        }
        
        /**
         * Sets the current node (unified interface).
         */
        public void setCurrentNode(FDDINode node) {
            if (swingCanvas != null) {
                // Swing canvas updates via TreeSelectionListener
                // We can't directly set the node, but this method provides consistency
            } else if (javaFXCanvas != null) {
                javaFXCanvas.setCurrentNode(node);
            }
        }
        
        /**
         * Sets the text font (unified interface).
         */
        public void setTextFont(Font font) {
            if (swingCanvas != null) {
                swingCanvas.setTextFont(font);
            } else if (javaFXCanvas != null) {
                javaFXCanvas.setTextFont(font);
            }
        }
        
        /**
         * Triggers a repaint (unified interface).
         */
        public void repaint() {
            if (swingCanvas != null) {
                swingCanvas.repaint();
            } else if (javaFXCanvas != null) {
                javaFXCanvas.repaint();
            }
        }
        
        /**
         * Triggers a revalidation (unified interface).
         */
        public void revalidate() {
            if (swingCanvas != null) {
                swingCanvas.revalidate();
            } else if (javaFXCanvas != null) {
                javaFXCanvas.revalidate();
            }
        }
        
        /**
         * Forces a reflow (unified interface).
         */
        public void reflow() {
            if (swingCanvas != null) {
                swingCanvas.reflow();
            } else if (javaFXCanvas != null) {
                javaFXCanvas.reflow();
            }
        }
        
        /**
         * Prints the canvas image (unified interface).
         */
        public void printImage() {
            if (swingCanvas != null) {
                swingCanvas.printImage();
            } else if (javaFXCanvas != null) {
                javaFXCanvas.printImage();
            }
        }
        
        /**
         * Gets the outer scroll pane (unified interface).
         * Note: JavaFX canvas handles scrolling internally.
         */
        public JScrollPane getOuterScrollPane() {
            if (swingCanvas != null) {
                return swingCanvas.getOuterScrollPane();
            } else {
                return null; // JavaFX handles scrolling internally
            }
        }
        
        /**
         * Sets the outer scroll pane (unified interface).
         * Note: Only applicable for Swing canvas.
         */
        public void setOuterScrollPane(JScrollPane scrollPane) {
            if (swingCanvas != null) {
                swingCanvas.setOuterScrollPane(scrollPane);
            }
            // JavaFX canvas doesn't need this
        }
        
        /**
         * Adds a tree selection listener (unified interface).
         * Note: Only applicable for Swing canvas.
         */
        public void addTreeSelectionListener(TreeSelectionListener listener) {
            if (swingCanvas != null && swingCanvas instanceof TreeSelectionListener) {
                // The Swing canvas IS a TreeSelectionListener
                // We need to add the listener to the tree selection model instead
                // This is handled by the calling code
            } else if (javaFXCanvas != null && javaFXCanvas instanceof TreeSelectionListener) {
                // Same for JavaFX bridge
                // Handled by calling code
            }
        }
        
        /**
         * Gets the TreeSelectionListener if this canvas implements it.
         */
        public TreeSelectionListener getTreeSelectionListener() {
            if (swingCanvas != null) {
                return swingCanvas;
            } else if (javaFXCanvas != null) {
                return javaFXCanvas;
            }
            return null;
        }
        
        /**
         * Adds a component listener (unified interface).
         */
        public void addComponentListener(ComponentListener listener) {
            component.addComponentListener(listener);
        }
        
        /**
         * Gets the ComponentListener if this canvas implements it.
         */
        public ComponentListener getComponentListener() {
            if (swingCanvas != null) {
                return swingCanvas;
            } else if (javaFXCanvas != null) {
                return javaFXCanvas;
            }
            return null;
        }
        
        // JavaFX-specific methods (only available for JavaFX canvas)
        
        /**
         * Sets zoom level (JavaFX only).
         */
        public void setZoom(double zoom) {
            if (javaFXCanvas != null) {
                javaFXCanvas.setZoom(zoom);
            }
        }
        
        /**
         * Gets current zoom level (JavaFX only).
         */
        public double getZoom() {
            if (javaFXCanvas != null) {
                return javaFXCanvas.getZoom();
            }
            return 1.0; // Default for Swing canvas
        }
        
        /**
         * Zooms in (JavaFX only).
         */
        public void zoomIn() {
            if (javaFXCanvas != null) {
                javaFXCanvas.zoomIn();
            }
        }
        
        /**
         * Zooms out (JavaFX only).
         */
        public void zoomOut() {
            if (javaFXCanvas != null) {
                javaFXCanvas.zoomOut();
            }
        }
        
        /**
         * Resets zoom to 100% (JavaFX only).
         */
        public void resetZoom() {
            if (javaFXCanvas != null) {
                javaFXCanvas.resetZoom();
            }
        }
        
        /**
         * Fits content to window (JavaFX only).
         */
        public void fitToWindow() {
            if (javaFXCanvas != null) {
                javaFXCanvas.fitToWindow();
            }
        }
        
        /**
         * Checks if zoom controls are available.
         */
        public boolean hasZoomControls() {
            return javaFXCanvas != null;
        }
    }
}
