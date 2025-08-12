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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
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
import net.sourceforge.fddtools.util.I18n;

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
    private final StackPane canvasHolder = new StackPane(canvas);
    private final Label zoomLabel = new Label("100%"); // TODO: i18n percent formats later
    private final ProgressBar zoomIndicator = new ProgressBar(1.0 / MAX_ZOOM);
    private final HBox actionBar;
    // Action bar components for responsive hiding
    private Label zoomTitleLabel;
    private Region actionBarSpacer;
    private Button saveButton, printButton;
    private String originalSaveText = I18n.get("Canvas.SaveImage.Button");
    private String originalPrintText = I18n.get("Canvas.Print.Button");
    private Button btnZoomIn, btnZoomOut, btnReset, btnFit;

    private FDDINode currentNode;
    private Font textFont;
    private final ReadOnlyDoubleWrapper zoomLevel = new ReadOnlyDoubleWrapper(this, "zoomLevel", 1.0);
    private double canvasWidth = 800, canvasHeight = 600;
    private int elementsInRow = 1;
    private double lastLayoutWidth = -1, lastLayoutHeight = -1;
    private long lastLayoutRequestNanos = 0;
    // Removed panning state fields (panning disabled)
    private ContextMenu sharedContextMenu; // reused to avoid multiple instances
    private boolean fitLayoutLock = false; // prevents column recalculation during fit
    private boolean autoFitActive = false; // if true, auto-refit on viewport resize
    private boolean fitting = false; // reentrancy guard

    public FDDCanvasFX(FDDINode node, Font font) {
        this.currentNode = node;
        this.textFont = font != null ? Font.font(font.getFamily(), FontWeight.SEMI_BOLD, font.getSize())
                                     : Font.font("Arial", FontWeight.SEMI_BOLD, 12);
        canvasHolder.setAlignment(Pos.TOP_LEFT);
        configureScrollPane();
        actionBar = createActionBar();
        setupLayout();
        setupHandlers();
        Platform.runLater(this::redraw);
    }

    private void configureScrollPane(){
        scrollPane.setContent(canvasHolder);
        scrollPane.setFitToWidth(false); // MODIFIED: Prevents layout feedback loop
        scrollPane.setFitToHeight(false); // MODIFIED: Allows canvas to dictate its own height for scrolling
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("scroll-pane-surface");
    }

    private HBox createActionBar(){
        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("action-bar");
        zoomTitleLabel=new Label(I18n.get("View.Zoom.Label"));
        btnZoomIn=new Button("+"); btnZoomIn.setOnAction(e->zoomIn()); btnZoomIn.setTooltip(new Tooltip(I18n.get("Canvas.ZoomIn.Tooltip")));
        btnZoomOut=new Button("-"); btnZoomOut.setOnAction(e->zoomOut()); btnZoomOut.setTooltip(new Tooltip(I18n.get("Canvas.ZoomOut.Tooltip")));
        btnReset=new Button(I18n.get("Canvas.Reset.Button")); btnReset.setOnAction(e->resetZoom()); btnReset.setTooltip(new Tooltip(I18n.get("Canvas.Reset.Tooltip")));
        btnFit=new Button(I18n.get("Canvas.Fit.Button")); btnFit.setOnAction(e->fitToWindow()); btnFit.setTooltip(new Tooltip(I18n.get("Canvas.Fit.Tooltip")));
        zoomLabel.setMinWidth(50); zoomIndicator.setPrefWidth(80);
        actionBarSpacer=new Region(); HBox.setHgrow(actionBarSpacer, Priority.ALWAYS);
        saveButton=new Button(I18n.get("Canvas.SaveImage.Button")); saveButton.setOnAction(e->saveImage()); saveButton.setTooltip(new Tooltip(I18n.get("Canvas.SaveImage.Tooltip")));
        printButton=new Button(I18n.get("Canvas.Print.Button")); printButton.setOnAction(e->printImage());
        bar.getChildren().addAll(zoomTitleLabel,zoomLabel,zoomIndicator,btnZoomIn,btnZoomOut,btnReset,btnFit,actionBarSpacer,saveButton,printButton);
        bar.prefWidthProperty().bind(widthProperty());
        // Listen for width changes to adaptively hide lower-priority items so right-side buttons remain visible
        bar.widthProperty().addListener((o,a,b)-> applyResponsiveActionBarLayout(b.doubleValue()));
        updateButtonDisableStates();
        return bar;
    }

    private void setupLayout(){
        setCenter(scrollPane);
        VBox bottom = new VBox(actionBar);
        bottom.setFillWidth(true);
        setBottom(bottom);
        canvas.setWidth(canvasWidth); canvas.setHeight(canvasHeight);
        widthProperty().addListener((o,a,b)-> relayout());
        heightProperty().addListener((o,a,b)-> relayout());
        scrollPane.widthProperty().addListener((o,a,b)-> relayout());
        scrollPane.heightProperty().addListener((o,a,b)-> relayout());
        // Initial responsive pass (after scene width is known later a second pass will occur)
        applyResponsiveActionBarLayout(getWidth());
    }

    // Removed shortcut label (tooltip-style hint) per UX simplification request

    private void relayout(){ Bounds vp=scrollPane.getViewportBounds(); if(vp!=null) requestResponsiveLayout(vp); }

    @Override protected void layoutChildren(){
        super.layoutChildren();
        double w=getWidth(), h=getHeight();
        if(Math.abs(w-lastLayoutWidth)>1 || Math.abs(h-lastLayoutHeight)>1){
            lastLayoutWidth=w; lastLayoutHeight=h; relayout();
        }
    }

    private void setupHandlers(){
        canvas.setOnScroll(this::onScroll);
        // Use explicit secondary-button detection; ignore Ctrl+Primary synthesis on mac
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if(e.getButton()==MouseButton.SECONDARY){ ensureContextMenu(); sharedContextMenu.show(canvas, e.getScreenX(), e.getScreenY()); e.consume(); }
        });
        // No panning handlers
        setOnKeyPressed(this::onKey);
        setFocusTraversable(true);
        canvas.widthProperty().addListener((o,a,b)-> redraw());
        canvas.heightProperty().addListener((o,a,b)-> redraw());
        scrollPane.viewportBoundsProperty().addListener((o,a,b)->{ if(b!=null){ updateButtonDisableStates(); requestResponsiveLayout(b); if(autoFitActive && !fitting){ Platform.runLater(this::fitToWindow); } } });
    }

    private void requestResponsiveLayout(Bounds nb){
        lastLayoutRequestNanos = System.nanoTime();
        long scheduled = lastLayoutRequestNanos;
        Platform.runLater(() -> { if(scheduled==lastLayoutRequestNanos) updateCanvasSize(nb); });
    }

    private void onScroll(ScrollEvent e){ if(e.isControlDown()){ e.consume(); if(e.getDeltaY()>0) zoomIn(); else zoomOut(); }}
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

    private void updateCanvasSize(Bounds nb){
        double viewportWidth = nb.getWidth();
        if(currentNode==null) return;
        double logicalAvailable = viewportWidth / getZoom();
        calculateCanvasSize(logicalAvailable); // initial pass
        // Safety: iteratively reduce columns if scaled content still wider than viewport (handles rounding / border / fringe math)
        int count = currentNode.getChildren()!=null ? currentNode.getChildren().size() : 0;
        if(count>0){
            double scaledContentWidth = canvasWidth * getZoom();
            int guard=0;
            while(scaledContentWidth > viewportWidth && elementsInRow>1 && guard<100){
                elementsInRow--;
                int rows = (int)Math.ceil((double)count / elementsInRow);
                int contentWidth=(elementsInRow*(FEATURE_ELEMENT_WIDTH+FRINGE_WIDTH))+FRINGE_WIDTH;
                canvasHeight = ((FEATURE_ELEMENT_HEIGHT+FRINGE_WIDTH)*rows)+(FRINGE_WIDTH*2)+BORDER_WIDTH;
                if(!(currentNode instanceof com.nebulon.xml.fddi.Feature)){
                    GraphicsContext gc=canvas.getGraphicsContext2D();
                    gc.setFont(textFont);
                    canvasHeight += CenteredTextDrawerFX.getTitleTextHeight(gc,currentNode.getName(),contentWidth);
                }
                canvasWidth = contentWidth + (2*BORDER_WIDTH);
                scaledContentWidth = canvasWidth * getZoom();
                guard++;
            }
        }
        // MODIFIED: Set canvas to its calculated width, not the viewport width, to break the layout loop.
        canvas.setWidth(canvasWidth);
        canvas.setHeight(Math.max(canvasHeight * getZoom(), nb.getHeight()));
        redraw();
    }

    private void calculateCanvasSize(double availableWidth){
        canvasHeight = FEATURE_ELEMENT_HEIGHT + (FRINGE_WIDTH*2) + FRINGE_WIDTH + BORDER_WIDTH;
        if(hasChildren()){
            int count=currentNode.getChildren().size();
            if(!fitLayoutLock){
                int natural=(count*(FRINGE_WIDTH+FEATURE_ELEMENT_WIDTH))+FRINGE_WIDTH;
                double usable=Math.max(FRINGE_WIDTH+FEATURE_ELEMENT_WIDTH, availableWidth-(2*BORDER_WIDTH));
                if(natural<=usable) elementsInRow=count; else elementsInRow=Math.max(1,(int)Math.floor((usable-FRINGE_WIDTH)/(FRINGE_WIDTH+FEATURE_ELEMENT_WIDTH)));
            }
            int rows=(int)Math.ceil((double)count/elementsInRow);
            canvasHeight=((FEATURE_ELEMENT_HEIGHT+FRINGE_WIDTH)*rows)+(FRINGE_WIDTH*2)+BORDER_WIDTH;
        }
        int contentWidth=(elementsInRow*(FEATURE_ELEMENT_WIDTH+FRINGE_WIDTH))+FRINGE_WIDTH;
        if(!(currentNode instanceof com.nebulon.xml.fddi.Feature)){
            GraphicsContext gc=canvas.getGraphicsContext2D();
            gc.setFont(textFont);
            canvasHeight += CenteredTextDrawerFX.getTitleTextHeight(gc,currentNode.getName(),contentWidth);
        }
        canvasWidth=contentWidth+(2*BORDER_WIDTH);
    }

    private boolean hasChildren(){ return currentNode!=null && !currentNode.getChildren().isEmpty(); }

    // --- Zoom API ---
    public void zoomIn(){ autoFitActive=false; setZoom(Math.min(MAX_ZOOM, getZoom()*ZOOM_FACTOR)); }
    public void zoomOut(){ autoFitActive=false; setZoom(Math.max(MIN_ZOOM, getZoom()/ZOOM_FACTOR)); }
    public void resetZoom(){ autoFitActive=false; setZoom(1.0); }
    public void fitToWindow(){
        if(currentNode==null) return; Bounds vp=scrollPane.getViewportBounds(); if(vp==null) return;
        fitting = true;
        net.sourceforge.fddtools.service.LoggingService.Span span = net.sourceforge.fddtools.service.LoggingService.getInstance().startPerf("fitToWindow", java.util.Map.of("action","fit"));
        int count = currentNode.getChildren()!=null ? currentNode.getChildren().size() : 0;
        if(count<=0){ calculateCanvasSize(Double.MAX_VALUE); double scale=computeFitScale(vp.getWidth(), vp.getHeight()); span.metric("targetScale", String.format(java.util.Locale.US, "%.3f", scale)); setZoom(scale); span.close(); return; }
        GraphicsContext gc=canvas.getGraphicsContext2D(); gc.setFont(textFont);
        double bestScale=MIN_ZOOM; int bestCols=1; double bestWidth=canvasWidth, bestHeight=canvasHeight;
        for(int cols=1; cols<=count; cols++){
            int rows = (int)Math.ceil((double)count/cols);
            int contentWidth=(cols*(FEATURE_ELEMENT_WIDTH+FRINGE_WIDTH))+FRINGE_WIDTH;
            double titleHeight=0; if(!(currentNode instanceof com.nebulon.xml.fddi.Feature)) titleHeight=CenteredTextDrawerFX.getTitleTextHeight(gc,currentNode.getName(),contentWidth);
            double width=contentWidth+(2*BORDER_WIDTH);
            double height=((FEATURE_ELEMENT_HEIGHT+FRINGE_WIDTH)*rows)+(FRINGE_WIDTH*2)+BORDER_WIDTH+titleHeight;
            double scale=Math.min(vp.getWidth()/width, vp.getHeight()/height);
            if(scale>MAX_ZOOM) scale=MAX_ZOOM; if(scale<MIN_ZOOM) scale=MIN_ZOOM;
            if(scale>bestScale){ bestScale=scale; bestCols=cols; bestWidth=width; bestHeight=height; }
        }
        elementsInRow=bestCols; canvasWidth=bestWidth; canvasHeight=bestHeight; fitLayoutLock=true;
        span.metric("targetScale", String.format(java.util.Locale.US, "%.3f", bestScale)).metric("cols", bestCols).metric("rows", (int)Math.ceil((double)count/bestCols));
        setZoom(bestScale);
        fitLayoutLock=false; autoFitActive=true; fitting=false; span.close();
    }
    public void setZoom(double z){ double f=Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, z)); if(Math.abs(f-getZoom())<0.0001){ updateZoomUI(); return;} zoomLevel.set(f); updateZoomUI(); }
    private void persistZoom(){ try { net.sourceforge.fddtools.util.PreferencesService.getInstance().setLastZoomLevel(getZoom()); net.sourceforge.fddtools.util.PreferencesService.getInstance().flushNow(); } catch (Exception ignored) {} }
    { // instance initializer to hook zoom listener for persistence
        zoomLevel.addListener((o,a,b)-> persistZoom());
    }
    public void restoreLastZoomIfEnabled(){ var prefs=net.sourceforge.fddtools.util.PreferencesService.getInstance(); if(prefs.isRestoreLastZoomEnabled()) { setZoom(prefs.getLastZoomLevel()); } }
    public double getZoom(){ return zoomLevel.get(); }
    public ReadOnlyDoubleProperty zoomLevelProperty(){ return zoomLevel.getReadOnlyProperty(); }

    private void updateZoomUI(){ Platform.runLater(()->{ double z=getZoom(); zoomLabel.setText(String.format("%.0f%%", z*100)); zoomIndicator.setProgress(z/MAX_ZOOM); Bounds vp=scrollPane.getViewportBounds(); if(vp!=null) updateCanvasSize(vp); updateButtonDisableStates(); }); }
    private void updateButtonDisableStates(){ if(btnZoomIn==null) return; double z=getZoom(); final double EPS=0.0001; btnZoomIn.setDisable(z>=(MAX_ZOOM-EPS)); btnZoomOut.setDisable(z<=(MIN_ZOOM+EPS)); btnReset.setDisable(Math.abs(z-1.0)<0.001); Bounds vp=scrollPane.getViewportBounds(); if(vp==null) btnFit.setDisable(true); else { double fit=computeFitScale(vp.getWidth(), vp.getHeight()); btnFit.setDisable(Math.abs(z-fit)<0.01);} }
    private double computeFitScale(double vw,double vh){
        // Compute scale that fits both width and height exactly (no 90% shrink) while honoring min/max
        calculateCanvasSize(Double.MAX_VALUE);
        double scale = Math.min(vw / canvasWidth, vh / canvasHeight);
        if(scale<MIN_ZOOM) scale=MIN_ZOOM; else if(scale>MAX_ZOOM) scale=MAX_ZOOM; return scale; }

    /**
     * Apply a simple priority-based responsive layout so that when horizontal space shrinks
     * the lower-priority text/controls are hidden (managed=false) before the right-side
     * Save/Print buttons are clipped. This avoids horizontal clipping while keeping core
     * zoom interactions accessible. Thresholds chosen empirically; easy to adjust.
     */
    private void applyResponsiveActionBarLayout(double availableWidth){
        if(actionBarSpacer==null || actionBar==null) return; // Not yet initialized
        restoreFullActionBarState();
        double margin = 16;
        double current = computeChildrenWidth();
        if(current <= availableWidth - margin) return; // Fits
        // Collapse order: remove lowest semantic cost first
        javafx.scene.Node[] collapse = new javafx.scene.Node[]{ zoomTitleLabel, zoomLabel, zoomIndicator, btnFit, btnReset };
        for(javafx.scene.Node n : collapse){
            if(current <= availableWidth - margin) break;
            showNode(n,false);
            current = computeChildrenWidth();
        }
        if(current <= availableWidth - margin) return;
        // Switch save/print to icon-only before hiding them entirely
        if(saveButton!=null && saveButton.getText()!=null && saveButton.getText().equals(originalSaveText)){
            saveButton.setText("💾");
            current = computeChildrenWidth();
        }
        if(current <= availableWidth - margin) return;
        if(printButton!=null && printButton.getText()!=null && printButton.getText().equals(originalPrintText)){
            printButton.setText("🖨");
            current = computeChildrenWidth();
        }
        if(current <= availableWidth - margin) return;
        // As last resort hide save then print (rare very narrow)
        if(current > availableWidth - margin && saveButton!=null){ showNode(saveButton,false); current = computeChildrenWidth(); }
        if(current > availableWidth - margin && printButton!=null){ showNode(printButton,false); }
    }
    private void restoreFullActionBarState(){
        // Show all base nodes
        showNode(zoomTitleLabel,true);
        showNode(zoomLabel,true);
        showNode(zoomIndicator,true);
        showNode(btnReset,true);
        showNode(btnFit,true);
        showNode(saveButton,true);
        showNode(printButton,true);
        if(saveButton!=null && !saveButton.getText().equals(originalSaveText)) saveButton.setText(originalSaveText);
        if(printButton!=null && !printButton.getText().equals(originalPrintText)) printButton.setText(originalPrintText);
        // Force layout pass so pref widths are accurate before measurement
        actionBar.applyCss(); actionBar.layout();
    }
    private double computeChildrenWidth(){
        double total=0;
        for(javafx.scene.Node n: actionBar.getChildren()) if(n.isManaged()) total += n.prefWidth(-1) + 8; // 8 is spacing heuristic
        return total;
    }
    private void showNode(javafx.scene.Node n, boolean show){ if(n!=null){ n.setVisible(show); n.setManaged(show); }}

    // --- Node / font / redraw ---
    public void setCurrentNode(FDDINode node){ this.currentNode=node; Platform.runLater(()->{ Bounds vp=scrollPane.getViewportBounds(); if(vp!=null) updateCanvasSize(vp); else redraw(); }); }
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
            int contentWidth=(elementsInRow*(FEATURE_ELEMENT_WIDTH+FRINGE_WIDTH))+FRINGE_WIDTH;
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
        for(FDDTreeNode tn: currentNode.getChildren()){
            FDDINode child=(FDDINode)tn;
            FDDGraphicFX g=new FDDGraphicFX(child,x+currentX,y+currentY,FEATURE_ELEMENT_WIDTH,FEATURE_ELEMENT_HEIGHT);
            g.draw(gc);
            currentWidth=currentX+g.getWidth()+FRINGE_WIDTH;
            if(currentWidth>imgWidth) imgWidth=currentWidth;
            currentHeight=currentY+g.getHeight()+FRINGE_WIDTH;
            if((currentWidth+g.getWidth()+FRINGE_WIDTH)>maxWidth){
                currentX=FRINGE_WIDTH;
                currentY+=(g.getHeight()+FRINGE_WIDTH);
            } else {
                currentX+=(g.getWidth()+FRINGE_WIDTH);
            }
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
            javafx.concurrent.Task<File> task = new javafx.concurrent.Task<>(){
                @Override protected File call() throws Exception {
                    updateMessage("Rendering...");
                    String ext=getExt(f.getName());
                    if(!ext.equalsIgnoreCase("png")) ext = "png"; // force png only
                    updateProgress(30,100);
                    updateMessage("Encoding PNG...");
                    net.sourceforge.fddtools.service.ImageExportService.getInstance().export(canvas, f, ext);
                    updateProgress(100,100); updateMessage("Done"); return f;
                }
            };
            net.sourceforge.fddtools.service.BusyService.getInstance().runAsync("Export Image", task, true, true,
                () -> { LOGGER.info("Image saved: {}", f.getAbsolutePath()); net.sourceforge.fddtools.service.LoggingService.getInstance().audit("imageExport", java.util.Map.of("action","exportImage"), f::getName); },
                () -> { new Alert(Alert.AlertType.ERROR, "Failed: "+task.getException().getMessage()).showAndWait(); net.sourceforge.fddtools.service.LoggingService.getInstance().audit("imageExportFail", java.util.Map.of("action","exportImage"), () -> task.getException().getClass().getSimpleName()); });
        }
    }
    private String getExt(String n){ int i=n.lastIndexOf('.'); return i>0? n.substring(i+1):""; }
    // Export conversion logic moved to ImageExportService (kept method removed)
    private void printImage(){ new Alert(Alert.AlertType.INFORMATION,"Print functionality will be implemented in a future version.").showAndWait(); }
    public void reflow(){ Platform.runLater(()->{ Bounds vp=scrollPane.getViewportBounds(); if(vp!=null) updateCanvasSize(vp); else redraw(); }); }

    // BEGIN TEST ACCESSOR
    /** Test-only accessor for verifying responsive layout calculations. */
    public int getElementsInRowForTest() { return elementsInRow; }
    // END TEST ACCESSOR
}