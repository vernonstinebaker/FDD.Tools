package net.sourceforge.fddtools.ui.fx;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.css.PseudoClass;
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
            try { var img = cell.snapshot(null, null); if (img!=null) db.setDragView(img, img.getWidth()/4, img.getHeight()/2); } catch (Exception ex) { LOGGER.debug("Snapshot for drag view failed: {}", ex.getMessage()); }
            e.consume();
        });
    cell.setOnDragOver(e -> {
            FDDINode dragSource = tree.dragSourceNode;
            FDDINode target = cell.getItem();
            if (dragSource == null || target == null) { e.consume(); return; }
            if (dragSource == target) { clear(cell, DROP_TARGET, INSERT_BEFORE, INSERT_AFTER); e.consume(); return; }
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
                // Freeze scroll unless near edges
                if (dragActive) maintainScrollLock(nearEdge);
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
                switch (dt){
                    case INTO -> {
                        if (tree.isValidReparent(dragSource, target)) {
                            CommandExecutionService.getInstance().execute(new MoveNodeCommand(dragSource, target));
                            // Incremental UI update avoids full refresh (prevents scroll jump)
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
                if (!success){ showTransientTooltip(cell, invalidReason(dragSource, target, dt)); LOGGER.debug("Invalid drop {} from {} to {}", dt, dragSource.getName(), target.getName()); }
            }
            clear(cell, DROP_TARGET, INSERT_BEFORE, INSERT_AFTER);
            e.setDropCompleted(success);
            if (success) restoreScrollPositionAsync();
            // avoid tree.refresh() to keep viewport stable
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
    cell.setOnDragDone(e -> { tree.dragSourceNode = null; clear(cell, DROP_TARGET, INSERT_BEFORE, INSERT_AFTER); cancelAutoExpand(state); restoreScrollPositionAsync(); dragActive=false; lockedV=null; });
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
            catch (Exception ex){ LOGGER.debug("Auto-expand failed: {}", ex.getMessage()); }
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
    private void maintainScrollLock(boolean nearEdge){
        ensureVBar(); if (vbar == null) return;
        if (!nearEdge) {
            if (lockedV == null) lockedV = vbar.getValue();
            vbar.setValue(lockedV);
        } else {
            lockedV = vbar.getValue();
        }
    }

    private void restoreScrollPositionAsync(){
        if (lockedV == null) return; ensureVBar(); if (vbar == null) return;
        Platform.runLater(() -> {
            try { vbar.setValue(lockedV); } catch (Exception ignored) {}
        });
    }
}
