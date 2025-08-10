package net.sourceforge.fddtools.ui.fx;

import javafx.animation.PauseTransition;
import javafx.css.PseudoClass;
import javafx.scene.control.TreeCell;
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

    private final FDDTreeViewFX tree;
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
                scheduleAutoExpand(cell, state);
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
                        if (tree.isValidReparent(dragSource, target)) { CommandExecutionService.getInstance().execute(new MoveNodeCommand(dragSource, target)); success = true; }
                    }
                    case BEFORE, AFTER -> {
                        FDDINode parent = (FDDINode) target.getParentNode();
                        if (parent != null && canInsertSibling(dragSource, target)) {
                            int idx = parent.getChildren().indexOf(target); if (dt == DropType.AFTER) idx += 1;
                            CommandExecutionService.getInstance().execute(new MoveNodeCommand(dragSource, parent, idx)); success = true;
                        }
                    }
                }
                if (!success){ showTransientTooltip(cell, invalidReason(dragSource, target, dt)); LOGGER.debug("Invalid drop {} from {} to {}", dt, dragSource.getName(), target.getName()); }
            }
            clear(cell, DROP_TARGET, INSERT_BEFORE, INSERT_AFTER);
            e.setDropCompleted(success);
            if (success) tree.refresh();
            e.consume();
        });
        cell.setOnDragEntered(e -> {
            FDDINode dragSource = tree.dragSourceNode; FDDINode target = cell.getItem();
            if (dragSource!=null && target!=null && dragSource!=target && tree.isValidReparent(dragSource, target)) scheduleAutoExpand(cell, state);
            e.consume();
        });
        cell.setOnDragExited(e -> { clear(cell, DROP_TARGET, INSERT_BEFORE, INSERT_AFTER); cancelAutoExpand(state); e.consume(); });
        cell.setOnDragDone(e -> { tree.dragSourceNode = null; clear(cell, DROP_TARGET, INSERT_BEFORE, INSERT_AFTER); cancelAutoExpand(state); });
    }

    private FDDTreeDragAndDropController.DropType deriveDropType(double y, double height){
        double h = height <= 0 ? 24 : height;
        if (y < h * 0.25) return FDDTreeDragAndDropController.DropType.BEFORE; else if (y > h * 0.75) return FDDTreeDragAndDropController.DropType.AFTER; else return FDDTreeDragAndDropController.DropType.INTO;
    }

    private boolean canInsertSibling(FDDINode dragSource, FDDINode reference){
        if (dragSource==null || reference==null) return false;
        FDDINode parent = (FDDINode) reference.getParentNode(); if (parent==null) return false; // cannot insert around root
        if (dragSource == reference) return false;
        if (dragSource.getParentNode() == parent && parent.getChildren().size()==1) return false; // no change
        return tree.hierarchyAccepts(parent, dragSource) && !tree.isDescendant(reference, dragSource);
    }

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
        if (state.expandDelay != null) return; if (cell.getTreeItem()==null) return; if (cell.getTreeItem().isExpanded()) return;
        state.expandDelay = new PauseTransition(Duration.millis(600));
        state.expandDelay.setOnFinished(ev -> {
            try { if (cell.getTreeItem()!=null && !cell.getTreeItem().isExpanded()) cell.getTreeItem().setExpanded(true); }
            catch (Exception ex){ LOGGER.debug("Auto-expand failed: {}", ex.getMessage()); }
            finally { state.expandDelay = null; }
        });
        state.expandDelay.play();
    }
    private void cancelAutoExpand(CellState state){ if (state.expandDelay != null){ state.expandDelay.stop(); state.expandDelay = null; } }

    private static class CellState { static final String KEY = "fdd.dnd.state"; FDDTreeDragAndDropController.DropType currentDropType; PauseTransition expandDelay; }
}
