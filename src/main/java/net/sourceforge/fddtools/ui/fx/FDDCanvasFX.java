package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.geometry.Bounds;
import javafx.geometry.BoundingBox;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
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
    private final Label zoomLabel = new Label("100%");
    private final ProgressBar zoomIndicator = new ProgressBar(1.0 / MAX_ZOOM);
    private final HBox actionBar;
    // Action bar components for responsive hiding
    private Label zoomTitleLabel;
    private Region actionBarSpacer;
    private Button saveButton, printButton;
    private String originalSaveText = "Save Image";
    private String originalPrintText = "Print";
    private Button btnZoomIn, btnZoomOut, btnReset, btnFit;

    private FDDINode currentNode;
    private Font textFont;
    private final ReadOnlyDoubleWrapper zoomLevel = new ReadOnlyDoubleWrapper(this, "zoomLevel", 1.0);
    private double canvasWidth = 800, canvasHeight = 600;
    private int elementsInRow = 1;
    private double lastLayoutWidth = -1, lastLayoutHeight = -1;
    private long lastLayoutRequestNanos = 0;
    private double lastMouseX, lastMouseY; private boolean isDragging;

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
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background:white;-fx-border-color:#ccc;");
    }

    private HBox createActionBar(){
    HBox bar = new HBox(8);
    bar.setAlignment(Pos.CENTER_LEFT);
    bar.setStyle("-fx-background-color:linear-gradient(to bottom,#fafafa,#e8e8e8);-fx-padding:6;-fx-border-color:#ccc;-fx-border-width:1 0 0 0;");
    zoomTitleLabel=new Label("Zoom:");
    btnZoomIn=new Button("+"); btnZoomIn.setOnAction(e->zoomIn()); btnZoomIn.setTooltip(new Tooltip("Zoom In"));
    btnZoomOut=new Button("-"); btnZoomOut.setOnAction(e->zoomOut()); btnZoomOut.setTooltip(new Tooltip("Zoom Out"));
    btnReset=new Button("Reset"); btnReset.setOnAction(e->resetZoom()); btnReset.setTooltip(new Tooltip("Reset to 100%"));
    btnFit=new Button("Fit"); btnFit.setOnAction(e->fitToWindow()); btnFit.setTooltip(new Tooltip("Scale to fit window"));
    zoomLabel.setMinWidth(50); zoomIndicator.setPrefWidth(80);
    actionBarSpacer=new Region(); HBox.setHgrow(actionBarSpacer, Priority.ALWAYS);
    saveButton=new Button("Save Image"); saveButton.setOnAction(e->saveImage()); saveButton.setTooltip(new Tooltip("Export current canvas to an image file"));
    printButton=new Button("Print"); printButton.setOnAction(e->printImage());
    bar.getChildren().addAll(zoomTitleLabel,zoomLabel,zoomIndicator,btnZoomIn,btnZoomOut,btnReset,btnFit,actionBarSpacer,saveButton,printButton);
    bar.prefWidthProperty().bind(widthProperty());
    // Listen for width changes to adaptively hide lower-priority items so right-side buttons remain visible
    bar.widthProperty().addListener((o,a,b)-> applyResponsiveActionBarLayout(b.doubleValue()));
    updateButtonDisableStates();
    return bar;
    }

    private void setupLayout(){
        setCenter(scrollPane);
        VBox bottom = new VBox(actionBar, createShortcutLabel());
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

    private Label createShortcutLabel(){
        Label l=new Label("\uD83D\uDCA1 Ctrl+Scroll: Zoom | Drag: Pan | Space+Drag: Pan");
        l.setStyle("-fx-padding:4;-fx-font-size:10px;-fx-text-fill:#666;");
        return l;
    }

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
        canvas.setOnMousePressed(this::onPress);
        canvas.setOnMouseDragged(this::onDrag);
        canvas.setOnMouseReleased(e->{isDragging=false; canvas.setCursor(Cursor.DEFAULT);});
        canvas.setOnContextMenuRequested(e->{ ContextMenu m=ctxMenu(); m.show(canvas,e.getScreenX(), e.getScreenY());});
        setOnKeyPressed(this::onKey);
        setFocusTraversable(true);
        canvas.widthProperty().addListener((o,a,b)-> redraw());
        canvas.heightProperty().addListener((o,a,b)-> redraw());
        scrollPane.viewportBoundsProperty().addListener((o,a,b)->{ if(b!=null){ updateButtonDisableStates(); requestResponsiveLayout(b);} });
    }

    private void requestResponsiveLayout(Bounds nb){
        lastLayoutRequestNanos = System.nanoTime();
        long scheduled = lastLayoutRequestNanos;
        Platform.runLater(() -> { if(scheduled==lastLayoutRequestNanos) updateCanvasSize(nb); });
    }

    private void onScroll(ScrollEvent e){ if(e.isControlDown()){ e.consume(); if(e.getDeltaY()>0) zoomIn(); else zoomOut(); }}
    private void onPress(MouseEvent e){ if(e.isPrimaryButtonDown()){ lastMouseX=e.getX(); lastMouseY=e.getY(); isDragging=true; canvas.setCursor(Cursor.CLOSED_HAND);} }
    private void onDrag(MouseEvent e){ if(isDragging && e.isPrimaryButtonDown()){ double dx=e.getX()-lastMouseX, dy=e.getY()-lastMouseY; scrollPane.setHvalue(Math.max(0,Math.min(1, scrollPane.getHvalue() - (dx / canvas.getWidth())*0.1))); scrollPane.setVvalue(Math.max(0,Math.min(1, scrollPane.getVvalue() - (dy / canvas.getHeight())*0.1))); lastMouseX=e.getX(); lastMouseY=e.getY(); }}
    private void onKey(KeyEvent e){ if(e.isControlDown()){ switch(e.getCode()){ case PLUS: case EQUALS: zoomIn(); e.consume(); break; case MINUS: zoomOut(); e.consume(); break; case DIGIT0: resetZoom(); e.consume(); break; default: } } else if(e.getCode()== KeyCode.SPACE){ e.consume(); }}

    private ContextMenu ctxMenu(){ ContextMenu m=new ContextMenu(); MenuItem in=new MenuItem("Zoom In"); in.setOnAction(e->zoomIn()); MenuItem out=new MenuItem("Zoom Out"); out.setOnAction(e->zoomOut()); MenuItem reset=new MenuItem("Reset Zoom"); reset.setOnAction(e->resetZoom()); MenuItem fit=new MenuItem("Fit to Window"); fit.setOnAction(e->fitToWindow()); MenuItem save=new MenuItem("Save as Image..."); save.setOnAction(e->saveImage()); MenuItem print=new MenuItem("Print..."); print.setOnAction(e->printImage()); MenuItem props=new MenuItem("Properties"); props.setDisable(true); m.getItems().addAll(in,out,reset,fit,new SeparatorMenuItem(),save,print,new SeparatorMenuItem(),props); return m; }

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
        canvas.setWidth(viewportWidth); // fill available area exactly
        canvas.setHeight(Math.max(canvasHeight * getZoom(), nb.getHeight()));
        redraw();
    }

    private void calculateCanvasSize(double availableWidth){
        canvasHeight = FEATURE_ELEMENT_HEIGHT + (FRINGE_WIDTH*2) + FRINGE_WIDTH + BORDER_WIDTH;
        if(hasChildren()){
            int count=currentNode.getChildren().size();
            int natural=(count*(FRINGE_WIDTH+FEATURE_ELEMENT_WIDTH))+FRINGE_WIDTH;
            double usable=Math.max(FRINGE_WIDTH+FEATURE_ELEMENT_WIDTH, availableWidth-(2*BORDER_WIDTH));
            if(natural<=usable) elementsInRow=count; else elementsInRow=Math.max(1,(int)Math.floor((usable-FRINGE_WIDTH)/(FRINGE_WIDTH+FEATURE_ELEMENT_WIDTH)));
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
    public void zoomIn(){ setZoom(Math.min(MAX_ZOOM, getZoom()*ZOOM_FACTOR)); }
    public void zoomOut(){ setZoom(Math.max(MIN_ZOOM, getZoom()/ZOOM_FACTOR)); }
    public void resetZoom(){ setZoom(1.0); }
    public void fitToWindow(){ if(currentNode==null) return; Bounds vp=scrollPane.getViewportBounds(); if(vp==null) return; net.sourceforge.fddtools.service.LoggingService.Span span = net.sourceforge.fddtools.service.LoggingService.getInstance().startPerf("fitToWindow", java.util.Map.of("action","fit")); calculateCanvasSize(Double.MAX_VALUE); double scale=computeFitScale(vp.getWidth(), vp.getHeight()); span.metric("targetScale", String.format(java.util.Locale.US, "%.3f", scale)); setZoom(scale); span.close(); }
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
    private double computeFitScale(double vw,double vh){ calculateCanvasSize(Double.MAX_VALUE); double scale=Math.min(vw/canvasWidth, vh/canvasHeight)*0.9; if(scale<MIN_ZOOM) scale=MIN_ZOOM; else if(scale>MAX_ZOOM) scale=MAX_ZOOM; return scale; }

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
    private void drawGraphics(GraphicsContext gc){ gc.setStroke(Color.BLACK); gc.setFill(Color.BLACK); if(hasChildren()){ int contentWidth=(elementsInRow*(FEATURE_ELEMENT_WIDTH+FRINGE_WIDTH))+FRINGE_WIDTH; double titleHeight=CenteredTextDrawerFX.getTitleTextHeight(gc,currentNode.getName(),contentWidth); CenteredTextDrawerFX.draw(gc,currentNode.getName(),BORDER_WIDTH,BORDER_WIDTH+FRINGE_WIDTH,contentWidth); Bounds sub=drawChildren(gc,BORDER_WIDTH,(int)(titleHeight+FRINGE_WIDTH+BORDER_WIDTH),contentWidth); gc.setStroke(Color.GRAY); gc.setLineWidth(2); gc.strokeRect(0,0, sub.getWidth()+(2*BORDER_WIDTH), sub.getHeight()+titleHeight+FRINGE_WIDTH+(2*BORDER_WIDTH)); gc.strokeRect(BORDER_WIDTH,BORDER_WIDTH, sub.getWidth(), sub.getHeight()+titleHeight+FRINGE_WIDTH); } else { new FDDGraphicFX(currentNode,FRINGE_WIDTH,FRINGE_WIDTH,FEATURE_ELEMENT_WIDTH,FEATURE_ELEMENT_HEIGHT).draw(gc); }}
    private Bounds drawChildren(GraphicsContext gc,int x,int y,int maxWidth){ double currentX=FRINGE_WIDTH,currentY=FRINGE_WIDTH,currentHeight=FRINGE_WIDTH,currentWidth=FRINGE_WIDTH,imgWidth=0; for(FDDTreeNode tn: currentNode.getChildren()){ FDDINode child=(FDDINode)tn; FDDGraphicFX g=new FDDGraphicFX(child,x+currentX,y+currentY,FEATURE_ELEMENT_WIDTH,FEATURE_ELEMENT_HEIGHT); g.draw(gc); currentWidth=currentX+g.getWidth()+FRINGE_WIDTH; if(currentWidth>imgWidth) imgWidth=currentWidth; currentHeight=currentY+g.getHeight()+FRINGE_WIDTH; if((currentWidth+g.getWidth()+FRINGE_WIDTH)>maxWidth){ currentX=FRINGE_WIDTH; currentY+=(g.getHeight()+FRINGE_WIDTH);} else { currentX+=(g.getWidth()+FRINGE_WIDTH);} } return new BoundingBox(0,0,imgWidth,currentHeight); }

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