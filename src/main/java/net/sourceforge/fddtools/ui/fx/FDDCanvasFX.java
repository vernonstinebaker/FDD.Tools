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

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import net.sourceforge.fddtools.model.FDDINode;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaFX implementation of FDD Canvas with modern panning and zooming capabilities.
 * This is the modern replacement for the Swing-based FDDCanvasView.
 */
public class FDDCanvasFX extends BorderPane {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FDDCanvasFX.class);
    
    // Canvas layout constants
    private static final int FRINGE_WIDTH = 20;
    private static final int FEATURE_ELEMENT_WIDTH = 100;
    private static final int FEATURE_ELEMENT_HEIGHT = 140;
    private static final int BORDER_WIDTH = 5;
    private static final int EXTRA_WIDTH = 5;
    
    // Zoom constants
    private static final double MIN_ZOOM = 0.1;
    private static final double MAX_ZOOM = 5.0;
    private static final double ZOOM_FACTOR = 1.1;
    
    // Components
    private final Canvas canvas;
    private final ScrollPane scrollPane;
    private final VBox controlPanel;
    private final Label zoomLabel;
    private final ProgressBar zoomIndicator;
    
    // State
    private FDDINode currentNode;
    private Font textFont;
    private double zoomLevel = 1.0;
    private double canvasWidth = 800;
    private double canvasHeight = 600;
    private int elementsInRow = 1;
    
    // Dragging for panning
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean isDragging = false;
    
    /**
     * Creates a new JavaFX FDD Canvas with the specified node and font.
     */
    public FDDCanvasFX(FDDINode fddiNode, Font font) {
        this.currentNode = fddiNode;
        
        // Use a modern, crisp font that renders well at various zoom levels
        if (font != null) {
            // Convert Swing font to JavaFX font with improved rendering
            this.textFont = createOptimalFont(font.getFamily(), font.getSize());
        } else {
            // Default to a high-quality system font
            this.textFont = createOptimalFont(null, 12);
        }
        
        // Initialize canvas
        this.canvas = new Canvas();
        this.canvas.setWidth(canvasWidth);
        this.canvas.setHeight(canvasHeight);
        
        // Initialize zoom controls BEFORE creating control panel
        this.zoomLabel = new Label("100%");
        this.zoomIndicator = new ProgressBar(zoomLevel / MAX_ZOOM);
        
        // Initialize scroll pane with panning support
        this.scrollPane = createScrollPane();
        
        // Initialize control panel (now that zoom controls exist)
        this.controlPanel = createControlPanel();
        
        setupLayout();
        setupEventHandlers();
        
        // Initial draw
        Platform.runLater(this::redraw);
    }
    
    /**
     * Creates an optimal font for canvas rendering with good zoom scaling.
     */
    private Font createOptimalFont(String fontFamily, double size) {
        // List of preferred fonts optimized for crisp rendering
        // Prioritize fonts with good hinting and pixel alignment
        String[] preferredFonts = {
            fontFamily,                    // Use requested font if available
            "SF Pro Text",                // macOS - excellent for small text
            "Segoe UI",                   // Windows - optimized for UI text  
            "Roboto",                     // Android/Chrome - excellent rendering
            "Source Sans Pro",            // Adobe - designed for UI
            "Liberation Sans",            // Linux - good fallback
            "DejaVu Sans",               // Cross-platform with good hinting
            "Helvetica Neue",            // Classic fallback
            "Arial",                     // Universal fallback
            "SansSerif"                  // Final system fallback
        };
        
        for (String family : preferredFonts) {
            if (family != null && !family.trim().isEmpty()) {
                try {
                    // Use semi-bold weight for better clarity at various zoom levels
                    Font testFont = Font.font(family, javafx.scene.text.FontWeight.SEMI_BOLD, 
                                            javafx.scene.text.FontPosture.REGULAR, size);
                    if (testFont != null && isReadableFont(testFont)) {
                        LOGGER.info("Selected optimal font for canvas: {} (Semi-Bold)", testFont.getFamily());
                        return testFont;
                    }
                } catch (Exception e) {
                    // Continue to next font
                }
            }
        }
        
        // Final fallback with semi-bold weight for better visibility
        Font fallbackFont = Font.font("Arial", javafx.scene.text.FontWeight.SEMI_BOLD, 
                                     javafx.scene.text.FontPosture.REGULAR, size);
    LOGGER.info("Using fallback font for canvas: {} (Semi-Bold)", fallbackFont.getFamily());
        return fallbackFont;
    }
    
    /**
     * Checks if a font is suitable for readable text (not decorative).
     */
    private boolean isReadableFont(Font font) {
        String family = font.getFamily().toLowerCase();
        
        // Exclude decorative fonts that are not suitable for UI text
        String[] decorativeFonts = {
            "impact", "comic", "papyrus", "brush", "script", "handwriting",
            "stencil", "chalkduster", "marker", "bradley", "herculanum"
        };
        
        for (String decorative : decorativeFonts) {
            if (family.contains(decorative)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Creates the scroll pane with proper configuration for panning.
     */
    private ScrollPane createScrollPane() {
        ScrollPane sp = new ScrollPane();
        sp.setContent(canvas);
        sp.setFitToWidth(false);
        sp.setFitToHeight(false);
        sp.setPannable(true); // Enable panning with mouse drag
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        // Style the scroll pane
        sp.setStyle("-fx-background: white; -fx-border-color: #ccc;");
        
        return sp;
    }
    
    /**
     * Creates the control panel with zoom controls.
     */
    private VBox createControlPanel() {
        VBox panel = new VBox(5);
        panel.setAlignment(Pos.CENTER);
        panel.setPrefWidth(180); // Increased from 120 to 180 for better usability
        panel.setMinWidth(160);
        panel.setMaxWidth(200);
        panel.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-border-color: #ccc;");
        
        // Zoom controls
        Label zoomTitle = new Label("Zoom");
        zoomTitle.setStyle("-fx-font-weight: bold;");
        
        Button zoomInBtn = new Button("➕ Zoom In");
        zoomInBtn.setOnAction(e -> zoomIn());
        zoomInBtn.setPrefWidth(140); // Increased button width
        
        Button zoomOutBtn = new Button("➖ Zoom Out");
        zoomOutBtn.setOnAction(e -> zoomOut());
        zoomOutBtn.setPrefWidth(140);
        
        Button resetZoomBtn = new Button("🔄 Reset");
        resetZoomBtn.setOnAction(e -> resetZoom());
        resetZoomBtn.setPrefWidth(140);
        
        Button fitToWindowBtn = new Button("📐 Fit to Window");
        fitToWindowBtn.setOnAction(e -> fitToWindow());
        fitToWindowBtn.setPrefWidth(140);
        
        // Separator
        Separator separator = new Separator();
        
        // Canvas tools
        Label toolsTitle = new Label("Tools");
        toolsTitle.setStyle("-fx-font-weight: bold;");
        
        Button saveBtn = new Button("💾 Save Image");
        saveBtn.setOnAction(e -> saveImage());
        saveBtn.setPrefWidth(140);
        
        Button printBtn = new Button("🖨️ Print");
        printBtn.setOnAction(e -> printImage());
        printBtn.setPrefWidth(140);
        
        panel.getChildren().addAll(
            zoomTitle, zoomLabel, zoomIndicator,
            zoomInBtn, zoomOutBtn, resetZoomBtn, fitToWindowBtn,
            separator,
            toolsTitle, saveBtn, printBtn
        );
        
        return panel;
    }
    
    /**
     * Sets up the main layout.
     */
    private void setupLayout() {
        setCenter(scrollPane);
        setRight(controlPanel);
        
        // Add keyboard shortcuts info
        Label shortcutsInfo = new Label("💡 Ctrl+Scroll: Zoom | Drag: Pan | Space+Drag: Pan");
        shortcutsInfo.setStyle("-fx-padding: 5; -fx-font-size: 10px; -fx-text-fill: #666;");
        setBottom(shortcutsInfo);
    }
    
    /**
     * Sets up all event handlers for interaction.
     */
    private void setupEventHandlers() {
        // Mouse wheel zoom
        canvas.setOnScroll(this::handleScroll);
        
        // Mouse drag panning
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);
        
        // Context menu
        canvas.setOnContextMenuRequested(event -> {
            ContextMenu contextMenu = createContextMenu();
            contextMenu.show(canvas, event.getScreenX(), event.getScreenY());
        });
        
        // Keyboard shortcuts
        setOnKeyPressed(this::handleKeyPressed);
        setFocusTraversable(true);
        
        // Canvas resize handling
        canvas.widthProperty().addListener((obs, oldVal, newVal) -> redraw());
        canvas.heightProperty().addListener((obs, oldVal, newVal) -> redraw());
        
        // Scroll pane viewport change handling
        scrollPane.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            if (newBounds != null) {
                updateCanvasSize(newBounds);
            }
        });
    }
    
    /**
     * Handles scroll events for zooming.
     */
    private void handleScroll(ScrollEvent event) {
        if (event.isControlDown()) {
            event.consume();
            
            double deltaY = event.getDeltaY();
            if (deltaY > 0) {
                zoomIn();
            } else {
                zoomOut();
            }
        }
    }
    
    /**
     * Handles mouse pressed for panning.
     */
    private void handleMousePressed(MouseEvent event) {
        if (event.isPrimaryButtonDown()) {
            lastMouseX = event.getX();
            lastMouseY = event.getY();
            isDragging = true;
            canvas.setCursor(javafx.scene.Cursor.CLOSED_HAND);
        }
    }
    
    /**
     * Handles mouse dragged for panning.
     */
    private void handleMouseDragged(MouseEvent event) {
        if (isDragging && event.isPrimaryButtonDown()) {
            double deltaX = event.getX() - lastMouseX;
            double deltaY = event.getY() - lastMouseY;
            
            // Pan the scroll pane
            double hValue = scrollPane.getHvalue();
            double vValue = scrollPane.getVvalue();
            
            // Calculate new scroll values
            double newHValue = hValue - (deltaX / canvas.getWidth()) * 0.1;
            double newVValue = vValue - (deltaY / canvas.getHeight()) * 0.1;
            
            // Apply bounds checking
            newHValue = Math.max(0, Math.min(1, newHValue));
            newVValue = Math.max(0, Math.min(1, newVValue));
            
            scrollPane.setHvalue(newHValue);
            scrollPane.setVvalue(newVValue);
            
            lastMouseX = event.getX();
            lastMouseY = event.getY();
        }
    }
    
    /**
     * Handles mouse released to end panning.
     */
    private void handleMouseReleased(MouseEvent event) {
        isDragging = false;
        canvas.setCursor(javafx.scene.Cursor.DEFAULT);
    }
    
    /**
     * Handles keyboard shortcuts.
     */
    private void handleKeyPressed(KeyEvent event) {
        if (event.isControlDown()) {
            switch (event.getCode()) {
                case PLUS:
                case EQUALS:
                    zoomIn();
                    event.consume();
                    break;
                case MINUS:
                    zoomOut();
                    event.consume();
                    break;
                case DIGIT0:
                    resetZoom();
                    event.consume();
                    break;
                default:
                    // Other keys are not handled
                    break;
            }
        } else if (event.getCode() == KeyCode.SPACE) {
            // Space bar for panning mode (visual feedback could be added)
            event.consume();
        }
    }
    
    /**
     * Creates context menu for right-click actions.
     */
    private ContextMenu createContextMenu() {
        ContextMenu menu = new ContextMenu();
        
        MenuItem zoomInItem = new MenuItem("Zoom In");
        zoomInItem.setOnAction(e -> zoomIn());
        
        MenuItem zoomOutItem = new MenuItem("Zoom Out");
        zoomOutItem.setOnAction(e -> zoomOut());
        
        MenuItem resetZoomItem = new MenuItem("Reset Zoom");
        resetZoomItem.setOnAction(e -> resetZoom());
        
        MenuItem fitToWindowItem = new MenuItem("Fit to Window");
        fitToWindowItem.setOnAction(e -> fitToWindow());
        
        SeparatorMenuItem separator1 = new SeparatorMenuItem();
        
        MenuItem saveImageItem = new MenuItem("Save as Image...");
        saveImageItem.setOnAction(e -> saveImage());
        
        MenuItem printItem = new MenuItem("Print...");
        printItem.setOnAction(e -> printImage());
        
        SeparatorMenuItem separator2 = new SeparatorMenuItem();
        
        MenuItem propertiesItem = new MenuItem("Properties");
        propertiesItem.setDisable(true); // Not implemented yet
        
        menu.getItems().addAll(
            zoomInItem, zoomOutItem, resetZoomItem, fitToWindowItem,
            separator1,
            saveImageItem, printItem,
            separator2,
            propertiesItem
        );
        
        return menu;
    }
    
    /**
     * Updates canvas size based on viewport and zoom.
     */
    private void updateCanvasSize(Bounds newBounds) {
        double viewportWidth = newBounds.getWidth();
        double viewportHeight = newBounds.getHeight();
        
        if (currentNode != null) {
            // Calculate required canvas size based on content and zoom
            calculateCanvasSize(viewportWidth);
            
            // Apply zoom
            double scaledWidth = canvasWidth * zoomLevel;
            double scaledHeight = canvasHeight * zoomLevel;
            
            canvas.setWidth(Math.max(scaledWidth, viewportWidth));
            canvas.setHeight(Math.max(scaledHeight, viewportHeight));
            
            redraw();
        }
    }
    
    /**
     * Calculates the required canvas size based on content.
     */
    private void calculateCanvasSize(double availableWidth) {
        // Default to one element
        canvasHeight = FEATURE_ELEMENT_HEIGHT + (FRINGE_WIDTH * 2) + FRINGE_WIDTH + BORDER_WIDTH;
        
        if (hasSubFDDElements()) {
            int childCount = currentNode.getChildren().size();
            int oneRowWidth = (int) ((childCount * (FRINGE_WIDTH + FEATURE_ELEMENT_WIDTH)) + 
                                   FRINGE_WIDTH + (2 * BORDER_WIDTH));
            
            if (oneRowWidth > availableWidth) {
                elementsInRow = Math.max(1, (int) Math.floor((availableWidth - (2 * BORDER_WIDTH) - FRINGE_WIDTH) / 
                                                           (FRINGE_WIDTH + FEATURE_ELEMENT_WIDTH)));
                int rows = (int) Math.ceil((float) childCount / elementsInRow);
                canvasHeight = ((FEATURE_ELEMENT_HEIGHT + FRINGE_WIDTH) * rows) + 
                             (FRINGE_WIDTH * 2) + BORDER_WIDTH;
            } else {
                elementsInRow = childCount;
            }
        }
        
        // Add space for title if not a Feature
        if (!(currentNode instanceof com.nebulon.xml.fddi.Feature)) {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFont(textFont);
            canvasHeight += CenteredTextDrawerFX.getTitleTextHeight(gc, currentNode.getName(), 
                                                                   canvasWidth);
        }
        
        canvasWidth = (elementsInRow * (FEATURE_ELEMENT_WIDTH + FRINGE_WIDTH)) + 
                     FRINGE_WIDTH + BORDER_WIDTH + (EXTRA_WIDTH + 1);
    }
    
    /**
     * Checks if current node has sub-elements.
     */
    private boolean hasSubFDDElements() {
    return currentNode != null && !currentNode.getChildren().isEmpty();
    }
    
    /**
     * Zoom in by the zoom factor.
     */
    public void zoomIn() {
        setZoom(Math.min(MAX_ZOOM, zoomLevel * ZOOM_FACTOR));
    }
    
    /**
     * Zoom out by the zoom factor.
     */
    public void zoomOut() {
        setZoom(Math.max(MIN_ZOOM, zoomLevel / ZOOM_FACTOR));
    }
    
    /**
     * Reset zoom to 100%.
     */
    public void resetZoom() {
        setZoom(1.0);
    }
    
    /**
     * Fit content to window size.
     */
    public void fitToWindow() {
        if (currentNode == null) return;
        
        Bounds viewportBounds = scrollPane.getViewportBounds();
        if (viewportBounds == null) return;
        
        double viewportWidth = viewportBounds.getWidth();
        double viewportHeight = viewportBounds.getHeight();
        
        // Calculate natural content size
        calculateCanvasSize(Double.MAX_VALUE); // Get natural size
        
        double scaleX = viewportWidth / canvasWidth;
        double scaleY = viewportHeight / canvasHeight;
        double scale = Math.min(scaleX, scaleY) * 0.9; // 90% of available space
        
        setZoom(Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, scale)));
    }
    
    /**
     * Sets the zoom level and updates the display.
     */
    public void setZoom(double zoom) {
        double finalZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
        this.zoomLevel = finalZoom;
        
        // Update UI components
        Platform.runLater(() -> {
            zoomLabel.setText(String.format("%.0f%%", finalZoom * 100));
            zoomIndicator.setProgress(finalZoom / MAX_ZOOM);
            
            // Update canvas size and redraw
            Bounds viewportBounds = scrollPane.getViewportBounds();
            if (viewportBounds != null) {
                updateCanvasSize(viewportBounds);
            }
        });
    }
    
    /**
     * Gets the current zoom level.
     */
    public double getZoom() {
        return zoomLevel;
    }
    
    /**
     * Updates the current node and redraws the canvas.
     */
    public void setCurrentNode(FDDINode node) {
        this.currentNode = node;
        Platform.runLater(() -> {
            Bounds viewportBounds = scrollPane.getViewportBounds();
            if (viewportBounds != null) {
                updateCanvasSize(viewportBounds);
            } else {
                redraw();
            }
        });
    }
    
    /**
     * Gets the current node.
     */
    public FDDINode getCurrentNode() {
        return currentNode;
    }
    
    /**
     * Sets the text font and redraws.
     */
    public void setTextFont(Font font) {
        if (font != null) {
            this.textFont = createOptimalFont(font.getFamily(), font.getSize());
        } else {
            this.textFont = createOptimalFont(null, 12);
        }
        redraw();
    }
    
    /**
     * Gets the current text font.
     */
    public Font getTextFont() {
        return textFont;
    }
    
    /**
     * Forces a redraw of the canvas.
     */
    public void redraw() {
        if (currentNode == null) return;
        
        Platform.runLater(() -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            
            // Enable high-quality rendering with pixel-perfect alignment
            gc.setImageSmoothing(false); // Disable smoothing for crisp text
            
            // Clear canvas with white background
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            
            // Apply zoom transformation
            gc.save();
            gc.scale(zoomLevel, zoomLevel);
            
            // Set font with optimal rendering settings
            gc.setFont(textFont);
            
            // Configure text rendering for maximum clarity
            gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
            gc.setTextBaseline(javafx.geometry.VPos.BASELINE);
            
            // Draw FDD graphics
            drawFDDGraphics(gc);
            
            gc.restore();
        });
    }
    
    /**
     * Draws the FDD graphics on the canvas.
     */
    private void drawFDDGraphics(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        
        if (hasSubFDDElements()) {
            // Draw title
            double titleHeight = CenteredTextDrawerFX.getTitleTextHeight(gc, currentNode.getName(), 
                                                                        canvasWidth);
            CenteredTextDrawerFX.draw(gc, currentNode.getName(), BORDER_WIDTH, 
                                    BORDER_WIDTH + FRINGE_WIDTH, canvasWidth);
            
            // Draw sub-elements
            Bounds subBounds = drawSubElements(gc, BORDER_WIDTH, 
                                             (int)(titleHeight + FRINGE_WIDTH + BORDER_WIDTH), 
                                             (int) canvasWidth - (2 * BORDER_WIDTH) - EXTRA_WIDTH);
            
            // Draw outer rectangle
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(2);
            gc.strokeRect(0, 0, subBounds.getWidth() + (2 * BORDER_WIDTH), 
                         subBounds.getHeight() + titleHeight + FRINGE_WIDTH + (2 * BORDER_WIDTH));
            gc.strokeRect(BORDER_WIDTH, BORDER_WIDTH, subBounds.getWidth(), 
                         subBounds.getHeight() + titleHeight + FRINGE_WIDTH);
        } else {
            // Draw single element
            FDDGraphicFX graphic = new FDDGraphicFX(currentNode, FRINGE_WIDTH, FRINGE_WIDTH, 
                                                   FEATURE_ELEMENT_WIDTH, FEATURE_ELEMENT_HEIGHT);
            graphic.draw(gc);
        }
    }
    
    /**
     * Draws sub-elements in a grid layout.
     */
    private Bounds drawSubElements(GraphicsContext gc, int x, int y, int maxWidth) {
        double currentX = FRINGE_WIDTH;
        double currentY = FRINGE_WIDTH;
        double currentHeight = FRINGE_WIDTH;
        double currentWidth = FRINGE_WIDTH;
        double imgWidth = 0;
        
        // Swing-free iteration using FDDTreeNode adapter
        java.util.List<? extends net.sourceforge.fddtools.model.FDDTreeNode> children = currentNode.getChildren();
        for (net.sourceforge.fddtools.model.FDDTreeNode tn : children) {
            FDDINode child = (FDDINode) tn; // safe cast during transition phase
            FDDGraphicFX childGraphic = new FDDGraphicFX(child, x + currentX, y + currentY, 
                                                        FEATURE_ELEMENT_WIDTH, FEATURE_ELEMENT_HEIGHT);
            childGraphic.draw(gc);
            
            currentWidth = currentX + childGraphic.getWidth() + FRINGE_WIDTH;
            if (currentWidth > imgWidth) {
                imgWidth = currentWidth;
            }
            
            currentHeight = currentY + childGraphic.getHeight() + FRINGE_WIDTH;
            
            if ((currentWidth + childGraphic.getWidth() + FRINGE_WIDTH) > maxWidth) {
                currentX = FRINGE_WIDTH;
                currentY += (childGraphic.getHeight() + FRINGE_WIDTH);
            } else {
                currentX += (childGraphic.getWidth() + FRINGE_WIDTH);
            }
    }
        
        return new javafx.geometry.BoundingBox(0, 0, imgWidth, currentHeight);
    }
    
    /**
     * Saves the canvas as an image file.
     */
    private void saveImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Canvas as Image");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PNG Files", "*.png"),
            new FileChooser.ExtensionFilter("JPEG Files", "*.jpg", "*.jpeg")
        );
        
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage((int) canvas.getWidth(), 
                                                              (int) canvas.getHeight());
                canvas.snapshot(null, writableImage);
                
                // Convert to BufferedImage for saving
                BufferedImage bufferedImage = toBufferedImage(writableImage);
                
                String extension = getFileExtension(file.getName()).toLowerCase();
                String formatName = extension.equals("jpg") || extension.equals("jpeg") ? "jpg" : "png";
                
                ImageIO.write(bufferedImage, formatName, file);
                
                // Success: no dialog (avoid redundant confirmation)
                LOGGER.info("Image saved to: {}", file.getAbsolutePath());
                
            } catch (IOException e) {
                LOGGER.error("Failed to save image", e);
                
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Save Failed");
                alert.setHeaderText("Error saving image");
                alert.setContentText("Failed to save image: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
    
    /**
     * Gets file extension from filename.
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }

    /**
     * Converts a JavaFX WritableImage to a BufferedImage without relying on SwingFXUtils (to avoid javafx-swing module).
     */
    private BufferedImage toBufferedImage(WritableImage writableImage) {
        int w = (int) writableImage.getWidth();
        int h = (int) writableImage.getHeight();
        BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        javafx.scene.image.PixelReader reader = writableImage.getPixelReader();
        if (reader != null) {
            int[] buffer = new int[w];
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    buffer[x] = reader.getArgb(x, y);
                }
                bufferedImage.setRGB(0, y, w, 1, buffer, 0, w);
            }
        }
        return bufferedImage;
    }
    
    /**
     * Prints the canvas (placeholder implementation).
     */
    private void printImage() {
        // TODO: Implement printing functionality
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Print");
        alert.setHeaderText(null);
        alert.setContentText("Print functionality will be implemented in a future version.");
        alert.showAndWait();
    }
    
    /**
     * Forces a reflow of the canvas layout.
     */
    public void reflow() {
        Platform.runLater(() -> {
            Bounds viewportBounds = scrollPane.getViewportBounds();
            if (viewportBounds != null) {
                updateCanvasSize(viewportBounds);
            } else {
                redraw();
            }
        });
    }
}
