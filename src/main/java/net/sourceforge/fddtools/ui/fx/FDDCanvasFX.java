package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.geometry.Bounds;
import javafx.geometry.BoundingBox;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.*;
import javafx.geometry.Orientation;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.model.FDDTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// AWT-based export now isolated inside ImageExportService (no direct imports here)
import java.io.File;
import net.sourceforge.fddtools.internationalization.I18n;

/**
 * Clean JavaFX canvas implementation with observable zoom and action bar bindings.
 */
public class FDDCanvasFX extends BorderPane {
    private static final Logger LOGGER = LoggerFactory.getLogger(FDDCanvasFX.class);
    private static final int FRINGE_WIDTH = 20;
    private static final int FEATURE_ELEMENT_WIDTH = 100;
    private static final int FEATURE_ELEMENT_HEIGHT = 140;
    private static final int BORDER_WIDTH = 5;
    private static final double MIN_ZOOM = 0.1;
    private static final double MAX_ZOOM = 5.0;
    private static final double ZOOM_FACTOR = 1.1;

    private final Canvas canvas = new Canvas();
    private final ScrollPane scrollPane = new ScrollPane();
    private final Pane canvasHolder = new Pane(canvas); // Changed from StackPane to Pane
    private final Label zoomLabel = new Label("100%"); // Percent format can be localized in a later pass
    private final ProgressBar zoomIndicator = new ProgressBar(1.0 / MAX_ZOOM);
    private final ToolBar actionBar;
    // Action bar components
    private Label zoomTitleLabel;
    private TextField zoomField;
    private Slider zoomSlider;
    private Button saveButton, printButton;
    private Button btnZoomIn, btnZoomOut, btnReset, btnFit;

    private FDDINode currentNode;
    private Font textFont;
    private final ReadOnlyDoubleWrapper zoomLevel = new ReadOnlyDoubleWrapper(this, "zoomLevel", 1.0);
    private double canvasWidth = 800, canvasHeight = 600;
    private int elementsInRow = 1;
    // Removed panning state fields (panning disabled)
    private ContextMenu sharedContextMenu; // reused to avoid multiple instances
    private boolean autoFitActive = false; // if true, auto-refit on viewport resize
    private boolean fitting = false; // reentrancy guard

    public FDDCanvasFX(FDDINode node, Font font) {
        this.currentNode = node;
        this.textFont = font != null ? Font.font(font.getFamily(), FontWeight.SEMI_BOLD, font.getSize())
                                     : Font.font("Arial", FontWeight.SEMI_BOLD, 12);
        // Pane positions canvas at (0,0) by default - no alignment needed
        configureScrollPane();
        actionBar = createActionBar();
        setupLayout();
        setupHandlers();
        Platform.runLater(this::redraw);
    }

    private void configureScrollPane(){
        scrollPane.setContent(canvasHolder);
        
        // Initial configuration - will be updated based on zoom level in updateScrollBehavior()
        scrollPane.setPannable(true);      // Enable mouse drag panning
        scrollPane.setMaxWidth(Double.MAX_VALUE);
        scrollPane.setMaxHeight(Double.MAX_VALUE);
        scrollPane.getStyleClass().add("scroll-pane-surface");
        
        // Set initial scroll behavior for 100% zoom
        updateScrollBehavior();
    }
    
    private void updateScrollBehavior() {
        double zoom = getZoom();
        
        // Special case for fit mode: no scrollbars at all
        if (autoFitActive) {
            scrollPane.setFitToWidth(false);
            scrollPane.setFitToHeight(false);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            return;
        }
        
        if (Math.abs(zoom - 1.0) < 0.01) {
            // At 100% zoom: fit to width (no horizontal scrolling), allow vertical scrolling
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(false);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        } else {
            // When zoomed: allow both horizontal and vertical scrolling
            scrollPane.setFitToWidth(false);
            scrollPane.setFitToHeight(false);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        }
    }

    private ToolBar createActionBar() {
        // Create toolbar with modern styling
        ToolBar bar = new ToolBar();
        bar.getStyleClass().addAll("action-bar", "tool-bar");
        bar.setMinHeight(44);
        bar.setPrefHeight(44);
        bar.setMaxHeight(44);
        
        // Create zoom control group with modern design
        HBox zoomPane = new HBox(6); // Better spacing
        zoomPane.setAlignment(Pos.CENTER_LEFT);
        zoomPane.getStyleClass().add("zoom-controls");
        
        // Modern zoom label
        zoomTitleLabel = new Label("Zoom:");
        zoomTitleLabel.getStyleClass().addAll("zoom-label", "control-label");
        
        // Editable zoom percentage field
        TextField zoomField = new TextField();
        zoomField.setMinWidth(55);
        zoomField.setPrefWidth(55);
        zoomField.setAlignment(Pos.CENTER);
        zoomField.getStyleClass().addAll("zoom-field", "editable-field");
        zoomField.setPromptText("100%");
        
        // Set initial value and make it editable
        zoomField.setText(String.format("%.0f%%", getZoom() * 100));
        zoomField.setOnAction(e -> {
            try {
                String text = zoomField.getText().replace("%", "").trim();
                double value = Double.parseDouble(text) / 100.0;
                setZoom(value);
            } catch (NumberFormatException ex) {
                // Reset to current value if invalid input
                zoomField.setText(String.format("%.0f%%", getZoom() * 100));
            }
        });
        
        // Update field when zoom changes programmatically
        zoomField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                zoomField.setText(String.format("%.0f%%", getZoom() * 100));
            }
        });
        
        // Store reference for updates
        this.zoomField = zoomField;
        
        // Interactive zoom slider
        Slider zoomSlider = new Slider(MIN_ZOOM * 100, MAX_ZOOM * 100, getZoom() * 100);
        zoomSlider.setPrefWidth(80);
        zoomSlider.setShowTickLabels(false);
        zoomSlider.setShowTickMarks(false);
        zoomSlider.getStyleClass().addAll("zoom-slider", "modern-slider");
        zoomSlider.setTooltip(new Tooltip("Drag to adjust zoom level"));
        
        // Handle slider changes
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!zoomSlider.isPressed()) return; // Only respond to user interaction
            setZoom(newVal.doubleValue() / 100.0);
        });
        
        // Update slider when zoom changes programmatically
        zoomLevelProperty().addListener((obs, oldVal, newVal) -> {
            if (!zoomSlider.isPressed()) {
                zoomSlider.setValue(newVal.doubleValue() * 100);
            }
        });
        
        this.zoomSlider = zoomSlider;
        
        // Create modern zoom buttons with symbols
        btnZoomOut = new Button("−"); // En dash (better than minus)
        btnZoomOut.setMinWidth(32);
        btnZoomOut.setPrefWidth(32);
        btnZoomOut.setMinHeight(28);
        btnZoomOut.setPrefHeight(28);
        btnZoomOut.setOnAction(e -> zoomOut());
        btnZoomOut.setTooltip(new Tooltip("Zoom Out (Ctrl + Mouse Wheel or ⌘−)"));
        btnZoomOut.getStyleClass().addAll("zoom-button", "zoom-out-button");
        
        btnZoomIn = new Button("+");
        btnZoomIn.setMinWidth(32);
        btnZoomIn.setPrefWidth(32);
        btnZoomIn.setMinHeight(28);
        btnZoomIn.setPrefHeight(28);
        btnZoomIn.setOnAction(e -> zoomIn());
        btnZoomIn.setTooltip(new Tooltip("Zoom In (Ctrl + Mouse Wheel or ⌘+)"));
        btnZoomIn.getStyleClass().addAll("zoom-button", "zoom-in-button");
        
        // Modern control buttons with better icons/text
        btnReset = new Button("100%");
        btnReset.setMinWidth(48);
        btnReset.setPrefWidth(48);
        btnReset.setMinHeight(28);
        btnReset.setPrefHeight(28);
        btnReset.setOnAction(e -> resetZoom());
        btnReset.setTooltip(new Tooltip("Reset to 100% (⌘0)"));
        btnReset.getStyleClass().addAll("zoom-button", "reset-button");
        
        btnFit = new Button("Fit"); // Clear text label instead of symbol
        btnFit.setMinWidth(40);
        btnFit.setPrefWidth(40);
        btnFit.setMinHeight(28);
        btnFit.setPrefHeight(28);
        btnFit.setOnAction(e -> fitToWindow());
        btnFit.setTooltip(new Tooltip("Fit to Window"));
        btnFit.getStyleClass().addAll("zoom-button", "fit-button");
        
        // Group zoom buttons in a contained group
        HBox zoomButtons = new HBox(2);
        zoomButtons.setAlignment(Pos.CENTER);
        zoomButtons.getStyleClass().add("button-group");
        zoomButtons.getChildren().addAll(btnZoomOut, btnZoomIn);
        
        // Add separator between button groups
        Separator buttonSeparator = new Separator(Orientation.VERTICAL);
        buttonSeparator.getStyleClass().add("toolbar-separator");
        
        // Add all zoom controls to zoom pane with logical grouping
        zoomPane.getChildren().addAll(
            zoomTitleLabel, 
            zoomField,  // Use the editable field instead of label
            zoomSlider, // Use the interactive slider instead of progress bar
            zoomButtons,
            buttonSeparator,
            btnReset, 
            btnFit
        );
        
        // Create modern action buttons with clear labels
        saveButton = new Button("Save Image");
        saveButton.setMinWidth(80);
        saveButton.setPrefWidth(80);
        saveButton.setMinHeight(32);
        saveButton.setPrefHeight(32);
        saveButton.setOnAction(e -> saveImage());
        saveButton.setTooltip(new Tooltip("Save as Image (⌘S)"));
        saveButton.getStyleClass().addAll("action-button", "save-button");
        
        printButton = new Button("Print");
        printButton.setMinWidth(60);
        printButton.setPrefWidth(60);
        printButton.setMinHeight(32);
        printButton.setPrefHeight(32);
        printButton.setOnAction(e -> printImage());
        printButton.setTooltip(new Tooltip("Print (⌘P)"));
        printButton.getStyleClass().addAll("action-button", "print-button");
        
        // Create spacer to push action buttons to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Create action button group
        HBox actionButtons = new HBox(4);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.getStyleClass().add("action-button-group");
        actionButtons.getChildren().addAll(saveButton, printButton);
        
        // Add main toolbar separator
        Separator mainSeparator = new Separator(Orientation.VERTICAL);
        mainSeparator.getStyleClass().add("main-toolbar-separator");
        
        // Add all items to toolbar with improved layout
        bar.getItems().addAll(
            zoomPane,
            spacer,
            mainSeparator,
            actionButtons
        );
        
        updateButtonDisableStates();
        return bar;
    }

    private void setupLayout() {
        // Simple BorderPane layout with toolbar at bottom as preferred
        setCenter(scrollPane); // Canvas in center
        setBottom(actionBar);  // Toolbar at bottom as originally designed
        
        // Set initial canvas height (width is bound to viewport)
        canvas.setHeight(canvasHeight);
        
        // CRITICAL: Listen to ScrollPane viewport changes like Swing does
        // This is equivalent to your Swing componentResized() listener
        scrollPane.viewportBoundsProperty().addListener((o, a, b) -> {
            if (b != null) {
                updateButtonDisableStates();
                // Only trigger reflow if not in fitting state
                if (!fitting && !autoFitActive) {
                    Platform.runLater(this::reflow);
                }
                // Only re-fit if auto-fit is active and we're not currently fitting
                if (autoFitActive && !fitting) { 
                    Platform.runLater(this::fitToWindow); 
                }
            }
        });
    }

    // Removed shortcut label (tooltip-style hint) per UX simplification request



    public final void reflow() {
        // Skip reflow if we're in the middle of fitting or auto-fit is active
        if (fitting || autoFitActive) return;
        
        // Update scroll behavior based on current zoom level
        updateScrollBehavior();
        
        // Calculate natural content size based on the reference viewport width (not current zoom)
        Bounds viewportBounds = scrollPane.getViewportBounds();
        double referenceWidth = viewportBounds != null ? viewportBounds.getWidth() : calculateNaturalContentWidth();
        if (referenceWidth <= 0) referenceWidth = calculateNaturalContentWidth();
        
        // ALWAYS calculate layout based on reference width to maintain consistent element arrangement
        double naturalWidth = referenceWidth;
        double naturalHeight = calculateCanvasHeight(naturalWidth);
        
        // Apply zoom factor to get actual canvas size
        double zoom = getZoom();
        
        // Handle canvas sizing based on zoom level
        if (Math.abs(zoom - 1.0) < 0.01) {
            // At 100% zoom: size canvas to fit viewport width
            canvas.setWidth(naturalWidth);
            canvas.setHeight(naturalHeight);
            
            // Set canvasHolder to use computed size - let ScrollPane control it
            canvasHolder.setPrefWidth(Region.USE_COMPUTED_SIZE);
            canvasHolder.setPrefHeight(naturalHeight);
            canvasHolder.setMinWidth(Region.USE_COMPUTED_SIZE);
            canvasHolder.setMinHeight(naturalHeight);
            
            // Store actual dimensions for drawing calculations
            canvasWidth = naturalWidth;
        } else {
            // When zoomed: scale the natural dimensions by zoom factor
            double canvasWidthWithZoom = naturalWidth * zoom;
            double canvasHeightWithZoom = naturalHeight * zoom;
            
            canvas.setWidth(canvasWidthWithZoom);
            canvas.setHeight(canvasHeightWithZoom);
            
            canvasHolder.setPrefWidth(canvasWidthWithZoom);
            canvasHolder.setPrefHeight(canvasHeightWithZoom);
            canvasHolder.setMinWidth(canvasWidthWithZoom);
            canvasHolder.setMinHeight(canvasHeightWithZoom);
            
            // Store NATURAL (unzoomed) dimensions for drawing calculations
            // This ensures layout is calculated based on original width, not zoomed width
            canvasWidth = naturalWidth;
        }
        
        // Position canvas at (0,0) in the Pane
        canvas.setLayoutX(0);
        canvas.setLayoutY(0);
        
        // Allow growth beyond current size unless we're in fit mode
        if (!autoFitActive) {
            canvasHolder.setMaxWidth(Double.MAX_VALUE);
            canvasHolder.setMaxHeight(Double.MAX_VALUE);
        }
        
        // Trigger redraw
        redraw();
    }
    
    private double calculateNaturalContentWidth() {
        // Calculate width needed to display content in an optimal layout
        // Use a reasonable default for programs/projects, or calculate based on child count
        if (!hasChildren()) {
            return FEATURE_ELEMENT_WIDTH + (2 * BORDER_WIDTH) + (2 * FRINGE_WIDTH);
        }
        
        int childCount = currentNode.getChildren().size();
        
        // For small numbers of children, use a reasonable minimum width
        if (childCount <= 3) {
            return Math.max(600, childCount * (FEATURE_ELEMENT_WIDTH + FRINGE_WIDTH) + FRINGE_WIDTH + (2 * BORDER_WIDTH));
        }
        
        // For larger numbers, aim for roughly 2:1 to 3:1 width:height ratio for good layout
        // This creates wider layouts that will require horizontal scrolling when zoomed
        double elementsPerRow = Math.ceil(Math.sqrt(childCount * 2.5)); // Bias toward wider layouts
        return Math.max(800, elementsPerRow * (FEATURE_ELEMENT_WIDTH + FRINGE_WIDTH) + FRINGE_WIDTH + (2 * BORDER_WIDTH));
    }
    
    private double calculateCanvasHeight(double availableWidth) {
        // Default to one element height
        double height = FEATURE_ELEMENT_HEIGHT + (FRINGE_WIDTH * 2) + FRINGE_WIDTH + BORDER_WIDTH;
        
        if(hasChildren()) {
            int childCount = currentNode.getChildren().size();
            
            // Calculate how many elements can fit in one row based on available width
            int maxElementsPerRow = Math.max(1, (int)Math.floor((availableWidth - (2 * BORDER_WIDTH) - FRINGE_WIDTH) / (FRINGE_WIDTH + FEATURE_ELEMENT_WIDTH)));
            
            // Use the smaller of: available space or total child count
            elementsInRow = Math.min(maxElementsPerRow, childCount);
            
            // Calculate number of rows needed
            int rows = (int)Math.ceil((double)childCount / elementsInRow);
            height = ((FEATURE_ELEMENT_HEIGHT + FRINGE_WIDTH) * rows) + (FRINGE_WIDTH * 2) + BORDER_WIDTH;
        } else {
            elementsInRow = 1; // Default for no children
        }
        
        // Add title height if not a feature  
        if(!(currentNode instanceof com.nebulon.xml.fddi.Feature) && currentNode != null) {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFont(textFont);
            height += CenteredTextDrawerFX.getTitleTextHeight(gc, currentNode.getName(), (int)availableWidth);
        }
        
        return height;
    }

    // Simple layout management - removed complex layoutChildren override

    private void setupHandlers(){
        canvas.setOnScroll(this::onScroll);
        
        // Mac trackpad gesture support for pinch-to-zoom
        canvas.setOnZoomStarted(this::onZoomStarted);
        canvas.setOnZoom(this::onZoom);
        canvas.setOnZoomFinished(this::onZoomFinished);
        
        // Use explicit secondary-button detection; ignore Ctrl+Primary synthesis on mac
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if(e.getButton()==MouseButton.SECONDARY){ ensureContextMenu(); sharedContextMenu.show(canvas, e.getScreenX(), e.getScreenY()); e.consume(); }
        });
        setOnKeyPressed(this::onKey);
        setFocusTraversable(true);
        canvas.widthProperty().addListener((o,a,b)-> redraw());
        canvas.heightProperty().addListener((o,a,b)-> redraw());
        
        // Viewport listener is set up in setupLayout() - no duplicate needed here
    }

    private void onScroll(ScrollEvent e){ 
        if(e.isControlDown()){ 
            // Ctrl+scroll = zoom
            e.consume(); 
            if(e.getDeltaY()>0) zoomIn(); 
            else zoomOut(); 
        } 
        // For natural scrolling without Ctrl, let ScrollPane handle it automatically
        // This allows smooth trackpad scrolling on Mac when zoomed
    }
    
    // Mac trackpad gesture support - zoom handlers
    private double gestureStartZoom = 1.0;
    
    private void onZoomStarted(ZoomEvent e) {
        gestureStartZoom = getZoom();
        e.consume();
    }
    
    private void onZoom(ZoomEvent e) {
        // Apply the gesture zoom factor to the starting zoom level
        double newZoom = gestureStartZoom * e.getTotalZoomFactor();
        setZoom(newZoom);
        e.consume();
    }
    
    private void onZoomFinished(ZoomEvent e) {
        // Final zoom adjustment if needed
        double finalZoom = gestureStartZoom * e.getTotalZoomFactor();
        setZoom(finalZoom);
        e.consume();
    }
    
    // Panning handlers removed
    private void onKey(KeyEvent e){ if(e.isControlDown()){ switch(e.getCode()){ case PLUS: case EQUALS: zoomIn(); e.consume(); break; case MINUS: zoomOut(); e.consume(); break; case DIGIT0: resetZoom(); e.consume(); break; default: } } else if(e.getCode()== KeyCode.SPACE){ e.consume(); }}

    private void ensureContextMenu(){
        if(sharedContextMenu!=null) return;
        sharedContextMenu = new ContextMenu();
        MenuItem in=new MenuItem(I18n.get("Canvas.Context.ZoomIn")); in.setOnAction(e->zoomIn());
        MenuItem out=new MenuItem(I18n.get("Canvas.Context.ZoomOut")); out.setOnAction(e->zoomOut());
        MenuItem reset=new MenuItem(I18n.get("Canvas.Context.ResetZoom")); reset.setOnAction(e->resetZoom());
        MenuItem fit=new MenuItem(I18n.get("Canvas.Context.FitToWindow")); fit.setOnAction(e->fitToWindow());
        MenuItem save=new MenuItem(I18n.get("Canvas.Context.SaveAsImage")); save.setOnAction(e->saveImage());
        MenuItem print=new MenuItem(I18n.get("Canvas.Context.Print")); print.setOnAction(e->printImage());
        MenuItem props=new MenuItem(I18n.get("Canvas.Context.Properties")); props.setDisable(true);
        sharedContextMenu.getItems().addAll(in,out,reset,fit,new SeparatorMenuItem(),save,print,new SeparatorMenuItem(),props);
    }

    // Removed old complex updateCanvasSize method - using simple reflow instead

    // Removed old complex calculateCanvasSize method - using simple calculateCanvasHeight instead

    private boolean hasChildren(){ return currentNode!=null && !currentNode.getChildren().isEmpty(); }

    // --- Zoom API ---
    public void zoomIn(){ 
        autoFitActive=false; 
        setZoom(Math.min(MAX_ZOOM, getZoom()*ZOOM_FACTOR)); 
        updateScrollBehavior(); // Reset scroll behavior when leaving fit mode
        Platform.runLater(this::reflow); // Trigger reflow for normal zoom
    }
    public void zoomOut(){ 
        autoFitActive=false; 
        setZoom(Math.max(MIN_ZOOM, getZoom()/ZOOM_FACTOR)); 
        updateScrollBehavior(); // Reset scroll behavior when leaving fit mode
        Platform.runLater(this::reflow); // Trigger reflow for normal zoom
    }
    public void resetZoom(){ 
        autoFitActive=false; 
        setZoom(1.0); 
        updateScrollBehavior(); // Reset scroll behavior when leaving fit mode
        Platform.runLater(this::reflow); // Trigger reflow for normal zoom
    }
    public void fitToWindow(){
        if(currentNode==null) return; 
        Bounds vp=scrollPane.getViewportBounds(); 
        if(vp==null) return;
        
        fitting = true;
        net.sourceforge.fddtools.service.LoggingService.Span span = net.sourceforge.fddtools.service.LoggingService.getInstance().startPerf("fitToWindow", java.util.Map.of("action","fit"));
        
        int count = currentNode.getChildren()!=null ? currentNode.getChildren().size() : 0;
        if(count<=0){ 
            // For leaf nodes, fit single element to exact viewport size
            canvasWidth = vp.getWidth();
            canvasHeight = vp.getHeight();
            setZoom(1.0); // Use 100% zoom and size canvas to viewport
            
            // Directly set canvas size to match viewport exactly
            canvas.setWidth(vp.getWidth());
            canvas.setHeight(vp.getHeight());
            canvasHolder.setPrefWidth(vp.getWidth());
            canvasHolder.setPrefHeight(vp.getHeight());
            canvasHolder.setMinWidth(vp.getWidth());
            canvasHolder.setMinHeight(vp.getHeight());
            canvasHolder.setMaxWidth(vp.getWidth());
            canvasHolder.setMaxHeight(vp.getHeight());
            
            span.metric("targetScale", "1.0").metric("mode", "leaf-exact-fit"); 
            fitting = false;
            span.close(); 
            redraw();
            return; 
        }
        
        GraphicsContext gc=canvas.getGraphicsContext2D(); 
        gc.setFont(textFont);
        
        // Find the layout that perfectly fits the viewport
        double bestScale = MIN_ZOOM; 
        int bestCols = 1; 
        double bestContentWidth = 0, bestContentHeight = 0;
        
        // Try different column arrangements to find one that fits exactly
        for(int cols=1; cols<=count; cols++){
            int rows = (int)Math.ceil((double)count/cols);
            
            // Calculate content dimensions
            int contentWidth = (cols * (FEATURE_ELEMENT_WIDTH + FRINGE_WIDTH)) + FRINGE_WIDTH;
            double titleHeight = 0; 
            if(!(currentNode instanceof com.nebulon.xml.fddi.Feature)) {
                titleHeight = CenteredTextDrawerFX.getTitleTextHeight(gc, currentNode.getName(), contentWidth);
            }
            
            double totalContentWidth = contentWidth + (2 * BORDER_WIDTH);
            double totalContentHeight = ((FEATURE_ELEMENT_HEIGHT + FRINGE_WIDTH) * rows) + 
                                      (FRINGE_WIDTH * 2) + BORDER_WIDTH + titleHeight;
            
            // Calculate scale to fit exactly in viewport
            double scaleWidth = vp.getWidth() / totalContentWidth;
            double scaleHeight = vp.getHeight() / totalContentHeight;
            double scale = Math.min(scaleWidth, scaleHeight);
            
            // Enforce zoom limits
            if(scale > MAX_ZOOM) scale = MAX_ZOOM; 
            if(scale < MIN_ZOOM) scale = MIN_ZOOM;
            
            // This layout fits better if it allows a larger scale
            if(scale > bestScale){
                bestScale = scale; 
                bestCols = cols; 
                bestContentWidth = totalContentWidth;
                bestContentHeight = totalContentHeight;
            }
        }
        
        // Apply the optimal layout and scale content to fit EXACTLY in viewport
        elementsInRow = bestCols; 
        
        // For perfect fit: Store the natural content dimensions for drawing
        canvasWidth = bestContentWidth;
        canvasHeight = bestContentHeight;
        
        // Set canvas to exact viewport dimensions - this is the KEY to no scrolling
        canvas.setWidth(vp.getWidth());
        canvas.setHeight(vp.getHeight());
        canvasHolder.setPrefWidth(vp.getWidth());
        canvasHolder.setPrefHeight(vp.getHeight());
        canvasHolder.setMinWidth(vp.getWidth());
        canvasHolder.setMinHeight(vp.getHeight());
        canvasHolder.setMaxWidth(vp.getWidth());
        canvasHolder.setMaxHeight(vp.getHeight());
        
        // Position canvas at (0,0)
        canvas.setLayoutX(0);
        canvas.setLayoutY(0);
        
        span.metric("targetScale", String.format(java.util.Locale.US, "%.3f", bestScale))
            .metric("cols", bestCols)
            .metric("rows", (int)Math.ceil((double)count/bestCols))
            .metric("contentSize", String.format("%.1fx%.1f", bestContentWidth, bestContentHeight))
            .metric("viewportSize", String.format("%.1fx%.1f", vp.getWidth(), vp.getHeight()));
            
        setZoom(bestScale);
        autoFitActive = true; 
        
        // CRITICAL: Update scroll behavior to disable all scrollbars for fit mode
        updateScrollBehavior();
        
        fitting = false; 
        span.close();
        
        // Force immediate redraw with exact sizing
        redraw();
    }
    public void setZoom(double z){ 
        double f=Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, z)); 
        if(Math.abs(f-getZoom())<0.0001){ 
            updateZoomUI(); 
            return;
        } 
        
        // Disable auto-fit when manually changing zoom (unless we're currently fitting)
        if (!fitting) {
            autoFitActive = false;
        }
        
        zoomLevel.set(f); 
        updateZoomUI(); 
    }
    private void persistZoom(){ try { net.sourceforge.fddtools.service.PreferencesService.getInstance().setLastZoomLevel(getZoom()); net.sourceforge.fddtools.service.PreferencesService.getInstance().flushNow(); } catch (Exception ignored) {} }
    { // instance initializer to hook zoom listener for persistence
        zoomLevel.addListener((o,a,b)-> persistZoom());
    }
    public void restoreLastZoomIfEnabled(){ var prefs=net.sourceforge.fddtools.service.PreferencesService.getInstance(); if(prefs.isRestoreLastZoomEnabled()) { setZoom(prefs.getLastZoomLevel()); } }
    public double getZoom(){ return zoomLevel.get(); }
    public ReadOnlyDoubleProperty zoomLevelProperty(){ return zoomLevel.getReadOnlyProperty(); }

    private void updateZoomUI(){ 
        Platform.runLater(()->{ 
            double z=getZoom(); 
            // Update the editable zoom field only if not currently focused
            if (zoomField != null && !zoomField.isFocused()) {
                zoomField.setText(String.format("%.0f%%", z*100));
            }
            // Update the zoom slider only if not currently being dragged
            if (zoomSlider != null && !zoomSlider.isPressed()) {
                zoomSlider.setValue(z * 100);
            }
            // Update legacy label for compatibility
            zoomLabel.setText(String.format("%.0f%%", z*100)); 
            zoomIndicator.setProgress(z/MAX_ZOOM); 
            reflow(); 
            updateButtonDisableStates(); 
        }); 
    }
    private void updateButtonDisableStates(){ if(btnZoomIn==null) return; double z=getZoom(); final double EPS=0.0001; btnZoomIn.setDisable(z>=(MAX_ZOOM-EPS)); btnZoomOut.setDisable(z<=(MIN_ZOOM+EPS)); btnReset.setDisable(Math.abs(z-1.0)<0.001); Bounds vp=scrollPane.getViewportBounds(); if(vp==null) btnFit.setDisable(true); else { double fit=computeFitScale(vp.getWidth(), vp.getHeight()); btnFit.setDisable(Math.abs(z-fit)<0.01);} }
    private double computeFitScale(double vw,double vh){
        // Compute scale that fits both width and height exactly (no 90% shrink) while honoring min/max
        calculateCanvasHeight(vw); // Use actual viewport width, not MAX_VALUE
        double scale = Math.min(vw / canvasWidth, vh / canvasHeight);
        if(scale<MIN_ZOOM) scale=MIN_ZOOM; else if(scale>MAX_ZOOM) scale=MAX_ZOOM; return scale; }



    // --- Node / font / redraw ---
    public void setCurrentNode(FDDINode node){ this.currentNode=node; Platform.runLater(this::reflow); }
    public FDDINode getCurrentNode(){ return currentNode; }
    public void setTextFont(Font font){ this.textFont = (font!=null? Font.font(font.getFamily(), FontWeight.SEMI_BOLD, font.getSize()) : Font.font("Arial", FontWeight.SEMI_BOLD,12)); redraw(); }
    public Font getTextFont(){ return textFont; }

    public void redraw(){ if(currentNode==null) return; Platform.runLater(()->{
        net.sourceforge.fddtools.service.LoggingService.Span span = net.sourceforge.fddtools.service.LoggingService.getInstance()
            .startPerf("canvasRedraw", java.util.Map.of("action","redraw"));
        GraphicsContext gc=canvas.getGraphicsContext2D();
        gc.setImageSmoothing(false);
        gc.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
        gc.setFill(Color.WHITE);
        gc.fillRect(0,0,canvas.getWidth(),canvas.getHeight());
        gc.save(); gc.scale(getZoom(), getZoom()); gc.setFont(textFont); drawGraphics(gc); gc.restore();
        int childCount = (currentNode!=null && currentNode.getChildren()!=null) ? currentNode.getChildren().size() : 0;
        span.metric("children", childCount).metric("zoom", getZoom()).metric("pixels", (int)(canvas.getWidth()*canvas.getHeight())).close();
    }); }
    private void drawGraphics(GraphicsContext gc){
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        if(hasChildren()){
            // CRITICAL FIX: When fitted, use the actual canvas width for layout; otherwise use stored canvasWidth
            int contentWidth = autoFitActive ? (int)(canvas.getWidth() / getZoom()) : (int)canvasWidth;
            double titleHeight=CenteredTextDrawerFX.getTitleTextHeight(gc,currentNode.getName(),contentWidth);
            CenteredTextDrawerFX.draw(gc,currentNode.getName(),BORDER_WIDTH,BORDER_WIDTH+FRINGE_WIDTH,contentWidth);
            Bounds sub=drawChildren(gc,BORDER_WIDTH,titleHeight+FRINGE_WIDTH+BORDER_WIDTH,contentWidth);
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(2);
            gc.strokeRect(0,0, sub.getWidth()+(2*BORDER_WIDTH), sub.getHeight()+titleHeight+FRINGE_WIDTH+(2*BORDER_WIDTH));
            gc.strokeRect(BORDER_WIDTH,BORDER_WIDTH, sub.getWidth(), sub.getHeight()+titleHeight+FRINGE_WIDTH);
        } else {
            new FDDGraphicFX(currentNode,FRINGE_WIDTH,FRINGE_WIDTH,FEATURE_ELEMENT_WIDTH,FEATURE_ELEMENT_HEIGHT).draw(gc);
        }
    }
    // Updated to accept double coordinates/width for layout flexibility (avoids int/double mismatch)
    private Bounds drawChildren(GraphicsContext gc,double x,double y,double maxWidth){
        double currentX=FRINGE_WIDTH,currentY=FRINGE_WIDTH,currentHeight=FRINGE_WIDTH,currentWidth=FRINGE_WIDTH,imgWidth=0;
        int elementIndex = 0;
        int elementsInCurrentRow = 0;
        
        // Calculate actual elements per row based on available width
        int actualElementsInRow = autoFitActive ? 
            Math.max(1, (int)Math.floor((maxWidth - (2 * BORDER_WIDTH) - FRINGE_WIDTH) / (FRINGE_WIDTH + FEATURE_ELEMENT_WIDTH))) :
            elementsInRow;
        
        for(FDDTreeNode tn: currentNode.getChildren()){
            FDDINode child=(FDDINode)tn;
            
            // Wrap when we've reached the calculated elements per row OR when element won't fit
            double elementEndX = currentX + FEATURE_ELEMENT_WIDTH + FRINGE_WIDTH;
            boolean shouldWrap = (elementsInCurrentRow >= actualElementsInRow) || 
                                (elementIndex > 0 && (x + elementEndX) > maxWidth);
            
            if(shouldWrap){
                currentX=FRINGE_WIDTH;
                currentY+=(FEATURE_ELEMENT_HEIGHT+FRINGE_WIDTH);
                elementsInCurrentRow = 0;
            }
            
            FDDGraphicFX g=new FDDGraphicFX(child,x+currentX,y+currentY,FEATURE_ELEMENT_WIDTH,FEATURE_ELEMENT_HEIGHT);
            g.draw(gc);
            currentWidth=currentX+g.getWidth()+FRINGE_WIDTH;
            if(currentWidth>imgWidth) imgWidth=currentWidth;
            currentHeight=currentY+g.getHeight()+FRINGE_WIDTH;
            
            // Move to next position
            currentX+=(g.getWidth()+FRINGE_WIDTH);
            elementIndex++;
            elementsInCurrentRow++;
        }
        return new BoundingBox(0,0,imgWidth,currentHeight);
    }

    // --- Export / misc ---
    private void saveImage(){
        FileChooser fc=new FileChooser(); fc.setTitle("Save Canvas as Image");
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PNG Files","*.png"));
        File f=fc.showSaveDialog(getScene().getWindow());
        if(f!=null){
            try {
                String ext=getExt(f.getName());
                if(!ext.equalsIgnoreCase("png")) ext = "png"; // force png only
                net.sourceforge.fddtools.service.ImageExportService.getInstance().export(canvas, f, ext);
                LOGGER.info("Image saved: {}", f.getAbsolutePath()); 
                net.sourceforge.fddtools.service.LoggingService.getInstance().audit("imageExport", java.util.Map.of("action","exportImage"), f::getName);
            } catch (Exception ex) {
                LOGGER.error("Image export failed: {}", ex.getMessage(), ex);
                new Alert(Alert.AlertType.ERROR, "Failed: "+ex.getMessage()).showAndWait(); 
                net.sourceforge.fddtools.service.LoggingService.getInstance().audit("imageExportFail", java.util.Map.of("action","exportImage"), () -> ex.getClass().getSimpleName());
            }
        }
    }
    private String getExt(String n){ int i=n.lastIndexOf('.'); return i>0? n.substring(i+1):""; }
    // Export conversion logic moved to ImageExportService (kept method removed)
    private void printImage(){ new Alert(Alert.AlertType.INFORMATION,"Print functionality will be implemented in a future version.").showAndWait(); }

    // BEGIN TEST ACCESSOR
    /** Test-only accessor for verifying responsive layout calculations. */
    public int getElementsInRowForTest() { return elementsInRow; }
    // END TEST ACCESSOR
}