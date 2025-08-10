package net.sourceforge.fddtools.ui.fx;

import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.command.*;
import net.sourceforge.fddtools.util.ObjectCloner;
import net.sourceforge.fddtools.state.ModelState;
import net.sourceforge.fddtools.service.LoggingService;
import net.sourceforge.fddtools.service.DialogService;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;

/** Clipboard + cut/copy/paste/delete operations extracted from main window. */
public class FDDNodeEditActions {
    private static final Logger LOGGER = LoggerFactory.getLogger(FDDNodeEditActions.class);
    private final CommandExecutionService exec;
    private FDDINode clipboard;
    private boolean uniqueNodeVersion = false;
    public interface Host {
        FDDINode getSelectedNode();
        void afterModelMutation(FDDINode nodeToSelect);
        void markDirty();
        boolean isRoot(FDDINode node);
        void showError(String title, String message);
        javafx.stage.Stage getPrimaryStage();
    }
    private final Host host;
    public FDDNodeEditActions(CommandExecutionService exec, Host host){ this.exec=exec; this.host=host; }

    public void cut(){
        FDDINode sel = host.getSelectedNode(); if (sel==null) return;
        if (host.isRoot(sel)) { host.showError("Cut Not Allowed","Cannot cut the root element."); return; }
        clipboard = (FDDINode) ObjectCloner.deepClone(sel);
        uniqueNodeVersion = false;
        if (clipboard == null) { LOGGER.error("Failed deep clone on cut: {}", sel.getClass().getSimpleName()); host.showError("Cut Error","Unable to copy node to clipboard."); return; }
        ModelState.getInstance().setClipboardNotEmpty(true);
        FDDINode parent = (FDDINode) sel.getParentNode();
        if (parent != null) { exec.execute(new DeleteNodeCommand(sel)); host.afterModelMutation(parent); }
        LOGGER.info("Cut (removed) node via command: {}", sel.getClass().getSimpleName());
        LoggingService.getInstance().audit("nodeCut", java.util.Map.of("selectedNode", sel.getName()), () -> sel.getClass().getSimpleName());
    }
    public void copy(){
        FDDINode sel = host.getSelectedNode(); if (sel==null) return;
        clipboard = (FDDINode) ObjectCloner.deepClone(sel); uniqueNodeVersion=false;
        if (clipboard!=null){ ModelState.getInstance().setClipboardNotEmpty(true); LOGGER.info("Copied node: {}", sel.getClass().getSimpleName()); LoggingService.getInstance().audit("nodeCopy", java.util.Map.of("selectedNode", sel.getName()), () -> sel.getClass().getSimpleName()); }
        else { LOGGER.error("Failed deep copy: {}", sel.getClass().getSimpleName()); host.showError("Copy Error","Failed to copy the selected node."); }
    }
    public void paste(){
        if (clipboard==null) return; FDDINode sel = host.getSelectedNode(); if (sel==null) return;
        try {
            PasteNodeCommand cmd = new PasteNodeCommand(sel, clipboard, !uniqueNodeVersion);
            exec.execute(cmd); uniqueNodeVersion=false;
            host.afterModelMutation(cmd.getPasted()!=null?cmd.getPasted():sel);
            LOGGER.info("Pasted node via command: {}", clipboard.getClass().getSimpleName());
            if (cmd.getPasted()!=null) LoggingService.getInstance().audit("nodePaste", java.util.Map.of("selectedNode", cmd.getPasted().getName()), () -> clipboard.getClass().getSimpleName());
        } catch (Exception e){ LOGGER.error("Failed to paste: {}", e.getMessage(), e); host.showError("Paste Error","An error occurred while pasting: "+e.getMessage()); }
    }
    public void delete(){
        FDDINode sel = host.getSelectedNode(); if (sel==null) return; if (host.isRoot(sel)) { host.showError("Delete Not Allowed","Cannot delete the root element."); return; }
    boolean confirmed = DialogService.getInstance().confirm(host.getPrimaryStage(),
            "Delete Node", "Delete " + sel.getClass().getSimpleName(), "Are you sure you want to delete this node?");
        if (confirmed){ FDDINode parent = (FDDINode) sel.getParentNode(); if (parent!=null) { exec.execute(new DeleteNodeCommand(sel)); host.afterModelMutation(parent); LOGGER.info("Deleted node via command: {}", sel.getClass().getSimpleName()); }}
    }
}
