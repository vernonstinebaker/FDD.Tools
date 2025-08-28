package net.sourceforge.fddtools.ui.fx;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.scene.control.TreeCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.Duration;
import net.sourceforge.fddtools.command.CommandExecutionService;
import net.sourceforge.fddtools.command.MoveNodeCommand;
import net.sourceforge.fddtools.model.FDDINode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller extracting drag & drop logic from {@link FDDTreeViewFX} to reduce
 * monolithic cell complexity and concentrate DnD behavior in one place.
 */
class FDDTreeDragAndDropController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FDDTreeDragAndDropController.class);
    /** Custom data format for intra-tree identification (string id fallback). */
    static final DataFormat FDD_NODE_FORMAT = new DataFormat("application/x-fdd-node-id");
    enum DropType { INTO, BEFORE, AFTER }

    // Thresholds (fraction of cell height) for before/after classification; center region is INTO.
    static final double BEFORE_THRESHOLD = 0.25; // y < 25% => BEFORE
    static final double AFTER_THRESHOLD = 0.75;  // y > 75% => AFTER

    private final FDDTreeViewFX tree;
    private ScrollBar vbar; // vertical scroll bar cache
    private boolean dragActive = false;
    private Double lockedV = null;
    
    // Edge scrolling configuration
    private static final double EDGE_SCROLL_THRESHOLD = 20.0; // pixels from edge to trigger scrolling
    private static final double SCROLL_SPEED = 0.02; // scroll increment per timer tick
    private Timeline edgeScrollTimer;
    private boolean isScrollingUp = false;
    private boolean isScrollingDown = false;
    FDDTreeDragAndDropController(FDDTreeViewFX tree){ this.tree = tree; }

    void attachTo(TreeCell<FDDINode> cell){
        CellState state = new CellState();
        cell.getProperties().put(CellState.KEY, state);
        final PseudoClass DROP_TARGET = PseudoClass.getPseudoClass("drop-target");
        final PseudoClass INSERT_BEFORE = PseudoClass.getPseudoClass("drop-insert-before");
        final PseudoClass INSERT_AFTER = PseudoClass.getPseudoClass("drop-insert-after");

        cell.setOnDragDetected(e -> {
            FDDINode item = cell.getItem();
            if (item == null) return;
            tree.dragSourceNode = item;
            dragActive = true; ensureVBar(); if (vbar != null) lockedV = vbar.getValue();
            Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            String id = item.getId() != null ? item.getId() : item.getName();
            content.put(FDD_NODE_FORMAT, id); content.putString(id);
            db.setContent(content);
            try { var img = cell.snapshot(null, null); if (img!=null) db.setDragView(img, img.getWidth()/4, img.getHeight()/2); } catch (Exception ignored) { }
            e.consume();
        });
    cell.setOnDragOver(e -> {
            FDDINode dragSource = tree.dragSourceNode;
            FDDINode target = cell.getItem();
            if (dragSource == null || target == null) { e.consume(); return; }
            if (dragSource == target) { clear(cell, DROP_TARGET, INSERT_BEFORE, INSERT_AFTER); e.consume(); return; }
            
            // Check for edge scrolling first
            handleEdgeScrolling(e.getSceneY());
            
            boolean okInto = tree.isValidReparent(dragSource, target);
            DropType dropType = deriveDropType(e.getY(), cell.getHeight());
            boolean ok = switch (dropType){
                case INTO -> okInto;
                case BEFORE, AFTER -> canInsertSibling(dragSource, target);
            };
            if (ok){
                e.acceptTransferModes(TransferMode.MOVE);
                apply(cell, dropType, DROP_TARGET, INSERT_BEFORE, INSERT_AFTER);
                // Only auto-expand when hovering the center (INTO) and near edges to avoid mid-view scroll
                boolean nearEdge = shouldAutoExpand(cell, /*edgeOnlyDefault*/ true);
                if (dropType == DropType.INTO && nearEdge) scheduleAutoExpand(cell, state); else cancelAutoExpand(state);
                // During edge scrolling, don't maintain scroll lock - allow controlled scrolling
                if (dragActive && !isScrollingUp && !isScrollingDown) maintainScrollLockAlways();
            } else {
                clear(cell, DROP_TARGET, INSERT_BEFORE, INSERT_AFTER);
                cancelAutoExpand(state);
            }
            state.currentDropType = ok? dropType : null;
            e.consume();
        });
        cell.setOnDragDropped(e -> {
            boolean success = false;
            FDDINode dragSource = tree.dragSourceNode;
            FDDINode target = cell.getItem();
            if (dragSource != null && target != null && dragSource != target){
                DropType dt = state.currentDropType == null ? DropType.INTO : state.currentDropType;
                
                // Capture the current scroll position BEFORE any changes
                double preservedScrollPosition = captureV();
                
                // Enable scroll suppression to prevent automatic scrolling during move
                tree.setSuppressAutoScroll(true);
                
                try {
                    switch (dt){
                        case INTO -> {
                            if (tree.isValidReparent(dragSource, target)) {
                                CommandExecutionService.getInstance().execute(new MoveNodeCommand(dragSource, target));
                                tree.updateAfterMove(dragSource, target, -1);
                                success = true;
                            }
                        }
                        case BEFORE, AFTER -> {
                            FDDINode parent = (FDDINode) target.getParentNode();
                            if (parent != null && canInsertSibling(dragSource, target)) {
                                int idx = parent.getChildren().indexOf(target); if (dt == DropType.AFTER) idx += 1;
                                CommandExecutionService.getInstance().execute(new MoveNodeCommand(dragSource, parent, idx));
                                tree.updateAfterMove(dragSource, parent, idx);
                                success = true;
                            }
                        }
                    }
                    
                    // After all operations are complete, restore the original scroll position
                    if (success && preservedScrollPosition >= 0) {
                        Platform.runLater(() -> {
                            ensureVBar();
                            if (vbar != null) {
                                vbar.setValue(preservedScrollPosition);
                            }
                            // Delay disabling suppression to allow updateAfterMove's deferred selection to complete
                            Platform.runLater(() -> {
                                tree.setSuppressAutoScroll(false);
                            });
                        });
                    } else {
                        // If not successful, just disable suppression
                        tree.setSuppressAutoScroll(false);
                    }
                } catch (Exception ex) {
                    LOGGER.error("Error during drag and drop operation", ex);
                    tree.setSuppressAutoScroll(false);
                }
                
                if (!success){ showTransientTooltip(cell, invalidReason(dragSource, target, dt)); }
            }
            clear(cell, DROP_TARGET, INSERT_BEFORE, INSERT_AFTER);
            e.setDropCompleted(success);
            e.consume();
        });
    cell.setOnDragEntered(e -> {
            FDDINode dragSource = tree.dragSourceNode; FDDINode target = cell.getItem();
            if (dragSource!=null && target!=null && dragSource!=target && tree.isValidReparent(dragSource, target)) {
        if (shouldAutoExpand(cell, /*edgeOnlyDefault*/ true)) scheduleAutoExpand(cell, state); else cancelAutoExpand(state);
            }
            e.consume();
        });
    cell.setOnDragExited(e -> { clear(cell, DROP_TARGET, INSERT_BEFORE, INSERT_AFTER); cancelAutoExpand(state); e.consume(); });
    cell.setOnDragDone(e -> { 
        tree.dragSourceNode = null; 
        clear(cell, DROP_TARGET, INSERT_BEFORE, INSERT_AFTER); 
        cancelAutoExpand(state); 
        stopEdgeScrolling(); // Stop any active edge scrolling
        restoreScrollPositionAsync(); 
        dragActive=false; 
        lockedV=null; 
    });
    }

    /**
     * Classify pointer Y within a cell to a drop type. Exposed (package) for unit tests.
     *  BEFORE: y < BEFORE_THRESHOLD * h
     *  AFTER:  y > AFTER_THRESHOLD * h
     *  INTO:   otherwise (center band, inclusive of thresholds)
     */
    static FDDTreeDragAndDropController.DropType deriveDropType(double y, double height){
        double h = height <= 0 ? 24 : height; // fallback typical row height
        if (y < h * BEFORE_THRESHOLD) return FDDTreeDragAndDropController.DropType.BEFORE;
        else if (y > h * AFTER_THRESHOLD) return FDDTreeDragAndDropController.DropType.AFTER;
        else return FDDTreeDragAndDropController.DropType.INTO;
    }

    // Package-visible for focused unit tests (sibling insertion eligibility)
    boolean canInsertSibling(FDDINode dragSource, FDDINode reference){ return FDDHierarchyRules.canInsertSibling(dragSource, reference, tree.isProgramBusinessLogicEnabled()); }

    private void apply(TreeCell<FDDINode> cell, FDDTreeDragAndDropController.DropType type, PseudoClass into, PseudoClass before, PseudoClass after){
        clear(cell, into, before, after);
        switch (type){
            case INTO -> cell.pseudoClassStateChanged(into, true);
            case BEFORE -> cell.pseudoClassStateChanged(before, true);
            case AFTER -> cell.pseudoClassStateChanged(after, true);
        }
    }
    private void clear(TreeCell<FDDINode> cell, PseudoClass into, PseudoClass before, PseudoClass after){
        cell.pseudoClassStateChanged(into,false); cell.pseudoClassStateChanged(before,false); cell.pseudoClassStateChanged(after,false);
    }

    private String invalidReason(FDDINode dragSource, FDDINode target, FDDTreeDragAndDropController.DropType type){
        if (type == FDDTreeDragAndDropController.DropType.INTO && !tree.isValidReparent(dragSource, target)) return "Cannot move under this target (hierarchy rule)";
        if ((type == FDDTreeDragAndDropController.DropType.BEFORE || type == FDDTreeDragAndDropController.DropType.AFTER) && !canInsertSibling(dragSource, target)) return "Cannot reorder here";
        return "Invalid drop";
    }

    private void showTransientTooltip(TreeCell<FDDINode> cell, String text){
        if (text==null || text.isBlank()) return; Tooltip tip = new Tooltip(text); Tooltip.install(cell, tip); PauseTransition hide = new PauseTransition(Duration.seconds(1.5)); hide.setOnFinished(ev-> Tooltip.uninstall(cell, tip)); hide.play(); }

    private void scheduleAutoExpand(TreeCell<FDDINode> cell, CellState state){
        if (System.getProperty("fdd.dnd.disableAutoExpand") != null) return; // feature toggle
        if (state.expandDelay != null) return; if (cell.getTreeItem()==null) return; if (cell.getTreeItem().isExpanded()) return;
        state.expandDelay = new PauseTransition(Duration.millis(700)); // slightly longer to reduce churn
        state.expandDelay.setOnFinished(ev -> {
            try { if (cell.getTreeItem()!=null && !cell.getTreeItem().isExpanded()) cell.getTreeItem().setExpanded(true); }
            catch (Exception ignored) {}
            finally { state.expandDelay = null; }
        });
        state.expandDelay.play();
    }

    /** Only auto-expand if cell is near viewport edges (top/bottom 20%) to avoid mid‑view scrolling. */
    private boolean shouldAutoExpand(TreeCell<FDDINode> cell, boolean edgeOnlyDefault){
        try {
            boolean edgeOnly = edgeOnlyDefault || System.getProperty("fdd.dnd.edgeExpandOnly") != null;
            if (cell.getScene()==null) return false;
            var pCell = cell.localToScene(0,0);
            var pTree = tree.localToScene(0,0);
            double treeHeight = tree.getHeight(); if (treeHeight <= 0) return true;
            double cellY = pCell.getY() - pTree.getY();
            double edgeBand = treeHeight * 0.20; // 20% top/bottom
            return !edgeOnly || cellY < edgeBand || cellY > (treeHeight - edgeBand);
        } catch (Exception ex){ return true; }
    }
    private void cancelAutoExpand(CellState state){ if (state.expandDelay != null){ state.expandDelay.stop(); state.expandDelay = null; } }

    private static class CellState { static final String KEY = "fdd.dnd.state"; FDDTreeDragAndDropController.DropType currentDropType; PauseTransition expandDelay; }

    private void ensureVBar(){
        if (vbar != null) return;
        try {
            if (tree.getScene()==null) return;
            var bars = tree.lookupAll(".scroll-bar");
            for (var n : bars) {
                if (n instanceof ScrollBar sb && sb.getOrientation() == javafx.geometry.Orientation.VERTICAL) { vbar = sb; break; }
            }
        } catch (Exception ignored) {}
    }
    
    /**
     * Always maintain scroll lock during drag operations.
     * This prevents JavaFX's automatic scrolling behavior that occurs when dragging near edges.
     * The scroll position will only change if explicitly controlled by our logic.
     */
    private void maintainScrollLockAlways(){
        ensureVBar(); 
        if (vbar == null) return;
        
        // Capture the initial locked position if not already set
        if (lockedV == null) {
            lockedV = vbar.getValue();
        }
        
        // Always restore the locked position to prevent any automatic scrolling
        if (Math.abs(vbar.getValue() - lockedV) > 0.001) {
            vbar.setValue(lockedV);
        }
    }
    private double captureV(){ try { ensureVBar(); return vbar==null? -1 : vbar.getValue(); } catch (Exception ex){ return -1; } }

    private void restoreScrollPositionAsync(){
        if (lockedV == null) return; ensureVBar(); if (vbar == null) return;
        Platform.runLater(() -> {
            try { vbar.setValue(lockedV); } catch (Exception ignored) {}
        });
    }
    
    /**
     * Handles edge scrolling during drag operations.
     * When the user drags near the top or bottom edge of the TreeView,
     * automatically scroll to reveal more drop targets.
     */
    private void handleEdgeScrolling(double sceneY) {
        if (!dragActive) return;
        
        ensureVBar();
        if (vbar == null) return;
        
        // Get the TreeView bounds in scene coordinates
        Bounds treeBounds = tree.localToScene(tree.getBoundsInLocal());
        double treeTop = treeBounds.getMinY();
        double treeBottom = treeBounds.getMaxY();
        
        // Check if we're near the top or bottom edge
        boolean nearTop = sceneY < (treeTop + EDGE_SCROLL_THRESHOLD);
        boolean nearBottom = sceneY > (treeBottom - EDGE_SCROLL_THRESHOLD);
        
        if (nearTop && !isScrollingUp) {
            startEdgeScrolling(true); // scroll up
        } else if (nearBottom && !isScrollingDown) {
            startEdgeScrolling(false); // scroll down
        } else if (!nearTop && !nearBottom) {
            stopEdgeScrolling();
        }
    }
    
    /**
     * Handles TreeView-level drag over events for edge scrolling detection.
     * This catches cases where the user drags outside visible cells.
     */
    void handleTreeViewDragOver(javafx.scene.input.DragEvent e) {
        if (dragActive && tree.dragSourceNode != null) {
            handleEdgeScrolling(e.getSceneY());
        }
    }
    
    /**
     * Starts continuous scrolling in the specified direction.
     */
    private void startEdgeScrolling(boolean scrollUp) {
        stopEdgeScrolling(); // Stop any existing scrolling
        
        isScrollingUp = scrollUp;
        isScrollingDown = !scrollUp;
        
        // Create a timer that continuously scrolls
        edgeScrollTimer = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            if (vbar != null && dragActive && (isScrollingUp || isScrollingDown)) {
                double currentValue = vbar.getValue();
                double newValue;
                
                if (scrollUp) {
                    newValue = Math.max(0, currentValue - SCROLL_SPEED);
                } else {
                    newValue = Math.min(1, currentValue + SCROLL_SPEED);
                }
                
                if (Math.abs(newValue - currentValue) > 0.001) {
                    vbar.setValue(newValue);
                    // Update locked position since this is user-initiated scrolling
                    lockedV = newValue;
                } else {
                    // No scroll applied: difference too small
                }
            } else {
                // Edge scroll timer skipped: conditions not met
            }
        }));
        edgeScrollTimer.setCycleCount(Timeline.INDEFINITE);
        edgeScrollTimer.play();
    }
    
    /**
     * Stops edge scrolling.
     */
    private void stopEdgeScrolling() {
        if (edgeScrollTimer != null) {
            edgeScrollTimer.stop();
            edgeScrollTimer = null;
        }
        
        isScrollingUp = false;
        isScrollingDown = false;
    }
}
