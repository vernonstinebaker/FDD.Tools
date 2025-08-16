package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import javafx.stage.Stage;
import net.sourceforge.fddtools.command.CommandExecutionService;
import net.sourceforge.fddtools.command.EditNodeCommand;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.service.ProjectService;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;

/**
 * Extracted mediator that centralizes selection-driven UI updates and edit dialog sequencing.
 * Reduces responsibilities of FDDMainWindowFX (was hosting selection, mutation, and refresh logic).
 */
public class SelectionCommandMediator {
    // Logger retained for future diagnostics if needed
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(SelectionCommandMediator.class);

    public interface Host {
        FDDTreeViewFX getProjectTree();
        FDDCanvasFX getCanvas();
        Stage getPrimaryStage();
        void configureDialogCentering(Stage stage);
        void updateUndoRedo();
    }

    private final Host host;
    private final CommandExecutionService commandExec;
    private final net.sourceforge.fddtools.ui.fx.FDDCommandBindings commandBindings; // used for undo/redo state updates

    public SelectionCommandMediator(Host host, CommandExecutionService exec, FDDCommandBindings bindings){
        this.host = host; this.commandExec = exec; this.commandBindings = bindings;
    }

    public void afterModelMutation(FDDINode nodeToSelect){
        if (host.getProjectTree() != null) {
            host.getProjectTree().refresh();
            host.getProjectTree().selectNode(nodeToSelect);
        }
        if (host.getCanvas() != null) {
            host.getCanvas().setCurrentNode(nodeToSelect);
            host.getCanvas().redraw();
        }
        markDirty();
    }

    public void onTreeSelectionChanged(FDDINode selected){
        if (host.getCanvas() != null && selected != null){
            host.getCanvas().setCurrentNode(selected);
        }
        updateInfoPanels(selected); // currently hides panels (logic preserved)
    }

    public void refreshView(){
        Platform.runLater(() -> {
            if (host.getProjectTree() != null) host.getProjectTree().refresh();
            if (host.getCanvas() != null) host.getCanvas().redraw();
        });
    }

    public void editSelectedNode(){ editSelectedNodeInternal(getSelectedNode()); }
    public void editSelectedNode(FDDINode node){ editSelectedNodeInternal(node); }

    private FDDINode getSelectedNode(){ return host.getProjectTree()!=null ? host.getProjectTree().getSelectedNode() : null; }

    private void editSelectedNodeInternal(FDDINode node){
        if (node == null) return;
        Platform.runLater(() -> {
            EditNodeCommand.Snapshot beforeSnapshot = EditNodeCommand.capture(node);
            var dlg = new FDDElementDialogFX(host.getPrimaryStage(), node);
            host.configureDialogCentering(dlg);
            dlg.showAndWait();
            if (dlg.getAccept()) {
                EditNodeCommand.Snapshot afterSnapshot = EditNodeCommand.capture(node);
                boolean changed = !beforeSnapshot.getName().equals(afterSnapshot.getName()) ||
                        (beforeSnapshot.getPrefix() == null ? afterSnapshot.getPrefix() != null : !beforeSnapshot.getPrefix().equals(afterSnapshot.getPrefix())) ||
                        (beforeSnapshot.getOwnerInitials() == null ? afterSnapshot.getOwnerInitials() != null : !beforeSnapshot.getOwnerInitials().equals(afterSnapshot.getOwnerInitials())) ||
                        (beforeSnapshot.getMilestoneStatuses() != null && afterSnapshot.getMilestoneStatuses() != null && !java.util.Arrays.equals(beforeSnapshot.getMilestoneStatuses(), afterSnapshot.getMilestoneStatuses())) ||
                        (!eq(beforeSnapshot.getWorkPackageName(), afterSnapshot.getWorkPackageName()));
                if (changed) {
                    // Revert fields prior to issuing command (EditNodeCommand applies after snapshot)
                    node.setName(beforeSnapshot.getName());
                    if (node instanceof com.nebulon.xml.fddi.Subject subj) subj.setPrefix(beforeSnapshot.getPrefix());
                    if (node instanceof com.nebulon.xml.fddi.Activity act) act.setInitials(beforeSnapshot.getOwnerInitials());
                    else if (node instanceof com.nebulon.xml.fddi.Feature feat) {
                        feat.setInitials(beforeSnapshot.getOwnerInitials());
                        // revert milestones
                        if (beforeSnapshot.getMilestoneStatuses() != null) {
                            var milestones = feat.getMilestone();
                            for (int i=0; i < Math.min(milestones.size(), beforeSnapshot.getMilestoneStatuses().length); i++) {
                                milestones.get(i).setStatus(beforeSnapshot.getMilestoneStatuses()[i]);
                            }
                        }
                        if (afterSnapshot.getWorkPackageName() != null || beforeSnapshot.getWorkPackageName() != null) {
                            var proj = getOwningProject(feat);
                            if (proj != null) {
                                for (var wp : proj.getWorkPackages()) { wp.getFeatureList().remove(Integer.valueOf(feat.getSeq())); }
                                if (beforeSnapshot.getWorkPackageName()!=null && !beforeSnapshot.getWorkPackageName().isEmpty()) {
                                    for (var wp : proj.getWorkPackages()) {
                                        if (beforeSnapshot.getWorkPackageName().equals(wp.getName())) {
                                            if (!wp.getFeatureList().contains(feat.getSeq())) wp.addFeature(feat.getSeq());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    commandExec.execute(new EditNodeCommand(node, beforeSnapshot, afterSnapshot));
                    commandBindings.updateUndoRedoState();
                }
                markDirty();
                if (host.getCanvas() != null) host.getCanvas().redraw();
                if (host.getProjectTree() != null) { host.getProjectTree().refresh(); host.getProjectTree().selectNode(node); }
            }
        });
    }

    private boolean eq(String a, String b){ return a==null ? b==null : a.equals(b); }

    private com.nebulon.xml.fddi.Project getOwningProject(com.nebulon.xml.fddi.Feature feat){
        net.sourceforge.fddtools.model.FDDTreeNode cur = feat.getParentNode();
        while(cur!=null){ if (cur instanceof com.nebulon.xml.fddi.Project p) return p; cur = cur.getParentNode(); }
        return null;
    }

    private void updateInfoPanels(FDDINode selected){
        // Panels currently hidden in simplified UI; logic placeholder retained for potential expansion
        // Could hook visibility logic here.
    }

    private void markDirty(){ ProjectService.getInstance().markDirty(); commandBindings.updateUndoRedoState(); }
}
